/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.influxdb.template;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "spring.influxdb")
public class InfluxDBProperty {

  /**
   * InfluxDB连接路径
   */
  private String url;
  /**
   * 用户名
   */
  private String username;
  /**
   * 密码
   */
  private String password;
  /**
   * 数据库
   */
  private String database;
  /**
   * 存储策略
   */
  private String retentionPolicy = "autogen";
  /**
   * 连接超时时间
   */
  private int connectTimeout = 10;
  /**
   * 读取超时时间
   */
  private int readTimeout = 30;
  /**
   * 写入超时时间
   */
  private int writeTimeout = 10;
  /**
   * gzip压缩
   */
  private boolean gzip = true;
  /**
   * 是否为批处理
   */
  private boolean enableBatch = true;
  /**
   * 批处理的响应数
   */
  private int batchActions = 100000;
  /**
   * 时间戳的字段名
   */
  private String timeFieldName = "time";
  /**
   * 查询的集群
   */
  private List<QueryCluster> queryCluster;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public String getRetentionPolicy() {
    return retentionPolicy;
  }

  public void setRetentionPolicy(String retentionPolicy) {
    this.retentionPolicy = retentionPolicy;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  public void setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
  }

  public int getWriteTimeout() {
    return writeTimeout;
  }

  public void setWriteTimeout(int writeTimeout) {
    this.writeTimeout = writeTimeout;
  }

  public boolean isGzip() {
    return gzip;
  }

  public void setGzip(boolean gzip) {
    this.gzip = gzip;
  }

  public boolean isEnableBatch() {
    return enableBatch;
  }

  public void setEnableBatch(boolean enableBatch) {
    this.enableBatch = enableBatch;
  }

  public int getBatchActions() {
    return batchActions;
  }

  public void setBatchActions(int batchActions) {
    this.batchActions = batchActions;
  }

  public String getTimeFieldName() {
    return timeFieldName;
  }

  public void setTimeFieldName(String timeFieldName) {
    this.timeFieldName = timeFieldName;
  }

  public void setQueryCluster(List<QueryCluster> queryCluster) {
    this.queryCluster = queryCluster;
  }

  public List<QueryCluster> getQueryCluster() {
    return queryCluster;
  }

  /**
   * 查询的集群
   */
  public static class QueryCluster {
    /**
     * InfluxDB连接路径
     */
    private String url = "http://localhost:8086/";
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

  }
}