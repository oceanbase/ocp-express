import { token } from '@oceanbase/design';
import { createStyles } from 'antd-style';

const useStyles = createStyles(() => {
  return {
    container: {
      '.ant-table-bordered': { borderBottom: 'none' },
      '.ant-table-wrapper .ant-table-thead th.ant-table-column-sort': {
        background: token.colorBgContainer,
      },
    },
  };
});
export default useStyles;
