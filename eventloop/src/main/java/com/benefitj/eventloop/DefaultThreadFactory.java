package com.benefitj.eventloop;


import com.google.common.base.Preconditions;

import java.util.Locale;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link ThreadFactory} implementation with a simple naming rule.
 */
public class DefaultThreadFactory implements ThreadFactory {

  private static final AtomicInteger poolId = new AtomicInteger();

  private final AtomicInteger nextId = new AtomicInteger();
  private final String prefix;
  private final boolean daemon;
  private final int priority;
  protected final ThreadGroup threadGroup;

  public DefaultThreadFactory(Class<?> poolType) {
    this(poolType, false, Thread.NORM_PRIORITY);
  }

  public DefaultThreadFactory(String poolName) {
    this(poolName, false, Thread.NORM_PRIORITY);
  }

  public DefaultThreadFactory(Class<?> poolType, boolean daemon) {
    this(poolType, daemon, Thread.NORM_PRIORITY);
  }

  public DefaultThreadFactory(String poolName, boolean daemon) {
    this(poolName, daemon, Thread.NORM_PRIORITY);
  }

  public DefaultThreadFactory(Class<?> poolType, int priority) {
    this(poolType, false, priority);
  }

  public DefaultThreadFactory(String poolName, int priority) {
    this(poolName, false, priority);
  }

  public DefaultThreadFactory(Class<?> poolType, boolean daemon, int priority) {
    this(toPoolName(poolType), daemon, priority);
  }

  public static String toPoolName(Class<?> poolType) {
    Preconditions.checkNotNull(poolType, "poolType");

    String poolName = poolType.getSimpleName();
    switch (poolName.length()) {
      case 0:
        return "unknown";
      case 1:
        return poolName.toLowerCase(Locale.US);
      default:
        if (Character.isUpperCase(poolName.charAt(0)) && Character.isLowerCase(poolName.charAt(1))) {
          return Character.toLowerCase(poolName.charAt(0)) + poolName.substring(1);
        } else {
          return poolName;
        }
    }
  }

  public DefaultThreadFactory(String poolName, boolean daemon, int priority, ThreadGroup threadGroup) {
    Preconditions.checkNotNull(poolName, "poolName");

    if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
      throw new IllegalArgumentException(
          "priority: " + priority + " (expected: Thread.MIN_PRIORITY <= priority <= Thread.MAX_PRIORITY)");
    }

    prefix = poolName + '-' + poolId.incrementAndGet() + '-';
    this.daemon = daemon;
    this.priority = priority;
    this.threadGroup = threadGroup;
  }

  public DefaultThreadFactory(String poolName, boolean daemon, int priority) {
    this(poolName, daemon, priority, System.getSecurityManager() == null ?
        Thread.currentThread().getThreadGroup() : System.getSecurityManager().getThreadGroup());
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread t = newThread(r, prefix + nextId.incrementAndGet());
    try {
      if (t.isDaemon() != daemon) {
        t.setDaemon(daemon);
      }

      if (t.getPriority() != priority) {
        t.setPriority(priority);
      }
    } catch (Exception ignored) {
      // Doesn't matter even if failed to set.
    }
    return t;
  }

  protected Thread newThread(Runnable r, String name) {
    return new Thread(threadGroup, r, name);
  }
}
