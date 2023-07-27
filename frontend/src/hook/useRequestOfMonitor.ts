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

import { useRef, useCallback } from 'react';
import type { BaseOptions, CombineService } from '@ahooksjs/use-request/es/types';
import { useRequest } from 'ahooks';

function useLockFn<P extends any[] = any[], V extends any = any>(fn: (...args: P) => Promise<V>) {
  const lockRef = useRef(false);
  return [
    useCallback(
      async (...args: P) => {
        if (lockRef.current) return;
        lockRef.current = true;
        try {
          const ret = await fn(...args);
          lockRef.current = false;
          return ret;
        } catch (e) {
          lockRef.current = false;
          throw e;
        }
      },
      [fn]
    ),
    (value: boolean) => {
      lockRef.current = value;
    },
  ];
}

function useRequestOfMonitor<R, P extends any[]>(
  service: CombineService<R, P>,
  options?: BaseOptions<R, P> & { isRealtime?: boolean }
) {
  const { isRealtime, ...restOptions } = options || {};
  const requestResult = useRequest(service, restOptions);

  // 使用竞态锁来防止监控请求堆积
  const [run, setLock] = useLockFn(requestResult.run);

  return {
    ...requestResult,
    run: (...params: P) => {
      // 实时模式下，接口进入轮询模式，采用竞态锁来防止接口堆积
      if (isRealtime) {
        run(...params);
      } else {
        setLock(false);
        requestResult.run(...params);
      }
    },
  };
}

export default useRequestOfMonitor;
