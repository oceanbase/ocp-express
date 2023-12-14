import { token } from '@oceanbase/design';
import { createStyles } from 'antd-style';

const useStyles = createStyles(({ token }) => {
  return {
    container: {
      display: 'flex',
      '.ant-card-body': {
        '.ant-empty': {
          marginTop: '120px',
        },
      },
    },
    db: {
      flex: '0.22',
      '.ant-card-head': { background: token.colorBgContainer },
    },
    transferWrapper: {
      display: 'flex',
      flex: '0.05',
      alignItems: 'center',
      justifyContent: 'center',
    },
    transfer: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      width: '24px',
      height: '24px',
      backgroundColor: token.colorBgLayout,
      border: '1px solid transparent',
      borderColor: token.colorBorder,
      borderRadius: '4px',
    },
    active: {
      color: token.colorBgContainer,
      backgroundColor: token.colorPrimary,
      borderColor: token.colorPrimary,
      cursor: 'pointer',
    },
    disabled: {
      color: token.colorTextQuaternary,
      backgroundColor: token.colorFillQuaternary,
      borderColor: token.colorBorder,
      cursor: 'not-allowed',
    },
    privilegedListContainer: {
      flex: '0.73',
      '.ant-card-head': { background: token.colorBgContainer },
    },
    description: {
      span: { marginRight: '8px' },
      color: token.colorTextTertiary,
      fontSize: '12px',
    },
    privilegedList: {
      height: '340px',
      overflow: 'auto',
    },
    privilegedItem: {
      marginTop: '8px',
      padding: '10px',
      paddingTop: '0',
      background: token.colorBgLayout,
    },
    privilegedTitle: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      marginBottom: '4px',
      padding: '5px 0',
      borderBottom: `1px solid ${token.colorBorder}`,
    },
    delete: {
      background: token.colorBgLayout,
      border: 'none',
      boxShadow: 'none',
    },
    deleteIcon: {
      color: token.colorPrimary,
    },
    privilegeCheckbox: {
      width: '151px',
      margin: '0',
      lineHeight: '26px',
    },
  };
});

export default useStyles;
