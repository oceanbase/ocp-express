import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    taskIdWrapper: {
      padding: '5px 12px',
      '.ant-typography': {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        color: 'rgba(0, 0, 0, 0.45)',
        '.ant-typography-copy': {
          color: 'rgba(0, 0, 0, 0.45)',
        },
      },
    },
  };
});
export default useStyles;
