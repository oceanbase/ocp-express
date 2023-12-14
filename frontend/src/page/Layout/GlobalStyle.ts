import { createGlobalStyle } from 'antd-style';

const GlobalStyle = createGlobalStyle`
  body, .ob-layout {
    background: ${p => p.theme.colorBgLayout};
  }
  .ob-layout .ob-layout-content-layout .ob-layout-content {
    background: ${p => p.theme.colorBgLayout};
  }
  .ob-layout .ob-layout-content-layout .ob-layout-sider {
    background: ${p => p.theme.colorBgLayout};
  }
  .ob-layout-header {
    background: ${p => p.theme.colorBgLayout};
    border-bottom: 1px solid ${p => p.theme.colorBorderSecondary} !important;
    box-shadow: none;
  }
  .ob-layout .ob-layout-content-layout .ob-layout-sider .ob-layout-sider-wrapper .ant-menu {
    .ant-menu-item-selected, .ant-menu-item-active, .ant-menu-submenu-active {
      ${p => {
        return p.themeMode === 'dark'
          ? `background-image: none; background: ${p.theme.controlItemBgActive}`
          : '';
      }}
    }
  }
  .ob-layout .ob-layout-content-layout .ob-layout-sider .ob-layout-sider-wrapper .ant-menu .ant-menu-item, .ob-layout .ob-layout-content-layout .ob-layout-sider .ob-layout-sider-wrapper .ant-menu .ant-menu-submenu {
    color: ${p => p.theme.colorText};
  }
  .ob-layout .ob-layout-content-layout .ob-layout-sider .ob-layout-sider-wrapper .ob-layout-sub-sider {
    border-right: 1px solid ${p => p.theme.colorBorderSecondary};
  }
  .ob-layout .ob-layout-content-layout .ob-layout-sider .ob-layout-sider-wrapper .ant-menu-vertical .ant-menu-item-selected .ant-menu-title-content {
    ${p => {
      return p.themeMode === 'dark'
        ? `background: ${p.theme.controlItemBgActive}; border: 0.5px solid ${p.theme.colorBorderSecondary}`
        : '';
    }}
  }
  .ob-layout .ob-layout-content-layout .ob-layout-sider .ob-layout-sider-wrapper .ant-menu-inline .ant-divider {
    ${p => {
      return p.themeMode === 'dark' ? 'border-image: none;' : '';
    }}
  }
  .ob-login-container .ob-login-card .ob-login-content .ob-login-form .ant-input-affix-wrapper .ant-input {
    ${p => {
      return p.themeMode === 'dark' ? 'box-shadow: none !important;' : '';
    }}
  }
  .ob-login-container .ob-login-card {
    ${p => {
      return p.themeMode === 'dark' ? `background-color: ${p.theme.colorBgLayout};` : '';
    }}
  }
  .ob-layout-header-about-wrapper .ob-layout-header-about .ob-layout-header-release-info .ob-layout-header-version {
    ${p => {
      return p.themeMode === 'dark' ? `color: ${p.theme.colorText};` : '';
    }}
  }
  .ob-layout-header-about-wrapper .ob-layout-header-about .ob-layout-header-release-info .ob-layout-header-date {
    ${p => {
      return p.themeMode === 'dark' ? `color: ${p.theme.colorTextTertiary}; opacity: 1;` : '';
    }}
  }
  .ob-layout-header-about-wrapper .ob-layout-header-about .ob-layout-header-copyright {
    ${p => {
      return p.themeMode === 'dark' ? `color: ${p.theme.colorTextTertiary}; opacity: 1;` : '';
    }}
  }
`;

export default GlobalStyle;
