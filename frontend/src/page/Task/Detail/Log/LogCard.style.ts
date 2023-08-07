import { createStyles } from "antd-style";

const useStyles = createStyles(({ token }) => {
  return {
    fullscreenIcon: {
      padding: "2px",
      fontSize: "12px",
      background: "rgba(0, 0, 0, 0.08)",
      borderRadius: "2px",
      cursor: "pointer",
    },
    subtaskName: {
      marginBottom: "0",
      color: token.colorText,
      fontSize: "14px",
      fontFamily: "SFProText-Medium",
    },
    subtaskId: {
      color: token.colorTextSecondary,
      fontSize: "12px",
      ".ant-typography-copy": { color: token.colorTextSecondary },
    },
    logNodeWrapper: {
      position: "absolute",
      paddingTop: "8px",
    },
    logNode: {
      display: "inline-block",
      height: "22px",
      padding: "1px 8px",
      color: "#fff",
      fontSize: "12px",
      lineHeight: "20px",
      background: "#7c8ca3",
      borderRadius: "20px",
      cursor: "pointer",
      "&:hover": { opacity: "0.8" },
    },
    container: {
      ".ant-card-head": {
        padding: "12px 16px",
        boxShadow: "0 3px 6px -4px rgba(0, 0, 0, 0.12)",
        ".ant-card-head-wrapper": {
          paddingBottom: "34px",
          ".ant-card-head-title": {
            padding: "0",
            color: token.colorTextSecondary,
            fontWeight: "normal",
          },
          ".ant-card-extra": {
            padding: "0",
            color: token.colorTextTertiary,
            fontSize: "12px",
          },
        },
      },
      ".ant-card-body": { padding: "0" },
    },
    active: {
      background: token.colorInfo,
    },
  };
});

export default useStyles;
