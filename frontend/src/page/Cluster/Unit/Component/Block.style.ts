import { createStyles } from "antd-style";

const useStyles = createStyles(({ token }) => {
  return {
    container: {
      display: "flex",
      alignItems: "center",
      padding: "0 16px",
      fontSize: "12px",
      background: token.colorBgLayout,
      border: "0.5px solid rgba(0, 0, 0, 0.06)",
      borderRadius: token.borderRadius,
      img: { width: "16px", height: "16px", marginTop: "4px" },
      ".ant-badge-status-text": { fontSize: "12px" },
      ".ant-typography": { fontSize: "12px" },
      ".ant-progress-inner": { backgroundColor: "rgba(65, 97, 128, 0.15)" },
      ".ant-progress-bg": { height: "4px" },
      ".ant-progress-bg, .ant-progress-success-bg": {
        backgroundColor: "rgba(62, 78, 132, 0.23)",
      },
    },
    metricWrapper: {
      width: "100%",
    },
    metricHeader: {
      display: "flex",
      justifyContent: "space-between",
    },
    metricPercent: {
      color: "rgba(0, 0, 0, 0.45)",
    },
    region: {
      width: "100%",
      height: "40px",
    },
    zone: {
      width: "100%",
      height: "40px",
    },
    server: {
      width: "100%",
      height: "40px",
    },
    unit: {
      width: "100%",
      height: "40px",
    },
    replica: {
      width: "100%",
      height: "40px",
    },
    regionWithTarget: {
      width: "100%",
      minWidth: "176px",
      height: "40px",
    },
    zoneWithTarget: {
      width: "100%",
      minWidth: "176px",
      height: "40px",
    },
    serverWithTarget: {
      width: "176px",
      height: "40px",
    },
    unitWithTarget: {
      width: "176px",
      height: "40px",
    },
    replicaWithTarget: {
      width: "176px",
      height: "40px",
    },
    memory: {
      width: "100%",
      height: "56px",
    },
    cpu: {
      width: "100%",
      height: "56px",
    },
    disk: {
      width: "100%",
      height: "56px",
    },
    memoryWithTarget: {
      width: "176px",
      height: "56px",
    },
    cpuWithTarget: {
      width: "176px",
      height: "56px",
    },
    diskWithTarget: {
      width: "176px",
      height: "56px",
    },
    tenant: {
      width: "100%",
      height: "100%",
      minHeight: "28px",
    },
    tenantWithTarget: {
      width: "100%",
      height: "28px",
      cursor: "pointer",
      "&:hover": {
        ".ant-typography": {
          color: token.colorPrimary,
        },
      },
    },
    tenantWithTargetForNoAuth: {
      cursor: "default",
      "&:hover": {
        ".ant-typography": {
          color: "rgba(0, 0, 0, 0.85)",
        },
      },
    },
  };
});

export default useStyles;
