package net.tabletopmc.patcher.util;

import java.util.function.Consumer;

@FunctionalInterface
public interface CatchingConsumer<T> extends Consumer<T> {
  static <T> Consumer<T> of(CatchingConsumer<T> predicate) {
    return predicate;
  }

  void acceptCatching(T t) throws Exception;

  @Override
  default void accept(T t) {
    try {
      acceptCatching(t);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
