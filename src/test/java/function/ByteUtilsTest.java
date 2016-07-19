package function;

import com.github.fastxml.util.ByteUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by weager on 2016/07/19.
 */
public class ByteUtilsTest {
    @Test
    public void testByteType() {
        Assert.assertEquals(true, ByteUtils.isValidTokenChar((byte) 'a'));
        Assert.assertEquals(true, ByteUtils.isValidTokenChar((byte) 'z'));
        Assert.assertEquals(true, ByteUtils.isValidTokenChar((byte) 'A'));
        Assert.assertEquals(true, ByteUtils.isValidTokenChar((byte) 'Z'));
        Assert.assertEquals(true, ByteUtils.isValidTokenChar((byte) '0'));
        Assert.assertEquals(true, ByteUtils.isValidTokenChar((byte) '9'));
        Assert.assertEquals(true, ByteUtils.isValidTokenChar((byte) '.'));
        Assert.assertEquals(true, ByteUtils.isValidTokenChar((byte) '-'));
        Assert.assertEquals(true, ByteUtils.isValidTokenChar((byte) '_'));
        Assert.assertEquals(true, ByteUtils.isValidTokenChar((byte) ':'));
        Assert.assertEquals(false, ByteUtils.isValidTokenChar((byte) ('a' - 1)));

        Assert.assertEquals(true, ByteUtils.isWhiteSpaceOrNewLine((byte) ' '));
        Assert.assertEquals(true, ByteUtils.isWhiteSpaceOrNewLine((byte) '\t'));
        Assert.assertEquals(true, ByteUtils.isWhiteSpaceOrNewLine((byte) '\r'));
        Assert.assertEquals(true, ByteUtils.isWhiteSpaceOrNewLine((byte) '\n'));
    }
}
