package com.benefitj.javastruct;


import com.benefitj.core.ReflectUtils;
import com.benefitj.javastruct.annotaion.JavaStructClass;
import com.benefitj.javastruct.annotaion.JavaStructField;
import com.benefitj.javastruct.convert.DateTimeFieldConverter;
import com.benefitj.javastruct.convert.DefaultPrimitiveFieldConverter;
import com.benefitj.javastruct.convert.FieldConverter;
import com.benefitj.javastruct.convert.PrimitiveFieldConverter;
import com.benefitj.javastruct.field.PrimitiveType;
import com.benefitj.javastruct.field.StructClass;
import com.benefitj.javastruct.field.StructField;
import com.benefitj.javastruct.resovler.DateTimeFieldResolver;
import com.benefitj.javastruct.resovler.DefaultPrimitiveFieldResolver;
import com.benefitj.javastruct.resovler.FieldResolver;
import com.benefitj.javastruct.resovler.PrimitiveFieldResolver;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 结构体管理
 */
public class JavaStructManager {

  private static final JavaStructManager INSTANCE = new JavaStructManager();

  static {
    INSTANCE.init();
  }

  public static JavaStructManager getInstance() {
    return JavaStructManager.INSTANCE;
  }

  /**
   * 字段转换器
   */
  private final Map<Class<?>, FieldConverter> fieldConverters = new ConcurrentHashMap<>();
  /**
   * 字段解析器
   */
  private final Map<Class<?>, FieldResolver<?>> fieldResolvers = new ConcurrentHashMap<>();
  /**
   * 缓存的类
   */
  private final Map<Class, StructClass> structClasses = new WeakHashMap<>();
  /**
   * 字符串编码
   */
  private String charset = Charset.defaultCharset().name();

  public JavaStructManager() {
  }

  /**
   * 初始化
   */
  public void init() {
    // 初始化转换器
    this.fieldConverters.put(PrimitiveFieldConverter.class, new DefaultPrimitiveFieldConverter());
    this.fieldConverters.put(DateTimeFieldConverter.class, new DateTimeFieldConverter());

    // 初始化解析器
    this.fieldResolvers.put(PrimitiveFieldResolver.class, new DefaultPrimitiveFieldResolver());
    this.fieldResolvers.put(DateTimeFieldResolver.class, new DateTimeFieldResolver());
  }

  public String getCharset() {
    return charset;
  }

  public void setCharset(String charset) {
    this.charset = charset;
  }

  public Map<Class, StructClass> getStructClasses() {
    return structClasses;
  }

  /**
   * 获取结构体信息
   *
   * @param type   对象类型
   * @param create 如果不存在是否创建
   * @return 返回结构体信息
   */
  public StructClass getStructClass(Class<?> type, boolean create) {
    if (create) {
      return this.structClasses.computeIfAbsent(type, this::parseStructClass);
    }
    return this.structClasses.get(type);
  }

  /**
   * 字段转换器
   */
  public Map<Class<?>, FieldConverter> getFieldConverters() {
    return fieldConverters;
  }

  /**
   * 获取字段转换器
   *
   * @param converterType 转换器类型
   * @return 返回对应的转换器
   */
  public FieldConverter getFieldConverter(Class<?> converterType) {
    return getFieldConverters().get(converterType);
  }

  /**
   * 字段解析器
   */
  public Map<Class<?>, FieldResolver<?>> getFieldResolvers() {
    return fieldResolvers;
  }

  /**
   * 获取字段解析器
   *
   * @param resolverType 解析器类型
   * @return 返回对应的解析器
   */
  public FieldResolver getFieldResolver(Class<?> resolverType) {
    return getFieldResolvers().get(resolverType);
  }

  /**
   * 转换对象
   *
   * @param o 结构体
   * @return 返回转换的字节数组
   */
  public byte[] convert(Object o) {
    return getStructClass(o.getClass(), true).convert(o);
  }

  /**
   * 解析结构体数据
   *
   * @param type 类型
   * @param data 数据
   * @param <T>  对象类型
   * @return 返回解析的对象
   */
  public <T> T parse(Class<T> type, byte[] data) {
    return parse(type, data, 0);
  }

