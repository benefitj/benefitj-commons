package com.benefitj.core.cmd;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * CMD执行(bat/shell)
 */
public class CmdManager {

  /**
   * 是否为windows
   */
  private static final boolean WINDOWS;
  public static final String CRLF;
  static {
    WINDOWS = System.getProperty("os.name").contains("Windows");
    CRLF = WINDOWS ? "\r\n" : "\n";
  }

  /**
   * 判断是否为Windows
   */
  public static boolean isWindows() {
    return WINDOWS;
  }

  private final AtomicReference<Thread> sign = new AtomicReference<>();
  private final AtomicInteger aliveProcess = new AtomicInteger();

  private final Object lock = new Object();
  /**
   * 超时时长，5分钟
   */
  private long timeout = 300_000;
  /**
   * 最大子进程数
   */
  private volatile int maxCallNum = 20;
  /**
   * 调度器
   */
  private volatile ScheduledExecutorService executor;
  /**
   * 等待中的执行命令
   */
  private final Map<String, CmdCallFuture> waitForFutures = new ConcurrentHashMap<>();
  /**
   * 销毁的监听
   */
  private DestroyListener destroyListener = DestroyListener.DISCARD;

  public CmdManager() {
  }

  public DestroyListener getDestroyListener() {
    return destroyListener;
  }

  public void setDestroyListener(DestroyListener destroyListener) {
    this.destroyListener = (destroyListener != null
        ? destroyListener : DestroyListener.DISCARD);
  }

  /**
   * 调度器
   */
  public ScheduledExecutorService getExecutor() {
    ScheduledExecutorService e = this.executor;
    if (e == null) {
      synchronized (this) {
        if ((e = this.executor) == null) {
          ThreadFactory threadFactory = new DefaultThreadFactory("cmd-", "-t-", true);
          e = (this.executor = Executors.newScheduledThreadPool(4, threadFactory));
        }
      }
    }
    return e;
  }

