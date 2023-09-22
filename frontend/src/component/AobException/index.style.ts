import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    container: {
      display: 'flex',
      alignItems: 'center',
      height: '80%',
      minHeight: '500px',
    },
    imgWrapper: {
      flex: '0 0 62.5%',
      width: '62.5%',
      paddingRight: '152px',
      zoom: '1',
      '&::before, &::after': { display: 'table', content: "' '" },
      '&::after': { clear: 'both', height: '0', fontSize: '0', visibility: 'hidden' },
    },
    img: {
      float: 'right',
      width: '100%',
      maxWidth: '430px',
      height: '360px',
      backgroundRepeat: 'no-repeat',
      backgroundPosition: '50% 50%',
      backgroundSize: 'contain',
    },
    content: {
      flex: 'auto',
      h1: {
        marginBottom: '24px',
        color: '#434e59',
        fontWeight: '600',
        fontSize: '72px',
        lineHeight: '72px',
      },
      actions: {
        'button:not(:last-child)': {
          marginRight: '8px',
        }
      }
    },
    desc: {
      marginBottom: '16px',
      color: 'rgba(0, 0, 0, 0.45)',
      fontSize: '20px',
      lineHeight: '28px',
    },
  };
});
export default useStyles;
