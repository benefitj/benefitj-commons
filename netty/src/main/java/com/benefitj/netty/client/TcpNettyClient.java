package com.benefitj.netty.client;

import com.benefitj.netty.DefaultThreadFactory;
import com.benefitj.netty.handler.ActiveChannelHandler;
import com.benefitj.netty.handler.ActiveState;
import com.benefitj.netty.handler.ChannelShutdownEventHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TCP客户端
 */
public class TcpNettyClient extends AbstractNettyClient<TcpNettyClient> {

  /**
   * 是否自动重连
   */
  private boolean autoReconnect = false;
  /**
   * 重连间隔，默认10秒
   */
  private int reconnectPeriod = 10;
  /**
   * 重连间隔的时间单位，默认是秒
   */
  private TimeUnit reconnectPeriodUnit = TimeUnit.SECONDS;
  /**
   * handler
   */
  private ChannelHandler handler;
  /**
   * 线程调度
   */
  private ScheduledExecutorService executor;

  public TcpNettyClient() {
  }

  @Override
  public TcpNettyClient self() {
    return this;
  }

  @Override
  public TcpNettyClient useDefaultConfig() {
    if (useLinuxNativeEpoll()) {
      this.group(new EpollEventLoopGroup());
      this.channel(EpollSocketChannel.class);
    } else {
      this.executeWhileNull(group(), () -> group(new NioEventLoopGroup()));
      this.executeWhileNull(channelFactory(), () -> channel(NioSocketChannel.class));
    }

    Map<ChannelOption<?>, Object> options = new HashMap<>(16);
    options.putAll(options());
    options.put(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    // 默认4MB，数据量较大，缓冲区较小会导致丢包
    options.putIfAbsent(ChannelOption.SO_RCVBUF, (1024 << 10) * 4);
    options.putIfAbsent(ChannelOption.SO_SNDBUF, (1024 << 10) * 4);
    options.put(ChannelOption.TCP_NODELAY, true);
    options.put(ChannelOption.SO_KEEPALIVE, true);
    options.put(ChannelOption.AUTO_READ, true);
    options.put(ChannelOption.AUTO_CLOSE, true);
    options.put(ChannelOption.ALLOW_HALF_CLOSURE, true);
    options.put(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000);
    this.options(options);

    if (autoReconnect()) {
      this.executeWhileNull(super.handler(), () ->
          super.handler(new WatchdogChannelInitializer(this)));
    } else {
      super.handler(handler);
    }

    return self();
  }

  @Override
  public ChannelHandler handler() {
    return handler;
  }

  @Override
  public TcpNettyClient handler(ChannelHandler handler) {
    this.handler = handler;
    return self();
  }

  @Override
  protected ChannelFuture startOnly(Bootstrap bootstrap, GenericFutureListener<? extends Future<Void>>... listeners) {
    return bootstrap.connect().addListeners(listeners);
  }

  @Override
  public TcpNettyClient stop(GenericFutureListener<? extends Future<Void>>... listeners) {
    return super.stop(listeners);
  }

  /**
   * 设置自动重连
   *
   * @param autoReconnect  是否自动重连
   * @param reconnectDelay 重连的间隔
   * @param unit           间隔的时间单位
   * @return 返回TCP客户端
   */
  public TcpNettyClient autoReconnect(boolean autoReconnect, int reconnectDelay, TimeUnit unit) {
    this.autoReconnect(autoReconnect);
    this.reconnectPeriod(reconnectDelay);
    this.reconnectPeriodUnit(unit);
    return self();
  }

  public boolean autoReconnect() {
    return autoReconnect;
  }

  public TcpNettyClient autoReconnect(boolean autoReconnect) {
    this.autoReconnect = autoReconnect;
    return self();
  }

  public int reconnectPeriod() {
    return reconnectPeriod;
  }

  public TcpNettyClient reconnectPeriod(int period) {
    this.reconnectPeriod = period;
    return self();
  }

  public TimeUnit reconnectPeriodUnit() {
    return reconnectPeriodUnit;
  }

  public TcpNettyClient reconnectPeriodUnit(TimeUnit periodUnit) {
    this.reconnectPeriodUnit = periodUnit;
    return self();
  }

  public ScheduledExecutorService executor() {
    return executor;
  }

  public TcpNettyClient executor(ScheduledExecutorService executor) {
    this.executor = executor;
    return self();
  }

  /**
   * 自动重连的初始化程序
   */
  @Slf4j
  static class WatchdogChannelInitializer extends ChannelInitializer<Channel>
      implements ActiveChannelHandler.ActiveStateListener {

    /**
     * Netty TCP 客户端
     */
    private final TcpNettyClient client;
    private final int period;
    private final TimeUnit periodUnit;
    /**
     * 重连的线程
     */
    private ScheduledExecutorService executor;
    /**
     * 定时器，每隔固定时间检查客户端状态
     */
    private ScheduledFuture<?> timer;

    public WatchdogChannelInitializer(TcpNettyClient client) {
      this.client = client;
      this.period = client.reconnectPeriod();
      this.periodUnit = client.reconnectPeriodUnit();

      ScheduledExecutorService e = client.executor();
      if (e != null) {
        this.executor = e;
      } else {
        ThreadFactory factory = new DefaultThreadFactory("tcp-", "-reconnect-", true);
        this.executor = Executors.newSingleThreadScheduledExecutor(factory);
      }
      // 注册启动时的监听
      client.addStartListeners(f -> startReconnectSchedule());
      // 注册停止时的监听
      client.addStopListeners(f -> stopReconnectSchedule());
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
      ch.pipeline()
          .addLast(ChannelShutdownEventHandler.INSTANCE)
          .addLast(ActiveChannelHandler.newHandler(this))
          .addLast(client.handler);
    }

    /**
     * 开始重连
     */
    void startReconnectSchedule() {
      synchronized (this) {
        ScheduledFuture<?> t = this.timer;
        if (t == null || t.isCancelled()) {
          // 开始调度
          this.timer = executor.scheduleAtFixedRate(this::startReconnectNow, period, period, periodUnit);
        }
      }
    }

    /**
     * 停止重连
     */
    void stopReconnectSchedule() {
      synchronized (this) {
        ScheduledFuture<?> t = this.timer;
        if (t != null) {
          t.cancel(true);
          this.timer = null;
        }
        this.executor.shutdownNow();
      }
    }

    /**
     * 开始重连任务
     */
    void startReconnectNow() {
      synchronized (this) {
        TcpNettyClient tcp = this.client;
        if (tcp.isConnected()) {
          return;
        }
        AtomicReference<Thread.State> holder = tcp.stateHolder();
        if (holder.get() != Thread.State.NEW) {
          holder.set(Thread.State.NEW);
        }
        tcp.start();
      }
    }

    @Override
    public void onChanged(ActiveChannelHandler handler, ChannelHandlerContext ctx, ActiveState state) {
      // 立刻重新尝试开启一个新的连接
      if (state == ActiveState.INACTIVE) {
        ScheduledExecutorService executor = this.executor;
        if (!executor.isShutdown()) {
          executor.schedule(this::startReconnectNow, 1, TimeUnit.MILLISECONDS);
        }
      } else {
        ScheduledFuture<?> t = this.timer;
        if (t != null) {
          t.cancel(true);
          this.timer = null;
        }
      }
    }
  }


}
