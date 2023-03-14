package org.galegofer.spring.data.querydsl.value.operators;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of {@link BaseExpressionProvider} for supporting {@link EnumPath}
 */
class EnumPathExpressionProviderImpl extends BaseExpressionProvider<EnumPath> {

    private static final LoadingCache<String, Pattern> REGEX_PATTERN_CACHE = CacheBuilder.newBuilder()
        .maximumSize(500)
        .build(
            new CacheLoader<>() {
                public Pattern load(String key) {
                    return Pattern.compile(key,
                        Pattern.CASE_INSENSITIVE);
                }
            });

    EnumPathExpressionProviderImpl() {
        super(ExpressionProviderFactory.isSupportsUnTypedValues()
            ? List.of(
            Operator.EQUAL,
            Operator.NOT_EQUAL,
            Operator.CONTAINS,
            Operator.STARTS_WITH,
            Operator.STARTSWITH,
            Operator.ENDS_WITH,
            Operator.ENDSWITH,
            Operator.NOT,
            Operator.MATCHES,
            Operator.CASE_IGNORE)
            : List.of(new Operator[]{
            Operator.EQUAL,
            Operator.NOT_EQUAL,
            Operator.NOT
        }));
    }

    @Override
    protected <S extends String> S getStringValue(EnumPath path, Object value) {
        return (Enum.class.isAssignableFrom(value.getClass()))
            ? (S) ((Enum) value).name()
            : (S) value.toString();
    }

    @Override
    protected BooleanExpression eq(EnumPath path, String value, boolean ignoreCase) {
        return path.eq(value);
    }

    @Override
    protected BooleanExpression ne(EnumPath path, String value, boolean ignoreCase) {
        return path.ne(value);
    }

    @Override
    protected BooleanExpression contains(EnumPath path, String value, boolean ignoreCase) {
        if (ExpressionProviderFactory.isSupportsUnTypedValues()) {
            return path.in(EnumUtils.getEnumList(path.getType())
                .stream()
                .filter(v -> ignoreCase
                    ? StringUtils.containsIgnoreCase(v.toString(), value)
                    : StringUtils.contains(v.toString(), value))
                .toList());
        }

        throw new UnsupportedOperationException(MessageFormat.format("Operator: {0} not supported with Enum values",
            Operator.CONTAINS));
    }

    @Override
    protected BooleanExpression startsWith(EnumPath path, String value, boolean ignoreCase) {
        if (ExpressionProviderFactory.isSupportsUnTypedValues()) {
            return path.in(EnumUtils.getEnumList(path.getType())
                .stream()
                .filter(v -> ignoreCase
                    ? StringUtils.startsWithIgnoreCase(v.toString(), value)
                    : StringUtils.startsWith(v.toString(), value))
                .toList());
        }

        throw new UnsupportedOperationException(MessageFormat.format("Operator: {0} not supported with Enum values",
            Operator.STARTS_WITH));
    }

    @Override
    protected BooleanExpression endsWith(EnumPath path, String value, boolean ignoreCase) {
        if (ExpressionProviderFactory.isSupportsUnTypedValues()) {
            return path.in(EnumUtils.getEnumList(path.getType())
                .stream()
                .filter(v -> ignoreCase
                    ? StringUtils.endsWithIgnoreCase(v.toString(), value)
                    : StringUtils.endsWith(v.toString(), value))
                .toList());
        }

        throw new UnsupportedOperationException(MessageFormat.format("Operator: {0} not supported with Enum values",
            Operator.ENDS_WITH));
    }

    @Override
    protected BooleanExpression matches(EnumPath path, String value) {
        if (ExpressionProviderFactory.isSupportsUnTypedValues()) {
            return path.in(EnumUtils.getEnumList(path.getType())
                .stream()
                .filter(v -> REGEX_PATTERN_CACHE.getUnchecked(value)
                    .matcher(v.toString())
                    .matches())
                .toList());
        }

        throw new UnsupportedOperationException(MessageFormat.format("Operator: {0} not supported with Enum values",
            Operator.MATCHES));
    }

    @Override
    protected BooleanExpression gt(EnumPath path, String value) {
        throw new UnsupportedOperationException("Enum value can't be searched using gt operator");
    }

    @Override
    protected BooleanExpression gte(EnumPath path, String value) {
        throw new UnsupportedOperationException("Enum value can't be searched using gte operator");
    }

    @Override
    protected BooleanExpression lt(EnumPath path, String value) {
        throw new UnsupportedOperationException("Enum value can't be searched using lt operator");
    }

    @Override
    protected BooleanExpression lte(EnumPath path, String value) {
        throw new UnsupportedOperationException("Enum value can't be searched using lte operator");
    }
}
