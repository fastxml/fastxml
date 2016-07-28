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

import com.github.fastxml.exception.NumberFormatException;
import com.github.fastxml.exception.ParseException;
import com.github.fastxml.util.ByteUtils;
import com.github.fastxml.util.ParseUtils;

import java.nio.charset.Charset;


/**
 * A simple, pull based XML parser for byte array which contain the whole document.
 * FastXml suppose the document is valid, and does not do full validation for best performance, just valid when necessary.
 * FastXml focus on xml content. DECLARE, comment and DOCTYPE will be ignored.
 * for example, no validation for end tag name and the first char of tag name, etc.
 * Notice:
 * <li>1. Text content should not contain comments.</li>
 * <li>2. TagName should not contain white space, tab or newline</li>
 * <li>3. both tag name and attribute name only contain ascii chars: number,alphabet,'-','_',':','.'</li>
 * Created by weager on 2016/06/07.
 */
public class FastXmlParser4ByteArray extends AbstractFastXmlParser {

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
        this.currentDepth = 0;
        this.charset = charset;
        this.docBytesLength = bytes.length;
    }

    public int next() throws ParseException {
        try {
            currentEvent = nextEvent;
            currentInDoubleQuote = false;
            currentHasEntityReference = false;

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
                    throw ParseException.otherError(this);
            }
            return currentEvent;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw ParseException.documentEndUnexpected(this);
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
                    return processEndDeclaration();
                } else { // charset has not been set, then find out encoding
                    for (; cursor < docBytesLength; moveCursor(1)) {
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
                                byte currentCursor = docBytes[cursor];
                                if (currentCursor == '\"' || currentCursor == '\'') {
                                    moveCursor(1);
                                    processEncodingValue(); // parse encoding="xxx"
                                    return processEndDeclaration();
                                } else {
                                    throw ParseException.formatError("need '\"' or '\'' here", this);
                                }
                            } else {
                                throw ParseException.formatError("need '=' here", this);
                            }
                        } else if (docBytes[cursor] == '?' && docBytes[cursor + 1] == '>') {
                            moveCursor(2);
                            skipUselessChar();
                            return _processEndDeclaration();
                        }
                    }
                    throw ParseException.formatError("xml declaration should contain encoding, or specify charset on method setInput(byte[], Charset)", this);
                }
            } else { // no declaration, no specified charset, so use the default charset, next event should be START_TAG
                moveCursor(1);
                if (charset == null) {
                    charset = DEFAULT_CHARSET;
                }
                return START_TAG; // next event: start tag
            }
        } else {
            throw ParseException.formatError("document should begin with '<'", this);
        }
    }

    /**
     * process end of declaration at the beginning of the document
     *
     * @return next event
     * @throws ParseException
     */
    private int processEndDeclaration() throws ParseException {
        for (; cursor < docBytesLength; moveCursor(1)) {
            if (docBytes[cursor] == '?' && docBytes[cursor + 1] == '>') {
                moveCursor(2);
                skipUselessChar();
                return _processEndDeclaration();
            }
        }
        throw ParseException.documentEndUnexpected(this);
    }

    /**
     * process end of declaration at the beginning of the document
     *
     * @return
     * @throws ParseException
     */
    private int _processEndDeclaration() throws ParseException {
        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }
        if (docBytes[cursor] == '<') {
            moveCursor(1);
            return START_TAG;
        } else {
            throw ParseException.formatError("should be a <tagName here", this);
        }
    }

    /**
     * process encoding value
     *
     * @throws ParseException
     */
    private void processEncodingValue() throws ParseException {
        // check doubleQuote or singleQuote
        currentInDoubleQuote = docBytes[cursor - 1] == '\"';
        currentIndex = cursor;
        for (; cursor < docBytesLength; moveCursor(1)) {
            byte cursorByte = docBytes[cursor];
            if ((currentInDoubleQuote && cursorByte == '\"') || (!currentInDoubleQuote && cursorByte == '\'')) {// found another quotation, it's the end of attribute value
                currentBytesLength = cursor - currentIndex; // length of attribute value
                try {
                    charset = Charset.forName(this.getString());
                } catch (Exception e) {
                    throw ParseException.formatError("encoding is not found or charset is not correct", this);
                }
                moveCursor(1); // skip another '\'' or '\"'
                return;
            }
        }
        throw ParseException.formatError("need another quotation", this);
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
        for (; cursor < docBytesLength; moveCursor(1)) {
            if (!ByteUtils.isValidTokenChar(docBytes[cursor])) {
                if (docBytes[cursor] == '>') { // start tag
                    currentBytesLength = cursor - currentIndex;
                    moveCursor(1);
                    return processAfterStartTag();
                } else {
                    int skipCharCount = skipUselessChar();
                    // tagName should not contain whitespace
                    currentBytesLength = cursor - skipCharCount - currentIndex;
                    if (docBytes[cursor] == '/') { // tag end immediately
                        moveCursor(1);
                        return END_TAG_WITHOUT_TEXT;
                    } else if (skipCharCount > 0) { // found attribute name
                        return ATTRIBUTE_NAME;
                    } else {
                        throw ParseException.formatError("should be '/' or attribute here", this);
                    }
                }
            }
        }
        throw ParseException.documentEndUnexpected(this);
    }

    /**
     * process end tag such like "</tagName>", and find out next event
     *
     * @return next event
     * @throws ParseException
     */
    private int processEndTag() throws ParseException {
        for (; cursor < docBytesLength; moveCursor(1)) {
            if (docBytes[cursor] == '>') {// the tag end
                currentBytesLength = cursor - currentIndex;
                moveCursor(1);
                return processAfterEndTag();
            } else if (!ByteUtils.isValidTokenChar(docBytes[cursor])) {
                throw ParseException.formatError("tag name should not contain invalid char", this);
            }
        }
        throw ParseException.documentEndUnexpected(this);
    }

    /**
     * process end tag without text, such like "<tagName name='aaa' />", and find out next event
     *
     * @return next event
     * @throws ParseException
     */
    private int processEndTagWithoutText() throws ParseException {
        if (docBytes[cursor] == '>') {
            moveCursor(1);
            return processAfterEndTag();
        } else {
            throw ParseException.tagNotClosed(this);
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
        int tempCursor = cursor;
        skipUselessChar();
        // continue to find out next event: another start tag or end tag or text
        if (docBytes[cursor] == '<') {
            byte nextByte = docBytes[cursor + 1];
            if (ByteUtils.isValidTokenChar(nextByte)) { // found out another start tag
                moveCursor(1); // skip "<"
                return START_TAG;
            } else if (nextByte == '/') { // found out end tag
                moveCursor(2); // skip "</"
                return END_TAG;
            } else { // so it should be text
                // restore
                cursor = tempCursor;
                return TEXT;
            }
        } else {
            // restore
            cursor = tempCursor;
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
        // continue to find out next event: end tag or another start tag
        if (cursor == docBytesLength) {
            return END_DOCUMENT;
        } else if (docBytes[cursor] == '<') {
            if (docBytes[cursor + 1] == '/') { // found another end tag
                moveCursor(2); // skip "</"
                return END_TAG;
            } else { // found a start tag
                moveCursor(1);
                return START_TAG;
            }
        } else {
            throw ParseException.formatError("need a start tag or end document here", this);
        }
    }

    /**
     * process attribute name, and find out next event
     *
     * @return next event
     * @throws ParseException
     */
    private int processAttributeName() throws ParseException {
        moveCursor(1); // the first char has been checked in previous event, so here just skip it
        for (; cursor < docBytesLength; moveCursor(1)) {// read tag bytes
            if (!ByteUtils.isValidTokenChar(docBytes[cursor])) {// this attribute name end
                currentBytesLength = cursor - currentIndex;
                skipUselessChar(); // skip ' ' and '\t' between attribute name and '='
                // read "=\"", '\'' should be ok
                if (docBytes[cursor] == '=') {
                    moveCursor(1);
                    skipUselessChar(); // skip ' ' and '\t' between '=' and attribute value
                    if (docBytes[cursor] == '\"' || docBytes[cursor] == '\'') { // found the quotation at the beginning of attribute value
                        moveCursor(1); // move to the first byte in quotes
                        return ATTRIBUTE_VALUE; // found attribute value
                    } else {
                        throw ParseException.formatError("need '\"' or '\'' here", this);
                    }
                } else {
                    throw ParseException.formatError("need '=' here", this);
                }
            }
        }
        throw ParseException.documentEndUnexpected(this);
    }

    /**
     * process attribute value, and find out next event
     *
     * @return next event
     * @throws ParseException
     */
    private int processAttributeValue() throws ParseException {
        // check doubleQuote or singleQuote
        currentInDoubleQuote = docBytes[cursor - 1] == '\"';
        for (; cursor < docBytesLength; moveCursor(1)) {
            byte cursorByte = docBytes[cursor];
            if ((currentInDoubleQuote && cursorByte == '\"') || (!currentInDoubleQuote && cursorByte == '\'')) {// found another quotation, it's the end of attribute value
                currentBytesLength = cursor - currentIndex; // length of attribute value
                moveCursor(1);
                // continue to read byte until find next event
                skipUselessChar();
                cursorByte = docBytes[cursor];
                if (ByteUtils.isValidTokenChar(cursorByte)) {// next attributeName
                    return ATTRIBUTE_NAME;
                } else if (cursorByte == '>') { // the start tag
                    moveCursor(1);
                    return processAfterStartTag();
                } else if (cursorByte == '/') {// found end tag
                    moveCursor(1);
                    return END_TAG_WITHOUT_TEXT;
                } else {
                    throw ParseException.formatError("should be space or '>' or '/>' or another attribute here", this);
                }
            } else if (cursorByte == '&') { // attribute value contains entity reference
                currentHasEntityReference = true;
            }
        }
        throw ParseException.formatError("need another quotation", this);
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
        for (; cursor < docBytesLength; moveCursor(1)) {
            byte currentCursor = docBytes[cursor];
            if (inCDATA) { // in CDATA block, then find out "]]>"
                if (currentCursor == ']' && docBytes[cursor + 1] == ']' && docBytes[cursor + 2] == '>') {
                    moveCursor(2);
                    inCDATA = false;
                }
            } else { // not in CDATA block
                if (currentCursor == '<') {
                    byte nextByte = docBytes[cursor + 1];
                    if (nextByte == '!' && docBytes[cursor + 2] == '[' && docBytes[cursor + 3] == 'C'
                            && docBytes[cursor + 4] == 'D' && docBytes[cursor + 5] == 'A' && docBytes[cursor + 6] == 'T'
                            && docBytes[cursor + 7] == 'A' && docBytes[cursor + 8] == '[') { // found CDATA block
                        moveCursor(8);
                        inCDATA = true;
                    } else if (nextByte == '/') { // found end tag
                        currentBytesLength = cursor - currentIndex;
                        moveCursor(2); // skip "</"
                        return END_TAG;
                    }
                } else if (currentCursor == '&') { // text content contains entity reference
                    currentHasEntityReference = true;
                }
            }
        }
        throw ParseException.documentEndUnexpected(this);
    }

    /**
     * skip useless chars, such as ' ', '\t', '\n', '\r', comment, DOCTYPE
     *
     * @return count of useless chars
     * @throws ParseException
     */
    private int skipUselessChar() throws ParseException {
        int beginIndex = cursor;
        for (; cursor < docBytesLength; moveCursor(1)) {
            byte cursorByte = docBytes[cursor];
            if (ByteUtils.isWhiteSpaceOrNewLine(cursorByte)) { // found useless character: ' ','\t','\r','\n'
                // continue
            } else if (cursorByte == '<' && docBytes[cursor + 1] == '!') {
                skipOtherUselessChar();
            } else { // found valid char
                break;
            }
        }
        return cursor - beginIndex;
    }

    /**
     * skip comment and DTA DOCTYPE
     *
     * @throws ParseException
     */
    private void skipOtherUselessChar() throws ParseException {
        if (docBytes[cursor + 2] == '-' && docBytes[cursor + 3] == '-') { // found comment
            moveCursor(4); // skip "<!--"
            skipComment();
            // continue
        } else if (docBytes[cursor + 2] == 'D' && docBytes[cursor + 3] == 'O' && docBytes[cursor + 4] == 'C'
                && docBytes[cursor + 5] == 'T' && docBytes[cursor + 6] == 'Y' && docBytes[cursor + 7] == 'P'
                && docBytes[cursor + 8] == 'E') { // found DTD DOCTYPE
            moveCursor(8); // skip "<!DOCTYPE"
            skipDocType();
            // continue
        }
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
        boolean docTypeDefineInDoc = false;
        for (; cursor < docBytesLength; moveCursor(1)) {
            if (!docTypeDefineInDoc && docBytes[cursor] == '[') { // DTD DOCTYPE defined in document
                docTypeDefineInDoc = true;
            } else if (docTypeDefineInDoc) {
                boolean foundEndBracket = false;
                for (; cursor < docBytesLength; moveCursor(1)) {
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
        throw ParseException.formatError("DTD DOCTYPE does not closed", this);
    }

    /**
     * skip comment block
     *
     * @throws ParseException
     */
    private void skipComment() throws ParseException {
        for (; cursor < docBytesLength; moveCursor(1)) {
            int endIndexOfComment = cursor + 2;
            if (docBytes[cursor] == '-' && docBytes[cursor + 1] == '-' && docBytes[cursor + 2] == '>') { // comment end
                moveCursor(2); // skip "-->"
                return;
            }
        }
        throw ParseException.formatError("comment does not closed", this);
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
            if (currentDepth == tempDepth && event == END_TAG) { // this tag and its descendants is skipped
                return;
            }
        }
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
    }

    public Charset getEncode() {
        return charset;
    }

    public int getDepth() {
        return currentDepth;
    }

    public boolean isMatch(byte[] expectBytes) {
        int length = expectBytes.length;
        if (expectBytes.length == currentBytesLength) {
            for (int i = currentIndex, j = 0; j < length; i++, j++) {
                if (docBytes[i] != expectBytes[j]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public byte[] getRawBytes() {
        byte[] bytes = new byte[currentBytesLength];
        System.arraycopy(docBytes, currentIndex, bytes, 0, currentBytesLength);
        return bytes;
    }

    public String getString() throws ParseException {

        try {
            return ParseUtils.parseString(docBytes, currentIndex, currentBytesLength);
        } catch (ParseException e) {
            e.setRowAndColumn(this);
            throw e;
        }
    }

    public String getStringWithDecoding() throws ParseException {
        try {
            return ParseUtils.parseStringWithDecoding(docBytes, currentIndex, currentBytesLength, charset);
        } catch (ParseException e) {
            e.setRowAndColumn(this);
            throw e;
        }
    }

    public short getShort() throws NumberFormatException {
        try {
            return (short) ParseUtils.parseInt(docBytes, currentIndex, currentBytesLength);
        } catch (NumberFormatException e) {
            e.setRowAndColumn(this);
            throw e;
        }
    }

    public int getInt() throws NumberFormatException {
        try {
            return ParseUtils.parseInt(docBytes, currentIndex, currentBytesLength);
        } catch (NumberFormatException e) {
            e.setRowAndColumn(this);
            throw e;
        }
    }

    public long getLong() throws NumberFormatException {
        try {
            return ParseUtils.parseLong(docBytes, currentIndex, currentBytesLength);
        } catch (NumberFormatException e) {
            e.setRowAndColumn(this);
            throw e;
        }
    }

    public float getFloat() throws NumberFormatException {
        try {
            return ParseUtils.parseFloat(docBytes, currentIndex, currentBytesLength);
        } catch (NumberFormatException e) {
            e.setRowAndColumn(this);
            throw e;
        }
    }

    public double getDouble() throws NumberFormatException {
        try {
            return ParseUtils.parseDouble(docBytes, currentIndex, currentBytesLength);
        } catch (NumberFormatException e) {
            e.setRowAndColumn(this);
            throw e;
        }
    }
}
