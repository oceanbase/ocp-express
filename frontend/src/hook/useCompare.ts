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

import { useRef } from 'react';
import { isEqual } from 'lodash';

// useEffect的依赖项是 对象 或者 数组
const useCompare = (value: any) => {
  const ref = useRef(null);
  if (!isEqual(value, ref.current)) {
    ref.current = value;
  }
  // 缓存数组或对象地址
  return ref.current;
};

export default useCompare;
