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

// 规则匹配的优先级，与属性声明的顺序有关，顺序越前优先级越高
const log = {
  // 匹配 (25852 ---) 的数字
  id: /(?:\d+)(?=\s---)/,

  // 匹配线程池，原先的匹配逻辑会将 [] 中的内容全部匹配上，会存在错误匹配到子句的情况，导致存在超长行，现匹配逻辑排除 [] 中存在 ]() 的内容，避免错误匹配到较长的子句
  'thread-description': /\[[^\]\(\)]+\]/,

  // 匹配 单双引号中的内容
  string: {
    // 单引号字符串不能与纯文本混淆。例如 Can't isn't Susan's Chris' toy
    pattern: /"(?:[^"\\\r\n]|\\.)*"|'(?![st] | \w)(?:[^'\\\r\n]|\\.)*'/,
    greedy: true,
  },

  // 匹配日志中的状态
  level: [
    {
      pattern: /\b(?:ALERT|CRIT|CRITICAL|EMERG|EMERGENCY|ERR|ERROR|FAILURE|FATAL|SEVERE)\b/,
      alias: ['error', 'important'],
    },
    {
      pattern: /\b(?:WARN|WARNING|WRN)\b/,
      alias: ['warning', 'important'],
    },
    {
      pattern: /\b(?:DISPLAY|INF|INFO|NOTICE|STATUS)\b/,
      alias: ['info', 'keyword'],
    },
    {
      pattern: /\b(?:DBG|DEBUG|FINE)\b/,
      alias: ['debug', 'keyword'],
    },
    {
      pattern: /\b(?:FINER|FINEST|TRACE|TRC|VERBOSE|VRB)\b/,
      alias: ['trace', 'comment'],
    },
  ],

  // 匹配分隔符
  property: {
    pattern:
      /((?:^|[\]|])[ \t]*)[a-z_](?:[\w-]|\b\/\b)*(?:[. ]\(?\w(?:[\w-]|\b\/\b)*\)?)*:(?=\s)/im,
    lookbehind: true,
  },

  // 匹配分隔符
  separator: {
    pattern: /(^|[^-+])-{3,}|={3,}|\*{3,}|- - /m,
    lookbehind: true,
    alias: 'comment',
  },

  url: /\b(?:https?|ftp|file):\/\/[^\s|,;'"]*[^\s|,;'">.]/,

  email: {
    pattern: /(^|\s)[-\w+.]+@[a-z][a-z0-9-]*(?:\.[a-z][a-z0-9-]*)+(?=\s)/,
    lookbehind: true,
    alias: 'url',
  },

  'ip-address': {
    pattern: /\b(?:\d{1,3}(?:\.\d{1,3}){3}(:\d{4})?)\b/i,
    alias: 'constant',
  },

  'mac-address': {
    pattern: /\b[a-f0-9]{2}(?::[a-f0-9]{2}){5}\b/i,
    alias: 'constant',
  },

  domain: {
    pattern: /(^|\s)[a-z][a-z0-9-]*(?:\.[a-z][a-z0-9-]*)*\.[a-z][a-z0-9-]+(?=\s)/,
    lookbehind: true,
    alias: 'constant',
  },

  uuid: {
    pattern: /\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\b/i,
    alias: 'constant',
  },

  hash: {
    pattern: /\b(?:[a-f0-9]{32}){1,2}\b/i,
    alias: 'constant',
  },

  'file-path': {
    // eslint-disable-next-line
    pattern: /\b[a-z]:[\\/][^\s|,;:(){}\[\]"']+|(^|[\s:\[\](>|])\.{0,2}\/\w[^\s|,;:(){}\[\]"']*/i,
    lookbehind: true,
    greedy: true,
    alias: 'string',
  },

  date: {
    pattern: RegExp(
      // eslint-disable-next-line
      /\b\d{4}[-/]\d{2}[-/]\d{2}(?:T(?=\d{1,2}:)|(?=\s\d{1,2}:))/.source +
        '|' +
        /\b\d{1,4}[-/ ](?:\d{1,2}|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[-/ ]\d{2,4}T?\b/
          .source +
        '|' +
        /\b(?:(?:Mon|Tue|Wed|Thu|Fri|Sat|Sun)(?:\s{1,2}(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec))?|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s{1,2}\d{1,2}\b/
          .source,
      'i'
    ),
    alias: 'number',
  },

  time: {
    pattern: /\b\d{1,2}:\d{1,2}:\d{1,2}(?:[.,:]\d+)?(?:\s?[+-]\d{2}:?\d{2}|Z)?\b/,
    alias: 'number',
  },

  boolean: /\b(?:true|false|null)\b/i,

  number: {
    pattern:
      /(^|[^.\w])(?:0x[a-f0-9]+|0o[0-7]+|0b[01]+|v?\d[\da-f]*(?:\.\d+)*(?:e[+-]?\d+)?[a-z]{0,3}\b)\b(?!\.\w)/i,
    lookbehind: true,
  },

  // 匹配操作符
  operator: /[;:?<=>~/@!$%&+\-|^(){}*#]/,

  // 匹配符号
  // eslint-disable-next-line
  punctuation: /[\[\].,]/,

  // 匹配具体的类名
  classes:
    /[a-zA-Z]+[0-9a-zA-Z_]*(\.[a-zA-Z]+[0-9a-zA-Z_]*)*\.[a-zA-Z]+[0-9a-zA-Z_]*(\$[a-zA-Z]+[0-9a-zA-Z_]*)*/,
};

export default log;
