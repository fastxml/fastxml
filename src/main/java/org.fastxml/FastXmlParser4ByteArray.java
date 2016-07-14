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
package org.fastxml;

import org.fastxml.exception.ParseException;
import org.fastxml.exception.NumberFormatException;
import org.fastxml.exception.ParseException;
import org.fastxml.util.ByteUtils;
import org.fastxml.util.ParseUtils;

import java.nio.charset.Charset;


/**
 * A simple, pull based XML parser for byte array which contain the whole document.
 * FastXml suppose the document is valid, and does not do full validation for best performance, just valid when necessary.
 * FastXml focus on xml content. DECLARE, comment and DOCTYPE will be ignored.
 * for example, no validation for end tag name and the first char of tag name, etc.
 * Notice:
 * <li>1. Text content should not contain comments.</li>
 * <li>2. TagName should not contain white space, tab or newline</li>
 * <li>3. both tag name and attribute name only contain ascii chars.</li>
 * <li>4. TODO: DTD definition should not be in the document</li>
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
            currentEvent = nextEvent;
            if (currentEvent != END_TAG_WITHOUT_TEXT) {
                resetCurrent();
            }
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
                        if (docBytes[cursor] == '?' && docBytes[cursor + 1] == '>') {
                            moveCursor(2);
                            return START_TAG;
                        }
                    }
                    throw ParseException.documentEndUnexpected(currentRow, currentColumn);
                } else { // charset has not been set, then find out encoding
                    for (; cursor < docBytes.length; moveCursor(1)) {
                        if ((docBytes[cursor] == 'e' || docBytes[cursor] == 'E')
                                && (docBytes[cursor + 1] == 'n' || docBytes[cursor + 1] == 'N')
                                && (docBytes[cursor + 2] == 'c' || docBytes[cursor + 2] == 'C')
                                && (docBytes[cursor + 3] == 'o' || docBytes[cursor + 3] == 'O')
                                && (docBytes[cursor + 4] == 'd' || docBytes[cursor + 4] == 'D')
                                && (docBytes[cursor + 5] == 'i' || docBytes[cursor + 5] == 'I')
                                && (docBytes[cursor + 6] == 'n' || docBytes[cursor + 6] == 'N')
                                && (docBytes[cursor + 7] == 'g' || docBytes[cursor + 7] == 'G')) {
                            moveCursor(8); // skip "encoding"
                            skipUselessChar();
                            if (docBytes[cursor] == '=') {
                                moveCursor(1);
                                skipUselessChar();
                                if (docBytes[cursor] == '\"' || docBytes[cursor] == '\'') {
                                    inDoubleQuote = docBytes[cursor] == '\"';
                                    moveCursor(1);
                                    currentIndex = cursor;
                                    for (; cursor < docBytes.length; moveCursor(1)) {
                                        if ((inDoubleQuote && docBytes[cursor] == '\"')
                                                || (!inDoubleQuote && docBytes[cursor] == '\'')) { // found the end quote
                                            currentBytesLength = cursor - currentIndex;
                                            String charsetString = null;
                                            try {
                                                charsetString = this.getString();
                                            } catch (ParseException e) {
                                                charsetString = null;
                                            }
                                            if (charsetString != null) {
                                                try {
                                                    charset = Charset.forName(charsetString);
                                                } catch (Exception e) {
                                                    throw ParseException.formatError("encoding is not found or charset is not correct", currentRow, currentColumn);
                                                }
                                            }
                                            if (charset == null) {
                                                throw ParseException.formatError("encoding is not found or charset is not correct", currentRow, currentColumn);
                                            }
                                            break;
                                        }
                                    }
                                } else {
                                    throw ParseException.formatError("need '\"' or '\'' here", currentRow, currentColumn);
                                }
                            } else {
                                throw ParseException.formatError("need '=' here", currentRow, currentColumn);
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
                moveCursor(1);
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
        // to see: processAfterEndTag() and processStartDocument()
        for (; cursor < docBytes.length; moveCursor(1)) {
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
            resetCurrent();
            for (; cursor < docBytes.length; moveCursor(1)) {
                if (docBytes[cursor] == '>') {// the tag end
                    currentBytesLength = cursor - currentIndex;
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
            currentBytesLength = cursor - currentIndex;
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
            } else if (docBytes[cursor + 1] == '!' && docBytes[cursor + 2] == '['
                    && docBytes[cursor + 3] == 'C' && docBytes[cursor + 4] == 'D'
                    && docBytes[cursor + 5] == 'A' && docBytes[cursor + 6] == 'T'
                    && docBytes[cursor + 7] == 'A' && docBytes[cursor + 8] == '[') {
                // restore
                currentRow = tempCurrentRow;
                cursor = tempCursor;
                currentColumn = tempCurrentColumn;
                return TEXT;
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
     *
     * @return next event
     * @throws ParseException
     */
    private int processAfterEndTag() throws ParseException {
        skipUselessChar();
        // continue to find out next event
        if (cursor == docBytes.length) {
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
            if (ByteUtils.isNotValidTokenChar(docBytes[cursor])) {// this attribute name end
                currentBytesLength = cursor - currentIndex;
                skipUselessChar(); // skip ' ' and '\t' between attribute name and '='
                // read "=\"", '\'' should be ok
                if (docBytes[cursor] == '=') {
                    moveCursor(1);
                    skipUselessChar(); // skip ' ' and '\t' between '=' and attribute value
                    if (docBytes[cursor] == '\"' || docBytes[cursor] == '\'') { // found the quotation at the beginning of attribute value
                        // check doubleQuote or singleQuote
                        inDoubleQuote = docBytes[cursor] == '\"';
                        moveCursor(1);
                        return ATTRIBUTE_VALUE; // found attribute value
                    } else {
                        throw ParseException.formatError("need '\"' or '\'' here", currentRow, currentColumn);
                    }
                } else {
                    throw ParseException.formatError("need '=' here", currentRow, currentColumn);
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
     * The exact text will be extract in getString() or getString(boolean) or getInt() or getLong() or getDouble() or getFloat() method etc.
     *
     * @return next event
     * @throws ParseException
     */
    private int processText() throws ParseException {
        boolean inCDATA = false;
        for (; cursor < docBytes.length; moveCursor(1)) {
            if (docBytes[cursor] == '\n') {
                newLine();
            }
            if (inCDATA) { // in CDATA block, then find out "]]>"
                if (docBytes[cursor] == ']' && docBytes[cursor + 1] == ']' && docBytes[cursor + 2] == '>') {
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
     * skip useless chars, such as ' ', '\t', '\n', '\r', comment, DOCTYPE
     *
     * @return count of useless chars
     * @throws ParseException
     */
    private int skipUselessChar() throws ParseException {
        int beginIndex = cursor;
        boolean inCommentBlock = false;

        for (; cursor < docBytes.length; moveCursor(1)) {
            if (docBytes[cursor] == ' ' || docBytes[cursor] == '\t' || docBytes[cursor] == '\r') { // found useless character
                // continue
            } else if (docBytes[cursor] == '\n') { // found new line
                newLine();
                // continue
            } else if (docBytes[cursor] == '<' && cursor + 3 < docBytes.length
                    && docBytes[cursor + 1] == '!' && docBytes[cursor + 2] == '-' && docBytes[cursor + 3] == '-') { // found comment
                skipComment();
                // continue
            } else if (docBytes[cursor] == '<' && cursor + 8 < docBytes.length
                    && docBytes[cursor + 1] == '!' && docBytes[cursor + 2] == 'D'
                    && docBytes[cursor + 3] == 'O' && docBytes[cursor + 4] == 'C'
                    && docBytes[cursor + 5] == 'T' && docBytes[cursor + 6] == 'Y'
                    && docBytes[cursor + 7] == 'P' && docBytes[cursor + 8] == 'E') { // found DTD DOCTYPE
                skipDocType();
                // continue
            } else {
                break;
            }
        }
        return cursor - beginIndex;
    }

    /**
     * skip DTD DOCTYPE block
     * <p>
     * DOCTYPE define in external file:
     * <!DOCTYPE customer SYSTEM "http://www.myserver.com/xml/custemer.dtd">
     * <p>
     * DOCTYPE define in doc:
     * <!DOCTYPE message [
     * <!ELEMENT message (header, body, (signature | footer))>
     * <!ElEMENT header (data, from, to+, subject, banner?)>
     * ......
     * ]>
     *
     * @throws ParseException
     */
    private void skipDocType() throws ParseException {
        moveCursor(8); // skip "<!DOCTYPE"
        boolean docTypeDefineInDoc = false;
        for (; cursor < docBytes.length; moveCursor(1)) {
            if (!docTypeDefineInDoc && docBytes[cursor] == '[') { // DTD DOCTYPE defined in document
                docTypeDefineInDoc = true;
            } else if (docTypeDefineInDoc) {
                boolean foundEndBracket = false;
                for (; cursor < docBytes.length; moveCursor(1)) {
                    if (!foundEndBracket && docBytes[cursor] == ']') {
                        foundEndBracket = true;
                    } else if (foundEndBracket && docBytes[cursor] == '>') { // doctype end
                        return;
                    }
                }
            } else if (docBytes[cursor] == '>') { // doctype end
                return;
            }
        }
        throw ParseException.formatError("DTD DOCTYPE does not closed", currentRow, currentColumn);
    }

    /**
     * skip comment block
     *
     * @throws ParseException
     */
    private void skipComment() throws ParseException {
        moveCursor(4); // skip "<!--"
        for (; cursor < docBytes.length; moveCursor(1)) {
            if (docBytes[cursor] == '-' && cursor + 2 < docBytes.length
                    && docBytes[cursor + 1] == '-' && docBytes[cursor + 2] == '>') { // comment end
                moveCursor(2); // skip "-->"
                return;
            }
        }
        throw ParseException.formatError("comment does not closed", currentRow, currentColumn);
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

    public String getString() throws ParseException {
        try {
            return ParseUtils.parseString(docBytes, currentIndex, currentBytesLength);
        } catch (ParseException e) {
            e.setRow(currentRow);
            e.setColumn(currentColumn);
            throw e;
        }
    }

    public String getString(boolean needDecode) throws ParseException {
        try {
            if (needDecode) {
                return ParseUtils.parseString(docBytes, currentIndex, currentBytesLength, charset);
            } else {
                return getString();
            }
        } catch (ParseException e) {
            e.setRow(currentRow);
            e.setColumn(currentColumn);
            throw e;
        }
    }

    public short getShort() throws NumberFormatException {
        try {
            return ParseUtils.parseShort(docBytes, currentIndex, currentBytesLength);
        } catch (NumberFormatException e) {
            try {
                e.setRawString(this.getString(true));
            } catch (ParseException ee) { // throw the real cause
                e = new NumberFormatException(ee.getMessage(), null);
            }
            e.setRow(currentRow);
            e.setColumn(currentColumn);
            throw e;
        }
    }

    public int getInt() throws NumberFormatException {
        try {
            return ParseUtils.parseInt(docBytes, currentIndex, currentBytesLength);
        } catch (NumberFormatException e) {
            try {
                e.setRawString(this.getString(true));
            } catch (ParseException ee) { // throw the real cause
                e = new NumberFormatException(ee.getMessage(), null);
            }
            e.setRow(currentRow);
            e.setColumn(currentColumn);
            throw e;
        }
    }

    public long getLong() throws NumberFormatException {
        try {
            return ParseUtils.parseLong(docBytes, currentIndex, currentBytesLength);
        } catch (NumberFormatException e) {
            try {
                e.setRawString(this.getString(true));
            } catch (ParseException ee) { // throw the real cause
                e = new NumberFormatException(ee.getMessage(), null);
            }
            e.setRow(currentRow);
            e.setColumn(currentColumn);
            throw e;
        }
    }

    public float getFloat() throws NumberFormatException {
        try {
            return ParseUtils.parseFloat(docBytes, currentIndex, currentBytesLength);
        } catch (NumberFormatException e) {
            try {
                e.setRawString(this.getString(true));
            } catch (ParseException ee) { // throw the real cause
                e = new NumberFormatException(ee.getMessage(), null);
            }
            e.setRow(currentRow);
            e.setColumn(currentColumn);
            throw e;
        }
    }

    public double getDouble() throws NumberFormatException {
        try {
            return ParseUtils.parseDouble(docBytes, currentIndex, currentBytesLength);
        } catch (NumberFormatException e) {
            try {
                e.setRawString(this.getString(true));
            } catch (ParseException ee) { // throw the real cause
                e = new NumberFormatException(ee.getMessage(), null);
            }
            e.setRow(currentRow);
            e.setColumn(currentColumn);
            throw e;
        }
    }
}
