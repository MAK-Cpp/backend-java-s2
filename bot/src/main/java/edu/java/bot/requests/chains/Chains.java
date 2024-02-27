package edu.java.bot.requests.chains;

public interface Chains<R extends Chains<R, T>, T> {
    T apply(T value);

    R and(R other);

    @SafeVarargs
    static <R extends Chains<R, T>, T> R allOf(R... others) {
        R result = others[0];
        for (int i = 1; i < others.length; i++) {
            result = result.and(others[i]);
        }
        return result;
    }
}
