/**
 Copyright 2016 FastXml author(https://github.com/fastxml/fastxml)

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.fastxml;

import org.fastxml.exception.ParseException;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by weager on 2016/06/07.
 */
public class FastXmlFactory {

    /**
     * to create fast xml parser according to the variate parameters, without locating any memory,
     * so you can call this method any times as you want.
     *
     * @param objs Usage: <br>
     *             <li>FastXmlFactory.newInstance(InputStream in, int bufferSize) *not support yet*</li>
     *             <li>FastXmlFactory.newInstance(byte[] byteArray)</li>
     *             <li>FastXmlFactory.newInstance(byte[] byteArray, Charset charset)</li>
     * @return
     * @throws ParseException
     */
    public static FastXmlParser newInstance(Object... objs) throws ParseException {
        if (objs == null) {
            return null;
        }
        if (objs.length == 2 && objs[0] instanceof InputStream && objs[1] instanceof Integer) {
            FastXmlParser4InputStream parser = new FastXmlParser4InputStream();
            parser.setInput((InputStream) objs[0], (Integer) objs[1]);
            return parser;
        } else if (objs.length >= 1 && objs[0] instanceof byte[]) {
            FastXmlParser4ByteArray parser = new FastXmlParser4ByteArray();
            if (objs.length > 1 && objs[1] instanceof Charset) {
                parser.setInput((byte[]) objs[0], (Charset) objs[1]);
            } else {
                parser.setInput((byte[]) objs[0], null);
            }

            return parser;
        } else {
            return null;
        }
    }

}
