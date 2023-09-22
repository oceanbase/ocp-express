import { createGlobalStyle } from 'antd-style';

const GlobalStyle = createGlobalStyle`
  body, .ob-layout {
    background: ${(p) => p.theme.colorBgLayout};
  }
  .ob-layout .ob-layout-content-layout .ob-layout-content {
    background: ${(p) => p.theme.colorBgLayout};
  }
  .ob-layout .ob-layout-content-layout .ob-layout-sider {
    background: ${(p) => p.theme.colorBgLayout};
  }
  .ob-layout-header {
    background: ${(p) => p.theme.colorBgLayout};
    border-bottom: 1px solid ${(p) => p.theme.colorBorderSecondary} !important;
    box-shadow: none;
  }
  .ob-layout .ob-layout-content-layout .ob-layout-sider .ob-layout-sider-wrapper .ant-menu-inline .ant-menu-item-selected {
    ${(p) => {
    return p.themeMode === 'dark' ? `background-image: none; background: ${p.theme.controlItemBgActive}` : ''
  }}
  }
  .ob-layout .ob-layout-content-layout .ob-layout-sider .ob-layout-sider-wrapper .ant-menu .ant-menu-item, .ob-layout .ob-layout-content-layout .ob-layout-sider .ob-layout-sider-wrapper .ant-menu .ant-menu-submenu {
    color: ${(p) => p.theme.colorText};
  }
  .ob-layout .ob-layout-content-layout .ob-layout-sider .ob-layout-sider-wrapper .ob-layout-sub-sider {
    border-right: 1px solid ${(p) => p.theme.colorBorderSecondary};
  }
  .ob-layout .ob-layout-content-layout .ob-layout-sider .ob-layout-sider-wrapper .ant-menu-vertical .ant-menu-item-selected .ant-menu-title-content {
    ${(p) => {
    return p.themeMode === 'dark' ? `background: ${p.theme.controlItemBgActive}; border: 0.5px solid ${p.theme.colorBorderSecondary}` : ''
  }}
  }
  .ob-layout .ob-layout-content-layout .ob-layout-sider .ob-layout-sider-wrapper .ant-menu-inline .ant-divider {
    ${(p) => {
    return p.themeMode === 'dark' ? 'border-image: none;' : ''
  }}
  }
`;

export default GlobalStyle;