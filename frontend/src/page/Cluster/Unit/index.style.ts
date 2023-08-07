import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
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
  };
});
export default useStyles;
