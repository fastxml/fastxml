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
package com.github.fastxml;

import java.nio.charset.Charset;

/**
 * Created by weager on 2016/06/07.
 */
public abstract class AbstractFastXmlParser implements FastXmlParser {
    /**
     * when use API: setInput(byte[] bytes), save whole xml file bytes.
     * when use API: setInput(InputStream in), temporarily save some bytes from input stream, and this docBytes will be used as a ring byte list
     */
    protected byte[] docBytes;

    /**
     * offset of document bytes, when traverse document bytes
     */
    protected int cursor;

    /**
     * current index of document bytes, it always is the start index of current token bytes
     */
    protected int currentIndex;

    /**
     * the length of the current token bytes which may be a tag or a attribute name or attribute value or a text,
     */
    protected int currentBytesLength;

    /**
     * mark current attribute value in double quotes when currentEvent==ATTRIBUTE_VALUE,
     * attribute value may be wrapped by double quotes or single quotes
     */
    protected boolean currentInDoubleQuote;

    /**
     * mark current token contain entity reference
     */
    protected boolean currentHasEntityReference;

    /**
     * current event that has already checked
     */
    protected int currentEvent;

    /**
     * next event that will be checked
     */
    protected int nextEvent;

    /**
     * current depth, begin from
     */
    protected int currentDepth;

    /**
     * byte length of the document
     */
    protected int docBytesLength;

    /**
     * the charset parsed from the start of document
     */
    protected Charset charset;
    protected final static Charset DEFAULT_CHARSET = Charset.defaultCharset();

    public byte[] getDocument() {
        return this.docBytes;
    }

    public final int getCursor() {
        return this.cursor;
    }

    public final int getCurrentEvent() {
        return this.currentEvent;
    }

    public final int getNextEvent() {
        return this.nextEvent;
    }

    public Charset getEncode() {
        return charset;
    }

    public int getDepth() {
        return currentDepth;
    }
}
