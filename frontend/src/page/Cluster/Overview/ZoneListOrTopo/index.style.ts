import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    stats: {
      fontSize: '10px',
      lineHeight: '22px',
      '.ant-progress-inner': { minWidth: '140px', height: '6px' },
    },
    table: {
      '.ant-table-measure-row': { borderBottom: '1px solid #e2e8f3' },
    },
  };
});
export default useStyles;
