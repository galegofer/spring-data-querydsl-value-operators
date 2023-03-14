package org.galegofer.spring.data.querydsl.value.operators;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import org.apache.commons.lang3.Validate;

import java.util.Date;
import java.util.List;

class DateTimePathExpressionProviderImpl extends BaseExpressionProvider<DateTimePath> {

    public DateTimePathExpressionProviderImpl() {
        super(List.of(Operator.EQUAL, Operator.NOT_EQUAL, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL,
                Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL));
    }

    @Override
    protected String getStringValue(DateTimePath path, Object value) {
        return value.toString();
    }

    @Override
    protected BooleanExpression eq(DateTimePath path, String value, boolean ignoreCase) {
        Validate.isTrue(isDate(value), "Invalid date value");
        return path.eq(convertToDate(value));
    }

    @Override
    protected BooleanExpression ne(DateTimePath path, String value, boolean ignoreCase) {
        Validate.isTrue(isDate(value), "Invalid date value");
        return path.ne(convertToDate(value));
    }

    @Override
    protected BooleanExpression contains(DateTimePath path, String value, boolean ignoreCase) {
        throw new UnsupportedOperationException("Datetime can't be searched using contains operator");
    }

    @Override
    protected BooleanExpression startsWith(DateTimePath path, String value, boolean ignoreCase) {
        throw new UnsupportedOperationException("Datetime can't be searched using startsWith operator");
    }

    @Override
    protected BooleanExpression endsWith(DateTimePath path, String value, boolean ignoreCase) {
        throw new UnsupportedOperationException("Datetime can't be searched using endsWith operator");
    }

    @Override
    protected BooleanExpression matches(DateTimePath path, String value) {
        throw new UnsupportedOperationException("Datetime can't be searched using matches operator");
    }

    @Override
    protected BooleanExpression gt(DateTimePath path, String value) {
        Validate.isTrue(isDate(value), "Invalid date value");
        return path.gt(convertToDate(value));
    }

    @Override
    protected BooleanExpression gte(DateTimePath path, String value) {
        Validate.isTrue(isDate(value), "Invalid date value");
        return path.goe(convertToDate(value));
    }

    @Override
    protected BooleanExpression lt(DateTimePath path, String value) {
        Validate.isTrue(isDate(value), "Invalid date value");
        return path.lt(convertToDate(value));
    }

    @Override
    protected BooleanExpression lte(DateTimePath path, String value) {
        Validate.isTrue(isDate(value), "Invalid date value");
        return path.loe(convertToDate(value));
    }

    private boolean isDate(String dateString) {
        try {
            new Date(dateString);

            return true;
        } catch (IllegalArgumentException iae) {
            return false;
        }
    }

    private Date convertToDate(String dateString) {
        // use the same conversion as used by the Conversion service
        return new Date(dateString);
    }
}
