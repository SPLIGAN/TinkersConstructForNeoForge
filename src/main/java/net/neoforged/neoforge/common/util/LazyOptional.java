package net.neoforged.neoforge.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Minimal legacy-compatible optional wrapper used by older Forge-style capability code.
 */
public class LazyOptional<T> {
  private Supplier<T> supplier;
  private T value;
  private boolean resolved;
  private final List<Consumer<LazyOptional<T>>> listeners = new ArrayList<>();

  private LazyOptional(Supplier<T> supplier) {
    this.supplier = supplier;
  }

  public static <T> LazyOptional<T> of(Supplier<T> supplier) {
    return new LazyOptional<>(supplier);
  }

  public static <T> LazyOptional<T> empty() {
    return new LazyOptional<>(() -> null);
  }

  private T resolveValue() {
    if (!resolved) {
      value = supplier == null ? null : supplier.get();
      resolved = true;
    }
    return value;
  }

  public boolean isPresent() {
    return resolveValue() != null;
  }

  public Optional<T> resolve() {
    return Optional.ofNullable(resolveValue());
  }

  public T orElse(T other) {
    T current = resolveValue();
    return current != null ? current : other;
  }

  public T orElseGet(Supplier<? extends T> other) {
    T current = resolveValue();
    return current != null ? current : other.get();
  }

  public void ifPresent(Consumer<? super T> consumer) {
    T current = resolveValue();
    if (current != null) {
      consumer.accept(current);
    }
  }

  public LazyOptional<T> filter(Predicate<? super T> predicate) {
    T current = resolveValue();
    if (current != null && predicate.test(current)) {
      return this;
    }
    return empty();
  }

  @SuppressWarnings("unchecked")
  public <C> LazyOptional<C> cast() {
    return (LazyOptional<C>) this;
  }

  public void addListener(Consumer<LazyOptional<T>> listener) {
    listeners.add(listener);
  }

  /** Legacy Forge hook; NeoForge block caps invalidate via level notifications instead. */
  public void removeListener(Consumer<LazyOptional<T>> listener) {
    listeners.remove(listener);
  }

  public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
    return resolve().map(mapper);
  }

  public void invalidate() {
    supplier = null;
    value = null;
    resolved = true;
    for (Consumer<LazyOptional<T>> listener : listeners) {
      listener.accept(this);
    }
    listeners.clear();
  }
}
