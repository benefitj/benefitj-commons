package com.benefitj.core;

import java.lang.ref.SoftReference;
import java.util.Random;
import java.util.UUID;

/**
 * 生成随机字符串，可能会重复
 */
public class IdUtils {

  private static final char[] CHARS_ARRAY = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
  private static final char[] LETTERS_ARRAY = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
  private static final char[] LOWER_LETTERS_ARRAY = "abcdefghijklmnopqrstuvwxyz".toCharArray();
  private static final char[] UPPER_LETTERS_ARRAY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
  private static final char[] NUMBERS_ARRAY = "0123456789".toCharArray();
  private static final char[] HEX_UPPER_ARRAY = "0123456789ABCDEF".toCharArray();
  private static final char[] HEX_LOWER_ARRAY = "0123456789abcdef".toCharArray();

  private static final ThreadLocal<SoftReference<Random>> randomLocal = ThreadLocal.withInitial(() -> new SoftReference<>(new Random()));

  private static Random getRandom() {
    Random r = randomLocal.get().get();
    if (r != null) {
      r = new Random();
      randomLocal.set(new SoftReference<>(r));
    }
    return r;
  }

  /**
   * 获取随机的字符串
   *
   * @param length 随机字符串长度
   * @return 返回随机字符串
   */
  public static String nextId(int length) {
    return nextId(null, null, length);
  }

  /**
   * 获取随机的字符串
   *
   * @param chars  字符
   * @param length 随机字符串长度
   * @return 返回随机字符串
   */
  public static String nextId(char[] chars, int length) {
    return nextId(chars, null, null, length);
  }

  /**
   * 获取随机的字符串
   *
   * @param prefix 前缀
   * @param suffix 后缀
   * @param length 随机字符串长度
   * @return 返回随机字符串
   */
  public static String nextId(String prefix, String suffix, int length) {
    return nextId(CHARS_ARRAY, prefix, suffix, length);
  }

  /**
   * 获取随机的字符串
   *
   * @param chars  字符
   * @param prefix 前缀
   * @param suffix 后缀
   * @param length 随机字符串长度
   * @return 返回随机字符串
   */
  public static String nextId(char[] chars, String prefix, String suffix, int length) {
    StringBuilder sb = new StringBuilder(length);
    sb.append(checkNotNull(prefix));
    for (int i = 0; i < length; i++) {
      sb.append(nextChar(chars));
    }
    sb.append(checkNotNull(suffix));
    return sb.toString();
  }

  /**
   * 获取随机的字符串
   *
   * @param length 随机字符串长度
   * @return 返回随机字符串
   */
  public static String nextLetterId(int length) {
    return nextLetterId(null, null, length);
  }

  /**
   * 获取随机的字符串
   *
   * @param prefix 前缀
   * @param suffix 后缀
   * @param length 随机字符串长度
   * @return 返回随机字符串
   */
  public static String nextLetterId(String prefix, String suffix, int length) {
    return nextId(LETTERS_ARRAY, prefix, suffix, length);
  }

  /**
   * 获取随机的小写字母的字符串
   *
   * @param length 随机字符串长度
   * @return 返回随机字符串
   */
  public static String nextLowerLetterId(int length) {
    return nextLowerLetterId(null, null, length);
  }

  /**
   * 获取随机的小写字母的字符串
   *
   * @param prefix 前缀
   * @param suffix 后缀
   * @param length 随机字符串长度
   * @return 返回随机字符串
   */
  public static String nextLowerLetterId(String prefix, String suffix, int length) {
    return nextId(LOWER_LETTERS_ARRAY, prefix, suffix, length);
  }

  /**
   * 获取随机的大写字母的字符串
   *
   * @param length 随机字符串长度
   * @return 返回随机字符串
   */
  public static String nextUpperLetterId(int length) {
    return nextUpperLetterId(null, null, length);
  }

  /**
   * 获取随机的大写字母的字符串
   *
   * @param prefix 前缀
   * @param suffix 后缀
   * @param length 随机字符串长度
   * @return 返回随机字符串
   */
  public static String nextUpperLetterId(String prefix, String suffix, int length) {
    return nextId(UPPER_LETTERS_ARRAY, prefix, suffix, length);
  }

  /**
   * 获取随机的16进制的小写字母的字符串
   *
   * @param length 随机字符串长度
   * @return 返回随机字符串
   */
  public static String nextHexLowerId(int length) {
    return nextHexLowerId(null, null, length);
  }

  /**
   * 获取随机的16进制的小写字母的字符串
   *
   * @param prefix 前缀
   * @param suffix 后缀
   * @param length 随机字符串长度
   * @return 返回随机字符串
   */
  public static String nextHexLowerId(String prefix, String suffix, int length) {
    return nextId(HEX_LOWER_ARRAY, prefix, suffix, length);
  }

  /**
   * 获取随机的16进制的大写字母的字符串
   *
   * @param length 随机字符串长度
   * @return 返回随机字符串
   */
  public static String nextHexUpperId(int length) {
    return nextHexUpperId(null, null, length);
  }

  /**
   * 获取随机的16进制的大写字母的字符串
   *
   * @param prefix 前缀
   * @param suffix 后缀
   * @param length 随机字符串长度
   * @return 返回随机字符串
   */
  public static String nextHexUpperId(String prefix, String suffix, int length) {
    return nextId(HEX_UPPER_ARRAY, prefix, suffix, length);
  }

  /**
   * 获取随机的数字字符串
   *
   * @param length 随机字符串长度
   * @return 返回随机字符串
   */
  public static String nextNumberId(int length) {
    return nextNumberId(null, null, length);
  }

  /**
   * 获取随机的数字字符串
   *
   * @param prefix 前缀
   * @param suffix 后缀
   * @param length 随机字符串长度
   * @return 返回随机字符串
   */
  public static String nextNumberId(String prefix, String suffix, int length) {
    return nextId(NUMBERS_ARRAY, prefix, suffix, length);
  }

  /**
   * 随机获取下一个字符
   *
   * @return 返回随机字符
   */
  public static char defaultNextChar() {
    return nextChar(CHARS_ARRAY);
  }

  /**
   * 随机获取下一个字符
   *
   * @param chars 字符数组
   * @return 返回随机字符
   */
  public static char nextChar(char[] chars) {
    return chars[getRandom().nextInt(chars.length)];
  }

  /**
   * 获取UUID
   */
  public static String uuid() {
    return rawUUID().replace("-", "");
  }

  /**
   * 获取原始的UUID
   */
  public static String rawUUID() {
    return UUID.randomUUID().toString();
  }


  private static String checkNotNull(String str) {
    return str != null ? str : "";
  }

}
