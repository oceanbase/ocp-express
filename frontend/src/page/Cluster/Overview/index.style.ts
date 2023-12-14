import { token } from '@oceanbase/design';
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
      color: token.colorTextSecondary,
    },
    'content:hover': {
      backgroundColor: token.colorBgLayout,
    },
    text: {
      cursor: 'text',
    },
  };
});
export default useStyles;
