import { createStyles } from "antd-style";

const useStyles = createStyles(({ token }) => {
  return {
    container: {
      padding: "12px",
      boxShadow: "none",
      transition: "all 0.3s",
      "&:hover": { background: token.colorFillQuaternary },
    },
    fullscreen: {
      visibility: "hidden",
      cursor: "pointer",
    },
    title: {
      fontSize: "16px",
    },
  };
});

export default useStyles;
