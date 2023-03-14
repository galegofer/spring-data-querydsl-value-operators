package org.galegofer.spring.data.querydsl.value.operators;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Main entry point for library consumers. Factory class provides access to
 * appropriate {@link ExpressionProvider} based on provided {@link Path} type.
 */
public final class ExpressionProviderFactory {

    private static final LoadingCache<Path, ExpressionProvider> LOADING_CACHE = CacheBuilder.newBuilder()
            .build(new CacheLoader<>() {
                @Override
                public ExpressionProvider load(Path key) {
                    return switch (key.getClass().getSimpleName()) {
                        case "StringPath" -> new StringPathExpressionProviderImpl();
                        case "EnumPath" -> new EnumPathExpressionProviderImpl();
                        case "NumberPath" -> new NumberPathExpressionProviderImpl();
                        case "DateTimePath" -> new DateTimePathExpressionProviderImpl();
                        default -> null;
                    };
                }
            });

    private static boolean supportsUnTypedValues = false;

    private static final Map<Path, String> PATH_ALIAS_REGISTRY = new HashMap<>();

    /**
     * Create a predicate based on implementation specific logic's processing of
     * supplied value(s).
     *
     * @param path  <code>Path</code> path
     * @param value Input value(s) (Must not be {@link Optional}, can be a primitive or collection
     * @return {@link Optional} of {@link Predicate} based on provided value.
     */
    public static Optional<Predicate> getPredicate(Path path, Object value) {
        return Optional.ofNullable(LOADING_CACHE.getUnchecked(path))
                .flatMap(p -> p.getPredicate(path, value));
    }

    /**
     * Method registers the new alias for given Path. It is assumed that
     * {@link QuerydslBindings.PathBinder} available from {@link QuerydslBindings} is also
     * updated with new alias prior to registering here.
     *
     * @param path  {@link Path} on which alias is applied
     * @param alias String alias value for supplied path
     */
    public static void registerAlias(Path path, String alias) {
        if (path != null && StringUtils.isNotBlank(alias)) {
            PATH_ALIAS_REGISTRY.put(path, alias);
        }
    }

    /**
     * @param path Path for which alias to be looked up from local registry.
     * @return {@link Optional} of alias if available, otherwise empty
     * {@link Optional}
     */
    public static Optional<String> findAlias(Path path) {
        return Optional.ofNullable(path)
                .map(PATH_ALIAS_REGISTRY::get);
    }

    /**
     * @return <code>true</code> when experimental features are turned on, implying that untyped
     * values are going to be made available to {@link ExpressionProvider} for
     * non-string paths, <code>false</code> is returned if experimental features are disabled
     */
    public static boolean isSupportsUnTypedValues() {
        return supportsUnTypedValues;
    }

    /**
     * Sets whether experimental features are turned on, implying that untyped
     * values are going to be made available to {@link ExpressionProvider} for
     * non-string paths.
     *
     * @param supportsUnTypedValues <code>Boolean</code> indicating status of support of untyped values (aka. experimental features)
     */
    public static void setSupportsUnTypedValues(boolean supportsUnTypedValues) {
        ExpressionProviderFactory.supportsUnTypedValues = supportsUnTypedValues;
    }
}
