import moment from 'moment';
import {
  // HH:mm:ss
  TIME_FORMAT,
  // HH:mm:ssZ
  RFC3339_TIME_FORMAT,
} from '@/constant/datetime';

describe('datetime format', () => {
  test('HH:mm:ssZ', () => {
    // 测试 HH:mm:ssZ 支持解析带毫秒信息的时间字符串
    expect(moment('21:42:11.966+08:00', RFC3339_TIME_FORMAT).format(TIME_FORMAT)).toEqual(
      '21:42:11'
    );
    expect(moment('21:42:11+08:00', RFC3339_TIME_FORMAT).format(TIME_FORMAT)).toEqual('21:42:11');
    expect(moment('04:00:00.966+08:00', RFC3339_TIME_FORMAT).format(TIME_FORMAT)).toEqual(
      '04:00:00'
    );
    expect(moment('04:00:00.000+08:00', RFC3339_TIME_FORMAT).format(TIME_FORMAT)).toEqual(
      '04:00:00'
    );
    expect(moment('04:00:00+08:00', RFC3339_TIME_FORMAT).format(TIME_FORMAT)).toEqual('04:00:00');
  });
  test('HH:mm:ssZ', () => {
    // 测试 HH:mm:ss.SSSZ 支持不带毫秒的时间字符串
    expect(moment('21:42:11+08:00', 'HH:mm:ss.SSSZ').format(TIME_FORMAT)).toEqual('21:42:11');
  });
});
