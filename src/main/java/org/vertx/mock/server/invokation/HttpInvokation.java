package org.vertx.mock.server.invokation;

import org.slf4j.Logger;
import org.vertx.java.core.http.HttpServerRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

public class HttpInvokation {

    private static final Logger LOGGER = getLogger(HttpInvokation.class);

    private Object target;

    private Method method;

    public HttpInvokation(Object target, Method method) {
        this.target = target;
        this.method = method;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public void invoke(HttpServerRequest request) throws InvocationTargetException, IllegalAccessException {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];
        for (int i = 0 ; i < parameterTypes.length ; i++) {
            if (parameterTypes[i] == HttpServerRequest.class) {
                parameters[i] = request;
            }
            else {
                throw new IllegalArgumentException(format("Parameter type not supported: '%1$s'", parameterTypes[i]));
            }
        }
        try {
            method.invoke(target, parameters);
        }
        catch(Exception e) {
            LOGGER.warn(e.getMessage(), e);
            throw e;
        }
    }

}
