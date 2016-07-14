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
package org.fastxml.util;

import org.fastxml.exception.NumberFormatException;
import org.fastxml.exception.ParseException;

import java.nio.charset.Charset;

/**
 * utils for parse raw byte array to readable value, such as short,int,long,float,double,string
 * Created by weager on 2016/06/08.
 */
public class ParseUtils {

    /**
     * parse bytes to string without any encoding, cast byte to char directly, and remove "<![CDATA[" and "]]>"
     *
     * @param bytes  the byte array
     * @param begin  the beginning index, inclusive.
     * @param length the length of bytes need to be parsed
     * @return string parse from bytes. if the length of string is 0, then return null
     */
    public static String parseString(byte[] bytes, int begin, int length) throws ParseException {
        return parseString(bytes, begin, length, null);
    }

    /**
     * parse bytes to string with specific encoding charset, remove "<![CDATA[" and "]]>" if necessary
     *
     * @param bytes   the byte array
     * @param begin   the beginning index, inclusive.
     * @param length  the length of bytes need to be parsed
     * @param charset use the charset to encoding bytes, directly cast byte to char if charset == null
     * @return string parse from bytes. if the length of string is 0, then return null
     */
    public static String parseString(byte[] bytes, int begin, int length, Charset charset) throws ParseException {
        if (length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean inCDATABlock = false;
        int beginIndex4Segment = begin;
        int length4Segment;
        int lastIndex = begin + length;
        for (int i = begin; i < lastIndex; i++)
            if (bytes[i] == '<' && bytes[i + 1] == '!' && bytes[i + 2] == '[' && bytes[i + 3] == 'C'
                    && bytes[i + 4] == 'D' && bytes[i + 5] == 'A' && bytes[i + 6] == 'T'
                    && bytes[i + 7] == 'A' && bytes[i + 8] == '[') { // found "<![CDATA["
                length4Segment = i - beginIndex4Segment;
                if (length4Segment > 0) {
                    sb.append(internalParseString(bytes, beginIndex4Segment, length4Segment, charset, false));
                }
                i += 8; // skip "<![CDATA["
                beginIndex4Segment = i + 1;
                inCDATABlock = true;
            } else if (inCDATABlock && bytes[i] == ']' && bytes[i + 1] == ']' && bytes[i + 2] == '>') { // found "]]>"
                length4Segment = i - beginIndex4Segment;
                if (length4Segment > 0) {
                    sb.append(internalParseString(bytes, beginIndex4Segment, length4Segment, charset, true));
                }
                i += 2; // skip "]]"
                beginIndex4Segment = i + 1;
                inCDATABlock = false;
            } else if (i + 1 >= lastIndex) { // last byte
                if(inCDATABlock){ // commend not closed
                    throw ParseException.formatError("comment not closed correctly", 0, i + 1);
                }
                length4Segment = i + 1 - beginIndex4Segment;
                if (length4Segment > 0) {
                    sb.append(internalParseString(bytes, beginIndex4Segment, length4Segment, charset, false));
                }
                break;
            }
        if (sb.length() == 0) {
            return null;
        } else {
            return sb.toString();
        }
    }

    /**
     * parse bytes to string with any leading and trailing whitespace removed, with any "<![CDATA[" and "]]>" removed, and without any encoding, cast byte to char directly
     *
     * @param bytes  the byte array
     * @param begin  the beginning index, inclusive.
     * @param length the length of bytes need to be parsed
     * @return string parse from bytes. if the length of string is 0, then return null
     */
    public static String parseTrimString(byte[] bytes, int begin, int length) throws ParseException {
        return parseTrimString(bytes, begin, length, null);
    }

    /**
     * parse bytes to string with any leading and trailing whitespace removed, with any "<![CDATA[" and "]]>" removed
     *
     * @param bytes   the byte array
     * @param begin   the beginning index, inclusive.
     * @param length  the length of bytes need to be parsed
     * @param charset use the charset to encoding bytes, directly cast byte to char if charset == null
     * @return string parse from bytes. if the length of string is 0, then return null
     */
    public static String parseTrimString(byte[] bytes, int begin, int length, Charset charset) throws ParseException {
        if (length == 0) {
            return null;
        }

        int last = begin + length;
        int newBegin = begin;
        int newLength;
        boolean allWhiteSpace = true; // to check whether all chars are ' ' or '\t'
        for (int i = begin; i < last; i++) { // forward to find a valid char
            if (bytes[i] != ' ' && bytes[i] != '\t') {
                allWhiteSpace = false;
                newBegin = i;
                if (i + 1 == last) { // newLength == 1
                    newLength = 1;
                    return parseString(bytes, newBegin, newLength, charset);
                }
                break;
            }
        }
        if (allWhiteSpace) { // all chars are ' ' or '\t'
            return null;
        }
        for (int j = last - 1; j >= newBegin; j--) { // backward to find a valid char
            if (bytes[j] != ' ' && bytes[j] != '\t') {
                newLength = j - newBegin + 1;
                return parseString(bytes, newBegin, newLength, charset);
            }
        }
        return null;
    }

    /**
     * parse bytes to string with processing entity references, the bytes should not contain CDATA block.
     *
     * @param bytes   the byte array
     * @param begin   the beginning index, inclusive.
     * @param length  the length of bytes need to be parsed
     * @param charset use the charset to encoding bytes, directly cast byte to char if charset == null
     * @return string parse from bytes. if the length of string is 0, then return null
     */
    private static String internalParseString(byte[] bytes, int begin, int length, Charset charset, boolean isCDATA) throws ParseException {
        if (isCDATA) {
            if (charset != null) { // need encoding
                return new String(bytes, begin, length, charset);
            } else { // no need encoding
                StringBuilder sb = new StringBuilder(length);
                for (int i = 0; i < length; i++) {
                    sb.append((char) bytes[i + begin]);
                }
                return sb.toString();
            }
        } else {
            if (charset != null) { // need encoding
                return parseEntityReference4String(new String(bytes, begin, length, charset));
            } else { // no need encoding
                return parseEntityReference4Bytes(bytes, begin, length);
            }
        }
    }

    /**
     * parse XML Entity References: https://msdn.microsoft.com/en-us/library/windows/desktop/dd892769(v=vs.85).aspx
     * @param str input string
     * @return string with replacing entity reference to the single specific character
     * @throws ParseException
     */
    private static String parseEntityReference4String(String str) throws ParseException {
        int length = str.length();
        if (length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            if (ch == '&') { // maybe an entity
                i++;
                int val = 0;
                switch (str.charAt(i)) {
                    case '#':
                        i++;
                        if (str.charAt(i) == 'x') { // Hexadecimal reference
                            for (; i < length; i++) {
                                ch = str.charAt(i);
                                if (ch >= '0' && ch <= '9') {
                                    val = (val << 4) + (ch - '0');
                                } else if (ch >= 'a' && ch <= 'f') {
                                    val = (val << 4) + (ch - 'a' + 10);
                                } else if (ch >= 'A' && ch <= 'F') {
                                    val = (val << 4) + (ch - 'A' + 10);
                                } else if (ch == ';') {
                                    sb.append((char) val);
                                    break;
                                } else
                                    throw ParseException.entityError("Errors in Entity: Illegal char following &#x");
                            }
                        } else { // Numeric reference
                            for (; i < length; i++) {
                                ch = str.charAt(i);
                                if (ch >= '0' && ch <= '9') {
                                    val = val * 10 + (ch - '0');
                                } else if (ch == ';') {
                                    sb.append((char) val);
                                    break;
                                } else
                                    throw ParseException.entityError("Errors in char reference: Illegal char following &#");
                            }
                        }
                    case 'a':
                        if (str.charAt(++i) == 'm') {
                            if (str.charAt(++i) == 'p' && str.charAt(++i) == ';') { // &amp; --> &
                                sb.append('&');
                            } else
                                throw ParseException.entityError("Errors in Entity: should be '&amp;' here");
                        } else if (str.charAt(++i) == 'p') {
                            if (str.charAt(++i) == 'o'
                                    && str.charAt(++i) == 's'
                                    && str.charAt(++i) == ';') { // &apos; --> '
                                sb.append('\'');
                            } else
                                throw ParseException.entityError("Errors in Entity: should be '&apos;' here");
                        } else
                            throw ParseException.entityError("Errors in Entity: Illegal builtin reference");

                    case 'q':
                        if (str.charAt(++i) == 'u'
                                && str.charAt(++i) == 'o'
                                && str.charAt(++i) == 't'
                                && str.charAt(++i) == ';') { // &quot; --> "
                            sb.append('"');
                        } else
                            throw ParseException.entityError("Errors in Entity: should be '&quot;' here");
                    case 'l':
                        if (str.charAt(++i) == 't' && str.charAt(++i) == ';') { // &lt; --> <
                            sb.append('<');
                        } else
                            throw ParseException.entityError("Errors in Entity: should be '&lt;' here");
                    case 'g':
                        if (str.charAt(++i) == 't' && str.charAt(++i) == ';') { // &gt; --> >
                            sb.append('>');
                        } else
                            throw ParseException.entityError("Errors in Entity: should be '&gt;' here");
                    default:
                        throw ParseException.entityError("Errors in Entity: Illegal entity char");
                }
            } else { // not an entity
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * parse XML Entity References: https://msdn.microsoft.com/en-us/library/windows/desktop/dd892769(v=vs.85).aspx
     * @param bytes the byte array
     * @param begin the beginning index, inclusive.
     * @param length the length of bytes need to be parsed
     * @return string with replacing entity reference to the single specific character
     * @throws ParseException
     */
    private static String parseEntityReference4Bytes(byte[] bytes, int begin, int length) throws ParseException {
        if (length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder(length);
        int end = begin + length;
        for (int i = begin; i < end; i++) {
            byte b = bytes[i];
            if (b == '&') { // maybe an entity
                i++;
                int val = 0;
                switch (bytes[i]) {
                    case '#':
                        i++;
                        if (bytes[i] == 'x') { // Hexadecimal reference
                            for (; i < end; i++) {
                                b = bytes[i];
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
                            for (; i < end; i++) {
                                b = bytes[i];
                                if (b >= '0' && b <= '9') {
                                    val = val * 10 + (b - '0');
                                } else if (b == ';') {
                                    sb.append((char) val);
                                    break;
                                } else
                                    throw ParseException.entityError("Errors in char reference: Illegal char following &#");
                            }
                        }
                        break;
                    case 'a':
                        if (bytes[++i] == 'm') {
                            if (bytes[++i] == 'p' && bytes[++i] == ';') { // &amp; --> &
                                sb.append('&');
                                break;
                            } else
                                throw ParseException.entityError("Errors in Entity: should be '&amp;' here");
                        } else if (bytes[++i] == 'p') { // &apos; --> '
                            if (bytes[++i] == 'o'
                                    && bytes[++i] == 's'
                                    && bytes[++i] == ';') {
                                sb.append('\'');
                                break;
                            } else
                                throw ParseException.entityError("Errors in Entity: should be '&apos;' here");
                        } else
                            throw ParseException.entityError("Errors in Entity: Illegal builtin reference");

                    case 'q':
                        if (bytes[++i] == 'u'
                                && bytes[++i] == 'o'
                                && bytes[++i] == 't'
                                && bytes[++i] == ';') { // &quot; --> "
                            sb.append('"');
                            break;
                        } else
                            throw ParseException.entityError("Errors in Entity: should be '&quot;' here");
                    case 'l':
                        if (bytes[++i] == 't' && bytes[++i] == ';') { // &lt; --> <
                            sb.append('<');
                            break;
                        } else
                            throw ParseException.entityError("Errors in Entity: should be '&lt;' here");
                    case 'g':
                        if (bytes[++i] == 't' && bytes[++i] == ';') { // &gt; --> >
                            sb.append('>');
                            break;
                        } else
                            throw ParseException.entityError("Errors in Entity: should be '&gt;' here");
                    default:
                        throw ParseException.entityError("Errors in Entity: Illegal entity char");
                }
            } else { // not an entity
                sb.append((char) b);
            }
        }
        return sb.toString();
    }


    /**
     * @param bytes  the byte array
     * @param begin  the beginning index, inclusive.
     * @param length the length of bytes need to be parsed, if length == 0, a NumberFormatException will thrown
     * @return integer number parsed from bytes
     * @throws NumberFormatException
     */
    public static int parseInt(byte[] bytes, int begin, int length) throws NumberFormatException {
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
                    throw NumberFormatException.intFormatException(ByteUtils.toString(bytes, begin, length));

                if (length == 1) // Cannot have lone "+" or "-"
                    throw NumberFormatException.intFormatException(ByteUtils.toString(bytes, begin, length));
                i++;
            }
            multmin = limit / radix;
            while (i < length) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit((char) bytes[i + begin], radix);
                i++;
                if (digit < 0) {
                    throw NumberFormatException.intFormatException(ByteUtils.toString(bytes, begin, length));
                }
                if (result < multmin) {
                    throw NumberFormatException.intFormatException(ByteUtils.toString(bytes, begin, length));
                }
                result *= radix;
                if (result < limit + digit) {
                    throw NumberFormatException.intFormatException(ByteUtils.toString(bytes, begin, length));
                }
                result -= digit;
            }
        } else {
            throw NumberFormatException.intFormatException("null");
        }
        return negative ? result : -result;
    }

    /**
     * @param bytes  the byte array
     * @param begin  the beginning index, inclusive.
     * @param length the length of bytes need to be parsed
     * @return short number parsed from bytes
     * @throws NumberFormatException
     */
    public static short parseShort(byte[] bytes, int begin, int length) throws NumberFormatException {
        return (short) parseInt(bytes, begin, length);
    }

    /**
     * @param bytes  the byte array
     * @param begin  the beginning index, inclusive.
     * @param length the length of bytes need to be parsed
     * @return long number parsed from bytes
     * @throws NumberFormatException
     */
    public static long parseLong(byte[] bytes, int begin, int length) throws NumberFormatException {
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
                    throw NumberFormatException.longFormatException(ByteUtils.toString(bytes, begin, length));

                if (length == 1) // Cannot have lone "+" or "-"
                    throw NumberFormatException.longFormatException(ByteUtils.toString(bytes, begin, length));
                i++;
            }
            multmin = limit / radix;
            while (i < length) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit((char) bytes[i + begin], radix);
                i++;
                if (digit < 0) {
                    throw NumberFormatException.longFormatException(ByteUtils.toString(bytes, begin, length));
                }
                if (result < multmin) {
                    throw NumberFormatException.longFormatException(ByteUtils.toString(bytes, begin, length));
                }
                result *= radix;
                if (result < limit + digit) {
                    throw NumberFormatException.longFormatException(ByteUtils.toString(bytes, begin, length));
                }
                result -= digit;
            }
        } else {
            throw NumberFormatException.intFormatException("null");
        }
        return negative ? result : -result;
    }

