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
package function;

import junit.framework.TestCase;
import org.fastxml.util.ParseUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Created by weager on 2016/07/06.
 */
public class ParseUtilsTest extends TestCase {
    @Test
    public void testParseString() throws UnsupportedEncodingException, UnsupportedCharsetException {

        String testString = "aaa<![CDATA[bbb]]>ccc";
        byte[] testBytes = testString.getBytes("utf-8");
        String expectString = "aaabbbccc";
        Assert.assertEquals(expectString, ParseUtils.parseString(testBytes, 0, testBytes.length, Charset.forName("utf-8")));
        Assert.assertEquals(expectString, ParseUtils.parseString(testBytes, 0, testBytes.length, null));


        testString = "人们都在干嘛aaaa。<![CDATA[我怎么知道呢?]]>你问我我问谁?";
        testBytes = testString.getBytes("utf-8");
        expectString = "人们都在干嘛aaaa。我怎么知道呢?你问我我问谁?";
        Assert.assertEquals(expectString, ParseUtils.parseString(testBytes, 0, testBytes.length, Charset.forName("utf-8")));

        testString = "人们都在干嘛aaaa。<![CDATA[我怎么知道呢?<li>test</li>]]>你问我.<![CDATA[我怎么知道呢?]]>我问谁?";
        testBytes = testString.getBytes("utf-8");
        expectString = "人们都在干嘛aaaa。我怎么知道呢?<li>test</li>你问我.我怎么知道呢?我问谁?";
        Assert.assertEquals(expectString, ParseUtils.parseString(testBytes, 0, testBytes.length, Charset.forName("utf-8")));
    }
}
