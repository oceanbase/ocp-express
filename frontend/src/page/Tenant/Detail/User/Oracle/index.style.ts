import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    card: {
      '.ant-card-head': {
        minHeight: '66px',
        padding: '16px',
        '.ant-tabs-nav .ant-tabs-tab': {
          padding: '12px 0',
        },
      },
    },
  };
});
export default useStyles;
