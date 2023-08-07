import { createStyles } from "antd-style";

const useStyles = createStyles(({ token }) => {
  return {
    container: {
      fontWeight: "normal",
      fontSize: "12px",
    },
    icon: {
      color: token.colorPrimary,
    },
    content: {
      color: "rgba(0, 0, 0, 0.45)",
    },
  };
});

export default useStyles;
