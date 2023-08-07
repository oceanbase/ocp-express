import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    addButton: {
      marginTop: '4px',
      borderStyle: 'dashed',
    },
    container: {
      '.ant-form-item': {
        marginBottom: '0',
        '.ant-select': {
          width: '100%',
        },
        '.ant-input-number': {
          width: '100%',
        },
      },
    },
    disabledBg: {
      padding: '10px 0',
      backgroundColor: '#f5f5f5c2',
    },
    editing: {
      '.ant-table-tbody > tr > td': {
        '.ant-legacy-form-item': {
          minHeight: '60px',
        },
        '.ant-form-item': {
          minHeight: '60px',
        },
      },
    },
    list: {
      '.ant-table-thead > tr > th': {
        padding: '0 4px',
        paddingBottom: '12px',
        fontWeight: 'normal',
        backgroundColor: 'transparent',
        borderBottom: 'none',
      },
      '.ant-table-tbody > tr > td': {
        padding: '0 4px',
        borderBottom: 'none',
        '.ant-legacy-form-item': {
          '.ant-legacy-form-item-control': { height: '32px', lineHeight: '32px' },
        },
        '.ant-form-item': {
          '.ant-form-item-control': { height: '32px', lineHeight: '32px' },
        },
        '&:first-child': {
          paddingLeft: '0',
        },
        '&:last-child': {
          paddingRight: '0',
        },
      },
      '.ant-table-expanded-row': { background: 'transparent' },
      '.ant-table-placeholder': {
        border: 'none',
        '.ant-empty-normal': {
          margin: '0',
          marginTop: '48px',
        },
      },
    },
  };
});
export default useStyles;
