package org.galegofer.spring.data.querydsl.value.operators;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.galegofer.spring.data.querydsl.value.operators.experimental.QuerydslHttpRequestContextHolder;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.galegofer.spring.data.querydsl.value.operators.ExpressionProvider.validateComposition;


abstract class BaseExpressionProvider<P extends Path> implements ExpressionProvider<P, Object> {

    private final List<Operator> supportedSingleValuedComparisonOperators;

    public BaseExpressionProvider(List<Operator> supportedSingleValueComparisonOperators) {
        Validate.isTrue(!CollectionUtils.isEmpty(supportedSingleValueComparisonOperators),
                "Supported Single value" + " operators must be > 1");
        this.supportedSingleValuedComparisonOperators = supportedSingleValueComparisonOperators;
    }

    @Override
    public Optional<BooleanExpression> getExpression(P path, Object value) {
        return Optional.ofNullable(path)
                .map(p -> value)
                .map(v -> (Collection.class.isAssignableFrom(v.getClass()))
                        ? new MultiValueExpressionBuilder(path, (Collection) v).getExpression()
                        : new MultiValueExpressionBuilder(path, List.of(
                        getStringValue(path, v))).getExpression());
    }


    /**
     * Returns String value for provided object (value supplied by bindings
     * during bindings invocation phase)
     *
     * @param path  Specific type of {@link Path}
     * @param value Value as received from bindings invoker.
     * @return String value for the provided value object.
     */
    protected abstract <S extends String> S getStringValue(P path, Object value);

    /**
     * Creates a expression for equals clause - {@link Operator#EQUAL} operator
     *
     * @param path       Specific type of {@link Path}
     * @param value      String value to be used for making expression.
     * @param ignoreCase if comparison must be done ignoring case if case is applicable to target value type.
     * @return {@link BooleanExpression} to be used further by downstream query
     * serialization logic for executing actual query
     * @throws UnsupportedOperationException if implementation doesn't support this {@link Operator}
     */
    protected abstract BooleanExpression eq(P path, String value, boolean ignoreCase);

    /**
     * Creates a expression for not-equals clause - {@link Operator#NOT_EQUAL}
     * operator
     *
     * @param path       Specific type of {@link Path}
     * @param value      String value to be used for making expression.
     * @param ignoreCase if comparison must be done ignoring case if case is applicable to target value type.
     * @return {@link BooleanExpression} to be used further by downstream query
     * serialization logic for executing actual query
     * @throws UnsupportedOperationException if implementation doesn't support this {@link Operator}
     */
    protected abstract BooleanExpression ne(P path, String value, boolean ignoreCase);

    /**
     * Creates a expression for contains/like clause - {@link Operator#CONTAINS}
     * operator
     *
     * @param path       Specific type of {@link Path}
     * @param value      String value to be used for making expression
     * @param ignoreCase if comparison must be done ignoring case if case is applicable to target value type.             .
     * @return {@link BooleanExpression} to be used further by downstream query
     * serialization logic for executing actual query
     * @throws UnsupportedOperationException if implementation doesn't support this {@link Operator}
     */
    protected abstract BooleanExpression contains(P path, String value, boolean ignoreCase);

    /**
     * Creates a expression for startsWith clause - {@link Operator#STARTS_WITH}
     * operator
     *
     * @param path       Specific type of {@link Path}
     * @param value      String value to be used for making expression.
     * @param ignoreCase if comparison must be done ignoring case if case is applicable to target value type.
     * @return {@link BooleanExpression} to be used further by downstream query
     * serialization logic for executing actual query
     * @throws UnsupportedOperationException if implementation doesn't support this {@link Operator}
     */
    protected abstract BooleanExpression startsWith(P path, String value, boolean ignoreCase);

    /**
     * Creates a expression for endsWith clause - {@link Operator#ENDS_WITH}
     * operator
     *
     * @param path       Specific type of {@link Path}
     * @param value      String value to be used for making expression.
     * @param ignoreCase if comparison must be done ignoring case if case is applicable to target value type.
     * @return {@link BooleanExpression} to be used further by downstream query
     * serialization logic for executing actual query
     * @throws UnsupportedOperationException if implementation doesn't support this {@link Operator}
     */
    protected abstract BooleanExpression endsWith(P path, String value, boolean ignoreCase);

    /**
     * Creates a expression for matches clause - {@link Operator#MATCHES}
     * operator
     *
     * @param path  Specific type of {@link Path}
     * @param value String value to be used for making expression.
     * @return {@link BooleanExpression} to be used further by downstream query
     * serialization logic for executing actual query
     * @throws UnsupportedOperationException if implementation doesn't support this {@link Operator}
     */
    protected abstract BooleanExpression matches(P path, String value);

    /**
     * Creates a expression for greater-than clause -
     * {@link Operator#GREATER_THAN} operator
     *
     * @param path  Specific type of {@link Path}
     * @param value String value to be used for making expression.
     * @return {@link BooleanExpression} to be used further by downstream query
     * serialization logic for executing actual query
     * @throws UnsupportedOperationException if implementation doesn't support this {@link Operator}
     */
    protected abstract BooleanExpression gt(P path, String value);

