/*
 * Copyright (c) 2023 OceanBase
 * OCP Express is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

import type { IApi } from 'umi';
import { join, parse } from 'path';
import { unlinkSync, readFileSync, writeFileSync, existsSync } from 'fs';
import { EOL } from 'os';
import resolveCwd from '@umijs/deps/compiled/resolve-cwd';

type path = { parentPath: path };

export const matchText = (text: string, path: path) => {
  const isConsoleLog = /^console\.log\(/gi.test(path.parentPath.toString());
  const isSpm = /^spm\=/g.test(path.parentPath.toString());
  const isDataSpm = /^data\-aspm\-desc\=/g.test(path.parentPath.toString());

  let isFormattedMessage = false;
  // 识别 <FormatMessage> 标签的文字层级
  try {
    isFormattedMessage = /^\<FormattedMessage/g.test(
      path.parentPath.parentPath.parentPath.parentPath.parentPath.toString()
    );
  } catch (e) { }
  return /[^\x00-\xff]/.test(text) && !isConsoleLog && !isFormattedMessage && !isDataSpm && !isSpm; // ^\x00-\xff 表示匹配中文字符
};

export const getContent = (content: string) => {
  const start = content.indexOf('{') + 1;
  const end = content.lastIndexOf('}');
  return content.substring(start, end);
};

/**
 * To relative path
 * @param {string} absolutePath absolute path, like: /Users/puss/work/ioc/vue-loader/index.js
 * @param {string} cwd cwd, like: /Users/puss/work/ioc/
 * @param {string} splitter splitter, win \ , linux /
 * @return {string} path , like: vue-loader/index.js
 */
export function toRelativePath(absolutePath, cwd, splitter) {
  return absolutePath.replace(cwd + splitter, '');
}

