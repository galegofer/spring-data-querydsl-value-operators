package org.galegofer.spring.data.querydsl.value.operators.experimental;

import org.galegofer.spring.data.querydsl.value.operators.ExpressionProviderFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
public class QuerydslPredicateArgumentResolverBeanPostProcessor implements BeanPostProcessor {

    private final QuerydslBindingsFactory querydslBindingsFactory;

    private final ConversionService conversionServiceDelegate;

    private final Class[] delegatedConversions;

    private final ConversionService delegationAwareConversionService = new ConversionService() {

        @Override
        public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
            return (isDelegatedConversion(sourceType) || isDelegatedConversion(targetType)) && conversionServiceDelegate.canConvert(sourceType, targetType);
        }

        @Override
        public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
            return (isDelegatedConversion(sourceType.getType()) || isDelegatedConversion(targetType.getType())) && conversionServiceDelegate.canConvert(sourceType, targetType);
        }

        @Override
        public <T> T convert(Object source, Class<T> targetType) {
            if (isDelegatedConversion(source.getClass()) || isDelegatedConversion(targetType))
                return conversionServiceDelegate.convert(source, targetType);

            throw new UnsupportedOperationException("Overridden ConversionService in "
                + "QuerydslPredicateArgumentResolver does not " + "support conversion");
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (isDelegatedConversion(sourceType.getType()) || isDelegatedConversion(targetType.getType()))
                return conversionServiceDelegate.convert(source, sourceType, targetType);

            throw new UnsupportedOperationException("Overridden ConversionService in "
                + "QuerydslPredicateArgumentResolver does not " + "support conversion");
        }

        private boolean isDelegatedConversion(Class<?> type) {
            return Optional.ofNullable(conversionServiceDelegate)
                .filter(delegate -> type != null && delegatedConversions != null)
                .flatMap(delegate -> Arrays.stream(delegatedConversions)
                    .filter(c -> c.equals(type))
                    .findFirst())
                .isPresent();
        }
    };

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean != null && QuerydslPredicateArgumentResolver.class.isAssignableFrom(bean.getClass())) {
            ExpressionProviderFactory.setSupportsUnTypedValues(true);

            try {
                try {
                    // Spring Boot 2.x
                    return ConstructorUtils.invokeConstructor(QuerydslPredicateArgumentResolver.class,
                        querydslBindingsFactory, Optional.of(delegationAwareConversionService));
                } catch (NoSuchMethodException | NoSuchMethodError e) {
                    // Spring boot 1.5.x
                    return ConstructorUtils.invokeConstructor(QuerydslPredicateArgumentResolver.class,
                        querydslBindingsFactory, delegationAwareConversionService);
                }
            } catch (Throwable t) {
                throw new RuntimeException("Failed to post-process QuerydslPredicateArgumentResolver", t);
            }
        }
        return bean;
    }

    /**
     * Implementing default method as-is since Spring Boot 1.5.x specific
     * dependencies don't have default methods so if library users use this with
     * an older spring, the runtime would fail. This is implemented as a
     * fail-safe mechanism.
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
