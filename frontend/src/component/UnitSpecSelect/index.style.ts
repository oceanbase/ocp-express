import { token } from '@oceanbase/design';
import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    option: {
      '&:hover': { background: token.colorFillQuaternary },
    },
  };
});
export default useStyles;
