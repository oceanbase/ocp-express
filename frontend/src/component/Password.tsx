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
import { Password } from '@oceanbase/design';
import type { PasswordProps } from '@oceanbase/design/dist/src/Password';

const OCPPassword: React.FC<PasswordProps> = props => {
  // 特殊字符支持 ~!@#%^&*_\-+=`|(){}[]:;',.?/
  const ocpPasswordRules = [
    {
      validate: (val?: string) => val?.length >= 8 && val?.length <= 32,
      message: formatMessage({
        id: 'ocp-express.src.component.Password.TheDescriptionMustBeTo',
        defaultMessage: '长度为 8~32 个字符',
      }),
    },

    {
      validate: (val?: string) => /^[0-9a-zA-Z~!@#%^&*_\-+=|(){}\[\]:;,.?/]+$/.test(val),
      message: formatMessage({
        id: 'ocp-express.src.component.Password.ItCanOnlyContainLetters',
        defaultMessage: '只能包含字母、数字和特殊字符（~!@#%^&*_-+=|(){}[]:;,.?/）',
      }),
    },

    {
      validate: (val?: string) =>
        /^(?=(.*[a-z]){2,})(?=(.*[A-Z]){2,})(?=(.*\d){2,})(?=(.*[~!@#%^&*_\-+=|(){}\[\]:;,.?/]){2,})[A-Za-z\d~!@#%^&*_\-+=|(){}\[\]:;,.?/]{2,}$/.test(
          val
        ),

      message: formatMessage({
        id: 'ocp-express.src.component.Password.ItMustContainAtLeast',
        defaultMessage: '大小写字母、数字和特殊字符都至少包含 2 个',
      }),
    },
  ];

  return <Password rules={ocpPasswordRules} {...props} />;
};

export default OCPPassword;
