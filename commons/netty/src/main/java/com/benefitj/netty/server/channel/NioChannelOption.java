package com.benefitj.netty.server.channel;

import io.netty.channel.ChannelException;
import io.netty.channel.ChannelOption;
import io.netty.util.internal.SuppressJava6Requirement;

import java.io.IOException;
import java.net.SocketOption;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Provides {@link ChannelOption} over a given {@link java.net.SocketOption} which is then passed through the underlying
 * {@link java.nio.channels.NetworkChannel}.
 */
final class NioChannelOption<T> extends ChannelOption<T> {

  private final java.net.SocketOption<T> option;

  @SuppressWarnings("deprecation")
  private NioChannelOption(java.net.SocketOption<T> option) {
    super(option.name());
    this.option = option;
  }

  /**
   * Returns a {@link ChannelOption} for the given {@link java.net.SocketOption}.
   */
  public static <T> ChannelOption<T> of(java.net.SocketOption<T> option) {
    return new NioChannelOption<T>(option);
  }

  // It's important to not use java.nio.channels.NetworkChannel as otherwise the classes that sometimes call this
  // method may not be used on Java 6, as method linking can happen eagerly even if this method was not actually
  // called at runtime.
  //
  // See https://github.com/netty/netty/issues/8166

  // Internal helper methods to remove code duplication between Nio*Channel implementations.
  @SuppressJava6Requirement(reason = "Usage guarded by java version check")
  static <T> boolean setOption(java.nio.channels.Channel jdkChannel, NioChannelOption<T> option, T value) {
    java.nio.channels.NetworkChannel channel = (java.nio.channels.NetworkChannel) jdkChannel;
    if (!channel.supportedOptions().contains(option.option)) {
      return false;
    }
    if (channel instanceof ServerSocketChannel && option.option == java.net.StandardSocketOptions.IP_TOS) {
      // Skip IP_TOS as a workaround for a JDK bug:
      // See http://mail.openjdk.java.net/pipermail/nio-dev/2018-August/005365.html
      return false;
    }
    try {
      channel.setOption(option.option, value);
      return true;
    } catch (IOException e) {
      throw new ChannelException(e);
    }
  }

  @SuppressJava6Requirement(reason = "Usage guarded by java version check")
  static <T> T getOption(java.nio.channels.Channel jdkChannel, NioChannelOption<T> option) {
    java.nio.channels.NetworkChannel channel = (java.nio.channels.NetworkChannel) jdkChannel;

    if (!channel.supportedOptions().contains(option.option)) {
      return null;
    }
    if (channel instanceof ServerSocketChannel && option.option == java.net.StandardSocketOptions.IP_TOS) {
      // Skip IP_TOS as a workaround for a JDK bug:
      // See http://mail.openjdk.java.net/pipermail/nio-dev/2018-August/005365.html
      return null;
    }
    try {
      return channel.getOption(option.option);
    } catch (IOException e) {
      throw new ChannelException(e);
    }
  }

  @SuppressJava6Requirement(reason = "Usage guarded by java version check")
  @SuppressWarnings("unchecked")
  static ChannelOption[] getOptions(java.nio.channels.Channel jdkChannel) {
    java.nio.channels.NetworkChannel channel = (java.nio.channels.NetworkChannel) jdkChannel;
    Set<SocketOption<?>> supportedOpts = channel.supportedOptions();

    if (channel instanceof ServerSocketChannel) {
      List<ChannelOption<?>> extraOpts = new ArrayList<ChannelOption<?>>(supportedOpts.size());
      for (java.net.SocketOption<?> opt : supportedOpts) {
        if (opt == java.net.StandardSocketOptions.IP_TOS) {
          // Skip IP_TOS as a workaround for a JDK bug:
          // See http://mail.openjdk.java.net/pipermail/nio-dev/2018-August/005365.html
          continue;
        }
        extraOpts.add(new NioChannelOption(opt));
      }
      return extraOpts.toArray(new ChannelOption[0]);
    } else {
      ChannelOption<?>[] extraOpts = new ChannelOption[supportedOpts.size()];

      int i = 0;
      for (java.net.SocketOption<?> opt : supportedOpts) {
        extraOpts[i++] = new NioChannelOption(opt);
      }
      return extraOpts;
    }
  }
}
