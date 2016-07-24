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

import com.github.fastxml.exception.NumberFormatException;
import com.github.fastxml.exception.ParseException;
import com.github.fastxml.util.ParseUtils;
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
        Assert.assertEquals(expectString, ParseUtils.parseStringWithDecoding(testBytes, 0, testBytes.length, Charset.forName("utf-8")));
        Assert.assertEquals(expectString, ParseUtils.parseString(testBytes, 0, testBytes.length));

        // CDATA contains the text content
        testString = "<![CDATA[这里是CDATA block]]>";
        testBytes = testString.getBytes("utf-8");
        expectString = "这里是CDATA block";
        Assert.assertEquals(expectString, ParseUtils.parseStringWithDecoding(testBytes, 0, testBytes.length, Charset.forName("utf-8")));

        // a xml tag in CDATA block
        testString = "<![CDATA[这里是<a href=\"#\">CDATA block</a>]]>";
        testBytes = testString.getBytes("utf-8");
        expectString = "这里是<a href=\"#\">CDATA block</a>";
        Assert.assertEquals(expectString, ParseUtils.parseStringWithDecoding(testBytes, 0, testBytes.length, Charset.forName("utf-8")));

        // utf-8 encoding
        testString = "人们都在干嘛aaaa。<![CDATA[我怎么知道呢?]]>你问我我问谁?";
        testBytes = testString.getBytes("utf-8");
        expectString = "人们都在干嘛aaaa。我怎么知道呢?你问我我问谁?";
        Assert.assertEquals(expectString, ParseUtils.parseStringWithDecoding(testBytes, 0, testBytes.length, Charset.forName("utf-8")));

        // multiple CDATA block
        testString = "人们都在干嘛aaaa。<![CDATA[我怎么知道呢?<li>test</li>]]>你问我.<![CDATA[我怎么知道呢?]]>我问谁?";
        testBytes = testString.getBytes("utf-8");
        expectString = "人们都在干嘛aaaa。我怎么知道呢?<li>test</li>你问我.我怎么知道呢?我问谁?";
        Assert.assertEquals(expectString, ParseUtils.parseStringWithDecoding(testBytes, 0, testBytes.length, Charset.forName("utf-8")));
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
        Assert.assertEquals("&côté", ParseUtils.parseStringWithDecoding(testBytes, 0, testBytes.length, charset));
        Assert.assertEquals("&côté", ParseUtils.parseString(testBytes, 0, testBytes.length));

        testBytes = "-&amp;c&#244;t&#233;<![CDATA[ &amp;c&#244;t&#233; ]]>&amp;c&#244;t&#233;".getBytes();
        Assert.assertEquals("-&côté &amp;c&#244;t&#233; &côté", ParseUtils.parseStringWithDecoding(testBytes, 0, testBytes.length, charset));
        Assert.assertEquals("-&côté &amp;c&#244;t&#233; &côté", ParseUtils.parseString(testBytes, 0, testBytes.length));
    }
}
