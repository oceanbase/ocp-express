import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    card: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '100%',
      padding: '24px 64px',
    },
  };
});
export default useStyles;
