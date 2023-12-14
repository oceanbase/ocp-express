import { token } from '@oceanbase/design';
import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    container: {
      paddingTop: '0',
    },
    id: {
      margin: '0 12px',
      color: token.colorTextTertiary,
      fontSize: '12px',
    },
    title: {
      '.ant-badge-status-text': { color: token.colorTextTertiary, fontSize: '12px' },
    },
  };
});
export default useStyles;
