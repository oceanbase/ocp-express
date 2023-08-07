import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    container: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      '.ant-input-number': { flex: '1' },
    },
    seperator: {
      margin: '0 4px',
    },
  };
});
export default useStyles;