  /**
   * 解析结构体数据
   *
   * @param type  类型
   * @param data  数据
   * @param start 开始的位置
   * @param <T>   对象类型
   * @return 返回解析的对象
   */
  public <T> T parse(Class<T> type, byte[] data, int start) {
    return getStructClass(type, true).parse(data, start);
  }

  /**
   * 解析结构体
   *
   * @param type 类型
   * @return 返回解析的结构体信息
   */
  protected StructClass parseStructClass(Class<?> type) {
    JavaStructClass jsc = type.getAnnotation(JavaStructClass.class);
    if (jsc == null) {
      throw new IllegalStateException("不支持的结构类[" + type + "]，请使用@ClassStruct注释！");
    }
    StructClass structClass = new StructClass(type);
    ReflectUtils.foreachField(type
        , f -> f.isAnnotationPresent(JavaStructField.class)
        , f -> structClass.getFields().add(createStructField(f)), f -> false);
    // 结构体大小
    structClass.setSize(Math.max(jsc.value(), structClass.getFields().stream()
        .mapToInt(StructField::size)
        .sum()));
    return structClass;
  }

  /**
   * 创建字段结构
   *
   * @param f 字段
   * @return 字段结构
   */
  protected StructField createStructField(Field f) {
    PrimitiveType primitiveType = PrimitiveType.getFieldType(f.getType());
    JavaStructField jsf = f.getAnnotation(JavaStructField.class);

    if (primitiveType == PrimitiveType.STRING
        && jsf.size() <= 0) {
      throw new IllegalStateException(String.format(
          "请指定[%s.%s]的长度", f.getDeclaringClass().getName(), f.getName()));
    }

    FieldConverter fc = findFieldConverter(f, jsf, primitiveType);
    FieldResolver<?> fr = findFieldResolver(f, jsf, primitiveType);
    StructField structField = new StructField(f);
    structField.setPrimitiveType(primitiveType);
    structField.setStructField(jsf);
    structField.setConverter(fc);
    structField.setResolver(fr);
    String charsetName = jsf.charset().trim();
    structField.setCharset(charsetName.isEmpty() ? getCharset() : charsetName);
    return structField;
  }

  /**
   * 查找字段转换器
   *
   * @param f             字段
   * @param jsf           结构注解
   * @param primitiveType 基本数据类型
   * @return 返回转换器
   */
  protected FieldConverter findFieldConverter(Field f, JavaStructField jsf, PrimitiveType primitiveType) {
    FieldConverter fc = null;
    if (jsf.converter() != FieldConverter.class) {
      fc = getFieldConverter(jsf.converter());
    }
    if (fc == null) {
      for (Map.Entry<Class<?>, FieldConverter> entry : getFieldConverters().entrySet()) {
        FieldConverter value = entry.getValue();
        if (value.support(f, jsf, primitiveType)) {
          fc = value;
          break;
        }
      }
    }

    if (fc == null) {
      throw new IllegalStateException("无法发现转换器: " + jsf.converter().getName());
    }

    if (!fc.support(f, jsf, primitiveType)) {
      throw new IllegalArgumentException(String.format(
          "不支持的数据类型: %s.%s [%s]", f.getDeclaringClass().getName(), f.getName(), f.getType().getName()));
    }
    return fc;
  }

  /**
   * 查找字段解析器
   *
   * @param f             字段
   * @param jsf           结构注解
   * @param primitiveType 基本数据类型
   * @return 返回解析器
   */
  protected FieldResolver<?> findFieldResolver(Field f, JavaStructField jsf, PrimitiveType primitiveType) {
    FieldResolver<?> fr = null;
    if (jsf.resolver() != FieldResolver.class) {
      fr = getFieldResolver(jsf.resolver());
    }
    if (fr == null) {
      for (Map.Entry<Class<?>, FieldResolver<?>> entry : getFieldResolvers().entrySet()) {
        FieldResolver<?> value = entry.getValue();
        if (value.support(f, jsf, primitiveType)) {
          fr = value;
          break;
        }
      }
    }

    if (fr == null) {
      throw new IllegalStateException("无法发现解析器: " + jsf.converter().getName());
    }

    if (!fr.support(f, jsf, primitiveType)) {
      throw new IllegalArgumentException(String.format(
          "不支持的数据类型: %s.%s [%s]", f.getDeclaringClass().getName(), f.getName(), f.getType().getName()));
    }
    return fr;
  }

}
