package org.galegofer.spring.data.querydsl.value.operators.experimental;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Path;
import org.galegofer.spring.data.querydsl.value.operators.ExpressionProvider;
import org.galegofer.spring.data.querydsl.value.operators.ExpressionProviderFactory;
import org.galegofer.spring.data.querydsl.value.operators.Operator;
import org.galegofer.spring.data.querydsl.value.operators.OperatorAndValue;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public final class QuerydslHttpRequestContext {

    private final EntityPath<?> root;
    private final HttpServletRequest servletRequest;
    private final Map<String, Collection<String>> originalParameters = new LinkedHashMap<>();
    private final Map<String, Collection<String>> transformedParameters;

    public QuerydslHttpRequestContext(EntityPath<?> root, HttpServletRequest servletRequest) {
        Validate.notNull(root, "EntityPath must not be null");
        Validate.notNull(servletRequest, "HttpServletRequest must not be null");
        this.root = root;
        this.servletRequest = servletRequest;

        this.servletRequest.getParameterMap()
            .keySet()
            .forEach(k -> originalParameters.put(String.valueOf(k),
                List.of(this.servletRequest.getParameterValues(String.valueOf(k)))));

        transformedParameters = this.originalParameters.keySet()
            .stream()
            .collect(Collectors.toMap(key -> key, key -> originalParameters.get(key)
                .stream()
                .map(this::extractTrueValue)
                .collect(Collectors.toList()), (e1, e2) -> e1, LinkedHashMap::new));
    }

    /**
     * @return decorated {@link HttpServletRequest} object containing search
     * request parameters devoid of any value operators.
     */
    HttpServletRequest getWrappedHttpServletRequest() {
        if (CollectionUtils.isEmpty(transformedParameters)) {
            return getOriginalHttpServletRequest();
        }

        return new HttpServletRequestWrapper(this.servletRequest) {
            @Override
            public String getParameter(String name) {
                final var values = getParameterValuesAsList(name);

                return (!CollectionUtils.isEmpty(values))
                    ? values.iterator()
                    .next()
                    : super.getParameter(name);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return transformedParameters.keySet()
                    .stream()
                    .collect(Collectors.toMap(key -> key, key -> transformedParameters.get(key)
                            .toArray(new String[0]),
                        (e1, e2) -> e1, LinkedHashMap::new));
            }

            @Override
            public String[] getParameterValues(String name) {
                final var values = getParameterValuesAsList(name);
                return values.toArray(new String[0]);
            }

            private Collection<String> getParameterValuesAsList(String name) {
                Validate.notNull(name, "Parameter name must not be blank");
                final var result = transformedParameters.get(name);
                return result != null ? result : new ArrayList<>(0);
            }
        };
    }

    /**
     * @return original {@link HttpServletRequest} containing inputs from client
     * request.
     */
    HttpServletRequest getOriginalHttpServletRequest() {
        return this.servletRequest;
    }

    /**
     * @param inPath {@link Path} for which original search request single value is
     *               required.
     * @return Original value from original HttpServletRequest for given
     * {@link Path} if available, <code>null</code> otherwise
     */
    public String getSingleValue(Path inPath) {
        return Optional.ofNullable(Optional.ofNullable(inPath)
                .map(p -> this.servletRequest.getParameter(findRequestParameterNameFromPath(inPath)))
                .orElseGet(() -> this.servletRequest.getParameter(inPath.toString())))
            .orElseGet(() -> ExpressionProviderFactory.findAlias(inPath)
                .map(this.servletRequest::getParameter)
                .orElse(null));
    }

    /**
     * @param inPath {@link Path} for which original search request all values are
     *               required.
     * @return Original values as {@link Array} of String from original
     * HttpServletRequest for given {@link Path} if available,
     * <code>null</code> otherwise
     */
    public String[] getAllValues(Path inPath) {
        return Optional.ofNullable(Optional.ofNullable(inPath)
                .map(p -> this.servletRequest.getParameterValues(findRequestParameterNameFromPath(inPath)))
                .orElseGet(() -> this.servletRequest.getParameterValues(inPath.toString())))
            .orElseGet(() -> ExpressionProviderFactory.findAlias(inPath)
                .map(this.servletRequest::getParameterValues)
                .orElse(null));
    }

    private String findRequestParameterNameFromPath(Path inPath) {
        Validate.notNull(inPath, "Input path must not be null to lookup original request parameter value");
        Validate.isTrue(inPath.getRoot()
            .getType()
            .equals(this.root.getType()), "Mismatch in type root in path and current context");

        return StringUtils.replace(inPath.toString(), this.root + ".", StringUtils.EMPTY, 1);
    }

    private String extractTrueValue(String input) {
        if (StringUtils.isNotBlank(input)) {
            while (true) {
                return ExpressionProvider.isOperator(input).isPresent()
                    ? extractTrueValue(new OperatorAndValue(input, List.of(Operator.values()), null).getValue())
                    : input;
            }
        }
        return StringUtils.EMPTY;
    }
}
