import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    flow: {
      '.ant-pro-grid-content .ant-descriptions': {
        marginRight: '-24px',
        marginLeft: '-24px',
        padding: '0 24px',
      },
    },
    descriptions: {
      zIndex: 1,
      position: 'relative'
    }
  };
});
export default useStyles;
