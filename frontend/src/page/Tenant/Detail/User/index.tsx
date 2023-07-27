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
import React from 'react';
import MySQL from './MySQL';
import Oracle from './Oracle';

export interface IndexProps {
  match: {
    params: {
      clusterId: number;
      tenantId: number;
    };
  };
  location: {
    pathname: string;
  };
}

const Index: React.FC<IndexProps> = ({
  match: {
    params: { clusterId, tenantId },
  },
  location: { pathname },
}) => {
  const { tenantData } = useSelector((state: DefaultRootState) => state.tenant);
  return (
    <>
      {tenantData?.mode === 'MYSQL' ? (
        <MySQL clusterId={clusterId} tenantId={tenantId} />
      ) : (
        <Oracle pathname={pathname} clusterId={clusterId} tenantId={tenantId} />
      )}
    </>
  );
};

export default Index;
