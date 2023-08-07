import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    container: {
      paddingBottom: '4px',
      boxShadow: 'none',
    },
    title: {
      fontSize: '16px',
    },
    fullscreen: {
      cursor: 'pointer',
    },
  };
});
export default useStyles;
