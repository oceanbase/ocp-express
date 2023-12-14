import { token } from '@oceanbase/design';
import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    settingIcon: {
      marginLeft: '16px',
      cursor: 'pointer',
    },
    filterTitle: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
    },
    group: {
      padding: '4px 16px',
      color: token.colorTextTertiary,
      fontSize: '12px',
      fontFamily: 'PingFangSC-Regular',
      '&:first-child': { paddingTop: '8px' },
    },
    item: {
      padding: '4px 16px 4px 0',
      '&:hover': { backgroundColor: token.colorInfoBgHover },
      '&:last-child': { paddingBottom: '8px' },
    },
    popover: {
      '.ant-popover-title': { padding: '8px 16px' },
      '.ant-popover-inner-content': {
        width: '230px',
        maxHeight: '300px',
        padding: '0',
        overflow: 'auto',
      },
    },
  };
});
export default useStyles;
