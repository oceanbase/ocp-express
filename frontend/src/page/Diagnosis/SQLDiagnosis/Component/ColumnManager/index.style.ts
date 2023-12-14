import { token } from '@oceanbase/design';
import { createStyles } from 'antd-style';

const useStyles = createStyles(({ token }) => {
  return {
    tableExtra: {
      float: 'right',
      color: token.colorTextTertiary,
      lineHeight: '28px',
      cursor: 'pointer',
      '&:hover': { color: token.colorPrimary, transition: 'color 0.5s' },
    },
    anticon: {
      marginRight: '4px',
    },
    fieldSearch: {
      textAlign: 'center',
      '.ant-input-search': {
        width: '400px',
        '.ant-input': {
          borderRadius: '0',
        },
        '.ant-btn': {
          borderRadius: '0',
        },
      },
    },
    drawer: {
      '.ant-drawer-wrapper-body': { position: 'relative' },
    },
    drawBodyContainer: {
      position: 'relative',
    },
    checkboxGroupGrid: {
      display: 'flex',
      flexFlow: 'row wrap',
      width: '100%',
    },

    listFooter: {
      position: 'absolute',
      right: '0',
      bottom: '0',
      width: '100%',
      padding: '10px 16px',
      textAlign: 'right',
      background: token.colorBgContainer,
      borderTop: '1px solid #e9e9e9',
      '.ant-btn': { marginLeft: '8px' },
    },
  };
});

export default useStyles;
