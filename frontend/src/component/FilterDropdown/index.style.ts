import { createStyles } from "antd-style";

const useStyles = createStyles(({ token }) => {
  return {
    filterIconFiltered: {
      color: token.colorPrimary,
    },
    anticon: {
      color: token.colorPrimary,
    },
    overlay: {
      width: "225px",
      ul: {
        marginBottom: "0",
        "& > li": {
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          marginRight: "0",
          padding: "7px 12px",
          cursor: "pointer",
          "&:hover": { backgroundColor: "#e6f7ff" },
        },
      },
    },
    searchWrapper: {
      padding: "8px",
    },
    searchIcon: {
      opacity: "0.45",
    },
    checkIcon: {
      color: token.colorPrimary,
      visibility: "visible",
      display: "none",
    },
    selected: {
      fontWeight: "600",
    },
  };
});

export default useStyles;
