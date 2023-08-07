import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    updateTime: {
      color: 'rgba(0, 0, 0, 0.45)',
      '.ant-form-item-label > label': {
        color: 'rgba(0, 0, 0, 0.45)',
        '&::after': {
          color: 'rgba(0, 0, 0, 0.45)',
        },
      },
    },
  };
});
export default useStyles;