  /**
   * Creates and executes a one-shot action that becomes enabled
   * after the given delay.
   *
   * @param command the task to execute
   * @param delay   the time from now to delay execution
   * @param unit    the time unit of the delay parameter
   * @return a ScheduledFuture representing pending completion of
   * the task and whose {@code get()} method will return
   * {@code null} upon completion
   */
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    return getExecutor().schedule(command, delay, unit);
  }

  /**
   * 获取等待中的进程
   */
  protected Map<String, CmdCallFuture> getWaitForFutures() {
    return waitForFutures;
  }

  /**
   * 是否处于等待中
   *
   * @param processId 进程ID
   * @return 是否等待
   */
  public boolean isWaitingFor(String processId) {
    return waitForFutures.containsKey(processId);
  }

  /**
   * 调用命令
   *
   * @param cmd 命令
   * @return 返回结果的值
   */
  public CmdCall call(String cmd) {
    return call(cmd, null, null, 0);
  }

  /**
   * 调用命令
   *
   * @param cmd     命令
   * @param timeout 超时时长
   * @return 返回结果的值
   */
  public CmdCall call(String cmd, long timeout) {
    return call(cmd, null, null, timeout);
  }

  /**
   * 调用命令
   *
   * @param cmd  命令
   * @param envp 环境变量
   * @param dir  上下文目录
   * @return 返回结果的值
   */
  public CmdCall call(String cmd, @Nullable List<String> envp, @Nullable File dir) {
    return call(cmd, envp, dir, 0);
  }

  /**
   * 调用命令
   *
   * @param cmd     命令
   * @param envp    环境变量
   * @param dir     上下文目录
   * @param timeout 超时时长
   * @return 执行命令后的响应
   */
  public CmdCall call(String cmd, @Nullable List<String> envp, @Nullable File dir, long timeout) {
    return call(cmd, envp, dir, timeout, null);
  }

  /**
   * 调用命令
   *
   * @param cmd      命令
   * @param envp     环境变量
   * @param dir      上下文目录
   * @param timeout  超时时长
   * @param callback 回调
   * @return 执行命令后的响应
   */
  public CmdCall call(String cmd, @Nullable List<String> envp, @Nullable File dir, long timeout, @Nullable Callback callback) {
    final Callback cb = callback != null ? callback : Callback.EMPTY_CALLBACK;
    final long start = now();
    final CmdCall call = createCmdCall(uuid());
    cb.onStart(call);
    try {
      return safeCall(timeout, start, () -> {
        String[] envparams = envp != null && !envp.isEmpty() ? envp.toArray(new String[0]) : null;
        call.setCmd(cmd);
        call.setCtxDir(dir);
        call.setEnvp(envparams);
        cb.onCallBefore(call, cmd, envparams, dir);
        final Process process = Runtime.getRuntime().exec(cmd, envparams, dir);
        call.setProcess(process);
        cb.onCallAfter(process, call);
        // 强制结束
        scheduleTimeout(call, timeout - (now() - start));
        cb.onWaitForBefore(process, call);
        try {
          // 等待
          int exitValue = process.waitFor();
          call.setCode(exitValue);
        } catch (InterruptedException e) {
          call.setCode(-1);
        }
        // 移除等待的缓存
        cancelTimeoutSchedule(call.getId());
        // 调用结束
        cb.onWaitForAfter(process, call);
        // 读取消息
        readMessage(process, call);

        return call;
      });
    } catch (Exception e) {
      throw new IllegalStateException(e);
    } finally {
      cb.onFinish(call);
    }
  }

  private void readMessage(Process process, CmdCall call) {
    try {
      Charset charset = Charset.defaultCharset();
      try (BufferedReader respBr = new BufferedReader(new InputStreamReader(process.getInputStream(), charset));
           BufferedReader errorBr = new BufferedReader(new InputStreamReader(process.getErrorStream(), charset));) {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = respBr.readLine()) != null) {
          sb.append(line).append(CRLF);
        }
        call.setMessage(sb.toString());

        sb.setLength(0);
        while ((line = errorBr.readLine()) != null) {
          sb.append(line).append(CRLF);
        }
        call.setError(sb.toString());
      }
    } catch (IOException e) {
      call.setError(e.getMessage());
    }
  }

  /**
   * 取消超时时长的调度
   *
   * @param id 进程ID
   */
  protected void cancelTimeoutSchedule(String id) {
    final CmdCallFuture crf = getWaitForFutures().remove(id);
    if (crf != null) {
      crf.cancel(true);
      CmdCall call = crf.getOriginal();
      getDestroyListener().onCancel(call.getProcess(), call, true);
    }
  }

  /**
   * 延迟结束调用
   *
   * @param call CMD响应
   * @param timeout  超时时长
   */
  protected void scheduleTimeout(CmdCall call, long timeout) {
    timeout = timeout > 0 ? timeout : getTimeout();
    final CmdCallFuture crf = new CmdCallFuture(call, (id, f) -> {
      getWaitForFutures().remove(id);
      final CmdCall cr = f.getOriginal();
      final Process p = cr.getProcess();
      if (p != null) {
        p.destroyForcibly();
        getDestroyListener().onDestroy(p, cr);
      }
      // 唤醒等待的线程
      lock(Object::notify);
    });
    getWaitForFutures().put(call.getId(), crf);
    ScheduledFuture<?> sf = schedule(crf, timeout, TimeUnit.MILLISECONDS);
    crf.setFuture(sf);
  }

  /**
   * 加锁
   *
   * @param timeout 超时时长
   * @param start   开始时间
   * @return 返回是否加锁
   */
  private <V> V safeCall(long timeout, long start, Callable<V> call) throws Exception {
    final AtomicReference<Thread> sign = this.sign;
    int maxProcess = getMaxCallNum();
    final Thread current = Thread.currentThread();
    for (;;) {
      if (sign.compareAndSet(null, current)) {
        // 执行的命令未达到最大值
        if (aliveProcess.get() < maxProcess) {
          try {
            aliveProcess.incrementAndGet();
            // 释放锁
            sign.set(null);
            return call.call();
          } finally {
            aliveProcess.decrementAndGet();
            // 唤醒等待的线程
            lock(Object::notify);
          }
        }
      }

      if ((timeout - (now() - start)) > 0) {
        lock((lock) -> {
          sign.set(null);
          // 等待时长
          sign.wait(Math.max(timeout - (now() - start), 0));
        });
      }
      // 判断超时状态
      if (isTimeout(start, timeout)) {
        // 超时了，结束操作
        throw new TimeoutException("等待超时！");
      }
    }
  }


  protected final void lock(LockObserver observer) {
    synchronized (lock) {
      try {
        observer.accept(lock);
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  public CmdCall createCmdCall(String id) {
    return new CmdCall(id);
  }


  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public int getMaxCallNum() {
    return maxCallNum;
  }

  public void setMaxCallNum(int maxCallNum) {
    this.maxCallNum = Math.min(maxCallNum, 1);
  }

  public int getAliveProcess() {
    return aliveProcess.get();
  }
  protected static boolean isTimeout(long start, long timeout) {
    return timeout > 0 && ((now() - start) >= timeout);
  }


  protected static long now() {
    return System.currentTimeMillis();
  }


  interface LockObserver {
    /**
     * 加锁中的处理
     *
     * @param lock 锁
     */
    void accept(Object lock) throws InterruptedException;
  }


  /**
   * 获取UUID
   */
  protected static String uuid() {
    return UUID.randomUUID().toString().replaceAll("-", "");
  }


  /**
   * The default thread factory
   */
  static class DefaultThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private boolean daemon = false;


    public DefaultThreadFactory(String prefix, String suffix, boolean daemon) {
      SecurityManager s = System.getSecurityManager();
      this.group = (s != null) ? s.getThreadGroup() :
          Thread.currentThread().getThreadGroup();
      this.namePrefix = prefix + poolNumber.getAndIncrement() + suffix;
      this.daemon = daemon;
    }

    public DefaultThreadFactory(ThreadGroup group, String prefix, String suffix) {
      this.group = group;
      this.namePrefix = prefix + poolNumber.getAndIncrement() + suffix;
    }

    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(group, r,
          namePrefix + threadNumber.getAndIncrement(), 0);
      t.setDaemon(daemon);
      if (t.getPriority() != Thread.NORM_PRIORITY) {
        t.setPriority(Thread.NORM_PRIORITY);
      }
      return t;
    }
  }

}
