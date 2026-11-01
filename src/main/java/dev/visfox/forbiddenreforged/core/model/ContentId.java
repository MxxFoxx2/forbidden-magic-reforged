package dev.visfox.forbiddenreforged.core.model;

import java.util.Objects;
import java.util.regex.Pattern;

public record ContentId(String namespace, String path) implements Comparable<ContentId> {
    private static final Pattern NAMESPACE = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern PATH = Pattern.compile("[a-z0-9/._-]+");

    public ContentId {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(path, "path");
        if (!NAMESPACE.matcher(namespace).matches()) {
            throw new IllegalArgumentException("Invalid content namespace: " + namespace);
        }
        if (!PATH.matcher(path).matches() || path.startsWith("/") || path.endsWith("/")) {
            throw new IllegalArgumentException("Invalid content path: " + path);
        }
    }

    public static ContentId parse(String value) {
        Objects.requireNonNull(value, "value");
        int separator = value.indexOf(':');
        if (separator <= 0 || separator != value.lastIndexOf(':') || separator == value.length() - 1) {
            throw new IllegalArgumentException("Expected a namespaced identifier, got: " + value);
        }
        return new ContentId(value.substring(0, separator), value.substring(separator + 1));
    }

    @Override
    public int compareTo(ContentId other) {
        int namespaceOrder = namespace.compareTo(other.namespace);
        return namespaceOrder != 0 ? namespaceOrder : path.compareTo(other.path);
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
