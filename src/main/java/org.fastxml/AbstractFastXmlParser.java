package org.fastxml;

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
     * current event that has already checked
     */
    protected int currentEvent;

    /**
     * next event that will be checked
     */
    protected int nextEvent;

    /**
     * current row No, begin from one
     */
    protected int currentRow;

    /**
     * current column No, begin from one
     */
    protected int currentColumn;

    /**
     * current depth, begin from
     */
    protected int currentDepth;

    /**
     * the charset parsed from the start of document
     */
    protected Charset charset;
    protected final static Charset DEFAULT_CHARSET = Charset.defaultCharset();

    public int current() {
        return this.currentEvent;
    }
}
