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

import { formatMessage } from '@/util/intl';
import React, { createElement } from 'react';
import { Button } from '@oceanbase/design';
import styles from './index.less';

interface AobExceptionProps {
  title?: React.ReactNode;
  desc?: React.ReactNode;
  img?: string;
  actions?: React.ReactNode;
  style?: React.CSSProperties;
  className?: string;
  linkElement?: string;
  backText?: string;
  redirect?: string;
  onBack?: () => void;
}

class AobException extends React.PureComponent<AobExceptionProps> {
  constructor(props: AobExceptionProps) {
    super(props);
    this.state = {};
  }

  public render() {
    const {
      className,
      backText = formatMessage({
        id: 'ocp-express.component.AobException.ReturnToHomePage',
        defaultMessage: '返回首页',
      }),
      title,
      desc,
      img,
      linkElement = 'a',
      actions,
      redirect = '/',
      onBack,
      ...rest
    } = this.props;

    return (
      <div className={`${styles.container} ${className}`} {...rest}>
        <div className={styles.imgWrapper}>
          <div className={styles.img} style={{ backgroundImage: `url(${img})` }} />
        </div>
        <div className={styles.content}>
          <h1>{title}</h1>
          <div className={styles.desc}>{desc}</div>
          <div className={styles.actions}>
            {actions ||
              (onBack ? (
                <Button
                  data-aspm-click="ca48180.da30493"
                  data-aspm-desc="异常页-返回首页"
                  data-aspm-param={``}
                  data-aspm-expo
                  type="primary"
                  onClick={onBack}
                >
                  {backText}
                </Button>
              ) : (
                createElement(
                  linkElement,
                  {
                    to: redirect,
                    href: redirect,
                  },
                  <Button
                    data-aspm-click="ca48180.da30493"
                    data-aspm-desc="异常页-返回首页"
                    data-aspm-param={``}
                    data-aspm-expo
                    type="primary"
                  >
                    {backText}
                  </Button>
                )
              ))}
          </div>
        </div>
      </div>
    );
  }
}

export default AobException;
