import { token } from '@oceanbase/design';
import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    taskIdWrapper: {
      padding: '5px 12px',
      '.ant-typography': {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        color: token.colorTextTertiary,
        '.ant-typography-copy': {
          color: token.colorTextTertiary,
        },
      },
    },
  };
});
export default useStyles;
