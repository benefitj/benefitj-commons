package org.springframework.data.influxdb.converter;

import org.springframework.data.influxdb.InfluxUtils;

/**
 * 行协议转换器
 *
 * @param <T>
 */
public class LineProtocolConverter<T> extends AbstractConverter<T, String> {

  public LineProtocolConverter(Class<T> type) {
    super(type);
  }

  @Override
  public String convert(T item) {
    return InfluxUtils.toLineProtocol(this, item);
  }

}