    /**
     * Creates a expression for greater-than-equal clause -
     * {@link Operator#GREATER_THAN_OR_EQUAL} operator
     *
     * @param path  Specific type of {@link Path}
     * @param value String value to be used for making expression.
     * @return {@link BooleanExpression} to be used further by downstream query
     * serialization logic for executing actual query
     * @throws UnsupportedOperationException if implementation doesn't support this {@link Operator}
     */
    protected abstract BooleanExpression gte(P path, String value);

    /**
     * Creates a expression for less-than clause - {@link Operator#LESS_THAN}
     * operator
     *
     * @param path  Specific type of {@link Path}
     * @param value String value to be used for making expression.
     * @return {@link BooleanExpression} to be used further by downstream query
     * serialization logic for executing actual query
     * @throws UnsupportedOperationException if implementation doesn't support this {@link Operator}
     */
    protected abstract BooleanExpression lt(P path, String value);

    /**
     * Creates a expression for less-than or equal clause -
     * {@link Operator#LESS_THAN_OR_EQUAL} operator
     *
     * @param path  Specific type of {@link Path}
     * @param value String value to be used for making expression.
     * @return {@link BooleanExpression} to be used further by downstream query
     * serialization logic for executing actual query
     * @throws UnsupportedOperationException if implementation doesn't support this {@link Operator}
     */
    protected abstract BooleanExpression lte(P path, String value);

    /**
     * Applies a logical NOT (negate) to provided expression.
     *
     * @param expression
     * @return Negated expression that must be used further in
     * expression-building process
     */
    protected final BooleanExpression not(BooleanExpression expression) {
        Validate.notNull(expression);
        return expression.not();
    }

    /**
     * Applies logical AND clause to provided expressions.
     *
     * @param left  Left operand for AND operation
     * @param right Right operand for AND operation
     * @return expression with AND clause applied to provided two values, this
     * must be used further in expression-building process
     */
    protected final BooleanExpression and(BooleanExpression left, BooleanExpression right) {
        Validate.notNull(left);
        Validate.notNull(right);
        return left.and(right);
    }

    /**
     * Applies logical OR clause to provided expressions.
     *
     * @param left  Left operand for OR operation
     * @param right Right operand for OR operation
     * @return expression with AND clause applied to provided two values, this
     * must be used further in expression-building process
     */
    protected final BooleanExpression or(BooleanExpression left, BooleanExpression right) {
        Validate.notNull(left);
        Validate.notNull(right);
        return left.or(right);
    }

    /**
     * Utility class for building stateful expressions from provided values
     */
    private class MultiValueExpressionBuilder {

        private final P path;
        private final Collection<Object> values;
        private BooleanExpression expression;

        private static final Collection<Operator> MULTI_VALUE_LOGICAL_OPERATORS = List.of(Operator.AND, Operator.OR);

        public MultiValueExpressionBuilder(P path, Collection<Object> values) {
            this.path = path;
            this.values = values;
            this.values.forEach(v -> validateComposition(getStringValue(path, v)));
        }

        public BooleanExpression getExpression() {

            Operator defaultOperator = null;

            if (!CollectionUtils.isEmpty(this.values)) {
                if (this.values.size() == 1) {
                    var value = checkIfOriginalRequestValueAvailable(path,
                            getStringValue(path, this.values.iterator()
                                    .next()));
                    while (true) {
                        if (ExpressionProvider
                                .isOperator(MULTI_VALUE_LOGICAL_OPERATORS
                                        .toArray(new Operator[0]), value)
                                .isPresent()) {
                            value = new OperatorAndValue(value, MULTI_VALUE_LOGICAL_OPERATORS, Operator.OR).getValue();
                        }

                        break;
                    }

                    return new SingleValueExpressionBuilder(path, value)
                            .getExpression();
                } else {
                    for (Object o : checkIfOriginalRequestValuesAvailable(path, values).stream()
                            .filter(Objects::nonNull)
                            .toList()) {
                        final var value = getStringValue(this.path, o);
                        final var operatorAndValue = new OperatorAndValue(value, MULTI_VALUE_LOGICAL_OPERATORS,
                                defaultOperator != null ? defaultOperator
                                        : Operator.OR);
                        if (defaultOperator == null && !Operator.NOT.equals(operatorAndValue.getOperator()))
                            defaultOperator = operatorAndValue.getOperator();

                        final SingleValueExpressionBuilder singleValueExpressionBuilder = new SingleValueExpressionBuilder(path,
                                Operator.NOT.equals(operatorAndValue.getOperator())
                                        ? value
                                        : operatorAndValue.getValue());
                        final var current = singleValueExpressionBuilder.getExpression();

                        if (current == null) {
                            continue;
                        }

                        if (expression == null) {
                            expression = current;
                        } else {
                            // compose
                            switch (operatorAndValue.getOperator()) {
                                case AND -> expression = and(expression, current);
                                case OR, NOT -> expression = or(expression, current);
                                default -> {
                                    final var msg = MessageFormat.format(
                                            "Illegal operator: {0}, Search Parameter: " + "{1}, Value: {2}",
                                            operatorAndValue.getOperator()
                                                    .toString(),
                                            path.toString(),
                                            value);
                                    throw new IllegalArgumentException(msg);
                                }
                            }
                        }
                    }
                }
            }
            return expression;
        }
    }

