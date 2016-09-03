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
package com.github.fastxml;

import com.github.fastxml.exception.ParseException;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by weager on 2016/06/07.
 */
public class FastXmlFactory {

    /**
     * create FastXmlParser with charset specified in document header
     * @param docBytes document bytes
     * @return FastXmlParser instance
     * @throws ParseException
     */
    public static FastXmlParser newInstance(byte[] docBytes) throws ParseException {
        return newInstance(docBytes, null);
    }

    /**
     * create FastXmlParser with charset
     * @param docBytes document bytes
     * @param charset if null, charset specified in document header will be used
     * @return FastXmlParser instance
     * @throws ParseException
     */
    public static FastXmlParser newInstance(byte[] docBytes, Charset charset) throws ParseException {
        FastXmlParser4ByteArray parser = new FastXmlParser4ByteArray();
        parser.setInput(docBytes, charset);
        return parser;
    }

    /**
     * create FastXmlParser for input stream
     * @param is input stream
     * @return
     * @throws ParseException
     */
    public static FastXmlParser newInstance(InputStream is) throws ParseException {
        return newInstance(is, FastXmlParser4InputStream.DEFAULT_BUFFER_SIZE, null);
    }

    /**
     * create FastXmlParser with charset
     * @param is input stream
     * @param charset if null, charset specified in document header will be used
     * @return
     * @throws ParseException
     */
    public static FastXmlParser newInstance(InputStream is, Charset charset) throws ParseException {
        return newInstance(is, FastXmlParser4InputStream.DEFAULT_BUFFER_SIZE, charset);
    }

    /**
     * create FastXmlParser with charset and buffer size
     * @param is input stream
     * @param bufferSize buffer size
     * @return
     * @throws ParseException
     */
    public static FastXmlParser newInstance(InputStream is, int bufferSize) throws ParseException {
        return newInstance(is, bufferSize);
    }

    /**
     * create FastXmlParser with charset and buffer size
     * @param is input stream
     * @param bufferSize buffer size
     * @param charset if null, charset specified in document header will be used
     * @return
     * @throws ParseException
     */
    public static FastXmlParser newInstance(InputStream is, int bufferSize, Charset charset) throws ParseException {
        FastXmlParser4InputStream parser = new FastXmlParser4InputStream();
        parser.setInput(is, bufferSize, charset);
        return parser;
    }

}
