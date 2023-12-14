import { createStyles } from 'antd-style';

const useStyles = createStyles(({ token }) => {
  return {
    containerWithBranch: {
      paddingRight: '40px',
    },
    container: {
      position: 'relative',
      paddingRight: '20px',
      overflowX: 'hidden',
      overflowY: 'auto',
      scrollBehavior: 'smooth',
    },
    node: {
      marginLeft: '8px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      marginBottom: '4px',
      padding: '10px 16px',
      fontSize: '12px',
      lineHeight: '20px',
      backgroundColor: token.colorBgContainer,
      borderRadius: '8px',
      cursor: 'pointer',
    },
    subNode: {
      marginLeft: '32px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      marginBottom: '4px',
      padding: '10px 16px',
      fontSize: '12px',
      lineHeight: '20px',
      backgroundColor: token.colorBgContainer,
      borderRadius: '8px',
      cursor: 'pointer',
    },
    icon: {
      position: 'absolute',
      fontSize: '16px',
    },
    nodeIcon: {
      left: '0px',
    },
    subNodeIcon: {
      left: '24px',
    },
    active: {
      border: `1px solid ${token.colorSuccess}`,
      borderRight: `4px solid ${token.colorSuccess}`,
      backgroundColor: token.colorSuccessBg,
      // 节点的右侧 padding 为 16px，需要将选中节点的右侧 padding 减小为 11px，以抵消左右两侧 5px 的 border 影响
      paddingRight: '11px',
    },
    left: {
      width: 'calc(100% - 250px)',
    },
    name: {
      marginBottom: '2px',
      color: token.colorText,
      fontFamily: 'PingFangSC-Medium',
    },
    id: {
      marginRight: '8px',
      color: token.colorTextSecondary,
    },
    description: {
      display: 'inline-block',
      width: 'calc(100% - 90px)',
      color: token.colorTextSecondary,
    },

    right: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      width: '220px',
      marginLeft: '30px',
      color: token.colorTextTertiary,
    },
    taskIdWrapper: {
      padding: '5px 12px',
      '.ant-typography': {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        color: token.colorTextSecondary,
        '.ant-typography-copy': {
          color: token.colorTextSecondary,
        },
      },
    },
  };
});

export default useStyles;
