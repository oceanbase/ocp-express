import { createStyles } from "antd-style";

const useStyles = createStyles(({ token }) => {
  return {
    splitPane: {
      top: "auto",
      width: "calc(100% - 208px)",
      height: "calc(100% - 187px)",
      minHeight: "calc(100% - 187px)",
      backgroundColor: "#fff",
      borderRadius: "6px",
      ".ant-tabs": {
        ".ant-tabs-nav": {
          marginBottom: "0",
          backgroundColor: "#fff",
        },
        ".ant-tabs-nav-list": {
          marginTop: "4px",
        },
        ".ant-tabs-tab": {
          display: "inline-flex",
          minWidth: "120px",
          height: "28px",
          margin: "0",
          padding: "0",
          fontSize: "12px",
          lineHeight: "28px",
          backgroundColor: "#fff",
          border: "none",
          borderBottom: "1px solid #e8e8e8",
          borderRadius: "0",
          transition: "none",
          "&:hover": { color: "rgba(0, 0, 0, 0.65)" },
          div: {
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            height: "20px",
            marginTop: "4px",
            padding: "0 8px",
            borderLeft: "1px solid #e8e8e8",
          },
          ".ant-tabs-tab-remove": {
            marginTop: "4px",
            marginRight: "8px",
            marginLeft: "0",
            padding: "0",
          },
        },
        ".ant-tabs-tab:first-child, .ant-tabs-tab:last-child": {
          div: { borderLeft: "1px solid transparent" },
        },
        ".ant-tabs-tab-active": {
          backgroundColor: "#f7f8fc",
          borderBottom: "none",
          borderLeft: "1px solid transparent",
          borderRadius: "2px",
          ".ant-tabs-tab-btn": {
            color: "rgba(0, 0, 0, 0.85)",
            fontWeight: "normal",
          },
          div: { borderLeft: "1px solid transparent" },
          "& + .ant-tabs-tab": {
            div: {
              borderLeft: "1px solid transparent",
            },
          },
        },
        ".ant-tabs-extra-content": {
          marginRight: "16px",
          lineHeight: "32px",
        },
      },
      margin: "0 -24px",
    },
    tabsWrapper: {
      width: "100%",
      padding: "0 16px 16px 16px",
      boxShadow: token.boxShadowSecondary,
    },
    log: {
      padding: "12px 16px",
      color: "rgba(0, 0, 0, 0.65)",
      fontSize: "12px",
      lineHeight: "22px",
      whiteSpace: "pre-wrap",
      wordBreak: "break-all",
    },
    english: {
      minHeight: "calc(100% - 140px - 38px)",
    },
  };
});

export default useStyles;
