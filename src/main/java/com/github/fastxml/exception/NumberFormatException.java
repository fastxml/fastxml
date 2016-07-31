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

/**
 * Created by weager on 2016/06/10.
 */
public class NumberFormatException extends ParseException {
    private String rawString;


    public NumberFormatException(String message, Throwable throwable) {
        super(message, throwable);
        this.rawString = rawString;
    }

    public String getRawString() {
        return rawString;
    }

    public void setRawString(String rawString) {
        this.rawString = rawString;
    }

    @Override
    public String getMessage() {
        return getMessage(" [rawString:" + rawString + "]" + super.getMessage());
    }

    public static NumberFormatException formatException(String message, Throwable throwable) {
        return new NumberFormatException(message, throwable);
    }
}
