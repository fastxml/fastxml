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

        String testString = "aaa<![CDATA[bbb]]ccc";
        byte[] testBytes = testString.getBytes("utf-8");
        String expectString = "aaabbbccc";
        Assert.assertEquals(expectString, ParseUtils.parseString(testBytes, 0, testBytes.length, Charset.forName("utf-8")));
        Assert.assertEquals(expectString, ParseUtils.parseString(testBytes, 0, testBytes.length, null));


        testString = "人们都在干嘛aaaa。<![CDATA[我怎么知道呢?]]你问我我问谁?";
        testBytes = testString.getBytes("utf-8");
        expectString = "人们都在干嘛aaaa。我怎么知道呢?你问我我问谁?";
        Assert.assertEquals(expectString, ParseUtils.parseString(testBytes, 0, testBytes.length, Charset.forName("utf-8")));

        testString = "人们都在干嘛aaaa。<![CDATA[我怎么知道呢?<li>test</li>]]你问我.<![CDATA[我怎么知道呢?]]我问谁?";
        testBytes = testString.getBytes("utf-8");
        expectString = "人们都在干嘛aaaa。我怎么知道呢?<li>test</li>你问我.我怎么知道呢?我问谁?";
        Assert.assertEquals(expectString, ParseUtils.parseString(testBytes, 0, testBytes.length, Charset.forName("utf-8")));
    }
}
