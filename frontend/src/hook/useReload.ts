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

import { useState } from 'react';
import { useUpdateEffect } from 'ahooks';

const useReload = (initialLoading: boolean, callback?: () => void): [boolean, () => void] => {
  const [loading, setLoading] = useState(initialLoading);
  const reload = () => {
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
    }, 100);
  };
  useUpdateEffect(() => {
    // 加载完成后执行回调函数
    if (!loading) {
      if (callback) {
        callback();
      }
    }
  }, [loading]);
  return [loading, reload];
};

export default useReload;
