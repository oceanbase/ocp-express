import { createStyles } from "antd-style";

const useStyles = createStyles(({ token }) => {
  return {
    "borderRight::after": {
      position: "absolute",
      top: "auto",
      right: "0",
      bottom: "10px",
      left: "auto",
      width: "1px",
      height: "22px",
      backgroundColor: token.colorBorderSecondary,
      content: "''",
    },
  };
});

export default useStyles;
