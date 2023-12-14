import { createStyles } from 'antd-style';

const useStyles = createStyles(({ token }) => {
  return {
    tenantInfo: {
      display: 'flex',
      flexDirection: 'row',
      justifyContent: 'space-between',
      paddingBottom: '16px',
      div: {
        display: 'flex',
        flexDirection: 'column',
        span: {
          height: '30px',
        },
      },
    },
    number: {
      color: token.colorPrimary,
    },
  };
});

export default useStyles;
