import { createStyles } from 'antd-style';

const useStyles = createStyles(({ token }) => {
  return {
    header: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      width: '100%',
      marginBottom: '20px',
    },
    title: {
      color: token.colorText,
      fontSize: '16px',
      fontFamily: 'PingFangSC-Medium',
    },
  };
});

export default useStyles;
