package org.galegofer.spring.data.querydsl.value.operators;

import com.querydsl.core.support.NumberConversions;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;

/**
 * Implementation of {@link BaseExpressionProvider} for supporting
 * {@link NumberPath}
 */
class NumberPathExpressionProviderImpl extends BaseExpressionProvider<NumberPath> {

    public NumberPathExpressionProviderImpl() {
        super(List.of(Operator.EQUAL, Operator.NOT_EQUAL, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL,
            Operator.LESS_THAN, Operator.NOT, Operator.LESS_THAN_OR_EQUAL));
    }

    @Override
    protected <S extends String> S getStringValue(NumberPath path, Object value) {
        return (S) String.valueOf(value);
    }

    @Override
    protected BooleanExpression eq(NumberPath path, String value, boolean ignoreCase) {
        Validate.isTrue(isNumeric(value), "Invalid numeric value");

        return path.eq(new NumberConversions<>(Projections.tuple(path)).newInstance(
            NumberUtils.createNumber(StringUtils.trim(value))).get(path));
    }

    @Override
    protected BooleanExpression ne(NumberPath path, String value, boolean ignoreCase) {
        Validate.isTrue(isNumeric(value), "Invalid numeric value");

        return path.ne(new NumberConversions<>(Projections.tuple(path)).newInstance(
            NumberUtils.createNumber(StringUtils.trim(value))).get(path));
    }

    @Override
    protected BooleanExpression contains(NumberPath path, String value, boolean ignoreCase) {
        throw new UnsupportedOperationException("Number can't be searched using contains operator");
    }

    @Override
    protected BooleanExpression startsWith(NumberPath path, String value, boolean ignoreCase) {
        throw new UnsupportedOperationException("Number can't be searched using startsWith operator");
    }

    @Override
    protected BooleanExpression endsWith(NumberPath path, String value, boolean ignoreCase) {
        throw new UnsupportedOperationException("Number can't be searched using endsWith operator");
    }

    @Override
    protected BooleanExpression matches(NumberPath path, String value) {
        throw new UnsupportedOperationException("Number can't be searched using matches operator");
    }

    @Override
    protected BooleanExpression gt(NumberPath path, String value) {
        Validate.isTrue(isNumeric(value), "Invalid numeric value");

        return path.gt((Number) new NumberConversions<>(Projections.tuple(path)).newInstance(
            NumberUtils.createNumber(StringUtils.trim(value))).get(path));
    }

    @Override
    protected BooleanExpression gte(NumberPath path, String value) {
        Validate.isTrue(isNumeric(value), "Invalid numeric value");

        return path.goe((Number) new NumberConversions<>(Projections.tuple(path)).newInstance(
            NumberUtils.createNumber(StringUtils.trim(value))).get(path));
    }

    @Override
    protected BooleanExpression lt(NumberPath path, String value) {
        Validate.isTrue(isNumeric(value), "Invalid numeric value");

        return path.lt((Number) new NumberConversions<>(Projections.tuple(path)).newInstance(
            NumberUtils.createNumber(StringUtils.trim(value))).get(path));
    }

    @Override
    protected BooleanExpression lte(NumberPath path, String value) {
        Validate.isTrue(isNumeric(value), "Invalid numeric value");

        return path.loe((Number) new NumberConversions<>(Projections.tuple(path)).newInstance(
            NumberUtils.createNumber(StringUtils.trim(value))).get(path));
    }


    private boolean isNumeric(String inValue) {
        return NumberUtils.isParsable(StringUtils.trim(inValue));
    }
}
