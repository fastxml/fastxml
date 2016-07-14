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
import org.fastxml.util.ByteUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;


/**
 * A simple, pull based XML parser for input stream.
 * this parser will reuse a ring byte array as a buffer,
 * so it's no need to create any byte array while read bytes from input stream.
 * Created by weager on 2016/06/07.
 */
public class FastXmlParser4InputStream extends AbstractFastXmlParser {

    /**
     * set input stream and set ring buffer size
     *
     * @param in
     * @param bufferSize bufferSize should be bigger than the length of longest token(such as: text, value etc.)
     * @throws ParseException
     */
    public void setInput(InputStream in, int bufferSize) throws ParseException {
        int available = 0;
        try {
            available = in.available();
        } catch (IOException e) {
            available = 0;
        }
        if (available == 0) {
            throw ParseException.emptyDocument();
        }

        // init
        int ringBufferSize = bufferSize > 0 ? bufferSize : 8 * 1024;
        this.docBytes = new byte[ringBufferSize];
        this.currentIndex = 0;
        this.currentBytesLength = 0;
    }

    public int next() {
        // document start

        // document end

        // tag start

        // tag end

        // attribute name

        // attribute value

        // text
        return 0;
    }


    public void skipCurrentTag() {

    }

    public Charset getEncode() {
        return null;
    }

    public int getDepth() {
        return 0;
    }

    public int getRow() {
        return 0;
    }

    public int getColumn() {
        return 0;
    }

    public boolean isMatch(byte[] expectBytes) {
        if (expectBytes.length != currentBytesLength) {
            return false;
        }
        return ByteUtils.equals(expectBytes, 0, docBytes, currentIndex, currentBytesLength);
    }

    public byte[] getRawBytes() {
        byte[] bytes = new byte[currentBytesLength];
        if (currentBytesLength > 0) {
            System.arraycopy(docBytes, currentIndex, bytes, 0, currentBytesLength);
        }
        return bytes;
    }

    public String getString() {

        return null;
    }

    public String getString(boolean needDecode) {
//        if()
        return null;
    }

    public short getShort() throws NumberFormatException {
        return 0;
    }

    public int getInt() throws NumberFormatException {
        return 0;
    }

    public float getFloat() throws NumberFormatException {
        return 0;
    }

    public double getDouble() throws NumberFormatException {
        return 0;
    }

    public long getLong() throws NumberFormatException {
        return 0;
    }
}
