import { token } from '@oceanbase/design';
import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    container: {
      display: 'flex',
      alignItems: 'center',
      width: '176px',
      height: '28px',
      padding: '2px 16px',
      fontSize: '12px',
      border: '0.5px solid rgba(0, 0, 0, 0.1)',
      borderRadius: '2px',
      boxShadow: '0 6px 6px -4px rgba(0, 0, 0, 0.12)',
      '&::before': {
        position: 'absolute',
        top: '0',
        right: '0',
        bottom: '0',
        left: '0',
        background: token.colorBgContainer,
        borderRadius: '10px',
        opacity: '0',
        content: "''",
      },
      '.ant-empty': {
        margin: '0 auto',
        '.ant-empty-image': {
          height: '18px',
          marginTop: '2px',
          marginBottom: '0',
        },
      },
    },
    left2Right: {
      '&::before': { animation: 'left2Right 2.4s cubic-bezier(0.23, 1, 0.32, 1) infinite' },
    },
    right2Left: {
      '&::before': { animation: 'right2Left 2.4s cubic-bezier(0.23, 1, 0.32, 1) infinite' },
    },
  };
});
export default useStyles;
