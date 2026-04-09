package net.tabletopmc.patcher.util;

import java.util.function.Function;

@FunctionalInterface
public interface CatchingFunction<T, R> extends Function<T, R> {
  static <T, R> Function<T, R> of(CatchingFunction<T, R> predicate) {
    return predicate;
  }

  R applyCatching(T t) throws Exception;

  @Override
  default R apply(T t) {
    try {
      return applyCatching(t);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
