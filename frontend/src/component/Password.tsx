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
import React from 'react';
import { Password } from '@oceanbase/ui';
import type { PasswordProps } from '@oceanbase/ui/es/Password';

const OCPPassword: React.FC<PasswordProps> = props => {
  // 特殊字符支持 ~!@#%^&*_-+=|(){}[]:;,.?/
  const ocpPasswordRules = [
    {
      validate: (val?: string) => val?.length >= 8 && val?.length <= 32,
      message: formatMessage({
        id: 'ocp-express.src.component.Password.TheDescriptionMustBeTo',
        defaultMessage: '长度为 8~32 个字符',
      }),
    },

    {
      validate: (val?: string) => {
        // 只能包含数字，大小写字母，特殊字符
        if (!/^[0-9A-Za-z~!@#%^&*\-_+=|(){}[\]:;,.?/]+$/.test(val)) {
          return false;
        }
        let count = 0;
        // 至少包含一个数字
        if (/.*[0-9]{1,}.*/.test(val)) {
          count = count + 1;
        }
        // 至少包含一个大写字母
        if (/.*[A-Z]{1,}.*/.test(val)) {
          count = count + 1;
        }
        // 至少包含一个小写字母
        if (/.*[a-z]{1,}.*/.test(val)) {
          count = count + 1;
        }
        // 至少包含一个 ~!@#%^&*\-_+=|(){}[\]:;,.?/
        if (/[~!@#%^&*\-_+=|(){}[\]:;,.?\/]/.test(val)) {
          count = count + 1;
        }

        return count >= 3 ? false : true;
      },
      message: formatMessage({
        id: 'ocp-express.src.component.Password.ItMustContainAtLeast',
        defaultMessage:
          '包含以下四种类型字符至少三种及以上：数字（0~9）、大写字母（A~Z）、小写字母(a~z)、特殊符号  ~!@#%^&*_-+=|(){}[]:;,.?/',
      }),
    },
  ];

  return <Password rules={ocpPasswordRules} {...props} />;
};

export default OCPPassword;
