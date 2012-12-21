package org.vertx.mock.server.resolver;


import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.mock.server.controller.HttpController;
import org.vertx.mock.server.invokation.HttpInvokation;
import org.vertx.mock.server.invokation.HttpInvokationCache;
import org.vertx.mock.server.model.HttpMethod;
import org.vertx.mock.server.model.HttpRequestHandler;
import org.vertx.mock.server.model.HttpUri;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.List;

import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static net.sf.cglib.proxy.Enhancer.isEnhanced;
import static org.apache.commons.lang.StringUtils.isEmpty;

public abstract class AbstractHttpInvokationResolver implements HttpInvokationResolver {

    @SuppressWarnings("unchecked")
    private static final List ACCEPTED_TYPES = unmodifiableList(asList(HttpServerRequest.class));

    private HttpInvokationCache invokationCache = new HttpInvokationCache();

    protected void initialize() throws MalformedURLException {
        List<Object> controllers = getHttpControllers();

        for (Object controller : controllers) {
            Class<?> controllerClass = getControllerClass(controller);
            HttpController httpController = controllerClass.getAnnotation(HttpController.class);
            for (Method method : controllerClass.getMethods()) {
                if (isInvokable(method)) {
                    HttpRequestHandler httpRequestHandler = method.getAnnotation(HttpRequestHandler.class);
                    HttpUri httpUri = new HttpUri(getUri(httpController, httpRequestHandler));
                    for (HttpMethod HttpMethod : httpRequestHandler.methods()) {
                        invokationCache.put(httpUri, HttpMethod, new HttpInvokation(controller, method));
                    }
                }
            }
        }
    }

    private String getUri(HttpController httpController, HttpRequestHandler httpResponseHandler) {
        String controllerPath = (!httpController.value().startsWith("/") ? "/" : "") + httpController.value();
        String requestPath = isEmpty(httpResponseHandler.value()) ? "" : (!httpResponseHandler.value().startsWith("/") ?  "/" : "") + httpResponseHandler.value();

        return controllerPath + requestPath;
    }

    private Class<?> getControllerClass(Object controller) {
        Class<?> _class;
        if (isEnhanced(controller.getClass())) {
            _class = controller.getClass().getSuperclass();
        }
        else {
            _class = controller.getClass();
        }
        return _class;
    }

    public abstract List<Object> getHttpControllers();

    @Override
    public HttpInvokation resolve(HttpUri httpUri, HttpMethod httpMethod) {
        return invokationCache.get(httpUri, httpMethod);
    }

    protected boolean isInvokable(Method method) {
        if ( !isPublic(method.getModifiers()) ||
             !method.isAnnotationPresent(HttpRequestHandler.class)) {
            return false;
        }
        
        for (Class<?> parameterType : method.getParameterTypes()) {
            if (!ACCEPTED_TYPES.contains(parameterType)) {
                return false;
            }
        }
        
        return true;
    }

}
