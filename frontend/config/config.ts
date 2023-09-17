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

import { defineConfig } from 'umi';
import AntdMomentWebpackPlugin from '@ant-design/moment-webpack-plugin';
import routes from './routes';

const ocp = {
  target: '', // OCP Express 后端地址
};

export default defineConfig({
  routes,
  singular: true,
  nodeModulesTransform: {
    type: 'none',
    exclude: [],
  },
  // mock: {
  //   exclude: [
  //     // 获取 loginKey 和登录用户信息直接走后端接口，不需要 mock，因为这些接口的 mock 数据实际不可用
  //     // 登录和退出登录实际也是直接走后端接口，因为这两个不是显式定义的接口，OneAPI 无法拉取到
  //     'mock/ocp-express/ProfileController/userInfo.js',
  //     'mock/ocp-express/IamController/getLoginKey.js',
  //     // 忽略 WebController 目录下的 mock，因为不是实际的后端接口，没有意义
  //     'mock/ocp-express/WebController/*.js',
  //   ],
  // },
  // appType: 'console',
  // 需要关闭 title 配置，避免与 useDocumentTitle 的逻辑冲突
  // 冲突的具体表现为: 切换路由时，父组件设置的文档标题会自动重置为配置的 title
  title: false,
  favicon: '/assets/logo/ocp_express_favicon.svg',
  // 接口代理配置
  proxy: {
    '/api/v1': {
      ...ocp,
    },
    '/api/v2': {
      ...ocp,
    },
    services: {
      ...ocp,
    },
  },

  antd: {
    disableBabelPluginImport: true,
  },
  dva: {
    disableModelsReExport: true,
  },
  // mfsu 无法通过环境变量动态开启，必须在 config.ts 中配置。但打包报告的资源合并插件只支持 webpack4，
  // 而 mfsu 默认开启 webpack5，存在版本冲突。因此本地打包报告时需要手动将 mfsu 关闭
  mfsu: {},
  locale: {
    default: 'zh-CN',
    antd: true,
    title: false,
  },
  // esbuild: {},
  dynamicImport: {
    loading: '@/component/PageLoading',
  },
  // 开启运行时 publicPath
  runtimePublicPath: true,
  chainWebpack: (config, { env }) => {
    if (env === 'production') {
      config.optimization.delete('noEmitOnErrors');
      config.plugins.delete('optimize-css');

      // 因为删除原来适配webpack4的css压缩插件，css压缩可以用 mini-css-extract-plugin
      config.optimization.minimize(true)
      //  config.optimization.minimizer(`css-esbuildMinify`).use(CSSMinimizerWebpackPlugin);
    }
    // 添加 AntdMomentWebpackPlugin 插件
    config.plugin('antd-moment').use(AntdMomentWebpackPlugin, [
      {
        // 关闭 dayjs alias，避免 antd 以外的 dayjs 被 alias 成 moment
        disableDayjsAlias: true,
      },
    ]);
    // 静态资源的文件限制调整为 1GB，避免视频等大文件资源阻塞项目启动
    config.performance.maxAssetSize(1000000000);
    return config;
  },
  headScripts: [
    `!function(modules){function __webpack_require__(moduleId){if(installedModules[moduleId])return installedModules[moduleId].exports;var module=installedModules[moduleId]={exports:{},id:moduleId,loaded:!1};return modules[moduleId].call(module.exports,module,module.exports,__webpack_require__),module.loaded=!0,module.exports}var installedModules={};return __webpack_require__.m=modules,__webpack_require__.c=installedModules,__webpack_require__.p="",__webpack_require__(0)}([function(module,exports){"use strict";!function(){if(!window.Tracert){for(var Tracert={_isInit:!0,_readyToRun:[],_guid:function(){return"xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g,function(c){var r=16*Math.random()|0,v="x"===c?r:3&r|8;return v.toString(16)})},get:function(key){if("pageId"===key){if(window._tracert_loader_cfg=window._tracert_loader_cfg||{},window._tracert_loader_cfg.pageId)return window._tracert_loader_cfg.pageId;var metaa=document.querySelectorAll("meta[name=data-aspm]"),spma=metaa&&metaa[0].getAttribute("content"),spmb=document.body&&document.body.getAttribute("data-aspm"),pageId=spma&&spmb?spma+"."+spmb+"_"+Tracert._guid()+"_"+Date.now():"-_"+Tracert._guid()+"_"+Date.now();return window._tracert_loader_cfg.pageId=pageId,pageId}return this[key]},call:function(){var argsList,args=arguments;try{argsList=[].slice.call(args,0)}catch(ex){var argsLen=args.length;argsList=[];for(var i=0;i<argsLen;i++)argsList.push(args[i])}Tracert.addToRun(function(){Tracert.call.apply(Tracert,argsList)})},addToRun:function(_fn){var fn=_fn;"function"==typeof fn&&(fn._logTimer=new Date-0,Tracert._readyToRun.push(fn))}},fnlist=["config","logPv","info","err","click","expo","pageName","pageState","time","timeEnd","parse","checkExpo","stringify","report"],i=0;i<fnlist.length;i++){var fn=fnlist[i];!function(fn){Tracert[fn]=function(){var argsList,args=arguments;try{argsList=[].slice.call(args,0)}catch(ex){var argsLen=args.length;argsList=[];for(var i=0;i<argsLen;i++)argsList.push(args[i])}argsList.unshift(fn),Tracert.addToRun(function(){Tracert.call.apply(Tracert,argsList)})}}(fn)}window.Tracert=Tracert}}()}]);`,
    'https://gw.alipayobjects.com/as/g/component/tracert/4.4.9/index.js',
  ],
});