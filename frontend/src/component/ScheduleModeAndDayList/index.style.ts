import { token } from '@oceanbase/design';
import { createStyles } from 'antd-style';

const useStyles = createStyles(({ token }) => {
  return {
    scheduleDayWrapper: {
      width: '268px',
      marginTop: '8px',
      padding: '12px 8px',
      backgroundColor: token.colorBorder,
      borderRadius: '2px',
      li: {
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: '24px',
        height: '24px',
        margin: '4px 6px',
        borderRadius: '2px',
        cursor: 'pointer',
        '&:hover': {
          color: token.colorPrimary,
          border: `1px solid ${token.colorPrimary}`,
        },
      },
    },
    selected: {
      color: token.colorBgContainer,
      backgroundColor: token.colorPrimary,
      '&:hover': { color: token.colorBgContainer },
    },
    disabled: {
      color: token.colorBorder,
      backgroundColor: '#f7f7f7',
      cursor: 'not-allowed',
      pointerEvents: 'none',
    },
  };
});

export default useStyles;
