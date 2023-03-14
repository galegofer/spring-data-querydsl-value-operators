package org.galegofer.spring.data.querydsl.value.operators;

/**
 * Defines the value-operators supported by this component.
 */
public enum Operator {

    /**
     * Provides a operator value delimiter prefix for when explicit delimiter is provided.
     * for e.g. operator(value)
     */
    EQUAL("eq"),
    /**
     * Provides a operator value delimiter suffix for when explicit delimiter is provided.
     * for e.g. operator(value)
     */
    NOT_EQUAL("ne"),
    /**
     * The operator for contains clause.
     * This operator should perform case insensitive searches when possible.
     */
    CONTAINS("contains"),
    /**
     * The operator for starts-with clause.
     * This operator should perform case insensitive searches when possible.
     */
    STARTS_WITH("startsWith"),
    /**
     * For Kebab case support "starts-with", behavior is same as {@link Operator#STARTS_WITH}
     */
    STARTSWITH("starts-with"),
    /**
     * The operator for ends-with clause.
     * This operator should perform case insensitive searches when possible.
     */
    ENDS_WITH("endsWith"),
    /**
     * For Kebab case support "ends-with", behavior is same as {@link Operator#ENDS_WITH}
     */
    ENDSWITH("ends-with"),
    /**
     * The operator for regular expression clause.
     * This operator should perform case insensitive searches when possible.
     */
    MATCHES("matches"),

    // Logical Operators
    /**
     * Logical AND operator in case of multiple values are provided for same search parameter.
     * This attribute doesn't apply on single valued search attributes.
     */
    AND("and"),
    /**
     * Logical OR operator in case of multiple values are provided for same search parameter.
     * This attribute doesn't apply on single valued search attributes.This is default operator for multi-valued
     * search parameters if no explicit multi-valued ({@link #AND} or {@link #OR})
     * operator is defined.
     */
    OR("or"),
    /**
     * Logical NOT operator which can be used to negate the result of any search parameter/logic.
     */
    NOT("not"),
    /**
     * Greater than operator primarily to be used for numeric values.
     */
    GREATER_THAN("gt"),
    /**
     * Greater than or equal operator primarily to be used for numeric values.
     */
    GREATER_THAN_OR_EQUAL("gte"),
    /**
     * Less than operator primarily to be used for numeric values
     */
    LESS_THAN("lt"),
    /**
     * Less than or equal operator primarily to be used for numeric values
     */
    LESS_THAN_OR_EQUAL("lte"),
    /**
     * Unary operator to indicate a particular other value operator must execute its logic by ignoring case.
     */
    CASE_IGNORE("ci");


    private final String value;

    Operator(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
