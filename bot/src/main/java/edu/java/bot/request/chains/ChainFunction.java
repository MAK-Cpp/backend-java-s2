package edu.java.bot.request.chains;

@FunctionalInterface
public interface ChainFunction<T> {
    T apply(T value);
}
