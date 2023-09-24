import { createStyles } from "antd-style";

const useStyles = createStyles(({ token }) => {
  return {
    container: {
      display: "flex",
      ".ant-card-body": {
        ".ant-empty": {
          marginTop: "120px",
        },
      },
    },
    db: {
      flex: "0.22",
      ".ant-card-head": { background: "#fff" },
    },
    transferWrapper: {
      display: "flex",
      flex: "0.05",
      alignItems: "center",
      justifyContent: "center",
    },
    transfer: {
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      width: "24px",
      height: "24px",
      backgroundColor: "#f5f8fe",
      border: "1px solid transparent",
      borderColor: "#cdd5e4",
      borderRadius: "4px",
    },
    active: {
      color: "#fff",
      backgroundColor: token.colorPrimary,
      borderColor: token.colorPrimary,
      cursor: "pointer",
    },
    disabled: {
      color: "rgba(0, 0, 0, 0.25)",
      backgroundColor: "#f5f5f5",
      borderColor: "#d9d9d9",
      cursor: "not-allowed",
    },
    privilegedListContainer: {
      flex: "0.73",
      ".ant-card-head": { background: "#fff" },
    },
    description: {
      span: { marginRight: "8px" },
      color: "#8592ad",
      fontSize: "12px",
    },
    privilegedList: {
      height: "340px",
      overflow: "auto",
    },
    privilegedItem: {
      marginTop: "8px",
      padding: "10px",
      paddingTop: "0",
      background: "#fafafa",
    },
    privilegedTitle: {
      display: "flex",
      alignItems: "center",
      justifyContent: "space-between",
      marginBottom: "4px",
      padding: "5px 0",
      borderBottom: "1px solid #f0f0f0",
    },
    delete: {
      background: "#fafafa",
      border: "none",
      boxShadow: "none",
    },
    deleteIcon: {
      color: token.colorPrimary,
    },
    privilegeCheckbox: {
      width: "151px",
      margin: "0",
      lineHeight: "26px",
    },
  };
});

export default useStyles;
