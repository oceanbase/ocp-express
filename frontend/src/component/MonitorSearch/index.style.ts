import { token } from '@oceanbase/design';
import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    updateTime: {
      color: token.colorTextTertiary,
      '.ant-form-item-label > label': {
        color: token.colorTextTertiary,
        '&::after': {
          color: token.colorTextTertiary,
        },
      },
    },
  };
});
export default useStyles;
