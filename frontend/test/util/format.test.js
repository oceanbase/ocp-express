import { formatTextWithSpace } from '@/util/format';

describe('formatTextWithSpace', () => {
  test('space between Chinese and Chinese', () => {
    expect(formatTextWithSpace('中文测试english test中文测试')).toEqual(
      '中文测试 english test 中文测试'
    );
    expect(formatTextWithSpace('中文測試english test中文測試')).toEqual(
      '中文測試 english test 中文測試'
    );
  });
  test('space between Chinese and English, Chinese and Chinese', () => {
    expect(formatTextWithSpace('中 文  测   试english test中 文  测   试')).toEqual(
      '中文测试 english test 中文测试'
    );
    expect(formatTextWithSpace('中 文  測   試english test中 文  測   試')).toEqual(
      '中文測試 english test 中文測試'
    );
  });
});
