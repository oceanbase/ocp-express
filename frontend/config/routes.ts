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

export default [
  {
    path: '/',
    component: 'Layout',
    name: '系统布局',
    spmb: 'b55539',
    routes: [
      {
        path: 'login',
        component: 'Login',
        name: '登录页',
        spmb: 'b55540',
      },
      {
        // 单个租户管理
        path: 'tenant/:tenantId([0-9]+)',
        component: 'Tenant/Detail',
        routes: [
          {
            path: '/tenant/:tenantId([0-9]+)',
            redirect: 'overview',
          },
          {
            path: 'overview',
            component: 'Tenant/Detail/Overview',
            name: '租户详情',
            spmb: 'b55541',
          },
          {
            path: 'monitor',
            component: 'Tenant/Detail/Monitor',
            name: '租户性能监控',
            spmb: 'b55575',
          },
          {
            path: 'database',
            component: 'Tenant/Detail/Database',
            name: '租户数据库',
            spmb: 'b55542',
          },
          {
            path: 'user',
            component: 'Tenant/Detail/User',
            name: '租户用户',
            spmb: 'b55543',
          },
          {
            path: 'user/role',
            component: 'Tenant/Detail/User',
            name: 'Oracle 租户角色',
            spmb: 'b55544',
          },
          {
            path: 'user/:username',
            component: 'Tenant/Detail/User/Oracle/UserOrRoleDetail',
            name: 'Oracle 租户用户详情',
            spmb: 'b55545',
          },
          {
            path: 'user/role/:roleName',
            component: 'Tenant/Detail/User/Oracle/UserOrRoleDetail',
            name: 'Oracle 租户角色详情',
            spmb: 'b55546',
          },
          {
            path: 'parameter',
            component: 'Tenant/Detail/Parameter',
            name: '租户参数',
            spmb: 'b55547',
          },
        ],
      },
      {
        path: '/',
        component: 'Layout/BasicLayout',
        routes: [
          {
            path: '/',
            redirect: '/overview',
          },
          {
            path: 'overview',
            component: 'Cluster/Overview',
            name: '集群详情',
            spmb: 'b55548',
          },
          {
            path: 'overview/server/:ip/:serverPort',
            component: 'Cluster/Host',
            name: 'OBServer 详情',
            spmb: 'b55549',
          },
          {
            path: 'overview/unit',
            component: 'Cluster/Unit',
            name: 'Unit 分布',
            spmb: 'b55550',
          },
          {
            path: 'overview/parameter',
            component: 'Cluster/Parameter',
            name: '参数管理',
            spmb: 'b55551',
          },
          {
            path: 'tenant',
            component: 'Tenant',
            name: '租户列表',
            spmb: 'b55552',
          },
          {
            path: 'monitor',
            component: 'Monitor',
            name: '集群监控',
            spmb: 'b55553',
            ignoreMergeRoute: true,
            queryTitle: 'scope',
          },
          {
            path: 'tenant/new',
            component: 'Tenant/New',
            name: '新建租户',
            spmb: 'b55554',
          },
          // 创建租户改为异步任务 结果页
          {
            path: 'tenant/result/:taskId',
            component: 'Tenant/Result/Success',
            name: '新建租户任务提交成功',
            spmb: 'b55555',
          },
          {
            path: 'diagnosis/session',
            component: 'Diagnosis/Session',
            name: '会话诊断',
            spmb: 'b55576',
            ignoreMergeRoute: true,
            queryTitle: 'tab',
          },
          {
            path: 'diagnosis/sql',
            component: 'Diagnosis/SQLDiagnosis',
            name: 'SQL 诊断',
            spmb: 'b55577',
            ignoreMergeRoute: true,
            queryTitle: 'tab',
          },
          {
            path: 'task',
            component: 'Task',
            name: '任务中心',
            spmb: 'b55556',
          },
          {
            path: 'task/:taskId',
            component: 'Task/Detail',
            name: '任务详情',
            spmb: 'b55557',
          },
          {
            path: 'property',
            component: 'Property',
            name: '系统配置',
            spmb: 'b55558',
          },
          {
            path: 'log',
            component: 'Log',
            name: '日志服务',
            spmb: 'b55559',
          },
          {
            path: 'error',
            name: '错误页',
            spmb: 'b55560',
            routes: [
              {
                path: '/error',
                redirect: '/error/404',
              },
              {
                path: '403',
                component: 'Error/403',
                name: '403 错误',
                spmb: 'b55561',
              },
              {
                path: '404',
                component: 'Error/404',
                name: '404 错误',
                spmb: 'b55562',
              },
            ],
          },
        ],
      },
      {
        // 前面的路径都没有匹配到，说明是 404 页面
        // 使用 Error/404 覆盖默认的 404 路径
        component: 'Error/404',
        name: '404 页面不存在',
        spmb: 'b55563',
      },
    ],
  },
];
