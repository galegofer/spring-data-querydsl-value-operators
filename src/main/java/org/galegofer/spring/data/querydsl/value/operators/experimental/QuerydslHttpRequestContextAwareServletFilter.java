package org.galegofer.spring.data.querydsl.value.operators.experimental;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.querydsl.core.types.EntityPath;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

@Slf4j
public class QuerydslHttpRequestContextAwareServletFilter implements Filter {
    private static final EntityPathResolver entityPathResolver = SimpleEntityPathResolver.INSTANCE;

    private final Map<String, Class<?>> URI_SEARCH_RESOURCE_TYPE_MAPPINGS = new TreeMap((Comparator<String>) (s1, s2) -> {
        if (s1.length() > s2.length()) {
            return -1;
        } else if (s1.length() < s2.length()) {
            return 1;
        }

        return s1.compareTo(s2);
    });

    private final LoadingCache<Class<?>, EntityPath<?>> loadingCache = CacheBuilder.newBuilder()
        .build(new CacheLoader<>() {
            @Override
            public EntityPath<?> load(Class<?> domainClass) {
                return entityPathResolver.createPath(domainClass);
            }
        });

    public QuerydslHttpRequestContextAwareServletFilter(Map<String, Class<?>> URI_SEARCH_RESOURCE_TYPE_MAPPINGS) {
        if (URI_SEARCH_RESOURCE_TYPE_MAPPINGS != null) {
            this.URI_SEARCH_RESOURCE_TYPE_MAPPINGS.putAll(URI_SEARCH_RESOURCE_TYPE_MAPPINGS);
        }

        try {
            loadingCache.getAll(this.URI_SEARCH_RESOURCE_TYPE_MAPPINGS.values());
        } catch (ExecutionException ex) {
            throw new RuntimeException("Failed to instantiate filter, possible mis-configurations?", ex);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        try {
            final var req = (HttpServletRequest) request;
            final var requestURI = req.getRequestURI();

            final var optionalPath = URI_SEARCH_RESOURCE_TYPE_MAPPINGS.keySet()
                .stream()
                .filter(resourceMapping -> resourceMapping.equalsIgnoreCase(requestURI))
                .findFirst()
                .map(URI_SEARCH_RESOURCE_TYPE_MAPPINGS::get)
                .map(resourceMapping -> {
                    try {
                        return loadingCache.get(resourceMapping);
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to load Path for " + "request uri: " + requestURI);
                    }
                });

            if (optionalPath.isPresent()) {
                log.debug("Processing {} on URI: {} for EntityPath: {}",
                    QuerydslHttpRequestContext.class, requestURI, optionalPath.get()
                        .getClass()
                        .getCanonicalName());
                final var context = new QuerydslHttpRequestContext(optionalPath.get(), req);
                QuerydslHttpRequestContextHolder.setContext(context);
                chain.doFilter(context.getWrappedHttpServletRequest(), response);
            } else {
                log.error(
                    "No EntityPath found on requestURI: {}, bad filter configurations (check filter url pattern and also the injected mappings), filter is turning into a no-op for this request",
                    requestURI);
                chain.doFilter(req, response);
            }
        } finally {
            QuerydslHttpRequestContextHolder.clearContext();
        }
    }

    @Override
    public void destroy() {
    }
}
