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

import junit.framework.TestCase;
import org.fastxml.exception.NumberFormatException;
import org.fastxml.exception.ParseException;
import org.fastxml.util.ParseUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Created by weager on 2016/07/06.
 */
public class ParseUtilsTest {
    @Test
    public void testParseString() throws UnsupportedEncodingException, UnsupportedCharsetException, ParseException {

        // CDATA block is the segment of a string
        String testString = "aaa<![CDATA[bbb]]>ccc";
        byte[] testBytes = testString.getBytes("utf-8");
        String expectString = "aaabbbccc";
        Assert.assertEquals(expectString, ParseUtils.parseString(testBytes, 0, testBytes.length, Charset.forName("utf-8"), false));
        Assert.assertEquals(expectString, ParseUtils.parseString(testBytes, 0, testBytes.length, false));

        // CDATA contains the text content
        testString = "<![CDATA[这里是CDATA block]]>";
        testBytes = testString.getBytes("utf-8");
        expectString = "这里是CDATA block";
        Assert.assertEquals(expectString, ParseUtils.parseString(testBytes, 0, testBytes.length, Charset.forName("utf-8"), false));

        // a xml tag in CDATA block
        testString = "<![CDATA[这里是<a href=\"#\">CDATA block</a>]]>";
        testBytes = testString.getBytes("utf-8");
        expectString = "这里是<a href=\"#\">CDATA block</a>";
        Assert.assertEquals(expectString, ParseUtils.parseString(testBytes, 0, testBytes.length, Charset.forName("utf-8"), false));

        // utf-8 encoding
        testString = "人们都在干嘛aaaa。<![CDATA[我怎么知道呢?]]>你问我我问谁?";
        testBytes = testString.getBytes("utf-8");
        expectString = "人们都在干嘛aaaa。我怎么知道呢?你问我我问谁?";
        Assert.assertEquals(expectString, ParseUtils.parseString(testBytes, 0, testBytes.length, Charset.forName("utf-8"), false));

        // multiple CDATA block
        testString = "人们都在干嘛aaaa。<![CDATA[我怎么知道呢?<li>test</li>]]>你问我.<![CDATA[我怎么知道呢?]]>我问谁?";
        testBytes = testString.getBytes("utf-8");
        expectString = "人们都在干嘛aaaa。我怎么知道呢?<li>test</li>你问我.我怎么知道呢?我问谁?";
        Assert.assertEquals(expectString, ParseUtils.parseString(testBytes, 0, testBytes.length, Charset.forName("utf-8"), false));
    }

    @Test
    public void testparseTrimedString() throws ParseException {
        byte[] testBytes = "  ".getBytes();
        Assert.assertEquals(null, ParseUtils.parseTrimedString(testBytes, 0, testBytes.length, false));

        testBytes = " \t 111".getBytes();
        Assert.assertEquals("111", ParseUtils.parseTrimedString(testBytes, 0, testBytes.length, false));

        testBytes = "111 \t ".getBytes();
        Assert.assertEquals("111", ParseUtils.parseTrimedString(testBytes, 0, testBytes.length, false));

        testBytes = "111".getBytes();
        Assert.assertEquals("111", ParseUtils.parseTrimedString(testBytes, 0, testBytes.length, true));

        testBytes = " \t 111 \t ".getBytes();
        Assert.assertEquals("111", ParseUtils.parseTrimedString(testBytes, 0, testBytes.length, true));
    }

    @Test
    public void testParseInt() throws NumberFormatException, ParseException {
        byte[] testBytes = "123".getBytes();
        Assert.assertEquals(123, ParseUtils.parseInt(testBytes, 0, testBytes.length));

        testBytes = "".getBytes();
        try {
            ParseUtils.parseInt(testBytes, 0, testBytes.length);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        testBytes = "asdf".getBytes();
        try {
            ParseUtils.parseInt(testBytes, 0, testBytes.length);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        testBytes = "000123".getBytes();
        try {
            int result = ParseUtils.parseInt(testBytes, 0, testBytes.length);
            Assert.assertEquals(123, result);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testParseLong() throws NumberFormatException, ParseException {
        byte[] testBytes = "123".getBytes();
        Assert.assertEquals(123l, ParseUtils.parseLong(testBytes, 0, testBytes.length));

        testBytes = "".getBytes();
        try {
            ParseUtils.parseLong(testBytes, 0, testBytes.length);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        testBytes = "asdf".getBytes();
        try {
            ParseUtils.parseLong(testBytes, 0, testBytes.length);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        testBytes = "000123".getBytes();
        try {
            long result = ParseUtils.parseLong(testBytes, 0, testBytes.length);
            Assert.assertEquals(123l, result);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testEntityReference() throws ParseException {
        byte[] testBytes = "&amp;c&#244;t&#233;".getBytes();
        Charset charset = Charset.forName("utf-8");
        Assert.assertEquals("&côté", ParseUtils.parseString(testBytes, 0, testBytes.length, true));
        Assert.assertEquals("&côté", ParseUtils.parseString(testBytes, 0, testBytes.length, charset, true));

        testBytes = "-&amp;c&#244;t&#233;<![CDATA[ &amp;c&#244;t&#233; ]]>&amp;c&#244;t&#233;".getBytes();
        Assert.assertEquals("-&côté &amp;c&#244;t&#233; &côté", ParseUtils.parseString(testBytes, 0, testBytes.length, true));
        Assert.assertEquals("-&côté &amp;c&#244;t&#233; &côté", ParseUtils.parseString(testBytes, 0, testBytes.length, charset, true));
    }
}
