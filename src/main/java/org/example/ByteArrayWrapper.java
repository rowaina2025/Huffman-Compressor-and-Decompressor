package org.example;

import java.util.Arrays;

class ByteArrayWrapper {
    private final byte[] content;

    ByteArrayWrapper(byte[] data) {
        this.content = data;
    }

    byte[] getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ByteArrayWrapper data = (ByteArrayWrapper) o;
        return Arrays.equals(content, data.content);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(content);
    }
}