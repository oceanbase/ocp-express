import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    drawer: {
      '.ant-drawer-body': { height: 'calc(100% - 56px)', padding: '0' },
    },
    row: {
      height: '100%',
    },
    left: {
      height: '100%',
      padding: '24px 0',
      '.ant-table-thead > tr > th:first-child, .ant-table-tbody > tr > td:first-child': {
        paddingLeft: '24px',
      },
      '.ant-table-thead > tr > th:last-child, .ant-table-tbody > tr > td:last-child': {
        paddingRight: '24px',
      },
    },
    title: {
      marginBottom: '16px',
      padding: '4px 0',
      fontSize: '16px',
      fontFamily: 'SFProText-Medium',
    },
    description: {
      marginLeft: '8px',
      color: 'rgba(0, 0, 0, 0.45)',
      fontSize: '12px',
    },
    selectedRow: {
      backgroundColor: '#fafafa',
    },
    middle: {
      height: '100%',
      padding: '24px 0 24px 24px',
      backgroundColor: '#fafafa',
    },
    all: {
      marginBottom: '16px',
    },
    zone: {
      paddingLeft: '24px',
      cursor: 'pointer',
    },
    selectedZone: {
      backgroundColor: '#edeff2',
    },
    right: {
      height: '100%',
      padding: '72px 0 24px 24px',
      backgroundColor: '#edeff2',
    },
  };
});
export default useStyles;
