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
package function;

import com.github.fastxml.FastXmlFactory;
import com.github.fastxml.FastXmlParser;
import com.github.fastxml.exception.ParseException;
import org.junit.Assert;
import org.junit.Test;
import util.FileLoaderUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * test traverse all event in xml document
 * Created by weager on 2016/07/12.
 */
public class TraverseXml4InputStreamTest {

    @Test
    public void testTraverseXml() throws IOException, ParseException {
        boolean printInfo = false;
        traverseXml("temp.xml", printInfo);
        traverseXml("bioinfo.xml", printInfo);
        traverseXml("book.xml", printInfo);
        traverseXml("form.xml", printInfo);
        traverseXml("nav.xml", printInfo);
        traverseXml("order.xml", printInfo);
        traverseXml("soap.xml", printInfo);
        traverseXml("test1.xml", printInfo);
        traverseXml("test1-gbk.xml", printInfo);
        traverseXml("test2.xml", printInfo);
        traverseXml("test2-no-declaration.xml", printInfo);
        try {
            traverseXml("error.xml", printInfo);
        }catch (Exception e){
            Assert.assertTrue(e instanceof ParseException);
            ParseException parseException = (ParseException)e;
            System.out.println(parseException.getRow());
            System.out.println(parseException.getColumn());
            Assert.assertEquals(parseException.getRow(), 40);
            Assert.assertEquals(parseException.getColumn(), 0);


        }
    }

    private void traverseXml(String fileName, boolean printInfo) throws ParseException, IOException {
        InputStream is = FileLoaderUtils.getInputStream(fileName);

        if (is == null || is.available() == 0) {
            throw ParseException.emptyDocument();
        }

        System.out.println("============[" + fileName + "] begin traverse test============");

        FastXmlParser parser = FastXmlFactory.newInstance(is);
        while (parser.next() != FastXmlParser.END_DOCUMENT) {
            if (printInfo) {
                String event;
                switch (parser.getCurrentEvent()) {
                    case FastXmlParser.START_DOCUMENT:
                        event = "start_document";
                        break;
                    case FastXmlParser.END_DOCUMENT:
                        event = "end_document";
                        break;
                    case FastXmlParser.START_TAG:
                        event = "start_tag";
                        break;
                    case FastXmlParser.END_TAG:
                        event = "end_tag";
                        break;
                    case FastXmlParser.END_TAG_WITHOUT_TEXT:
                        event = "end_tag_without_text";
                        break;
                    case FastXmlParser.ATTRIBUTE_NAME:
                        event = "attribute_name";
                        break;
                    case FastXmlParser.ATTRIBUTE_VALUE:
                        event = "attribute_value";
                        break;
                    case FastXmlParser.TEXT:
                        event = "text";
                        break;
                    default:
                        event = "";
                }
                System.out.println("[" + event + "]: " + parser.getStringWithDecoding());
            }
        }

        System.out.println("============[" + fileName + "] end traverse test============\n");
    }
}
