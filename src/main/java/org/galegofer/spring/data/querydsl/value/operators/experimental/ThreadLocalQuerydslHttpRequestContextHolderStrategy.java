package org.galegofer.spring.data.querydsl.value.operators.experimental;

import org.apache.commons.lang3.Validate;

public class ThreadLocalQuerydslHttpRequestContextHolderStrategy implements QuerydslHttpRequestContextHolderStrategy {

    private final ThreadLocal<QuerydslHttpRequestContext> holder;

    public ThreadLocalQuerydslHttpRequestContextHolderStrategy(boolean inheritable) {
        this.holder = inheritable
            ? new InheritableThreadLocal()
            : new ThreadLocal();
    }

    @Override
    public void clearContext() {
        this.holder.remove();
    }

    @Override
    public QuerydslHttpRequestContext getContext() {
        return this.holder.get();
    }

    @Override
    public void setContext(QuerydslHttpRequestContext context) {
        Validate.notNull(context, "Supplied context is null");
        this.holder.set(context);
    }
}
