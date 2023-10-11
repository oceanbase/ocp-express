import { createStyles } from 'antd-style';

const useStyles = createStyles((token) => {
  return {
    container: {
      '.ant-card-extra': { width: '100%' },
      '.ant-card-body': { padding: '0', paddingRight: '24px' },
    },
    tag: {
      width: '16px',
      height: '16px',
      borderRadius: '2px',
    },
    zoneCol: {
      flexShrink: '0',
      margin: '16px 0 24px 0',
      padding: '0 12px',
      borderLeft: '1px dashed rgba(0, 0, 0, 0.15)',
    },
    unitRow: {
      position: 'sticky',
      left: 0,
      zIndex: 2,
      backgroundColor: token.colorBgContainer,
      padding: '16px 12px 24px 24px',
      marginBottom: 0,
      marginTop: 0,
      width: 300,
      // 仅设置 width: 300 不生效，还需要设置 min-width
      minWidth: 300,
      borderRadius: token.borderRadius,
    },
  };
});
export default useStyles;
