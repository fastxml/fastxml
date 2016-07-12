package function;

import junit.framework.TestCase;
import org.fastxml.FastXmlFactory;
import org.fastxml.FastXmlParser;
import org.fastxml.exception.ParseException;
import org.junit.Test;
import util.TestUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by weager on 2016/07/12.
 */
public class TestTraverseXml extends TestCase {

    @Test
    public void testTraverseXml() throws IOException, ParseException {
        boolean printInfo = true;
        traverseXml("bioinfo.xml", printInfo);
        traverseXml("book.xml", printInfo);
        traverseXml("form.xml", printInfo);
        traverseXml("nav.xml", printInfo);
        traverseXml("order.xml", printInfo);
        traverseXml("soap.xml", printInfo);
        traverseXml("test1.xml", printInfo);
    }

    private void traverseXml(String fileName, boolean printInfo) throws ParseException, IOException {
        byte[] doc = TestUtils.loadClasspathFile(fileName, this.getClass().getClassLoader());
        if (doc == null || doc.length == 0) {
            throw ParseException.emptyDocument();
        }

        System.out.println("============[" + fileName + "] begin parsing============");

        FastXmlParser parser = FastXmlFactory.newInstance(doc);
        while (parser.next() != FastXmlParser.END_DOCUMENT) {
            if(printInfo) {
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
                System.out.println("[" + event + "]: " + parser.getString(true));
            }
        }

        System.out.println("============[" + fileName + "] end parsing============\n");
    }
}
