import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    parameters: {
      marginBottom: '0',
      '.ant-form-item-label > label': { display: 'block', width: '100%' },
    },
    table: {
      marginBottom: '24px',
      '.ant-table-thead > tr > th': {
        padding: '0',
        fontWeight: 'normal',
        backgroundColor: 'transparent',
        borderBottom: 'none',
      },
    },
  };
});
export default useStyles;
