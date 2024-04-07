package edu.java.bot.request.chains;

public abstract class AbstractChains<R extends AbstractChains<R, T>, T> implements Chains<R, T> {
    protected final ChainFunction<T> function;

    public AbstractChains(ChainFunction<T> function) {
        this.function = function;
    }

    @Override
    public final T apply(T value) {
        return function.apply(value);
    }

    protected abstract R instance(ChainFunction<T> function);

    @Override
    public final R and(R other) {
        return instance(value -> other.apply(this.apply(value)));
    }
}
