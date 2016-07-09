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
 * Created by weager on 2016/06/10.
 */
public class NumberFormatException extends Exception {
    private String rawString;

    public NumberFormatException(String message, String rawString) {
        super(message);
        this.rawString = rawString;
    }

    public String getRawString() {
        return rawString;
    }

    public static NumberFormatException formatException(String rawString, String type) {
        String message = String.format("could not parse string[%s] to %s", rawString, type);
        return new NumberFormatException(message, rawString);
    }

    public static NumberFormatException intFormatException(String rawString) {
        return formatException(rawString, "int");
    }

    public static NumberFormatException shortFormatException(String rawString) {
        return formatException(rawString, "short");
    }

    public static NumberFormatException longFormatException(String rawString) {
        return formatException(rawString, "long");
    }

    public static NumberFormatException floatFormatException(String rawString) {
        return formatException(rawString, "float");
    }

    public static NumberFormatException doubleFormatException(String rawString) {
        return formatException(rawString, "double");
    }
}
