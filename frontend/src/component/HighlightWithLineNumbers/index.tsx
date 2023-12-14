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

import { theme } from '@oceanbase/design';
import React from 'react';
import { useSelector } from 'umi';
import Highlight, { defaultProps } from 'prism-react-renderer';
import vsLightTheme from 'prism-react-renderer/themes/vsLight';
import vsDarkTheme from 'prism-react-renderer/themes/vsDark';
import Prism from 'prism-react-renderer/prism/index';
import javaLog from './languages/javaLog';

Prism.languages.javaLog = javaLog;

type Language =
  | 'markup'
  | 'bash'
  | 'clike'
  | 'c'
  | 'cpp'
  | 'css'
  | 'javascript'
  | 'jsx'
  | 'coffeescript'
  | 'actionscript'
  | 'css-extr'
  | 'diff'
  | 'git'
  | 'go'
  | 'graphql'
  | 'handlebars'
  | 'json'
  | 'less'
  | 'makefile'
  | 'markdown'
  | 'objectivec'
  | 'ocaml'
  | 'python'
  | 'reason'
  | 'sass'
  | 'scss'
  | 'sql'
  | 'stylus'
  | 'tsx'
  | 'typescript'
  | 'wasm'
  | 'yaml'
  | 'javaLog';

export interface HighlightWithLineNumbersProps {
  content: string;
  showLineNumber?: boolean | undefined;
  language: Language;
}

const HighlightWithLineNumbers: React.FC<HighlightWithLineNumbersProps> = ({
  content = '',
  showLineNumber = true,
  language = 'javaLog',
}) => {
  const { token } = theme.useToken();
  const { themeMode } = useSelector((state: DefaultRootState) => state.global);

  return (
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
              padding: 16, //'0.5em',
              backgroundColor:
                themeMode === 'light' ? token.colorFillQuaternary : 'rgb(42, 39, 52)',
            }}
          >
            {tokens.map((line, i) => {
              /**
               * const Line = styled.div`
               *  display: flex;
               * `;
               * <Line key={i} {...getLineProps({ line, key: i })}></Line>;
               * prism-react-renderer 默认推荐使用 styled-components 来自定义组件样式，但是不符合项目的一个规范，所以下面我采用 style 重新赋值的一个写法
               */
              const { style: lineStyle, ...lineRestProps } = getLineProps({ line, key: i });
              return (
                <div key={i} {...lineRestProps} style={{ ...lineStyle, display: 'flex' }}>
                  {showLineNumber && (
                    <div
                      style={{
                        textAlign: 'right',
                        paddingRight: '1em',
                        userSelect: 'none',
                        opacity: 0.5,
                      }}
                    >
                      {i + 1}
                    </div>
                  )}
                  <div>
                    {line.map((token, key) => {
                      // 对 url 做特殊的处理，让其支持跳转
                      if (token.types.includes('url')) {
                        return (
                          <a
                            href={token.content}
                            target="_blank"
                            rel="noopener noreferer"
                            style={{ float: 'left' }}
                          >
                            {token.content}
                          </a>
                        );
                      }

                      const { style: tokenStyle, ...tokenRestProps } = getTokenProps({
                        token,
                        key,
                      });

                      return (
                        <span
                          key={key}
                          {...tokenRestProps}
                          style={{ ...tokenStyle, float: 'left' }}
                        />
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </pre>
        );
      }}
    </Highlight>
  );
};

export default HighlightWithLineNumbers;
