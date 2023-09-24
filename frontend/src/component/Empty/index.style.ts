import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    page: {
      height: 'calc(100vh - 72px)',
    },
    description: {
      margin: '20px 0',
      color: '#8592ad',
      fontSize: '12px',
    },
    empty: {
      '.ant-empty-image': { height: '102px' },
      '.ant-empty-footer': { marginTop: '24px' },
    },
    title: {
      marginBottom: '4px',
      fontSize: '14px',
    },
    small: {
      '.ant-empty-image': { height: '72px' },
    },
  };
});
export default useStyles;
