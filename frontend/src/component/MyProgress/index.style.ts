import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    progress: {
      display: 'flex',
      '.ant-progress-inner': { backgroundColor: 'rgba(65, 97, 128, 0.15)', borderRadius: '0' },
      '.ant-progress-circle, .ant-progress-dashboard': {
        '.ant-progress-inner': {
          backgroundColor: 'transparent',
        },
      },
    },
    wrapper: {
      display: 'inline-block',
      flex: '1',
    },
    prefix: {
      marginRight: '8px',
    },
    affix: {
      marginLeft: '8px',
    },
  };
});
export default useStyles;
