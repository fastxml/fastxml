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
