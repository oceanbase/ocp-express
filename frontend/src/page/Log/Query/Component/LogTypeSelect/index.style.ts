import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    LogTypeSelect: {
      width: '100%',
      '.ant-space-item:nth-child(1) .ant-select-selector': {
        borderTopRightRadius: 0,
        borderBottomRightRadius: 0
      },
      '.ant-space-item:nth-child(2)': {
        width: '100%',
        '.ant-select-selector': {
          borderLeft: 0,
          borderTopLeftRadius: 0,
          borderBottomLeftRadius: 0,
        },
        '&:hover': {
          zIndex: 1,
        },
        '.ant-select': {
          width: '100%',
        }
      },

    },
  };
});
export default useStyles;
