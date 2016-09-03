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
package util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by weager on 2016/07/12.
 */
public class FileLoaderUtils {

    public static byte[] loadClasspathFile(String fileName) throws IOException {
        return loadClasspathFile(fileName, null);
    }

    public static byte[] loadClasspathFile(String fileName, ClassLoader classLoader) throws IOException {
        InputStream inputStream = getInputStream(fileName, classLoader);
        int available = inputStream.available();
        byte[] doc = new byte[available];
        inputStream.read(doc);
        inputStream.close();
        return doc;
    }

    public static InputStream getInputStream(String fileName) throws IOException {
        return getInputStream(fileName, null);
    }

    public static InputStream getInputStream(String fileName, ClassLoader classLoader) throws IOException {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = FileLoaderUtils.class.getClassLoader();
            }
        }
        return classLoader.getResourceAsStream(fileName);
    }
}
