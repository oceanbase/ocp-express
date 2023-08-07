import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    footerBar: {
      padding: '10px 24px',
    },
    planDivider: {
      margin: '12px 0 12px 0',
    },
    obPlanTable: {
      '.ant-table': {
        '.ant-table-tbody > tr > td.ant-table-cell': {
          height: '24px',
          padding: '0 8px',
          whiteSpace: 'nowrap',
        },
        '.ant-table-row-indent + .ant-table-row-expand-icon': {
          marginTop: '0',
        },
        '.ant-table-thead > tr > th': {
          height: '24px',
          padding: '0 8px',
        },
        '.ant-table-cell': {
          fontSize: '12px',
        },
      },
    },
    planExtra: {
      display: 'block',
      float: 'right',
      '.ant-form-inline': {
        '.ant-form-item': {
          marginRight: '0',
        },
      },
    },
  };
});
export default useStyles;