export default (api: IApi) => {
  const { utils } = api;
  const { chalk, rimraf } = utils;
  api.registerCommand({
    name: 'ob-must',
    fn: async ({ args = {} }) => {
      if (!args._[0]) {
        console.log(chalk.green('replace all Chinese into i18n formatMessage'));
      }
      const fileType = args.file || 'ts'; // --提取的文件类型，默认 ts, 会同时提取 js, jsx, ts, tsx 文件
      const sourcePath = args.path || 'src'; // 默认提取的文件目录
      const outputPath = api.config.singular ? 'src/locale' : 'src/locales'; // 生成的翻译文件目录，用户不可配
      const { exclude } = args; // 排除文件，支持多个

      if (!existsSync(sourcePath)) {
        console.log(chalk.red('提取源目录不存在！可以使用 -path=xxx 指定提取路径'));
        return;
      }

      if (!['js', 'ts', 'html', 'vue'].includes(fileType)) {
        console.log(chalk.red('指定文件类型错误，支持的类型包括：js, ts, html 和 vue'));
        return;
      }

      const { name: pkgName, version } = require(join(api.cwd, 'package.json'));

      const JSX_OPTIONS = {
        cwd: api.cwd,
        name: pkgName,
        sourcePath: sourcePath, // 源文件地址
        fileType, // 提取类型, 会同时读取文件后缀为 js|jsx|ts|tsx 的文件
        // 需要手动开启，才会上传到美杜莎
        isNeedUploadCopyToMedusa: true,
        // 上传到美杜莎
        medusa: {
          // 美杜莎上对应的应用名
          appName: 'ocp-express',
          // 将当前的 OCP 版本作为提取文案的 tag，便于翻译同学识别
          tag: version,
          exportType: 'upload',
        },
        exclude: (path: string) => {
          // 支持以逗号分隔的多个排除文件
          const excludeList = exclude?.split(',') || [];
          return (
            // 不处理 .umi 下的文件
            path.includes('src/.umi') ||
            path.includes('src/locale') ||
            excludeList.some(item => path.includes(item))
          );
        },
        matchCopy: matchText,
        macro: {
          path: outputPath,
          method: `formatMessage({id: '$key$', defaultMessage:"$defaultMessage$"})`,
          // TODO: 可能切换有问题，从插件件兼容掉
          import: "import { formatMessage } from '@/util/intl'",
          // 自定义文案 key 的生成: 默认逻辑可能会生成跨组件重复的 key，导致文案出现混乱
          keyGenerator: (copy, filePath, config) => {
            const enCopy = copy['en-US'];
            // key 分隔符
            const keySplitter = '.';
            // generate world part
            const words = enCopy.split(/[^a-zA-Z]/i);
            const namedWords = words
              .filter(w => /^[\w|\d]+$/i.test(w))
              .map(w => w.toLowerCase().replace(/(?:^|\s)\S/g, a => a.toUpperCase()));

            if (namedWords.length === 0) {
              return '';
            }

            // generate path part
            const { name } = config;
            const isWin = process.platform === 'win32';
            const splitter = isWin ? '\\' : '/';
            const relativeFilePath = toRelativePath(filePath, config.cwd, splitter);
            const parsedPath = parse(relativeFilePath);
            const dirPart = parsedPath.dir
              .split(splitter)
              // get the last two
              .slice(-2)
              .filter(p => !!p);

            let keyArray = [name];
            keyArray = keyArray.concat(dirPart);

            if (parsedPath.name && parsedPath.name !== 'index') keyArray.push(parsedPath.name);
            const filePathKey = keyArray.join(keySplitter);

            return `${filePathKey}${keySplitter}${namedWords.slice(0, 8).join('')}`;
          },
        },
      };

      // 实现初版，通过时候拼接文件实现 增量 + 兼容旧版 Bigfish 国际化
      // 后续优化可以在 must 中进行这些操作
      (async () => {
        let extract;
        try {
          extract = require(resolveCwd('@ali/parrot-tool-must')).extract;
        } catch (e) {
          console.error(
            chalk.red(`
需要手动安装 @ali/parrot-tool-must 依赖，请先执行以下安装命令:

  tnpm i @ali/parrot-tool-must -D

`)
          );
          process.exit(1);
        }
        console.log(chalk.cyan(chalk.bold('推荐先提交 git 修改记录再进行提取')));
        // 执行提取操作
        await extract.run(
          JSX_OPTIONS,
          // 关闭二次确认，直接进行提取和上传到美杜莎
          true
        );

        let zh_CN_source = '';
        let en_US_source = '';
        try {
          // 这里处理有问题，待修复
          zh_CN_source = getContent(
            JSON.stringify(require(join(api.cwd, outputPath, 'strings', 'zh-CN.json')), null, 2)
          );
          en_US_source = getContent(
            JSON.stringify(require(join(api.cwd, outputPath, 'strings', 'en-US.json')), null, 2)
          );
        } catch {
          console.log(chalk.red('没有进行任何文案替换'));
          rimraf.sync(join(api.cwd, outputPath, 'strings'));
          // 尝试删除 生成的 index.js
          try {
            unlinkSync(join(api.cwd, outputPath, fileType === 'js' ? 'index.js' : 'index.ts'));
          } catch { }
          return;
        }

        let zh_CN;
        let en_US;
        try {
          zh_CN = getContent(
            readFileSync(join(api.cwd, outputPath, 'zh-CN.js'), {
              encoding: 'utf8',
            })
          );
        } catch {
          zh_CN = '';
        }
        try {
          en_US = getContent(
            readFileSync(join(api.cwd, outputPath, 'en-US.js'), {
              encoding: 'utf8',
            })
          );
        } catch {
          en_US = '';
        }

        if (!zh_CN && !en_US) {
          // 全量复制翻译
          zh_CN = `export default {${zh_CN_source}}`;
          en_US = `export default {${en_US_source}}`;
        } else {
          // 增量插入翻译
          zh_CN = `export default {${zh_CN + ',  //' + new Date().toLocaleString() + ' 新增文案' + zh_CN_source
            }}`;
          en_US = `export default {${en_US + ',  //' + new Date().toLocaleString() + ' 新增文案' + en_US_source
            }}`;
        }

        try {
          writeFileSync(join(api.cwd, outputPath, 'zh-CN.js'), zh_CN);
          writeFileSync(join(api.cwd, outputPath, 'en-US.js'), en_US);
        } catch (e) {
          console.log(chalk.red('写入翻译文件失败'));
        }

        // 尝试删除 生成的 index.js 或 index.ts
        try {
          rimraf.sync(join(api.cwd, outputPath, 'strings'));
          unlinkSync(join(api.cwd, outputPath, fileType === 'js' ? 'index.js' : 'index.ts'));
        } catch {
          console.log(chalk.red('清理临时文件失败'));
        }

        if (!api.userConfig.locale) {
          console.log(
            chalk.red(
              [
                '检测到项目还没有开启国际化配置',
                '运行前请先在配置中添加',
                chalk.cyan('  locale: {}'),
              ].join(EOL)
            )
          );
          return;
        }
      })();
    },
  });
};
