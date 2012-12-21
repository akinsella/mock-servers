package org.vertx.mock.server.resolver;

import org.vertx.mock.server.invokation.HttpInvokation;
import org.vertx.mock.server.model.HttpMethod;
import org.vertx.mock.server.model.HttpUri;

public interface HttpInvokationResolver {
    
    HttpInvokation resolve(HttpUri uri, HttpMethod method);
    
}
