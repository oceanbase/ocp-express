import { token } from '@oceanbase/design';
import { createStyles } from 'antd-style';

const useStyles = createStyles(({ token }) => {
  return {
    container: {
      fontWeight: 'normal',
      fontSize: '12px',
    },
    icon: {
      color: token.colorPrimary,
    },
    content: {
      color: token.colorTextTertiary,
    },
  };
});

export default useStyles;
