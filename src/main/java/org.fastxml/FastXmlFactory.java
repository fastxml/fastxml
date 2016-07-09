package org.fastxml;

import org.fastxml.exception.ParseException;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by weager on 2016/06/07.
 */
public class FastXmlFactory {

    /**
     * to create fast xml parser according to the variate parameters.
     *
     * @param objs Usage: <br>
     *             <li>FastXmlParser.newInstance(getInputStream(), bufferSize)</li>
     *             <li>FastXmlParser.newInstance(byteArray)</li>
     *             <li>FastXmlParser.newInstance(byteArray, Charset.name("utf8")</li>
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
