import { token } from '@oceanbase/design';
import { createStyles } from 'antd-style';

const useStyles = createStyles(({ token }) => {
  return {
    splitPane: {
      top: 'auto !important',
      // width: "calc(100% - 208px) !important",
      height: 'calc(100% - 187px) !important',
      minHeight: 'calc(100% - 187px) !important',
      backgroundColor: token.colorBgContainer,
      borderRadius: '6px',
      '.ant-tabs': {
        '.ant-tabs-nav': {
          marginBottom: '0',
          backgroundColor: token.colorBgContainer,
        },
        '.ant-tabs-nav-list': {
          marginTop: '4px',
        },
        '.ant-tabs-tab': {
          display: 'inline-flex',
          minWidth: '120px',
          height: '28px',
          margin: '0',
          padding: '0',
          fontSize: '12px',
          lineHeight: '28px',
          backgroundColor: token.colorBgContainer,
          border: 'none',
          borderBottom: `1px solid ${token.colorBorder}`,
          borderRadius: '0',
          transition: 'none',
          '&:hover': { color: token.colorTextSecondary },
          div: {
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            height: '20px',
            marginTop: '4px',
            padding: '0 8px',
            borderLeft: `1px solid ${token.colorBorder}`,
          },
          '.ant-tabs-tab-remove': {
            marginTop: '4px',
            marginRight: '8px',
            marginLeft: '0',
            padding: '0',
          },
        },
        '.ant-tabs-tab:first-child, .ant-tabs-tab:last-child': {
          div: { borderLeft: '1px solid transparent' },
        },
        '.ant-tabs-tab-active': {
          backgroundColor: token.colorBgLayout,
          borderBottom: 'none',
          borderLeft: '1px solid transparent',
          borderRadius: '2px',
          '.ant-tabs-tab-btn': {
            color: token.colorText,
            fontWeight: 'normal',
          },
          div: { borderLeft: '1px solid transparent' },
          '& + .ant-tabs-tab': {
            div: {
              borderLeft: '1px solid transparent',
            },
          },
        },
        '.ant-tabs-extra-content': {
          marginRight: '16px',
          lineHeight: '32px',
        },
      },
      margin: '0 -24px',
    },
    tabsWrapper: {
      width: '100%',
      padding: '0 16px 16px 16px',
      boxShadow: token.boxShadowSecondary,
    },
    log: {
      padding: '12px 16px',
      color: token.colorTextSecondary,
      fontSize: '12px',
      lineHeight: '22px',
      whiteSpace: 'pre-wrap',
      wordBreak: 'break-all',
    },
    english: {
      minHeight: 'calc(100% - 140px - 38px)',
    },
  };
});

export default useStyles;
