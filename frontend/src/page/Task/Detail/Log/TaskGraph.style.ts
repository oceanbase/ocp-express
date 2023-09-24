import { createStyles } from "antd-style";

const useStyles = createStyles(({ token }) => {
  return {
    containerWithBranch: {
      paddingRight: "40px",
    },
    container: {
      position: "relative",
      paddingRight: "20px",
      overflowX: "hidden",
      overflowY: "auto",
      scrollBehavior: "smooth",
    },
    node: {
      marginLeft: "8px",
    },
    subNode: {
      marginLeft: "32px",
    },
    icon: {
      left: "24px",
    },
    left: {
      width: "calc(100% - 250px)",
    },
    name: {
      marginBottom: "2px",
      color: token.colorText,
      fontFamily: "PingFangSC-Medium",
    },
    id: {
      marginRight: "8px",
      color: token.colorTextSecondary,
    },
    description: {
      display: "inline-block",
      color: token.colorTextSecondary,
    },
    right: {
      display: "flex",
      alignItems: "center",
      justifyContent: "space-between",
      width: "220px",
      marginLeft: "30px",
      color: token.colorTextTertiary,
    },
    taskIdWrapper: {
      padding: "5px 12px",
      ".ant-typography": {
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        color: "rgba(0, 0, 0, 0.45)",
        ".ant-typography-copy": {
          color: "rgba(0, 0, 0, 0.45)",
        },
      },
    },
  };
});

export default useStyles;
