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
package com.github.fastxml.exception;

import com.github.fastxml.FastXmlParser;
import com.github.fastxml.FastXmlParser4ByteArray;
import com.github.fastxml.FastXmlParser4InputStream;

import java.io.IOException;

/**
 * Created by weager on 2016/06/07.
 */
public class ParseException extends Exception {

    private int row = -1;
    private int column = -1;

    public ParseException(String message) {
        super(message);
    }

    public ParseException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public ParseException(String message, FastXmlParser parser) {
        this(message, parser, null);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(String message, FastXmlParser parser, Throwable cause) {
        super(message, cause);
        setRowAndColumn(parser);
    }

    public void setRowAndColumn(FastXmlParser parser) {
        if (parser == null) {
            return;
        }
        if (parser instanceof FastXmlParser4ByteArray) {
            byte[] docBytes = parser.getDocument();
            row = 1;
            int cursor = parser.getCursor();
            int lastNewLine = 1;
            for (int i = 0; i <= cursor; i++) {
                if (docBytes[i] == '\n') {
                    row++;
                    lastNewLine = i;
                }
            }
            column = cursor - lastNewLine;
        } else if(parser instanceof FastXmlParser4InputStream){
            FastXmlParser4InputStream parser4InputStream = (FastXmlParser4InputStream) parser;
            row = parser4InputStream.getRow();
            column = parser4InputStream.getColumn();
        }
    }

    @Override
    public String getMessage() {
        return getMessage(super.getMessage());
    }

    protected String getMessage(String message) {
        StringBuilder sb = new StringBuilder();
        // position
        sb.append("line[").append(row).append("], column[").append(column).append("]: ");
        sb.append(message);
        return sb.toString();
    }

    public static ParseException tagNotClosed(FastXmlParser parser) {
        return new ParseException("tag does not close correctly", parser);
    }

    public static ParseException emptyDocument() {
        return new ParseException("document should not be empty");
    }

    public static ParseException otherError(FastXmlParser parser) {
        return new ParseException("Other error: invalid parser state", parser);
    }

    public static ParseException entityError(String message) {
        return new ParseException(message);
    }

    public static ParseException documentEndUnexpected(FastXmlParser parser) {
        return new ParseException("Document end unexpected", parser);
    }

    public static ParseException formatError(String msg) {
        return new ParseException(msg);
    }

    public static ParseException formatError(String msg, FastXmlParser parser) {
        return new ParseException(msg, parser);
    }

    public static ParseException ioException(IOException e) {
        return new ParseException(e);
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
