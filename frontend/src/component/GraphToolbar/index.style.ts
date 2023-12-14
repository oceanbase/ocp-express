import { createStyles } from 'antd-style';

const useStyles = createStyles(({ token }) => {
  return {
    divider: {
      margin: '0',
    },
    zoomWrapper: {
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      width: '106px',
      height: '24px',
      padding: '0 8px',
      color: token.colorTextSecondary,
      fontSize: '12px',
      backgroundColor: token.colorBgLayout,
      borderRadius: '2px',
    },
    anticon: {
      fontSize: '12px',
    },
    fixed: {
      position: 'absolute',
      top: '24px',
      right: '24px',
      height: '40px',
      padding: '8px 24px',
      background: token.colorBgContainer,
      borderRadius: '20px',
      boxShadow: token.boxShadowSecondary,
    },
  };
});

export default useStyles;
