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

import { useSetState, useRequest } from 'ahooks';
import * as ObClusterController from '@/service/ocp-express/ObClusterController';

export default () => {
  const [state, setState] = useSetState({
    clusterData: {} as API.ClusterInfo,
    clusterDataLoading: false,
  });

  const { run: getClusterData } = useRequest(ObClusterController.getClusterInfo, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        setState({
          clusterData: res.data,
        });
      }
      setState({
        clusterDataLoading: false,
      });
    },
  });

  return {
    getClusterData: (...prams: Parameters<typeof getClusterData>) => {
      setState({
        clusterDataLoading: true,
      });
      return getClusterData(...prams);
    },
    update: setState,
    ...state,
  };
};
