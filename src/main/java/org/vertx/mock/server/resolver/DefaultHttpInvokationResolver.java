package org.vertx.mock.server.resolver;

import java.net.MalformedURLException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class DefaultHttpInvokationResolver extends AbstractHttpInvokationResolver {

    private List<Object> beans = newArrayList();

    public DefaultHttpInvokationResolver(Object... beans) throws MalformedURLException {
        this(newArrayList(beans));
    }

    public DefaultHttpInvokationResolver(List<Object> beans) throws MalformedURLException {
        this.beans.addAll(newArrayList(beans));
        initialize();
    }

    public List<Object> getHttpControllers() {
        return beans;
    }

}
