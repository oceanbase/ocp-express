import { includesChinese, splitFirst, splitLast, isURL } from '@/util';

describe('includesChinese', () => {
  test('Chinese string', () => {
    expect(includesChinese('中文测试')).toEqual(true);
    expect(includesChinese('中文測試')).toEqual(true);
  });
  test('English string', () => {
    expect(includesChinese('english test')).toEqual(false);
  });
  test('Chinese and English string', () => {
    expect(includesChinese('中文测试 english test')).toEqual(true);
    expect(includesChinese('中文測試 english test')).toEqual(true);
  });
  test('Chinese symbols', () => {
    expect(includesChinese('「」：（）')).toEqual(true);
  });
  test('null value', () => {
    expect(includesChinese(null)).toEqual(false);
    expect(includesChinese(undefined)).toEqual(false);
    expect(includesChinese('')).toEqual(false);
  });
});

describe('splitFirst', () => {
  test('single seperator', () => {
    expect(splitFirst('clusterName:100', ':')).toEqual(['clusterName', '100']);
  });
  test('multiple seperators', () => {
    expect(splitFirst('clusterName:abc:100', ':')).toEqual(['clusterName', 'abc:100']);
    expect(splitFirst('clusterName:abc:100:200', ':')).toEqual(['clusterName', 'abc:100:200']);
  });
  test('no seperator', () => {
    expect(splitFirst('clusterName', ':')).toEqual(['clusterName']);
  });
  test('empty string', () => {
    expect(splitFirst('', ':')).toEqual(['']);
  });
  test('nil value', () => {
    expect(splitFirst(null, ':')).toEqual(null);
    expect(splitFirst(undefined, ':')).toEqual(undefined);
  });
});

describe('splitLast', () => {
  test('single seperator', () => {
    expect(splitLast('clusterName:100', ':')).toEqual(['clusterName', '100']);
  });
  test('multiple seperators', () => {
    expect(splitLast('clusterName:abc:100', ':')).toEqual(['clusterName:abc', '100']);
    expect(splitLast('clusterName:abc:100:200', ':')).toEqual(['clusterName:abc:100', '200']);
  });
  test('no seperator', () => {
    expect(splitLast('clusterName', ':')).toEqual(['clusterName']);
  });
  test('empty string', () => {
    expect(splitLast('', ':')).toEqual(['']);
  });
  test('nil value', () => {
    expect(splitFirst(null, ':')).toEqual(null);
    expect(splitFirst(undefined, ':')).toEqual(undefined);
  });
});

describe('isURL', () => {
  test('domain', () => {
    expect(isURL('http://www.127.0.0.1.com')).toEqual(true);
    // 带路径
    expect(isURL('http://www.127.0.0.1.com/login')).toEqual(true);
    // 带端口
    expect(isURL('http://www.127.0.0.1.com:8080/login')).toEqual(true);
    // 带 query 参数
    expect(isURL('http://www.127.0.0.1.com:8080/login?callback=/test')).toEqual(true);
    // 带 hash 参数
    expect(isURL('http://www.127.0.0.1.com:8080/login?callback=/test#title')).toEqual(true);
    // query 参数是 URL
    expect(
      isURL(
        `http://www.127.0.0.1.com:8080/login?callback=${encodeURIComponent(
          'https://www.127.0.0.1.com:8080/test'
        )}#title`
      )
    ).toEqual(true);
  });
  test('ip', () => {
    expect(isURL('http://127.0.0.1')).toEqual(true);
    // 带路径
    expect(isURL('http://127.0.0.1/login')).toEqual(true);
    // 带端口
    expect(isURL('http://127.0.0.1:8080/login')).toEqual(true);
    // 带 query 参数
    expect(isURL('http://127.0.0.1:8080/login?callback=/test')).toEqual(true);
    // 带 hash 参数
    expect(isURL('http://127.0.0.1:8080/login?callback=/test#title')).toEqual(true);
    // query 参数是 URL
    expect(
      isURL(
        `http://127.0.0.1:8080/login?callback=${encodeURIComponent(
          'https://127.0.0.1:8080/test'
        )}#title`
      )
    ).toEqual(true);
  });
});
