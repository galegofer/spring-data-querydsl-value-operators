package org.galegofer.spring.data.querydsl.value.operators;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.regex.Pattern;

class StringPathExpressionProviderImpl extends BaseExpressionProvider<StringPath> {

    private static final Pattern WHITE_LISTED = Pattern.compile("[A-Za-z0-9-_@:]{1,64}");

    public StringPathExpressionProviderImpl() {
        super(List.of(Operator.EQUAL, Operator.NOT_EQUAL, Operator.CONTAINS, Operator.STARTS_WITH, Operator.STARTSWITH,
            Operator.ENDS_WITH, Operator.ENDSWITH, Operator.NOT, Operator.MATCHES, Operator.CASE_IGNORE));
    }

    @Override
    protected <S extends String> S getStringValue(StringPath path, Object value) {
        return (S) value.toString();
    }

    @Override
    protected BooleanExpression eq(StringPath path, String value, boolean ignoreCase) {
        Validate.isTrue(isValidString(value), "Invalid string value");

        return ignoreCase
            ? path.equalsIgnoreCase(value)
            : path.eq(value);
    }

    @Override
    protected BooleanExpression ne(StringPath path, String value, boolean ignoreCase) {
        Validate.isTrue(isValidString(value), "Invalid string value");

        return ignoreCase
            ? path.notEqualsIgnoreCase(value)
            : path.ne(value);
    }

    @Override
    protected BooleanExpression contains(StringPath path, String value, boolean ignoreCase) {
        Validate.isTrue(isValidString(value), "Invalid string value");

        return ignoreCase
            ? path.containsIgnoreCase(value)
            : path.contains(value);
    }

    @Override
    protected BooleanExpression startsWith(StringPath path, String value, boolean ignoreCase) {
        Validate.isTrue(isValidString(value), "Invalid string value");

        return ignoreCase
            ? path.startsWithIgnoreCase(value)
            : path.startsWith(value);
    }

    @Override
    protected BooleanExpression endsWith(StringPath path, String value, boolean ignoreCase) {
        Validate.isTrue(isValidString(value), "Invalid string value");

        return ignoreCase
            ? path.endsWithIgnoreCase(value)
            : path.endsWith(value);
    }

    @Override
    protected BooleanExpression matches(StringPath path, String value) {
        Validate.isTrue(isValidString(value), "Invalid string value");

        return path.matches(value);
    }

    @Override
    protected BooleanExpression gt(StringPath path, String value) {
        throw new UnsupportedOperationException("String value can't be searched using gt operator");
    }

    @Override
    protected BooleanExpression gte(StringPath path, String value) {
        throw new UnsupportedOperationException("String value can't be searched using gte operator");
    }

    @Override
    protected BooleanExpression lt(StringPath path, String value) {
        throw new UnsupportedOperationException("String value can't be searched using lt operator");
    }

    @Override
    protected BooleanExpression lte(StringPath path, String value) {
        throw new UnsupportedOperationException("String value can't be searched using lte operator");
    }

    private boolean isValidString(String value) {
        return WHITE_LISTED.matcher(value).matches();
    }
}
