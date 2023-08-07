import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    container: {
      '.ant-form-item:last-child': { marginBottom: '0' },
      '.ant-legacy-form-item': { marginBottom: '4px' },
      '.ant-select-selection--multiple': { minHeight: '90px' },
    },
  };
});
export default useStyles;
