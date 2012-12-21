package org.vertx.mock.server.model;

import com.google.common.collect.ImmutableList;
import org.springframework.web.util.UriTemplate;

import java.net.MalformedURLException;
import java.util.List;

import static com.google.common.base.Splitter.on;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.max;
import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang.builder.ToStringBuilder.reflectionToString;

public class HttpUri implements Comparable<HttpUri> {

    private String uri;
    private String basePath;
    private UriTemplate uriTemplate;
    private String relativePath;
    private PathParts relativePathParts;

    public HttpUri(String uri) throws MalformedURLException {
        this.uri = uri;
        this.basePath = "";
        this.relativePath = uri;
        this.relativePathParts = new PathParts(uri);
        this.uriTemplate = new UriTemplate(uri);
    }

    public HttpUri(String uri, String basePath, String relativePath) throws MalformedURLException {
        this.uri = uri;
        this.basePath = basePath;
        this.relativePath = relativePath;
        this.relativePathParts = new PathParts(uri);
        this.uriTemplate = new UriTemplate(uri);
    }

    public String getUri() {
        return uri;
    }

    public boolean match(HttpUri uri) {
        return uriTemplate.matches(uri.getRelativePath());
    }

    public String getBasePath() {
        return basePath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public PathParts getRelativePathParts() {
        return relativePathParts;
    }

    @Override
    public boolean equals(Object o) {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }

    @Override
    public int compareTo(HttpUri httpUri) {
        return -getRelativePathParts().compareTo(httpUri.getRelativePathParts());
    }

    private class PathParts implements Comparable<PathParts> {

        List<String> parts;

        private PathParts(String path) {
            parts = newArrayList(on("/").split(path));
        }

        public List<String> getParts() {
            return ImmutableList.copyOf(parts);
        }

        @Override
        public int compareTo(PathParts pathParts) {
            List<String> parts1 = getParts();
            List<String> parts2 = pathParts.getParts();
            int parts1Length = parts1.size();
            int parts2Length = parts2.size();
            int length = max(parts1Length, parts2Length);

            for (int i = 0 ; i < length ; i++) {
                if (parts1Length == i  || parts2Length == i) {
                    if (parts1Length == i) {
                        return -1;
                    }
                    else if (parts2Length == i) {
                        return 1;
                    }
                }
                if (isSameParts(parts1, parts2, i)) {
                    continue;
                }
                if (isPartToken(parts1, i) && isPartToken(parts2, i)) {
                    return compareParts(parts1, parts2, i);
                }
                if (isPartToken(parts1, i)) {
                    return -1;
                }
                if (isPartToken(parts2, i)) {
                    return 1;
                }
                return compareParts(parts1, parts2, i);
            }

            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        private boolean isSameParts(List<String> parts1, List<String> parts2, int i) {
            return parts1.get(i).equals(parts2.get(i));
        }

        private int compareParts(List<String> parts1, List<String> parts2, int i) {
            return parts1.get(i).compareTo(parts2.get(i));
        }

        private boolean isPartToken(List<String> parts1, int i) {
            return parts1.get(i).startsWith("{") && parts1.get(i).endsWith("}");
        }

        @Override
        public boolean equals(Object o) {
            return reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            return reflectionHashCode(this);
        }

        @Override
        public String toString() {
            return reflectionToString(this);
        }

    }
    
}
