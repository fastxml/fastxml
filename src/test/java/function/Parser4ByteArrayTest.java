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
package function;

import org.fastxml.FastXmlFactory;
import org.fastxml.FastXmlParser;
import org.fastxml.exception.NumberFormatException;
import org.fastxml.exception.ParseException;
import org.junit.Assert;
import org.junit.Test;
import util.FileLoaderUtils;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by weager on 2016/06/07.
 */
public class Parser4ByteArrayTest {
    /**
     * test xml declaration and check encoding
     *
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testXmlDeclare() throws IOException, ParseException {
        byte[] bytes = FileLoaderUtils.loadClasspathFile("test2.xml");
        FastXmlParser parser = FastXmlFactory.newInstance(bytes, null);
        if (parser.next() == FastXmlParser.START_DOCUMENT) {
            Charset charset = parser.getEncode();
            Assert.assertTrue(charset != null && charset.equals(Charset.forName("utf-8")));
        }
    }

    /**
     * test skipCurrentTag()
     */
    @Test
    public void testSkipCurrentTag() throws IOException, ParseException {
        byte[] totalBytes = FileLoaderUtils.loadClasspathFile("test2.xml");
        FastXmlParser parser = FastXmlFactory.newInstance(totalBytes, null);
        StringBuilder sb = new StringBuilder();

        int packageCount = 0;
        for (int event = parser.next(); ; event = parser.next()) {
            if (event == FastXmlParser.END_DOCUMENT) {
                Assert.assertEquals("<bix><package sex=\"male\"><id>222</id><name hasEntityReference=\"false\">weager</name></package><package><id>333</id><name/></package></bix>", sb.toString());
                return;
            }
            switch (event) {
                case FastXmlParser.START_TAG:
                    if ("package".equals(parser.getString())) {
                        packageCount++;
                        if (packageCount == 1) {
                            parser.skipCurrentTag();
                            break;
                        }
                    }
                    sb.append('<').append(parser.getString());
                    if (parser.getNextEvent() == FastXmlParser.TEXT || parser.getNextEvent() == FastXmlParser.END_TAG || parser.getNextEvent() == FastXmlParser.START_TAG) {
                        sb.append('>');
                    }
                    break;
                case FastXmlParser.END_TAG:
                    sb.append("</").append(parser.getString()).append('>');
                    break;
                case FastXmlParser.END_TAG_WITHOUT_TEXT:
                    sb.append("/>");
                    break;
                case FastXmlParser.ATTRIBUTE_NAME:
                    sb.append(' ').append(parser.getString()).append("=");
                    break;
                case FastXmlParser.ATTRIBUTE_VALUE:
                    sb.append('\"').append(parser.getString()).append('\"');
                    int nextEvent = parser.getNextEvent();
                    if (nextEvent == FastXmlParser.TEXT || nextEvent == FastXmlParser.END_TAG || nextEvent == FastXmlParser.START_TAG) {
                        sb.append('>');
                    } else if (nextEvent == FastXmlParser.END_TAG_WITHOUT_TEXT) {
                        sb.append("/>");
                    }
                    break;
                case FastXmlParser.TEXT:
                    String text = parser.getString();
                    if (text != null) {
                        sb.append(text);
                    }
                    break;
            }
        }
    }

    /**
     * parse text to number
     * @throws IOException
     * @throws ParseException
     * @throws NumberFormatException
     */
    @Test
    public void testGetNumber() throws IOException, ParseException, NumberFormatException {
        byte[] totalBytes = FileLoaderUtils.loadClasspathFile("test2.xml");
        FastXmlParser parser = FastXmlFactory.newInstance(totalBytes, null);
        byte[] str111 = "111".getBytes();
        byte[] str222 = "222".getBytes();
        for (int event = parser.next(); event != FastXmlParser.END_DOCUMENT; event = parser.next()) {
            if (event == FastXmlParser.TEXT) {
                if (parser.isMatch(str111)) {
                    Assert.assertEquals(111, parser.getInt());
                } else if (parser.isMatch(str222)) {
                    Assert.assertEquals(222l, parser.getLong());
                }
            }
        }
    }

    /**
     * parse bytes to string
     * @throws IOException
     * @throws ParseException
     * @throws NumberFormatException
     */
    @Test
    public void testGetString() throws IOException, ParseException, NumberFormatException {
        byte[] totalBytes = FileLoaderUtils.loadClasspathFile("test2.xml");
        FastXmlParser parser = FastXmlFactory.newInstance(totalBytes, null);
        byte[] name1 = "汤姆克鲁兹".getBytes();
        byte[] name2 = "weager".getBytes();
        byte[] age = "age".getBytes();
        byte[] sex = "sex".getBytes();
        byte[] hasEntityReference = "hasEntityReference".getBytes();
        byte[] TRUE = "true".getBytes();
        byte[] FALSE = "false".getBytes();

        for (int event = parser.next(); event != FastXmlParser.END_DOCUMENT; event = parser.next()) {
            if (event == FastXmlParser.TEXT) { // text content
                if (parser.isMatch(name1)) {
                    Assert.assertEquals("汤姆克鲁兹", parser.getString(true));
                } else if (parser.isMatch(name2)) {
                    Assert.assertEquals("weager", parser.getString());
                }
            }else if(event == FastXmlParser.END_TAG_WITHOUT_TEXT){ // tagName
                Assert.assertEquals("name", parser.getString());
            }
            if(parser.getNextEvent() == FastXmlParser.ATTRIBUTE_NAME){ // tagName
                String tagName = parser.getString();
                parser.next();
                if(parser.isMatch(sex)){
                    Assert.assertEquals("package", tagName);
                    parser.next();
                    Assert.assertEquals("male", parser.getString());
                }

            }
            if(parser.getCurrentEvent() == FastXmlParser.ATTRIBUTE_NAME && parser.isMatch(age)){
                parser.next();
                Assert.assertEquals(null, parser.getString());
            }
            if(parser.getCurrentEvent() == FastXmlParser.ATTRIBUTE_NAME && parser.isMatch(hasEntityReference)){
                parser.next(); // move to attribute value
                if(parser.isMatch(TRUE)) {
                    parser.next(); // move to text
                    Assert.assertEquals("  汤姆克鲁兹-&côté &amp;c&#244;t&#233;  ", parser.getString(true));
                    Assert.assertEquals("汤姆克鲁兹-&côté &amp;c&#244;t&#233;  ", parser.getTrimedString(true));
                }else if(parser.isMatch(FALSE)){
                    parser.next();
                    Assert.assertEquals("weager", parser.getString(true));
                }
            }
        }
    }

}
