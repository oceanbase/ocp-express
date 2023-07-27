import { formatTimeWithMicroseconds, getMicroseconds, diffWithMicroseconds } from '@/util/datetime';

describe('formatTimeWithMicroseconds', () => {
  // 单测环境下 src/global.ts 中 moment 配置化逻辑不会生效，写断言时需要注意
  test('6 digit', () => {
    expect(formatTimeWithMicroseconds('2021-04-06T21:42:11.966709+08:00')).toEqual(
      '2021-04-06 21:42:11.966709'
    );
  });
  test('1 ~ 5 digit', () => {
    expect(formatTimeWithMicroseconds('2021-04-08T14:28:03.8+08:00')).toEqual(
      '2021-04-08 14:28:03.800000'
    );
    expect(formatTimeWithMicroseconds('2021-04-08T14:28:03.80+08:00')).toEqual(
      '2021-04-08 14:28:03.800000'
    );
    expect(formatTimeWithMicroseconds('2021-04-08T14:28:03.806+08:00')).toEqual(
      '2021-04-08 14:28:03.806000'
    );
    expect(formatTimeWithMicroseconds('2021-04-08T14:28:03.8065+08:00')).toEqual(
      '2021-04-08 14:28:03.806500'
    );
    expect(formatTimeWithMicroseconds('2021-04-08T14:28:03.80652+08:00')).toEqual(
      '2021-04-08 14:28:03.806520'
    );
  });
  test('0 digit', () => {
    expect(formatTimeWithMicroseconds('2021-04-09T12:31:52+08:00')).toEqual(
      '2021-04-09 12:31:52.000000'
    );
  });
  test('Z time zone', () => {
    expect(formatTimeWithMicroseconds('2021-04-06T21:42:11.966709Z')).toEqual(
      '2021-04-07 05:42:11.966709'
    );
  });
});

describe('getMicroseconds', () => {
  test('6 digit', () => {
    expect(getMicroseconds('2021-04-06T21:42:11.966709+08:00')).toEqual(966709);
  });
  test('1 ~ 5 digit', () => {
    expect(getMicroseconds('2021-04-08T14:28:03.8+08:00')).toEqual(800000);
    expect(getMicroseconds('2021-04-08T14:28:03.80+08:00')).toEqual(800000);
    expect(getMicroseconds('2021-04-08T14:28:03.806+08:00')).toEqual(806000);
    expect(getMicroseconds('2021-04-08T14:28:03.8065+08:00')).toEqual(806500);
    expect(getMicroseconds('2021-04-08T14:28:03.80652+08:00')).toEqual(806520);
  });
  test('0 digit', () => {
    expect(getMicroseconds('2021-04-09T12:31:52+08:00')).toEqual(0);
  });
  test('Z time zone', () => {
    expect(getMicroseconds('2021-04-06T21:42:11.966709Z')).toEqual(966709);
  });
});

describe('diffWithMicroseconds', () => {
  test('6 digit', () => {
    expect(
      diffWithMicroseconds('2021-04-06T21:42:12.966709+08:00', '2021-04-06T21:42:11.948709+08:00')
    ).toEqual(1018000);
  });
  test('1 ~ 5 digit', () => {
    expect(
      diffWithMicroseconds('2021-04-08T14:28:03.8+08:00', '2021-04-08T14:28:03.7+08:00')
    ).toEqual(100000);
    expect(
      diffWithMicroseconds('2021-04-08T14:28:03.80+08:00', '2021-04-08T14:28:03.70+08:00')
    ).toEqual(100000);
    expect(
      diffWithMicroseconds('2021-04-08T14:28:03.806+08:00', '2021-04-08T14:28:03.805+08:00')
    ).toEqual(1000);
    expect(
      diffWithMicroseconds('2021-04-08T14:28:03.8065+08:00', '2021-04-08T14:28:03.8064+08:00')
    ).toEqual(100);
    expect(
      diffWithMicroseconds('2021-04-08T14:28:03.80652+08:00', '2021-04-08T14:28:03.80651+08:00')
    ).toEqual(10);
  });
  test('0 digit', () => {
    expect(diffWithMicroseconds('2021-04-09T12:31:52+08:00', '2021-04-09T12:31:51+08:00')).toEqual(
      1000000
    );
  });
  test('Z time zone', () => {
    expect(
      diffWithMicroseconds('2021-04-06T21:42:12.966709Z', '2021-04-06T21:42:11.948709Z')
    ).toEqual(1018000);
  });
});
