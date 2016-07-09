package org.fastxml.util;

/**
 * Created by weager on 2016/06/08.
 */
public class ByteUtils {

    /**
     * validate the byte to check that is a char of tag name or attribute name.
     *
     * @param b a byte to be valid
     * @return true if valid, otherwise false
     */
    public static boolean isValidTokenChar(byte b) {
        return b > 0 && ((b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z') || (b >= '0' && b <= '9')
                || b == ':' || b == '-' || b == '_' || b == '.');
    }

    /**
     * contrary to isValidTokenChar method
     *
     * @param b a byte to be valid
     * @return false if valid, otherwise true
     */
    public static boolean isNotValidTokenChar(byte b) {
        return !isValidTokenChar(b);
    }

    /**
     * compare two byte array segment.
     * Note: if both are empty, they are the same
     * !null, null -> false; null, !null -> false; null, null -> true
     *
     * @param bs1    byte array 1
     * @param start1 index of bs1
     * @param bs2    byte array 2
     * @param start2 index of bs2
     * @param length the length of each bytes array will be compared
     * @return true if two byte array segments equal, otherwise false
     */
    public static boolean equals(byte[] bs1, int start1, byte[] bs2, int start2, int length) {
        if (bs1 != null && bs2 != null && start1 + length <= bs1.length && start2 + length <= bs2.length) {
            for (int i = 0, i1 = start1, i2 = start2; i < length; i++, i1++, i2++) {
                if (bs1[i1] != bs2[i2]) {
                    return false;
                }
            }
            return true;
        }
        return bs1 == null && bs2 == null;
    }
}
