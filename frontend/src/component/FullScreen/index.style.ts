import { createStyles } from "antd-style";

const useStyles = createStyles(({ token }) => {
  return {
    container: {
      height: "100%",
    },
    bubble: {
      position: "absolute",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      width: "48px",
      height: "48px",
      marginTop: "24px",
      marginLeft: "24px",
      fontSize: "20px",
      background: "#fff",
      borderRadius: "48px",
      boxShadow: "0 2px 8px 0 rgba(0, 0, 0, 0.1)",
    },
    icon: {
      cursor: "pointer",
    },
    titleBar: {
      position: "absolute",
      height: "54px",
      marginTop: "17px",
      marginLeft: "24px",
      padding: "16px 24px",
      background: "#fff",
      borderRadius: "48px",
      boxShadow: "0 2px 8px 0 rgba(0, 0, 0, 0.1)",
    },
    title: {
      color: token.colorText,
      fontFamily: "PingFangSC-Medium",
    },
    description: {
      color: "rgba(0, 0, 0, 0.65)",
    },
    toolbar: {
      position: "absolute",
    },
    "tech-fullscreen-box-header-icon": {
      color: "rgba(0, 0, 0, 0.85)",
      fontSize: "16px",
    },
  };
});

export default useStyles;
