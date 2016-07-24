/**
 * Copyright 2016 FastXml author(https://github.com/fastxml/fastxml)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fastxml.util;

import com.github.fastxml.exception.NumberFormatException;
import com.github.fastxml.exception.ParseException;
import java.nio.charset.Charset;

/**
 * utils for parse raw byte array to readable value, such as short,int,long,float,double,string
 * Created by weager on 2016/06/08.
 */
public class ParseUtils {

    /**
     * parse bytes to string with specific encoding charset, remove "<![CDATA[" and "]]>" and replace entity reference if necessary
     *
     * @param bytes  the byte array
     * @param begin  the beginning index, inclusive.
     * @param length the length of bytes need to be parsed
     * @return string parse from bytes. if the length of string is 0, then return null
     */
    public final static String parseString(final byte[] bytes, int begin, int length) throws ParseException {
        int last = begin + length;
        FastStringBuilder sb = new FastStringBuilder(length);
        for (; begin < last; begin++) { // found CDATA block
            if (bytes[begin] == '<' && bytes[begin + 1] == '!' && bytes[begin + 2] == '['
                    && bytes[begin + 3] == 'C' && bytes[begin + 4] == 'D' && bytes[begin + 5] == 'A'
                    && bytes[begin + 6] == 'T' && bytes[begin + 7] == 'A' && bytes[begin + 8] == '[') {
                begin += 9;
                begin = parseCDATA4Byte(bytes, begin, last, sb);
            } else if (bytes[begin] == '&') { // found entity reference
                begin = parseEntityReference4Byte(bytes, ++begin, last, sb);
            } else {
                sb.append((char) bytes[begin]);
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private final static int parseCDATA4Byte(final byte[] bytes, int begin, int last, FastStringBuilder sb) throws ParseException {
        for (; begin < last; begin++) {
            if (bytes[begin] == ']' && bytes[begin + 1] == ']' && bytes[begin + 2] == '>') {
                begin += 2;
                return begin;
            } else {
                sb.append((char) bytes[begin]);
            }
        }
        throw ParseException.formatError("CDATA is not closed");
    }

    private final static int parseEntityReference4Byte(final byte[] bytes, int begin, int last, FastStringBuilder sb) throws ParseException {

        for (; begin < last; begin++) {
            byte b = bytes[begin];
            int val = 0;
            switch (bytes[begin]) {
                case '#':
                    begin++;
                    if (bytes[begin] == 'x') { // Hexadecimal reference
                        begin++;
                        for (; begin < last; begin++) {
                            b = bytes[begin];
                            if (b >= '0' && b <= '9') {
                                val = (val << 4) + (b - '0');
                            } else if (b >= 'a' && b <= 'f') {
                                val = (val << 4) + (b - 'a' + 10);
                            } else if (b >= 'A' && b <= 'F') {
                                val = (val << 4) + (b - 'A' + 10);
                            } else if (b == ';') {
                                sb.append((char) val);
                                break;
                            } else
                                throw ParseException.entityError("Errors in Entity: Illegal char following &#x");
                        }
                    } else { // Numeric reference
                        for (; begin < last; begin++) {
                            b = bytes[begin];
                            if (b >= '0' && b <= '9') {
                                val = val * 10 + (b - '0');
                            } else if (b == ';') {
                                sb.append((char) val);
                                break;
                            } else
                                throw ParseException.entityError("Errors in char reference: Illegal char following &#");
                        }
                    }
                    return begin;
                case 'a':
                    begin++;
                    if (bytes[begin] == 'm') {
                        if (bytes[++begin] == 'p' && bytes[++begin] == ';') { // &amp; --> &
                            sb.append('&');
                            return begin;
                        } else
                            throw ParseException.entityError("Errors in Entity: should be '&amp;' here");
                    } else if (bytes[begin] == 'p') { // &apos; --> '
                        if (bytes[++begin] == 'o'
                                && bytes[++begin] == 's'
                                && bytes[++begin] == ';') {
                            sb.append('\'');
                            return begin;
                        } else
                            throw ParseException.entityError("Errors in Entity: should be '&apos;' here");
                    } else
                        throw ParseException.entityError("Errors in Entity: Illegal builtin reference");

                case 'q':
                    if (bytes[++begin] == 'u'
                            && bytes[++begin] == 'o'
                            && bytes[++begin] == 't'
                            && bytes[++begin] == ';') { // &quot; --> "
                        sb.append('"');
                        return begin;
                    } else
                        throw ParseException.entityError("Errors in Entity: should be '&quot;' here");
                case 'l':
                    if (bytes[++begin] == 't' && bytes[++begin] == ';') { // &lt; --> <
                        sb.append('<');
                        return begin;
                    } else
                        throw ParseException.entityError("Errors in Entity: should be '&lt;' here");
                case 'g':
                    if (bytes[++begin] == 't' && bytes[++begin] == ';') { // &gt; --> >
                        sb.append('>');
                        return begin;
                    } else
                        throw ParseException.entityError("Errors in Entity: should be '&gt;' here");
                default:
                    throw ParseException.entityError("Errors in Entity: Illegal entity char");
            }
        }
        return begin;
    }


    public final static String parseStringWithDecoding(final byte[] bytes, int begin, int length, Charset charset) throws ParseException {
        String strNeedDecoding = new String(bytes, begin, length, charset);
        char[] chars = strNeedDecoding.toCharArray();
        int last = chars.length;
        FastStringBuilder sb = new FastStringBuilder(length);
        for (int i = 0; i < last; i++) { // found CDATA block
            if (chars[i] == '<' && chars[i + 1] == '!' && chars[i + 2] == '['
                    && chars[i + 3] == 'C' && chars[i + 4] == 'D' && chars[i + 5] == 'A'
                    && chars[i + 6] == 'T' && chars[i + 7] == 'A' && chars[i + 8] == '[') {
                i += 9;
                i = parseCDATA4String(chars, i, last, sb);
            } else if (chars[i] == '&') { // found entity reference
                i = parseEntityReference4String(chars, ++i, last, sb);
            } else {
                sb.append(chars[i]);
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private final static int parseCDATA4String(final char[] chars, int begin, int last, FastStringBuilder sb) throws ParseException {
        for (; begin < last; begin++) {
            if (chars[begin] == ']' && chars[begin + 1] == ']' && chars[begin + 2] == '>') {
                begin += 2;
                return begin;
            } else {
                sb.append(chars[begin]);
            }
        }
        throw ParseException.formatError("CDATA is not closed");
    }

    private final static int parseEntityReference4String(final char[] chars, int begin, int last, FastStringBuilder sb) throws ParseException {

        for (; begin < last; begin++) {
            char b = chars[begin];
            int val = 0;
            switch (chars[begin]) {
                case '#':
                    begin++;
                    if (chars[begin] == 'x') { // Hexadecimal reference
                        begin++;
                        for (; begin < last; begin++) {
                            b = chars[begin];
                            if (b >= '0' && b <= '9') {
                                val = (val << 4) + (b - '0');
                            } else if (b >= 'a' && b <= 'f') {
                                val = (val << 4) + (b - 'a' + 10);
                            } else if (b >= 'A' && b <= 'F') {
                                val = (val << 4) + (b - 'A' + 10);
                            } else if (b == ';') {
                                sb.append((char) val);
                                break;
                            } else
                                throw ParseException.entityError("Errors in Entity: Illegal char following &#x");
                        }
                    } else { // Numeric reference
                        for (; begin < last; begin++) {
                            b = chars[begin];
                            if (b >= '0' && b <= '9') {
                                val = val * 10 + (b - '0');
                            } else if (b == ';') {
                                sb.append((char) val);
                                break;
                            } else
                                throw ParseException.entityError("Errors in char reference: Illegal char following &#");
                        }
                    }
                    return begin;
                case 'a':
                    begin++;
                    if (chars[begin] == 'm') {
                        if (chars[++begin] == 'p' && chars[++begin] == ';') { // &amp; --> &
                            sb.append('&');
                            return begin;
                        } else
                            throw ParseException.entityError("Errors in Entity: should be '&amp;' here");
                    } else if (chars[begin] == 'p') { // &apos; --> '
                        if (chars[++begin] == 'o'
                                && chars[++begin] == 's'
                                && chars[++begin] == ';') {
                            sb.append('\'');
                            return begin;
                        } else
                            throw ParseException.entityError("Errors in Entity: should be '&apos;' here");
                    } else
                        throw ParseException.entityError("Errors in Entity: Illegal builtin reference");

                case 'q':
                    if (chars[++begin] == 'u'
                            && chars[++begin] == 'o'
                            && chars[++begin] == 't'
                            && chars[++begin] == ';') { // &quot; --> "
                        sb.append('"');
                        return begin;
                    } else
                        throw ParseException.entityError("Errors in Entity: should be '&quot;' here");
                case 'l':
                    if (chars[++begin] == 't' && chars[++begin] == ';') { // &lt; --> <
                        sb.append('<');
                        return begin;
                    } else
                        throw ParseException.entityError("Errors in Entity: should be '&lt;' here");
                case 'g':
                    if (chars[++begin] == 't' && chars[++begin] == ';') { // &gt; --> >
                        sb.append('>');
                        return begin;
                    } else
                        throw ParseException.entityError("Errors in Entity: should be '&gt;' here");
                default:
                    throw ParseException.entityError("Errors in Entity: Illegal entity char");
            }
        }
        return begin;
    }


    /**
     * parse bytes to integer
     *
     * @param bytes  the byte array
     * @param begin  the beginning index, inclusive.
     * @param length the length of bytes need to be parsed, if length == 0, a NumberFormatException will thrown
     * @return integer number parsed from bytes
     * @throws NumberFormatException
     */
    public final static int parseInt(final byte[] bytes, int begin, int length) throws NumberFormatException {
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
                    throw NumberFormatException.formatException(toString(bytes, begin, length), null);

                if (length == 1) // Cannot have lone "+" or "-"
                    throw NumberFormatException.formatException(toString(bytes, begin, length), null);
                i++;
            }
            multmin = limit / radix;
            while (i < length) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit((char) bytes[i + begin], radix);
                i++;
                if (digit < 0) {
                    throw NumberFormatException.formatException(toString(bytes, begin, length), null);
                }
                if (result < multmin) {
                    throw NumberFormatException.formatException(toString(bytes, begin, length), null);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw NumberFormatException.formatException(toString(bytes, begin, length), null);
                }
                result -= digit;
            }
        } else {
            throw NumberFormatException.formatException("can't convert null to integer", null);
        }
        return negative ? result : -result;
    }

    /**
     * parse bytes to long
     *
     * @param bytes  the byte array
     * @param begin  the beginning index, inclusive.
     * @param length the length of bytes need to be parsed
     * @return long number parsed from bytes
     * @throws NumberFormatException
     */
    public final static long parseLong(final byte[] bytes, int begin, int length) throws NumberFormatException {
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
                    throw NumberFormatException.formatException(toString(bytes, begin, length), null);

                if (length == 1) // Cannot have lone "+" or "-"
                    throw NumberFormatException.formatException(toString(bytes, begin, length), null);
                i++;
            }
            multmin = limit / radix;
            while (i < length) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit((char) bytes[i + begin], radix);
                i++;
                if (digit < 0) {
                    throw NumberFormatException.formatException(toString(bytes, begin, length), null);
                }
                if (result < multmin) {
                    throw NumberFormatException.formatException(toString(bytes, begin, length), null);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw NumberFormatException.formatException(toString(bytes, begin, length), null);
                }
                result -= digit;
            }
        } else {
            throw NumberFormatException.formatException("can't convert null to long", null);
        }
        return negative ? result : -result;
    }

