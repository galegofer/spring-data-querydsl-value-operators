package org.galegofer.spring.data.querydsl.value.operators.experimental;

import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public final class QuerydslHttpRequestContextHolder {

    /**
     * {@link ThreadLocal} based QuerydslHttpRequestContextHolderStrategy to be
     * used.
     */
    public static final String MODE_THREADLOCAL = "MODE_THREADLOCAL";

    /**
     * {@link InheritableThreadLocal} based
     * QuerydslHttpRequestContextHolderStrategy to be used.
     */
    public static final String MODE_INHERITABLETHREADLOCAL = "MODE_INHERITABLETHREADLOCAL";

    public static final String SYSTEM_PROPERTY = "querydsl.experimental.operator.web.context.strategy";
    private static String strategyName = System.getProperty(SYSTEM_PROPERTY);
    private static QuerydslHttpRequestContextHolderStrategy strategy;

    static {
        initialize();
    }

    /**
     * Explicitly clears the context value from the current thread.
     */
    public static void clearContext() {
        strategy.clearContext();
    }

    /**
     * Obtain the current <code>QuerydslHttpRequestContext</code>.
     *
     * @return the context if available, <code>null</code> otherwise
     */
    public static QuerydslHttpRequestContext getContext() {
        return strategy.getContext();
    }

    private static void initialize() {
        if (!StringUtils.hasText(strategyName)) {
            // Set default
            strategyName = MODE_THREADLOCAL;
        }

        strategy = selectStrategy(strategyName);
    }

    private static QuerydslHttpRequestContextHolderStrategy selectStrategy(String strategyName) {
        return switch (strategyName) {
            case MODE_THREADLOCAL -> new ThreadLocalQuerydslHttpRequestContextHolderStrategy(false);
            case MODE_INHERITABLETHREADLOCAL -> new ThreadLocalQuerydslHttpRequestContextHolderStrategy(true);
            default -> {
                try {
                    final var clazz = Class.forName(strategyName);
                    final var customStrategy = clazz.getConstructor();
                    yield (QuerydslHttpRequestContextHolderStrategy) customStrategy.newInstance();
                } catch (Exception ex) {
                    ReflectionUtils.handleReflectionException(ex);
                    yield null;
                }
            }
        };
    }

    /**
     * Associates a new <code>QuerydslHttpRequestContext</code> with the holder
     * strategy
     *
     * @param context the new <code>QuerydslHttpRequestContext</code> (may not be
     *                <code>null</code>)
     */
    public static void setContext(QuerydslHttpRequestContext context) {
        strategy.setContext(context);
    }
}
