package org.galegofer.spring.data.querydsl.value.operators;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * Interface establishes contract for providing {@link Predicate} or {@link BooleanExpression} to be used to pass to
 * QueryDSL for querying underlying store.
 *
 * <p>
 * This interface and entire component of QueryDSL extension is to build on top of Spring data QueryDSL extensions
 * for QueryDSL by providing further low level of search operators within values which is a powerful extension by
 * empowering client application to perform variety searches, an improvement over largely static capability out of
 * the box to statically define different binding using QuerydslBinderCustomizer.
 * </p>
 * <p>
 * <p>
 * With this extension, client's can request in a variety of forms like below:
 * <ul>
 * <li>Search all resources which has either any email in 'company.com' domain or else have a
 * specific email specified by second parameter
 * /api/user/search?emails.value=endsWith(@company.com)&amp;emails.value=johndoe@somemail.com</li>
 * <li>Search for a user having <b>any <i>*admin*</i></b> role - /api/user/search?role=contains(admin)</li>
 * <li>Search for any user having not having email in some specific domain - /api/user/search?emails
 * .value=not(contains(@company.com))</li>
 * <li>Search any user not having a specific attribute - job_level as "executive"
 * /api/user/search?profile.job_level=ne(executive)</li>
 * </ul>
 *
 * <p>
 * Interface also defines supported operators though specific implementation may only support a subset of them so
 * must be checked on implementation on supported Operators to avoid unpredictable errors in search logic/processing.
 * Implementation must support all Logical Operators.
 * </p>
 *
 * @param <P> type of {@link Path}, example - {@link com.querydsl.core.types.dsl.StringPath}
 *            or {@link com.querydsl.core.types.dsl.EnumPath}.
 * @param <V> type of value for path. Depending on type of path, this could be a collection of String or else
 *            Collection of String or String or Enum or Collection of Enum. This must not be wrapped in Optional.
 * @see Operator
 */
public interface ExpressionProvider<P extends Path, V> {

    /**
     * Provides a operator value delimiter prefix for when explicit delimiter is provided.
     * for e.g. operator(value)
     */
    String OPERATOR_VALUE_DELIMITER_PREFIX = "(";
    /**
     * Provides a operator value delimiter suffix for when explicit delimiter is provided.
     * for e.g. operator(value)
     */
    String OPERATOR_VALUE_DELIMITER_SUFFIX = ")";


    /**
     * Method establishes contract to retrieve a predicate based on implementation specific logic's processing of
     * supplied value(s).
     * <p>
     * Default implementation delegates to {@link #getExpression(Path, Object)}
     * </p>
     *
     * @param path  Q Path <code>Path</code> for which expression is to be formed using supplied <code>value</code>
     * @param value Input value <code>value(s)</code> to be used to form expression, this can be a primitive or a collection of values but must not be wrapped in {@link Optional}
     * @return {@link Optional} of {@link Predicate} based on provided value.
     */
    default Optional<Predicate> getPredicate(P path, V value) {
        return this.getExpression(path, value)
            .map(Predicate.class::cast);
    }

    /**
     * Method establishes the contract to retrieve a {@link BooleanExpression} based on implementation specific
     * logic's processing of supplied value(s).
     * <p>
     * Default implementation returns an empty Optional.
     * </p>
     *
     * @param path  Q Path <code>Path</code> for which expression is to be formed using supplied <code>value</code>
     * @param value Input value <code>value(s)</code> to be used to form expression, this can be a primitive or a collection of values but must not be wrapped in {@link Optional}
     * @return {@link Optional} of {@link BooleanExpression} based on provided value.
     * @throws UnsupportedOperationException if an operator is used in unsupported order or on unsupported digits
     *                                       (for example, startWith operator being used on String values)
     */
    default Optional<BooleanExpression> getExpression(P path, V value) {
        return Optional.empty();
    }


    /**
     * Utility function to check if provided value starts with an Operator.
     * Compares against all available Operators.
     *
     * @param value input string to check for Operator
     * @return <code>Operator</code> if supplied String starts with an operator, <code>empty Optional</code> otherwise
     * @throws UnsupportedOperationException if an operator is used in unsupported order or on unsupported digits
     *                                       (for example, startWith operator being used on String values)
     */
    static <S extends String> Optional<Operator> isOperator(final S value) {
        return isOperator(Operator.values(), value);
    }

    /**
     * Utility function to check if provided value starts with an Operator.
     * Compares against provided operators.
     *
     * @param value     input string to check for Operator
     * @param operators List of operators to check against
     * @return <code>Operator</code> if supplied String starts with an operator, <code>empty Optional</code> otherwise
     * @throws UnsupportedOperationException if an operator is used in unsupported order or on unsupported digits
     *                                       (for example, startWith operator being used on String values)
     */
    static <S extends String> Optional<Operator> isOperator(Operator[] operators, final S value) {
        return Optional.ofNullable(operators)
            .filter(array -> StringUtils.isNotBlank(value))
            .flatMap(array -> Arrays.stream(array)
                .filter(operator -> isOperator(operator, value))
                .findFirst());
    }

    /**
     * Returns <code>true</code> if provided value is wrapped in supplied <code>operator</code>
     *
     * @param operator <code>Operator</code> to check for on provided value
     * @param value    <code>value</code> to check against if it's wrapped in provided <code>operator</code>
     * @return <code>true</code> if provided value is wrapped in supplied <code>operator</code>, <code>false</code> otherwise
     */
    static boolean isOperator(Operator operator, final String value) {
        return value.startsWith(operator.toString() + OPERATOR_VALUE_DELIMITER_PREFIX) && value.endsWith(
            OPERATOR_VALUE_DELIMITER_SUFFIX);
    }

    /**
     * Utility method which validates proper ordering and opening/closing delimiters of operators on supplied <code>value</code>
     *
     * @param value <code>value</code> to check for proper composition
     * @throws IllegalArgumentException if an invalid composition is found in provided <code>value</code>
     */
    static void validateComposition(final String value) {
        // Check if value contains an operator
        final var operator = isOperator(value);
        // If value is not blank and contains an operator
        if (StringUtils.isNotBlank(value) && operator.isPresent()) {
            // Count the number of opening delimiters in value
            int count = value.chars()
                .filter(c -> c == OPERATOR_VALUE_DELIMITER_PREFIX.charAt(0))
                .map(c -> 1)
                .sum();
            // Subtract the number of closing delimiters from the count
            count -= value.chars()
                .filter(c -> c == OPERATOR_VALUE_DELIMITER_SUFFIX.charAt(0))
                .map(c -> 1)
                .sum();
            // If the count is non-zero, throw an exception
            if (count != 0) {
                final var message = count < 0 ? "Malformed (Incompletely closed) value: " :
                    "Malformed (bad-ordering) value: ";
                throw new IllegalArgumentException(message + value);
            }
        }
    }
}
