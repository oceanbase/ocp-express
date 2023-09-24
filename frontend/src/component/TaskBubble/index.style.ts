import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    popover: {
      width: '850px',
      maxWidth: 'none',
      '.ant-popover-arrow': { border: 'none' },
      '.ant-popover-inner-content': { padding: '24px' },
    },
  };
});
export default useStyles;
