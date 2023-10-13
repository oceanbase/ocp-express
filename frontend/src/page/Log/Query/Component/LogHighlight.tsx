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
import { useSelector } from 'umi';
import { LOG_LEVEL } from '@/constant/log';
import React, { useMemo } from 'react';
import { flatten, omit, trim } from 'lodash';
import Highlight, { defaultProps } from 'prism-react-renderer';
import vsLightTheme from 'prism-react-renderer/themes/vsLight';
import vsDarkTheme from 'prism-react-renderer/themes/vsDark';
import Prism from 'prism-react-renderer/prism/index';
import javaLog from '@/component/HighlightWithLineNumbers/languages/javaLog';
import MyParagraph from './MyParagraph';

Prism.languages.javaLog = javaLog;

export interface LogHighlightProps {
  content?: string;
  language?: string;
  keywordList?: string[];
}

const LogHighlight: React.FC<LogHighlightProps> = ({
  keywordList,
  content = '',
  language = 'javaLog',
}) => {
  const { themeMode } = useSelector((state: DefaultRootState) => state.global);
  console.log('themeMode', themeMode)
  const renderLogLevelStytle = (logLevel?: API.LogLevel) => {
    let color;
    switch (trim(logLevel)) {
      case 'INFO':
        color = '#00C200';
        break;
      case 'WARN':
        color = '#FAAD14';
        break;
      case 'ERROR':
        color = '#F8636B';
        break;
      case 'DEBUG':
        color = '#EA0081';
        break;
      case 'EDIAG':
        color = '#FA8C16';
        break;
      case 'WDIAG':
        color = '#722ED1';
        break;
      case 'TRACE':
        color = '#FA541C';
        break;
      default:
        color = 'rgba(0,0,0, 0.45)';
        break;
    }
    return { color, padding: logLevel?.includes(' ') ? 0 : '0 5px' };
  };

  const keywordStyle = {
    color: '#000',
    fontWeight: 900,
    backgroundColor: 'yellow',
  };

  if (keywordList?.length > 0) {
    // 将输入的关键字，进行分组，生成一个正则表达式字符串
    const regStr = keywordList
      ?.map(keyword => {
        return `(${keyword})`;
      })
      ?.join('|');

    const findReg = new RegExp(regStr || '', 'g');

    // 将 高亮查找 注入到当前使用的语法规则中
    if (Prism.languages[language]) {
      Prism.languages[language] = {
        // Prism 内部使用 for in 对规则进行遍历匹配，highlight 写在属性创建的第一位优先级会是最高的
        highlight: findReg,
        ...omit(Prism.languages[language], 'highlight'),
      };
    }
  } else {
    Prism.languages[language] = omit(Prism.languages[language], 'highlight');
  }

  const defaultStyle = {
    whiteSpace: 'normal',
    wordBreak: 'break-all',
  };

  return useMemo(
    () => (
      <Highlight
        {...defaultProps}
        // 自定义 Prism，默认的 Prism 不支持 log 语言，主要是在原先的 Prism 上增加了 log 语言的支持
        Prism={Prism as any}
        theme={themeMode === 'light' ? vsLightTheme : vsDarkTheme}
        code={content}
        language={language as any}
      >
        {({ className, style, tokens, getLineProps, getTokenProps }) => {
          return (
            <pre
              className={className}
              style={{
                ...style,
                textAlign: 'left',
                margin: '1em 0',
              }}
            >
              {tokens.map((line, i) => {
                const logList = flatten(line).filter(item => item.content !== '\n');
                if (logList?.length === 0) {
                  return;
                }
                const { style: lineStyle, ...lineRestProps } = getLineProps({ line, key: i });
                return (
                  <div
                    key={i}
                    {...lineRestProps}
                    style={{
                      ...lineStyle,
                      display: 'flex',
                      whiteSpace: 'normal',
                      borderBottom: '1px solid #E2E8F3',
                      padding: '8px 0',
                    }}
                  >

                    <MyParagraph
                      style={{ marginBottom: 0 }}
                    >
                      {line.map((token, key) => {
                        if (token.content === '\n') {
                          return;
                        }
                        const { style: tokenStyle, ...tokenRestProps } = getTokenProps({
                          token,
                          key,
                        });

                        const highlight = token.types.includes('highlight');

                        const isLogLevel = LOG_LEVEL.includes(trim(token.content));
                        const logLevelStyle = isLogLevel
                          ? renderLogLevelStytle(token.content)
                          : null;

                        if (highlight) {
                          return (
                            <span
                              key={key}
                              {...tokenRestProps}
                              style={
                                isLogLevel
                                  ? {
                                    ...logLevelStyle,
                                    ...tokenStyle,
                                    ...defaultStyle,
                                  }
                                  : {
                                    ...tokenStyle,
                                    ...defaultStyle,
                                  }
                              }
                            >
                              <span style={keywordStyle}>{token?.content}</span>
                            </span>
                          );
                        } else {
                          return (
                            <span
                              key={key}
                              {...tokenRestProps}
                              style={
                                highlight
                                  ? {
                                    ...tokenStyle,
                                    ...keywordStyle,
                                    ...defaultStyle,
                                  }
                                  : isLogLevel
                                    ? {
                                      ...logLevelStyle,
                                      ...tokenStyle,
                                      ...defaultStyle,
                                    }
                                    : { ...tokenStyle, ...defaultStyle }
                              }
                            >
                              {token?.content}
                            </span>
                          );
                        }
                      })}
                    </MyParagraph>
                  </div>
                );
              })}
            </pre>
          );
        }}
      </Highlight>
    ),
    [content, keywordList, themeMode]
  );
};

export default LogHighlight;
