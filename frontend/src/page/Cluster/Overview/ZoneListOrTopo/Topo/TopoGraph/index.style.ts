import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    container: {
      paddingTop: '0',
    },
    id: {
      margin: '0 12px',
      color: 'rgba(0, 0, 0, 0.45)',
      fontSize: '12px',
    },
    title: {
      '.ant-badge-status-text': { color: 'rgba(0, 0, 0, 0.45)', fontSize: '12px' },
    },
  };
});
export default useStyles;