    private class SingleValueExpressionBuilder {
        private final P path;
        private final String value;
        private final Operator operator;
        private SingleValueExpressionBuilder parent;
        private SingleValueExpressionBuilder next;
        private boolean ignoreCase = false;

        public SingleValueExpressionBuilder(P path, String value) {
            this.path = path;

            final var operatorAndValue = new OperatorAndValue(value, supportedSingleValuedComparisonOperators,
                    Operator.EQUAL);
            this.operator = operatorAndValue.getOperator();
            this.value = operatorAndValue.getValue();

            init(path, operatorAndValue);
        }

        private SingleValueExpressionBuilder(final P path, String value, final SingleValueExpressionBuilder parent) {
            this.parent = parent;

            this.path = path;

            final var operatorAndValue = new OperatorAndValue(value, supportedSingleValuedComparisonOperators,
                    Operator.EQUAL);
            this.operator = operatorAndValue.getOperator();
            this.value = operatorAndValue.getValue();

            init(path, operatorAndValue);
        }

        /**
         * @return if case should be ignored.
         */
        public boolean isIgnoreCase() {
            return ignoreCase;
        }

        /**
         * @param ignoreCase set <code>true</code> if case sensitivity should be ignored.
         */
        public void setIgnoreCase(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
        }

        private void init(P path, OperatorAndValue operatorAndValue) {
            Validate.notNull(this.operator, "Operator must not be null");

            if (Operator.NOT.equals(this.operator)) {
                Validate.isTrue(StringUtils.isNotBlank(this.value),
                        "Sub-operation must be available with NOT operator");
                this.next = new SingleValueExpressionBuilder(path, operatorAndValue.getValue(), this);
            } else if (Operator.CASE_IGNORE.equals(this.operator)) {
                Validate.isTrue(StringUtils.isNotBlank(this.value),
                        "Sub-operation must be available with CASE_IGNORE operator");
                this.next = new SingleValueExpressionBuilder(path, operatorAndValue.getValue(), this);
                this.next.setIgnoreCase(true);
            } else if (ExpressionProvider.isOperator(supportedSingleValuedComparisonOperators.toArray(
                            new Operator[0]), this.value)
                    .isPresent()) {
                this.next = new SingleValueExpressionBuilder(path, operatorAndValue.getValue(), this);
            }

            if (this.parent != null) {
                Validate.isTrue(
                        !(Operator.AND.equals(this.operator) || Operator.OR.equals(this.operator)
                                || (Operator.NOT.equals(this.operator) && !Operator.NOT.equals(this.parent.operator))),
                        "Boolean operators cannot be composed within other operators"); // last
            }
        }

        public BooleanExpression getExpression() {
            return switch (this.operator) {
                case CASE_IGNORE -> this.next.getExpression();
                case EQUAL -> eq(path, this.value, this.isIgnoreCase());
                case NOT_EQUAL -> ne(path, this.value, this.isIgnoreCase());
                case CONTAINS -> contains(path, this.value, this.isIgnoreCase());
                case STARTS_WITH, STARTSWITH -> startsWith(path, this.value, this.isIgnoreCase());
                case ENDS_WITH, ENDSWITH -> endsWith(path, this.value, this.isIgnoreCase());
                case MATCHES -> matches(path, this.value);
                case NOT -> {
                    final var preResult = this.next.getExpression();

                    yield preResult != null
                            ? preResult.not()
                            : null;
                }
                case LESS_THAN -> lt(path, this.value);
                case LESS_THAN_OR_EQUAL -> lte(path, this.value);
                case GREATER_THAN -> gt(path, this.value);
                case GREATER_THAN_OR_EQUAL -> gte(path, this.value);
                default -> null;
            };
        }
    }

    private String checkIfOriginalRequestValueAvailable(Path path, String defaultValue) {
        final var ctx = QuerydslHttpRequestContextHolder.getContext();
        String result = null;

        if (ctx != null) {
            result = ctx.getSingleValue(path);
        }

        if (StringUtils.isBlank(result)) {
            result = defaultValue;
        }
        return result;
    }

    private Collection<Object> checkIfOriginalRequestValuesAvailable(Path path, Collection<Object> defaultValues) {
        final var ctx = QuerydslHttpRequestContextHolder.getContext();

        final var result = ctx != null
                ? Arrays.stream(Optional.ofNullable(ctx.getAllValues(path))
                        .orElseGet(() -> new String[]{}))
                .map(val -> (Object) val)
                .collect(Collectors.toCollection(LinkedList::new))
                : null;

        return !CollectionUtils.isEmpty(result)
                ? result
                : defaultValues;
    }
}
