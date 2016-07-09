package org.fastxml.util;

import org.fastxml.exception.NumberFormatException;

import java.nio.charset.Charset;

/**
 * utils for parse raw byte array to readable value, such as short,int,long,float,double,string
 * Created by weager on 2016/06/08.
 */
public class ParseUtils {

    /**
     * parse bytes to string without any encoding, cast byte to char directly, and remove "<![CDATA[" and "]]" if necessary
     *
     * @param bytes
     * @param begin
     * @param length
     * @return
     */
    public static String parseString(byte[] bytes, int begin, int length) {
        return parseString(bytes, begin, length, null);
    }

    /**
     * parse bytes to string with specific encoding charset, remove "<![CDATA[" and "]]" if necessary
     *
     * @param bytes
     * @param begin
     * @param length
     * @param charset use charset to encoding bytes, directly cast byte to char if charset parameter equals null
     * @return
     */
    public static String parseString(byte[] bytes, int begin, int length, Charset charset) {
        if (length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean inCDATABlock = false;
        int beginIndex4Segment = begin;
        int length4Segment;
        int lastIndex = begin + length;
        for (int i = begin; i < lastIndex; i++) {
            if (bytes[i] == '<' && bytes[i + 1] == '!' && bytes[i + 2] == '[' && bytes[i + 3] == 'C'
                    && bytes[i + 4] == 'D' && bytes[i + 5] == 'A' && bytes[i + 6] == 'T'
                    && bytes[i + 7] == 'A' && bytes[i + 8] == '[') { // found "<![CDATA["
                length4Segment = i - beginIndex4Segment;
                if (length4Segment > 0) {
                    sb.append(internalParseString(bytes, beginIndex4Segment, length4Segment, charset));
                }
                i += 8; // skip "<![CDATA["
                beginIndex4Segment = i + 1;
                inCDATABlock = true;
            } else if (inCDATABlock && bytes[i] == ']' && bytes[i + 1] == ']') { // found "]]"
                length4Segment = i - beginIndex4Segment;
                if (length4Segment > 0) {
                    sb.append(internalParseString(bytes, beginIndex4Segment, length4Segment, charset));
                }
                i += 1; // skip "]]"
                beginIndex4Segment = i + 1;
            } else if (i + 1 >= lastIndex) { // last byte
                length4Segment = i + 1 - beginIndex4Segment;
                if (length4Segment > 0) {
                    sb.append(internalParseString(bytes, beginIndex4Segment, length4Segment, charset));
                }
                break;
            }
        }
        if (sb.length() == 0) {
            return null;
        } else {
            return sb.toString();
        }
    }

    private static String internalParseString(byte[] bytes, int begin, int length, Charset charset) {
        if (charset != null) { // need encoding
            return new String(bytes, begin, length, charset);
        } else { // no need encoding
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append((char) bytes[i + begin]);
            }
            return sb.toString();
        }
    }

    public static int parseInt(byte[] bytes, int begin, int length, Charset charset) throws NumberFormatException {
        int result = 0;
        boolean negative = false;
        int i = 0;
        int limit = -Integer.MAX_VALUE;
        int radix = 10;
        int multmin;
        int digit;

        if (length > 0) {
            char firstChar = (char) bytes[begin];
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+')
                    throw NumberFormatException.intFormatException(parseString(bytes, begin, length, charset));

                if (length == 1) // Cannot have lone "+" or "-"
                    throw NumberFormatException.intFormatException(parseString(bytes, begin, length, charset));
                i++;
            }
            multmin = limit / radix;
            while (i < length) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit((char) bytes[i + begin], radix);
                i++;
                if (digit < 0) {
                    throw NumberFormatException.intFormatException(parseString(bytes, begin, length, charset));
                }
                if (result < multmin) {
                    throw NumberFormatException.intFormatException(parseString(bytes, begin, length, charset));
                }
                result *= radix;
                if (result < limit + digit) {
                    throw NumberFormatException.intFormatException(parseString(bytes, begin, length, charset));
                }
                result -= digit;
            }
        } else {
            throw NumberFormatException.intFormatException(parseString(bytes, begin, length, charset));
        }
        return negative ? result : -result;
    }

    public static short parseShort(byte[] bytes, int begin, int length, Charset charset) throws NumberFormatException {
        return (short) parseInt(bytes, begin, length, charset);
    }

    public static long parseLong(byte[] bytes, int begin, int length, Charset charset) throws NumberFormatException {
        long result = 0;
        boolean negative = false;
        int i = 0;
        long limit = -Long.MAX_VALUE;
        int radix = 10;
        long multmin;
        int digit;

        if (length > 0) {
            char firstChar = (char) bytes[begin];
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Long.MIN_VALUE;
                } else if (firstChar != '+')
                    throw NumberFormatException.longFormatException(parseString(bytes, begin, length, charset));

                if (length == 1) // Cannot have lone "+" or "-"
                    throw NumberFormatException.longFormatException(parseString(bytes, begin, length, charset));
                i++;
            }
            multmin = limit / radix;
            while (i < length) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit((char) bytes[i + begin], radix);
                i++;
                if (digit < 0) {
                    throw NumberFormatException.longFormatException(parseString(bytes, begin, length, charset));
                }
                if (result < multmin) {
                    throw NumberFormatException.longFormatException(parseString(bytes, begin, length, charset));
                }
                result *= radix;
                if (result < limit + digit) {
                    throw NumberFormatException.longFormatException(parseString(bytes, begin, length, charset));
                }
                result -= digit;
            }
        } else {
            throw NumberFormatException.longFormatException(parseString(bytes, begin, length, charset));
        }
        return negative ? result : -result;
    }

    public static float parseFloat(byte[] bytes, int begin, int length, Charset charset) throws NumberFormatException {
        String str = parseString(bytes, begin, length);
        if (str == null) {
            return 0;
        }
        try {
            return Float.parseFloat(str);
        } catch (java.lang.NumberFormatException e) {
            throw NumberFormatException.floatFormatException(parseString(bytes, begin, length, charset));
        }
    }

    public static double parseDouble(byte[] bytes, int begin, int length, Charset charset) throws NumberFormatException {
        String str = parseString(bytes, begin, length);
        if (str == null) {
            return 0;
        }
        try {
            return Double.parseDouble(str);
        } catch (java.lang.NumberFormatException e) {
            throw NumberFormatException.doubleFormatException(parseString(bytes, begin, length, charset));
        }
    }


}
