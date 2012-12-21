package org.vertx.mock.server.invokation;

import org.slf4j.Logger;
import org.vertx.mock.server.model.HttpMethod;
import org.vertx.mock.server.model.HttpUri;
import org.vertx.mock.server.resolver.AbstractHttpInvokationResolver;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newTreeSet;
import static org.slf4j.LoggerFactory.getLogger;

public class HttpInvokationCache {

    private static final Logger LOGGER = getLogger(AbstractHttpInvokationResolver.class);

    private Map<HttpMethod, Map<HttpUri, HttpInvokation>> cache = newHashMap();

    public HttpInvokationCache() {
        for (HttpMethod httpMethod : HttpMethod.values()) {
            Map<HttpUri, HttpInvokation> invokationMap = newHashMap();
            cache.put(httpMethod, invokationMap);
        }
    }

    public boolean containsKey(HttpUri httpUri, HttpMethod httpMethod) {
        return cache.get(httpMethod).containsKey(httpUri);
    }

    public HttpInvokation get(HttpUri httpUri, HttpMethod httpMethod) {
        Map<HttpUri, HttpInvokation> methodCache = cache.get(httpMethod);

        Set<HttpUri> httpUris = newTreeSet(methodCache.keySet());
        for (HttpUri currentHttpUri : httpUris) {
            if (currentHttpUri.match(httpUri)) {
                LOGGER.info("Invokation found for httpMethod [{}] and currentHttpUri: '{}'", new Object[] { httpMethod.name(), httpUri.getUri() });
                return methodCache.get(currentHttpUri);
            }
        }
        LOGGER.info("No invokation found for httpMethod [{}] and httpUri: '{}'", new Object[] { httpMethod.name(), httpUri.getUri() });
        return null;
    }

    public void put(HttpUri httpUri, HttpMethod httpMethod, HttpInvokation httpInvokation) {
        LOGGER.info("Adding httpInvokation to cache for httpMethod [{}]: '{}'", new Object[] { httpMethod.name(), httpUri.getUri() });
        cache.get(httpMethod).put(httpUri, httpInvokation);
    }
 
}
