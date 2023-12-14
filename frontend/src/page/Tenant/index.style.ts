import { token } from '@oceanbase/design';
import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    left: {
      paddingRight: '16px',
      paddingBottom: '0',
    },
    right: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      height: '200px',
      textAlign: 'center',
      borderLeft: `1px solid ${token.colorBorder}`,
    },
    textWrapper: {
      textAlign: 'left',
    },
    name: {
      display: 'block',
      color: token.colorText,
    },
    value: {
      color: token.colorText,
      fontSize: '30px',
      fontFamily: 'SFProText-Medium',
    },
  };
});
export default useStyles;
