import { createStyles } from 'antd-style';

const useStyles = createStyles(({ token }) => {
  return {
    sessionsInfo: {
      backgroundColor: token.colorBgLayout,
    },
  };
});
export default useStyles;
