import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    sideHeader: {
      padding: '4px 0 4px 6px',
    },
    tenantSelect: {
      width: '120px',
      '.ant-select-selector': { padding: '0 0 0 11px' },
      '.ant-select-selection-item': { fontWeight: '600', fontSize: '18px' },
    },
  };
});
export default useStyles;
