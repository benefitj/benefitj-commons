package com.benefitj.netty.log;

/**
 * 日志接口，直接拷贝自LOG4J
 */
public interface INettyLogger {
  /**
   * Log a message at the DEBUG level.
   *
   * @param msg the message string to be logged
   */
  void debug(String msg);

  /**
   * Log a message at the DEBUG level according to the specified format and argument.
   *
   * <p>
   *
   * <p>This form avoids superfluous object creation when the logger is disabled for the DEBUG
   * level.
   *
   * @param format the format string
   * @param arg    the argument
   */
  void debug(String format, Object arg);

  /**
   * Log a message at the DEBUG level according to the specified format and arguments.
   *
   * <p>
   *
   * <p>This form avoids superfluous object creation when the logger is disabled for the DEBUG
   * level.
   *
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  void debug(String format, Object arg1, Object arg2);

  /**
   * Log a message at the DEBUG level according to the specified format and arguments.
   *
   * <p>
   *
   * <p>This form avoids superfluous string concatenation when the logger is disabled for the DEBUG
   * level. However, this variant incurs the hidden (and relatively small) cost of creating an
   * <code>Object[]</code> before invoking the method, even if this logger is disabled for DEBUG.
   * The variants taking {@link #debug(String, Object) one} and {@link #debug(String, Object,
   * Object) two} arguments exist solely in order to avoid this hidden cost.
   *
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  void debug(String format, Object... arguments);

  /**
   * Log an exception (throwable) at the DEBUG level with an accompanying message.
   *
   * @param msg the message accompanying the exception
   * @param t   the exception (throwable) to log
   */
  void debug(String msg, Throwable t);

  /**
   * Log a message at the INFO level.
   *
   * @param msg the message string to be logged
   */
  void info(String msg);

  /**
   * Log a message at the INFO level according to the specified format and argument.
   *
   * <p>
   *
   * <p>This form avoids superfluous object creation when the logger is disabled for the INFO level.
   *
   * @param format the format string
   * @param arg    the argument
   */
  void info(String format, Object arg);

  /**
   * Log a message at the INFO level according to the specified format and arguments.
   *
   * <p>
   *
   * <p>This form avoids superfluous object creation when the logger is disabled for the INFO level.
   *
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  void info(String format, Object arg1, Object arg2);

  /**
   * Log a message at the INFO level according to the specified format and arguments.
   *
   * <p>
   *
   * <p>This form avoids superfluous string concatenation when the logger is disabled for the INFO
   * level. However, this variant incurs the hidden (and relatively small) cost of creating an
   * <code>Object[]</code> before invoking the method, even if this logger is disabled for INFO. The
   * variants taking {@link #info(String, Object) one} and {@link #info(String, Object, Object) two}
   * arguments exist solely in order to avoid this hidden cost.
   *
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  void info(String format, Object... arguments);

  /**
   * Log an exception (throwable) at the INFO level with an accompanying message.
   *
   * @param msg the message accompanying the exception
   * @param t   the exception (throwable) to log
   */
  void info(String msg, Throwable t);

  /**
   * Log a message at the WARN level.
   *
   * @param msg the message string to be logged
   */
  void warn(String msg);

  /**
   * Log a message at the WARN level according to the specified format and argument.
   *
   * <p>
   *
   * <p>This form avoids superfluous object creation when the logger is disabled for the WARN level.
   *
   * @param format the format string
   * @param arg    the argument
   */
  void warn(String format, Object arg);

  /**
   * Log a message at the WARN level according to the specified format and arguments.
   *
   * <p>
   *
   * <p>This form avoids superfluous string concatenation when the logger is disabled for the WARN
   * level. However, this variant incurs the hidden (and relatively small) cost of creating an
   * <code>Object[]</code> before invoking the method, even if this logger is disabled for WARN. The
   * variants taking {@link #warn(String, Object) one} and {@link #warn(String, Object, Object) two}
   * arguments exist solely in order to avoid this hidden cost.
   *
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  void warn(String format, Object... arguments);

  /**
   * Log a message at the WARN level according to the specified format and arguments.
   *
   * <p>
   *
   * <p>This form avoids superfluous object creation when the logger is disabled for the WARN level.
   *
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  void warn(String format, Object arg1, Object arg2);

  /**
   * Log an exception (throwable) at the WARN level with an accompanying message.
   *
   * @param msg the message accompanying the exception
   * @param t   the exception (throwable) to log
   */
  void warn(String msg, Throwable t);

  /**
   * Log a message at the ERROR level.
   *
   * @param msg the message string to be logged
   */
  void error(String msg);

  /**
   * Log a message at the ERROR level according to the specified format and argument.
   *
   * <p>
   *
   * <p>This form avoids superfluous object creation when the logger is disabled for the ERROR
   * level.
   *
   * @param format the format string
   * @param arg    the argument
   */
  void error(String format, Object arg);

  /**
   * Log a message at the ERROR level according to the specified format and arguments.
   *
   * <p>
   *
   * <p>This form avoids superfluous object creation when the logger is disabled for the ERROR
   * level.
   *
   * @param format the format string
   * @param arg1   the first argument
   * @param arg2   the second argument
   */
  void error(String format, Object arg1, Object arg2);

  /**
   * Log a message at the ERROR level according to the specified format and arguments.
   *
   * <p>
   *
   * <p>This form avoids superfluous string concatenation when the logger is disabled for the ERROR
   * level. However, this variant incurs the hidden (and relatively small) cost of creating an
   * <code>Object[]</code> before invoking the method, even if this logger is disabled for ERROR.
   * The variants taking {@link #error(String, Object) one} and {@link #error(String, Object,
   * Object) two} arguments exist solely in order to avoid this hidden cost.
   *
   * @param format    the format string
   * @param arguments a list of 3 or more arguments
   */
  void error(String format, Object... arguments);

  /**
   * Log an exception (throwable) at the ERROR level with an accompanying message.
   *
   * @param msg the message accompanying the exception
   * @param t   the exception (throwable) to log
   */
  void error(String msg, Throwable t);
}