    /**
     * @param bytes  the byte array
     * @param begin  the beginning index, inclusive.
     * @param length the length of bytes need to be parsed
     * @return float number parsed from bytes
     * @throws NumberFormatException
     */
    public static float parseFloat(byte[] bytes, int begin, int length) throws NumberFormatException {
        String str = ByteUtils.toString(bytes, begin, length);
        if (str == null) {
            throw NumberFormatException.intFormatException("null");
        }
        try {
            return Float.parseFloat(str);
        } catch (java.lang.NumberFormatException e) {
            throw NumberFormatException.floatFormatException(ByteUtils.toString(bytes, begin, length));
        }
    }

    /**
     * @param bytes  the byte array
     * @param begin  the beginning index, inclusive.
     * @param length the length of bytes need to be parsed
     * @return double number parsed from bytes
     * @throws NumberFormatException
     */
    public static double parseDouble(byte[] bytes, int begin, int length) throws NumberFormatException {
        String str = ByteUtils.toString(bytes, begin, length);
        if (str == null) {
            throw NumberFormatException.intFormatException("null");
        }
        try {
            return Double.parseDouble(str);
        } catch (java.lang.NumberFormatException e) {
            throw NumberFormatException.doubleFormatException(ByteUtils.toString(bytes, begin, length));
        }
    }


}
