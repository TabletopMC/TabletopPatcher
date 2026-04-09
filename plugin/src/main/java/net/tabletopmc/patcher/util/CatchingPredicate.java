package net.tabletopmc.patcher.util;

import java.util.function.Predicate;

@FunctionalInterface
public interface CatchingPredicate<T> extends Predicate<T> {
  static <T> Predicate<T> of(CatchingPredicate<T> predicate) {
    return predicate;
  }

  boolean testCatching(T t) throws Exception;

  @Override
  default boolean test(T t) {
    try {
      return testCatching(t);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
