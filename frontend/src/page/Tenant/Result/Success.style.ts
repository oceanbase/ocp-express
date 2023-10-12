import { createStyles } from 'antd-style';

const useStyles = createStyles(({ token }) => {
  return {
    newOBProxyInfo: {
      padding: '32px',
    },
    detail: {
      backgroundColor: token.colorInfoBg,
    },
  };
});
export default useStyles;