    /**
     * parse bytes to float
     *
     * @param bytes  the byte array
     * @param begin  the beginning index, inclusive.
     * @param length the length of bytes need to be parsed
     * @return float number parsed from bytes
     * @throws NumberFormatException
     */
    public final static float parseFloat(final byte[] bytes, int begin, int length) throws NumberFormatException {
        try {
            return Float.parseFloat(parseString(bytes, begin, length));
        } catch (Exception e) {
            throw NumberFormatException.formatException(e.getMessage(), e);
        }
    }

    /**
     * parse bytes to double
     *
     * @param bytes  the byte array
     * @param begin  the beginning index, inclusive.
     * @param length the length of bytes need to be parsed
     * @return double number parsed from bytes
     * @throws NumberFormatException
     */
    public final static double parseDouble(final byte[] bytes, int begin, int length) throws NumberFormatException {
        try {
            return Double.parseDouble(parseString(bytes, begin, length));
        } catch (Exception e) {
            throw NumberFormatException.formatException(e.getMessage(), e);
        }
    }

    /**
     * parse byte to char one by one
     *
     * @param bytes  the byte array
     * @param begin  the beginning index, inclusive.
     * @param length the length of bytes need to be parsed, should > 0
     * @return string parse from bytes
     */
    final static String toString(final byte[] bytes, int begin, int length) {
        int last = begin + length;
        FastStringBuilder sb = new FastStringBuilder(length);
        for (; begin < last; begin++) {
            sb.append(bytes[begin]);
        }
        return sb.toString();
    }
}
