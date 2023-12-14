import { createStyles } from 'antd-style';

const useStyles = createStyles(({ token }) => {
  return {
    container: {
      display: 'flex',
      justifyContent: 'space-between',
      '.ant-card-body': { minHeight: '300px' },
    },
    zone: {
      display: 'inline-block',
      width: 'calc((100% - 96px) / 2)',
    },
    transferWrapper: {
      display: 'inline-block',
      marginTop: '150px',
      padding: '0 24px',
    },
    transfer: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      width: '32px',
      height: '32px',
      backgroundColor: token.colorBgLayout,
      border: `1px solid ${token.colorBorder}`,
      borderRadius: '4px',
    },
    active: {
      color: token.colorPrimary,
      backgroundColor: token.colorPrimary,
      borderColor: token.colorPrimary,
      cursor: 'pointer',
    },
    icon: {
      color: token.colorBgContainer,
    },
    disabled: {
      color: token.colorTextQuaternary,
      backgroundColor: token.colorFillQuaternary,
      borderColor: token.colorBorder,
      cursor: 'not-allowed',
    },
    description: {
      marginLeft: '8px',
      color: token.colorTextTertiary,
      fontWeight: 'normal',
      fontSize: '12px',
      fontFamily: 'PingFangSC-Regular',
      lineHeight: '22px',
    },
    priorityList: {
      display: 'inline-block',
      width: 'calc((100% - 96px) / 2)',
      '.ant-card-body': { padding: '0' },
    },
    dragList: {
      height: '100%',
    },
    dragItem: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '8px 16px',
      borderBottom: '1px solid #e2e8f3',
    },
    deleteIcon: {
      cursor: 'pointer',
    },
  };
});

export default useStyles;
