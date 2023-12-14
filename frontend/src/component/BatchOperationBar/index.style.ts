import { createStyles } from 'antd-style';

const useStyles = createStyles(({ token }) => {
  return {
    container: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: "`~'@{sizeSM}px' ~'@{sizeLG}px'`",
      overflow: 'hidden',
      backgroundColor: token.colorPrimaryBg,
      borderRadius: "`~'@{borderRadius}px'`",
    },
    small: {
      padding: "`~'@{sizeSM}px' ~'@{sizeMD}px'`",
      marginRight: "`~'@{sizeMD}px'`",
    },
    large: {
      padding: "`~'@{sizeMD}px' ~'@{sizeLG}px'`",
    },
    title: {
      marginRight: "`~'@{sizeMD}px'`",
    },
    cancel: {
      marginLeft: "`~'@{sizeXS}px'`",
      color: token.colorPrimary,
      cursor: 'pointer',
    },
    actionItem: {
      marginLeft: "`~'@{sizeXS}px'`",
    },
    left: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'flex-start',
    },
    right: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'flex-end',
    },
  };
});

export default useStyles;
