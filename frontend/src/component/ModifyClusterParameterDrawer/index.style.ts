import { createStyles } from 'antd-style';

const useStyles = createStyles(({ token }) => {
  return {
    parameterName: {
      marginLeft: '24px',
      '.ant-legacy-form-item-control-wrapper .ant-legacy-form-item-control': { lineHeight: '32px' },
    },
    row: {
      height: '100%',
    },
    left: {
      height: '100%',
      padding: '24px 0',
      '.ant-table-thead > tr > th:first-child, .ant-table-tbody > tr > td:first-child': {
        paddingLeft: '24px',
      },
      '.ant-table-thead > tr > th:last-child, .ant-table-tbody > tr > td:last-child': {
        paddingRight: '24px',
      },
      '.ant-legacy-form-item .ant-legacy-form-item-control': { lineHeight: '60px' },
    },
    title: {
      marginBottom: '16px',
      marginLeft: '24px',
      padding: '4px 0',
      fontSize: '16px',
      fontFamily: 'SFProText-Medium',
    },
    description: {
      marginLeft: '8px',
      color: token.colorTextTertiary,
      fontSize: '12px',
    },
    selectedRow: {
      backgroundColor: token.colorBgLayout,
    },
    middle: {
      height: '100%',
      padding: '24px 0',
      backgroundColor: token.colorBgLayout,
    },
    all: {
      marginBottom: '16px',
      marginLeft: '24px',
    },
    zoneFormItem: {
      '.ant-legacy-form-item-children': { width: '100%' },
      '.ant-checkbox-group': { width: '100%' },
    },
    zone: {
      padding: '8px 24px',
      cursor: 'pointer',
    },
    selectedZone: {
      backgroundColor: token.colorBgLayout,
    },
    right: {
      height: '100%',
      padding: '72px 0 24px 24px',
      backgroundColor: token.colorBgLayout,
    },
    restartAlert: {
      marginBottom: '8px',
    },
    alert: {
      marginBottom: '24px',
    },
    tenantName: {
      paddingLeft: '5px',
      lineHeight: '30px',
    },
    newValue: {
      paddingLeft: '5px',
      lineHeight: '30px',
      backgroundColor: token.colorBgTextActive,
    },
    treeNewValue: {
      width: '205px',
      paddingLeft: '5px',
    },
  };
});
export default useStyles;
