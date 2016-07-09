package org.fastxml;

import org.fastxml.exception.NumberFormatException;
import org.fastxml.exception.ParseException;
import org.fastxml.util.ByteUtils;
import org.fastxml.util.ParseUtils;

import java.nio.charset.Charset;


/**
 * A simple, pull based XML parser for byte array which contain the whole document.
 * FastXml suppose the document is valid, and does not do full validation for best performance, just valid when necessary.
 * FastXml focus on xml content, DECLARE, comment and DOCTYPE will be ignored.
 * for example, no validation for end tag, the first char of tag name etc.
 * Notice:
 * <li>1. Text should not contain comments.</li>
 * <li>2. TagName should not contain white space, tab or newline</li>
 * <li>3. all attribute value should be wrapped in double quote</li>
 * <li>4. attribute name should be close to '=', and '=' should be close to '\"' </li>
 * <li>5. both tag name and attribute name only contain ascii chars.</li>
 * Created by weager on 2016/06/07.
 */
public class FastXmlParser4ByteArray extends AbstractFastXmlParser {

    private boolean inDoubleQuote; // is in double quote, attribute value may be wrapped by double quote or single quote

    /**
     * Set input bytes, and set set charset if no charset specified in document.
     *
     * @param bytes   byte array need to be parsed
     * @param charset if param charset is null, then encoding in document will be used;
     *                if both param charset and encoding in document is empty, then AbstractFastXmlParser.defaultCharset will be used
     * @throws ParseException
     */
    public void setInput(byte[] bytes, Charset charset) throws ParseException {
        if (bytes == null || bytes.length == 0) {
            throw ParseException.emptyDocument();
        }
        // init
        this.docBytes = bytes;
        this.cursor = 0;
        this.currentIndex = 0;
        this.currentBytesLength = 0;
        this.currentEvent = END_DOCUMENT;
        this.nextEvent = START_DOCUMENT;
        this.currentRow = 1;
        this.currentColumn = 1;
        this.currentDepth = 0;
        this.charset = charset;
    }

    public int next() throws ParseException {
        try {
            resetCurrent();
            currentEvent = nextEvent;
            switch (currentEvent) {
                case START_DOCUMENT:
                    nextEvent = processStartDocument();
                    break;
                case END_DOCUMENT:
                    nextEvent = -1;
                    break;
                case START_TAG:
                    currentDepth++;
                    nextEvent = processStartTag();
                    break;
                case END_TAG:
                    currentDepth--;
                    nextEvent = processEndTag();
                    break;
                case END_TAG_WITHOUT_TEXT:
                    currentDepth--;
                    nextEvent = processEndTagWithoutText();
                    break;
                case ATTRIBUTE_NAME:
                    nextEvent = processAttributeName();
                    break;
                case ATTRIBUTE_VALUE:
                    nextEvent = processAttributeValue();
                    break;
                case TEXT:
                    nextEvent = processText();
                    break;
                default:
                    throw ParseException.otherError(currentRow, currentColumn);
            }
            return currentEvent;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw ParseException.documentEndUnexpected(currentRow, currentColumn);
        }
    }

