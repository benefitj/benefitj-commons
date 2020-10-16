package com.benefitj.netty.handler;

import com.benefitj.netty.ByteBufCopy;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * 监听 Channel 的状态
 */
public class ActiveChangeChannelHandler extends ChannelDuplexHandler {

  private final ByteBufCopy bufCopy = new ByteBufCopy();

  private ActiveStateListener listener;

  public ActiveChangeChannelHandler(ActiveStateListener listener) {
    this.listener = listener;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);
    getListener().onChanged(ActiveState.ACTIVE, ctx, this);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    getListener().onChanged(ActiveState.INACTIVE, ctx, this);
  }

  public ActiveStateListener getListener() {
    return listener;
  }

  public void setListener(ActiveStateListener listener) {
    this.listener = listener;
  }

  public ByteBufCopy getBufCopy() {
    return bufCopy;
  }

  public interface ActiveStateListener {
    /**
     * 监听
     *
     * @param state   状态
     * @param ctx     上下文
     * @param handler 当前的Handler
     */
    void onChanged(ActiveState state, ChannelHandlerContext ctx, ActiveChangeChannelHandler handler);
  }


  /**
   * 创建 Handler
   *
   * @param listener 监听
   * @return 返回创建的Handler
   */
  public static ActiveChangeChannelHandler newHandler(ActiveStateListener listener) {
    return new ActiveChangeChannelHandler(listener);
  }
}