import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    opcGridTarget: {
      marginLeft: '12px',
    },
    table: {
      '.ant-table-measure-row': { borderBottom: '1px solid #e2e8f3' },
    },
  };
});
export default useStyles;
