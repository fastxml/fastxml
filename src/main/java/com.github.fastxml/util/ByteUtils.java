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

/**
 * Created by weager on 2016/06/08.
 */
public class ByteUtils {

    /**
     * to valid a char.
     * <li>1st bit: tagName\attribute name char</li>
     * <li>2st bit: useless char, such as whitespace\tab\return\newline</li>
     */
    private final static byte[] byteType = {
            0, // 0
            0, 0, 0, 0, 0, 0, 0, 0, // 1~8
            2, // 9: '\t'
            2, // 10: '\n'
            0, 0, // 11~12
            2, // 13: '\r'
            0, 0, 0, 0, 0, 0, 0, // 14~ 20
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 21~30
            0, // 31
            2, // 32: ' '
            0, 0, 0, 0, 0, 0, 0, 0, // 33~40
            0, 0, 0, 0, // 41~44
            1, // 45: '-'
            1, // 46: '.'
            0, // 47
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 48~57: '0'~'9'
            1, // 58: ':'
            0, 0, 0, 0, 0, 0, // 59~64
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 65~90: 'A'~'Z'
            0, 0, 0, 0, // 91~94
            1, // 95: '_'
            0, // 96
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 97~122: 'a'~'z'
    };
        /*
        SAPCE 32
        TAB 9
        RETURN 13
        NEWLINE 10
         */

    /**
     * check whether the byte is a char of tag name or attribute name.
     *
     * @param b a byte to be valid
     * @return true if valid, otherwise false
     */
    public final static boolean isValidTokenChar(final byte b) {
        // to check validChars
        return b >= 0 && b <= 122 && (byteType[b] & 1) > 0;
//        return b > 0 && ((b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z') || (b >= '0' && b <= '9')
//                || b == ':' || b == '-' || b == '_' || b == '.');
    }

    /**
     * check whether the byte is ' ' or '\t' or '\r' or '\n'
     *
     * @param b a byte to be valid
     * @return
     */
    public final static boolean isWhiteSpaceOrNewLine(final byte b) {
        return b >= 0 && b <= 122 && (byteType[b] & 2) > 0;
    }

}
