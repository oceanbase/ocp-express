import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    task: {
      marginBottom: "'12px'",
      color: "'rgba(0,0,0,0.85)'",
      fontWeight: "'bold'",
      fontSize: "'16px'",
    },
    title: {
      color: 'rgba(0, 0, 0, 0.65)',
    },
    'content:hover': {
      backgroundColor: 'rgba(0, 0, 0, 0.04)',
    },
    text: {
      cursor: 'text',
    },
  };
});
export default useStyles;
