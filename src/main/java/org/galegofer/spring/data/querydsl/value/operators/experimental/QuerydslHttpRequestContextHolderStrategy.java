package org.galegofer.spring.data.querydsl.value.operators.experimental;

/**
 * Strategy interface for persistence and retrieval of
 * {@link QuerydslHttpRequestContext}. It is not recommended to use this
 * directly by users. Users are advised to use
 * {@link QuerydslHttpRequestContextHolder} for storage if needed as
 * {@link QuerydslHttpRequestContextHolder} is internally aware of which
 * strategy to use based on runtime configurations
 */
public interface QuerydslHttpRequestContextHolderStrategy {

    /**
     * Clears the current context.
     */
    void clearContext();

    /**
     * Obtains the current context.
     *
     * @return a context if available, <code>null</code> otherwise.
     */
    QuerydslHttpRequestContext getContext();

    /**
     * Sets the current context.
     *
     * @param context to the new argument (should never be <code>null</code>,
     *                although implementations must check if <code>null</code> has
     *                been passed and throw an <code>IllegalArgumentException</code>
     *                in such cases)
     */
    void setContext(QuerydslHttpRequestContext context);
}