    /**
     * read the beginning bytes of document, parse the charset, and return next event
     *
     * @return next event
     * @throws ParseException
     */
    private int processStartDocument() throws ParseException {
        skipUselessChar();
        if (docBytes[cursor] == '<') {
            if (docBytes[cursor + 1] == '?'
                    && (docBytes[cursor + 2] == 'x' || docBytes[cursor + 2] == 'X')
                    && (docBytes[cursor + 3] == 'm' || docBytes[cursor + 3] == 'M')
                    && (docBytes[cursor + 4] == 'l' || docBytes[cursor + 4] == 'L')) {
                moveCursor(5);
                skipUselessChar();

                if (charset != null) {// if charset has been set, then just finish declaration.
                    for (; cursor < docBytes.length; moveCursor(1)) {
//                        System.out.print((char) docBytes[cursor]);
                        if (docBytes[cursor] == '?' && docBytes[cursor + 1] == '>') {
                            moveCursor(2);
                            return START_TAG;
                        }
                    }
                    throw ParseException.documentEndUnexpected(currentRow, currentColumn);
                } else { // charset has not been set, then find out encoding
                    for (; cursor < docBytes.length; moveCursor(1)) {
//                        System.out.print((char) docBytes[cursor]);
                        if ((docBytes[cursor] == 'e' || docBytes[cursor] == 'E')
                                && (docBytes[cursor + 1] == 'n' || docBytes[cursor + 1] == 'N')
                                && (docBytes[cursor + 2] == 'c' || docBytes[cursor + 2] == 'C')
                                && (docBytes[cursor + 3] == 'o' || docBytes[cursor + 3] == 'O')
                                && (docBytes[cursor + 4] == 'd' || docBytes[cursor + 4] == 'D')
                                && (docBytes[cursor + 5] == 'i' || docBytes[cursor + 5] == 'I')
                                && (docBytes[cursor + 6] == 'n' || docBytes[cursor + 6] == 'N')
                                && (docBytes[cursor + 7] == 'g' || docBytes[cursor + 7] == 'G')
                                && docBytes[cursor + 8] == '=' && (docBytes[cursor + 9] == '\"' || docBytes[cursor + 9] == '\'')) {
                            if(docBytes[cursor + 9] == '\"') {
                                inDoubleQuote = true;
                            }else {
                                inDoubleQuote = false;
                            }
                            moveCursor(10);
                            // parse encoding value
                            int firstByteIndex = cursor;
                            for (; cursor < docBytes.length; moveCursor(1)) {
//                                System.out.print((char) docBytes[cursor]);
                                if ((inDoubleQuote && docBytes[cursor] == '\"') || (!inDoubleQuote && docBytes[cursor] == '\'')) {
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = firstByteIndex; i < cursor; i++) {
                                        sb.append((char) docBytes[i]);
                                    }
                                    try {
                                        charset = Charset.forName(sb.toString());
                                    } catch (Exception e) {
                                        charset = DEFAULT_CHARSET;
                                    }
                                    moveCursor(1);
                                    skipUselessChar();
                                    moveCursor(-1);
                                    break;
                                }
                            }
                            if (charset == null) {
                                throw ParseException.documentEndUnexpected(currentRow, currentColumn);
                            }
                        } else if (docBytes[cursor] == '?' && docBytes[cursor + 1] == '>') {
                            moveCursor(2);
                            skipUselessChar();
                            if (charset == null) {
                                charset = DEFAULT_CHARSET;
                            }
                            if (docBytes[cursor] == '<') {
                                moveCursor(1);
                                return START_TAG;
                            } else {
                                throw ParseException.formatError("should be a <tagName here", currentRow, currentColumn);
                            }
                        }
                    }
                    throw ParseException.formatError("xml declaration is not closed correctly", currentRow, currentColumn);
                }
            } else {
                return START_TAG; // next event: start tag
            }
        } else {
            throw ParseException.formatError("document should begin with '<'", currentRow, currentColumn);
        }
    }

    /**
     * process start tag, and find out next event
     *
     * @return next event
     * @throws ParseException
     */
    private int processStartTag() throws ParseException {
        // the first char has bean validated in previous event, so just skip it.
        // to see: processAfterEndTag()
        for (; cursor < docBytes.length; moveCursor(1)) {
//            System.out.print((char) docBytes[cursor]);
            if (ByteUtils.isNotValidTokenChar(docBytes[cursor])) {
                if (docBytes[cursor] == '>') { // start tag
                    currentBytesLength = cursor - currentIndex;
                    return processAfterStartTag();
                } else {
                    int skipCharCount = skipUselessChar();
                    if (docBytes[cursor] == '/' && docBytes[cursor + 1] == '>') { // tag end immediately
                        return END_TAG_WITHOUT_TEXT;
                    } else if (skipCharCount > 0) { // found attribute name
                        currentBytesLength = cursor - skipCharCount - currentIndex;
                        return ATTRIBUTE_NAME;
                    } else {
                        throw ParseException.formatError("should be '>' or attribute here", currentRow, currentColumn);
                    }
                }
            }
        }
        throw ParseException.documentEndUnexpected(currentRow, currentColumn);
    }

    /**
     * process end tag such like "</tagName>", and find out next event
     *
     * @return next event
     * @throws ParseException
     */
    private int processEndTag() throws ParseException {
        if (docBytes[cursor] == '<' && docBytes[cursor + 1] == '/') {
            moveCursor(2);
            for (; cursor < docBytes.length; moveCursor(1)) {
//                System.out.print((char) docBytes[cursor]);
                if (docBytes[cursor] == '>') {// the tag end
                    moveCursor(1);
                    if (cursor == docBytes.length) {
                        return END_DOCUMENT;
                    } else {
                        return processAfterEndTag();
                    }
                } else if (ByteUtils.isNotValidTokenChar(docBytes[cursor])) {
                    throw ParseException.formatError("tag name should not contain invalid char", currentRow, currentColumn);
                }
            }
        }
        throw ParseException.formatError("need '</tagName' here", currentRow, currentColumn);
    }

    /**
     * process end tag without text, such like "<tagName name='aaa' />", and find out next event
     *
     * @return next event
     * @throws ParseException
     */
    private int processEndTagWithoutText() throws ParseException {
        if (docBytes[cursor] == '/' && docBytes[cursor + 1] == '>') {
            moveCursor(2);
            return processAfterEndTag();
        } else {
            throw ParseException.tagNotClosed(currentRow, currentColumn);
        }
    }

    /**
     * process bytes after a start tag, and find out next event.
     * There are three possibility for next event:
     * <li>another tag starts</li>
     * <li>this tag end</li>
     * <li>text for this tag</li>
     *
     * @return next event
     * @throws ParseException
     */
    private int processAfterStartTag() throws ParseException {
        moveCursor(1);
        int tempCurrentRow = currentRow;
        int tempCursor = cursor;
        int tempCurrentColumn = currentColumn;
        skipUselessChar();
        // continue to find out next event: text or another start tag
        if (docBytes[cursor] == '<') {
            if (ByteUtils.isValidTokenChar(docBytes[cursor + 1])) { // another start tag
                moveCursor(1);
                return START_TAG;
            } else if (docBytes[cursor + 1] == '/') { // found out end tag
                return END_TAG;
            } else {
                throw ParseException.formatError("should be </EndTagName> or <StartTagName", currentRow, currentColumn);
            }
        } else {
            // restore
            currentRow = tempCurrentRow;
            cursor = tempCursor;
            currentColumn = tempCurrentColumn;
            return TEXT;
        }
    }

    /**
     * process bytes after end tag.
     * There are two possibilities:
     * <li>another end tag</li>
     * <li>another start tag</li>
     * <li>end document</li>
     * @return next event
     * @throws ParseException
     */
    private int processAfterEndTag() throws ParseException {
        skipUselessChar();
        // continue to find out next event
        if(cursor == docBytes.length) {
            return END_DOCUMENT;
        } else if (docBytes[cursor] == '<') {
            if (docBytes[cursor + 1] == '/') { // found another end tag
                return END_TAG;
            } else if (ByteUtils.isValidTokenChar(docBytes[cursor + 1])) { // found a start tag
                moveCursor(1);
                return START_TAG;
            } else {
                throw ParseException.formatError("need '</tagName>' or '<tagName' here", currentRow, currentColumn);
            }
        } else {
            throw ParseException.formatError("need a start tag or end document here", currentRow, currentColumn);
        }
    }

    /**
     * process attribute name, and find out next event
     *
     * @return next event
     * @throws ParseException
     */
    private int processAttributeName() throws ParseException {
        for (; cursor < docBytes.length; moveCursor(1)) {// read tag bytes
//            System.out.print((char) docBytes[cursor]);
            if (ByteUtils.isNotValidTokenChar(docBytes[cursor])) {// this attribute name end
                currentBytesLength = cursor - currentIndex;
                // read "=\"", '\'' should be ok, but we suppose all developers only use double quote
                if (docBytes[cursor] == '=' && cursor + 1 < docBytes.length && (docBytes[cursor + 1] == '\"' || docBytes[cursor + 1] == '\'')) {// TODO:
                    if(docBytes[cursor + 1] == '\"'){
                        inDoubleQuote = true;
                    }else{
                        inDoubleQuote = false;
                    }
                    moveCursor(2);
                    return ATTRIBUTE_VALUE; // found attribute value
                } else {
                    throw ParseException.formatError("need '=\"' here", currentRow, currentColumn);
                }
            } else if (docBytes[cursor] == '\n') {
                newLine();
                throw ParseException.formatError("unexpected new line", currentRow, currentColumn);
            }
        }
        throw ParseException.documentEndUnexpected(currentRow, currentColumn);
    }

    /**
     * process attribute value, and find out next event
     *
     * @return next event
     * @throws ParseException
     */
    private int processAttributeValue() throws ParseException {
        for (; cursor < docBytes.length; moveCursor(1)) {
//            System.out.print((char) docBytes[cursor]);
            if ((inDoubleQuote && docBytes[cursor] == '\"') || (!inDoubleQuote && docBytes[cursor] == '\'')) {// found another quotation, it's the end of attribute value
                currentBytesLength = cursor - currentIndex; // length of attribute value
                moveCursor(1);
                // continue to read byte until find next event
                skipUselessChar();
                if (ByteUtils.isValidTokenChar(docBytes[cursor])) {// next attributeName
                    return ATTRIBUTE_NAME;
                } else if (docBytes[cursor] == '>') { // the start tag
                    return processAfterStartTag();
                } else if (docBytes[cursor] == '/' && cursor + 1 < docBytes.length && docBytes[cursor + 1] == '>') {// found end tag
                    return END_TAG_WITHOUT_TEXT;
                } else {
                    throw ParseException.formatError("should be space or '>' or '/>' or another attribute here", currentRow, currentColumn);
                }
            } else if (docBytes[cursor] == '\n') {
                newLine();
            }
        }
        throw ParseException.formatError("need another quotation", currentRow, currentColumn);
    }

    /**
     * Text wrapped by a pair of tag.
     * this method will not get rid of CDATA block, because it will break the order of docBytes.
     * The exact text will be extract in get method of IConverter, such as getString(),getInt()
     *
     * @return next event
     * @throws ParseException
     */
    private int processText() throws ParseException {
        boolean inCDATA = false;
        for (; cursor < docBytes.length; moveCursor(1)) {
//            System.out.print((char) docBytes[cursor]);
            if (docBytes[cursor] == '\n') {
                newLine();
            }
            if (inCDATA) { // in CDATA block, then find out "]]"
                if (docBytes[cursor] == ']' && docBytes[cursor + 1] == ']') {
                    moveCursor(2);
                    inCDATA = false;
                }
            } else { // not in CDATA block
                if (docBytes[cursor] == '<') {
                    if (docBytes[cursor + 1] == '!' && docBytes[cursor + 2] == '['
                            && docBytes[cursor + 3] == 'C' && docBytes[cursor + 4] == 'D'
                            && docBytes[cursor + 5] == 'A' && docBytes[cursor + 6] == 'T'
                            && docBytes[cursor + 7] == 'A' && docBytes[cursor + 8] == '[') { // CDATA block
                        moveCursor(8);
                        inCDATA = true;
                    } else {
                        currentBytesLength = cursor - currentIndex;
                        return END_TAG;
                    }
                }
            }
        }
        throw ParseException.documentEndUnexpected(currentRow, currentColumn);
    }

    /**
     * skip useless char, such as ' ', '\t', '\n', '\r', comment, DOCTYPE
     *
     * @return count of useless chars
     * @throws ParseException
     */
    private int skipUselessChar() throws ParseException {
        int beginIndex = cursor;
        boolean inCommentBlock = false;

        // DOCTYPE define in external file:
        // <!DOCTYPE customer SYSTEM "http://www.myserver.com/xml/custemer.dtd">
        //
        // DOCTYPE define in doc:
        // <!DOCTYPE message [
        //      <!ELEMENT message (header, body, (signature | footer))>
        //      <!ElEMENT header (data, from, to+, subject, banner?)>
        //      ......
        // ]>
        boolean docTypeDefineInDoc = false;
        boolean inDocTypeBlock = false;

        for (; cursor < docBytes.length; moveCursor(1)) {
//            System.out.print((char) docBytes[cursor]);
            if (docBytes[cursor] == ' ' || docBytes[cursor] == '\t' || docBytes[cursor] == '\r') {
                continue;
            } else if (docBytes[cursor] == '\n') {
                newLine();
                continue;
            } else if (inCommentBlock || inDocTypeBlock) {
                if (docBytes[cursor] == '-' && cursor + 2 < docBytes.length && docBytes[cursor + 1] == '-' && docBytes[cursor + 2] == '>') { // comment end
                    moveCursor(2); // jump 2 step to finish comment
                    inCommentBlock = false;
                } else if ((docBytes[cursor] == '>' && !docTypeDefineInDoc)
                        || (docBytes[cursor] == '>' && docBytes[cursor - 1] == ']' && docTypeDefineInDoc)) { // DOCTYPE block end
                    inDocTypeBlock = false;
                }
                continue;
            } else if (docBytes[cursor] == '<' && cursor + 3 < docBytes.length
                    && docBytes[cursor + 1] == '!' && docBytes[cursor + 2] == '-' && docBytes[cursor + 3] == '-') { // found comment
                inCommentBlock = true;
                moveCursor(3);
                continue;
            } else if (docBytes[cursor] == '<' && cursor + 8 < docBytes.length
                    && docBytes[cursor + 1] == '!' && docBytes[cursor + 2] == 'D'
                    && docBytes[cursor + 3] == 'O' && docBytes[cursor + 4] == 'C'
                    && docBytes[cursor + 5] == 'T' && docBytes[cursor + 6] == 'Y'
                    && docBytes[cursor + 7] == 'P' && docBytes[cursor + 8] == 'E') { // found DOCTYPE block
                inDocTypeBlock = true;
                moveCursor(8);
                continue;
            } else {
                if (inDocTypeBlock && docBytes[cursor] == '[') {
                    docTypeDefineInDoc = true;
                    continue;
                } else {
                    break;
                }
            }
        }
        if (inCommentBlock) { // comment does close
            throw ParseException.formatError("comment should be closed", currentRow, currentColumn);
        }
        if (inDocTypeBlock) { // comment does close
            throw ParseException.formatError("DOCTYPE should be closed", currentRow, currentColumn);
        }
        int skipCharCount = cursor - beginIndex;
        return skipCharCount;
    }

    /**
     * Skip current tag and its descendants。
     * This method should be called after next()==START_TAG.
     */
    public void skipCurrentTag() throws ParseException {
        int event; // temp
        int tempDepth = currentDepth - 1; // the depth before this tag
        for (; ; ) {
            event = next();
            if (event == END_TAG && currentDepth == tempDepth) { // this tag and its descendants is skipped
                return;
            }
        }
    }

    /**
     * reset currentRow and currentColumn
     */
    private void newLine() {
        currentRow++;
        currentColumn = 0;
    }

    /**
     * reset currentIndex and currentBytesLength when traverse to another element
     */
    private void resetCurrent() {
        currentIndex = cursor;
        currentBytesLength = 0;
    }

    private void moveCursor(int count) {
        cursor += count;
        currentColumn += count;
    }

    public Charset getEncode() {
        return charset;
    }

    public int getDepth() {
        return currentDepth;
    }

    public int getRow() {
        return currentRow;
    }

    public int getColumn() {
        return currentColumn;
    }

    public boolean isMatch(byte[] expectBytes) {
        return expectBytes.length == currentBytesLength && ByteUtils.equals(expectBytes, 0, docBytes, currentIndex, currentBytesLength);
    }

    public byte[] getRawBytes() {
        byte[] bytes = new byte[currentBytesLength];
        if (currentBytesLength > 0) {
            System.arraycopy(docBytes, currentIndex, bytes, 0, currentBytesLength);
        }
        return bytes;
    }

    public String getString() {
        return ParseUtils.parseString(docBytes, currentIndex, currentBytesLength);
    }

    public String getString(boolean needDecode) {
        return ParseUtils.parseString(docBytes, currentIndex, currentBytesLength, charset);
    }

    public short getShort() throws NumberFormatException {
        return ParseUtils.parseShort(docBytes, currentIndex, currentBytesLength, charset);
    }

    public int getInt() throws NumberFormatException {
        return ParseUtils.parseInt(docBytes, currentIndex, currentBytesLength, charset);
    }

    public long getLong() throws NumberFormatException {
        return ParseUtils.parseLong(docBytes, currentIndex, currentBytesLength, charset);
    }

    public float getFloat() throws NumberFormatException {
        return ParseUtils.parseFloat(docBytes, currentIndex, currentBytesLength, charset);
    }

    public double getDouble() throws NumberFormatException {
        return ParseUtils.parseDouble(docBytes, currentIndex, currentBytesLength, charset);
    }
}
