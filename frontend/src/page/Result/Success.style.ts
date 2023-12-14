import { token } from '@oceanbase/design';
import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    detail: {
      backgroundColor: token.colorBgLayout,
    },
  };
});
export default useStyles;
