package edu.java.bot.requests.chains;

@FunctionalInterface
public interface ChainFunction<T> {
    T apply(T value);
}
