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
package org.fastxml.exception;

/**
 * Created by weager on 2016/06/07.
 */
public class ParseException extends Exception {

    private int row;
    private int column;

    public ParseException(String message){
        super(message);
    }

    public ParseException(String message, int row, int column){
        this(message, row, column, null);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(String message, int row, int column, Throwable cause) {
        super(message, cause);
        this.row = row;
        this.column = column;
    }

    @Override
    public String getMessage(){
        return getMessage(super.getMessage());
    }

    protected String getMessage(String message){
        StringBuilder sb = new StringBuilder();
        // position
        sb.append("line[").append(row).append("], column[").append(column).append("]: ");
        sb.append(message);
        return sb.toString();
    }

    public static ParseException tagNotClosed(int row, int column) {
        String cause = String.format("tag does not close correctly", row, column);
        return new ParseException(cause, row, column);
    }

    public static ParseException emptyDocument() {
        return new ParseException("document should not be empty", 1, 1);
    }

    public static ParseException otherError(int row, int column) {
        String cause = String.format("Other error: invalid parser state", row, column);
        return new ParseException(cause, row, column);
    }

    public static ParseException entityError(String message){
        return new ParseException(message);
    }

    public static ParseException documentEndUnexpected(int row, int column) {
        String cause = String.format("Document end unexpected", row, column);
        return new ParseException(cause, row, column);
    }

    public static ParseException formatError(String msg, int row, int column) {
        String cause = String.format("Document has error format: %s", row, column, msg);
        return new ParseException(cause, row, column);
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
