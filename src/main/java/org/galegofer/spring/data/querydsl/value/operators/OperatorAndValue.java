package org.galegofer.spring.data.querydsl.value.operators;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.util.Pair;
import org.springframework.util.CollectionUtils;

import java.util.Collection;

public class OperatorAndValue {

    private final Operator operator;
    private final String value;

    /**
     * Constructor
     * <p>
     * Leans towards selecting picking case-insensitive Operation.
     *
     * @param input           Input String (search parameter's, usually an attribute, value)
     * @param inOperators     Input list of operators to be used to check for operator
     * @param defaultOperator Default Operator if the supplied input string doesn't have any
     *                        operator
     */
    public OperatorAndValue(final String input, final Collection<Operator> inOperators,
                            final Operator defaultOperator) {
        Validate.isTrue(StringUtils.isNotBlank(input), "Input string cannot be blank");
        Validate.isTrue(!CollectionUtils.isEmpty(inOperators), "Input operators must not be empty");

        final var operatorValue = inOperators.stream()
            .filter(operator -> ExpressionProvider.isOperator(operator, input))
            .findFirst()
            .map(operator -> Pair.of(operator,
                StringUtils.substringBeforeLast(
                    StringUtils.substringAfter(input,
                        operator + ExpressionProvider.OPERATOR_VALUE_DELIMITER_PREFIX),
                    ExpressionProvider.OPERATOR_VALUE_DELIMITER_SUFFIX)))
            .orElseGet(() -> Pair.of(defaultOperator, StringUtils.trim(input)));

        this.operator = operatorValue.getFirst();
        this.value = operatorValue.getSecond();
    }

    /**
     * @return Operator extracted from supplied value or else provided default operator
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * @return the String value from supplied value after stripping first operator if it was an operator wrapped value
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("operator", operator)
            .append("value", value)
            .toString();
    }
}
