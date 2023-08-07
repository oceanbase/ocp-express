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
      borderLeft: '1px solid #e8e8e8',
    },
    textWrapper: {
      textAlign: 'left',
    },
    name: {
      display: 'block',
      color: 'rgba(0, 0, 0, 0.85)',
    },
    value: {
      color: 'rgba(0, 0, 0, 0.85)',
      fontSize: '30px',
      fontFamily: 'SFProText-Medium',
    },
  };
});
export default useStyles;
