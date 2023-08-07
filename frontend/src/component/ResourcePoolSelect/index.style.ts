import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    container: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      'span:first-child': { flex: '1', marginRight: '8px' },
      '.ant-select': { flex: '1', marginRight: '8px' },
      '.ant-input-number': { flex: '1' },
    },
  };
});
export default useStyles;
