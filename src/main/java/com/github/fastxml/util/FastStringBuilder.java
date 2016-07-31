package com.github.fastxml.util;

/**
 * no boundary check for retrieving best performance.
 * Boundary check should be done in caller method or be confirmed by primary logic
 * Created by weager on 2016/07/24.
 */
public final class FastStringBuilder {
    private char[] chars; // char array holder
    private int last = 0; // last index to append a byte or a char

    public FastStringBuilder(int length) {
        this.chars = new char[length];
    }

    public void append(byte b) {
        chars[last] = (char) b;
        last++;
    }

    public void append(char c) {
        chars[last] = c;
        last++;
    }

    public int length() {
        return last;
    }

    public String toString() {
        return new String(chars, 0, last);
    }
}
