package com.benefitj.netty.client;

import com.benefitj.netty.AbstractNetty;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.BootstrapConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.SocketAddress;

/**
 * Netty的客戶端
 */
public abstract class AbstractNettyClient<S extends AbstractNettyClient<S>> extends AbstractNetty<Bootstrap, S> {

  public AbstractNettyClient() {
  }

  @Override
  public abstract S useDefaultConfig();

  /**
   * 创建 Bootstrap 实例
   */
  @Override
  public Bootstrap createBootstrap() {
    return new Bootstrap();
  }

  @Override
  public BootstrapConfig config() {
    return bootstrap().config();
  }

  /**
   * 连接或绑定本地端口
   *
   * @param bootstrap 启动器
   * @return 返回ChannelFuture
   */
  @Override
  public final ChannelFuture startOnly(Bootstrap bootstrap) {
    return startOnly(bootstrap, f -> {
      SocketAddress localAddress = config().localAddress();
      SocketAddress remoteAddress = config().remoteAddress();
      if (f.isSuccess()) {
        log.info("Netty client start at localAddress: " + localAddress + ", remoteAddress: " + remoteAddress);
      } else {
        setServeChannel(null);
        log.info("Netty client start failed at localAddress: " + localAddress + ", remoteAddress: " + remoteAddress);
      }
    });
  }

  /**
   * 连接或绑定本地端口
   *
   * @param bootstrap 启动器
   * @return 返回ChannelFuture
   */
  protected abstract ChannelFuture startOnly(Bootstrap bootstrap, GenericFutureListener<? extends Future<Void>>... listeners);

  @Override
  public S stop(GenericFutureListener<? extends Future<Void>>... listeners) {
    GenericFutureListener<? extends Future<Void>> l = f ->
        log.info("Netty client stop at localAddress: " + config().localAddress()
            + ", remoteAddress: " + config().remoteAddress());
    return super.stop(copyListeners(l, listeners));
  }

  /**
   * 是否已连接
   *
   * @return 如果已连接返回 true，否则返回 false
   */
  public boolean isConnected() {
    final Channel channel = getServeChannel();
    return channel != null && channel.isActive();
  }
}