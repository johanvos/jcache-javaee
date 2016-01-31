package javax.enterprise.cache.spi;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <cache-unit>
 * <class>com.hazelcast.cache.HazelcastCachingProvider</class>
 * <cache name="articles">
 * <configuration>
 * <property name="store.by.value" value="true"/>
 * <property name="management.enabled" value="true"/>
 * <property name="statistics.enabled" value="true"/>
 * <property name="vendor.specific" value="unicorn"/>
 * </configuration>
 * </cache>
 * </cache-unit>
 *
 * @author airhacks.com
 */
public interface DeploymentDescriptorParser {

    static String ELEMENT_CACHING = "caching";
    static String ELEMENT_CACHE = "cache";
    static String ELEMENT_CLASS = "class";
    static String ATTRIBUTE_CACHE_NAME = "name";

    static CacheUnit parse(String content) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        CacheUnit unit = new CacheUnit();
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        }
        try (StringReader reader = new StringReader(content)) {
            try {
                Document document = builder.parse(new InputSource(reader));
                Node caching = document.getElementsByTagName(ELEMENT_CACHING).item(0);
                String cachingProvider = findSubNode(ELEMENT_CLASS, caching);
                unit.setCachingProviderClass(cachingProvider);

                NodeList childNodes = caching.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node item = childNodes.item(i);
                    if (ELEMENT_CACHE.equalsIgnoreCase(item.getNodeName())) {
                        Node nameAttribute = item.getAttributes().getNamedItem(ATTRIBUTE_CACHE_NAME);
                        if (nameAttribute != null) {
                            unit.setName(nameAttribute.getNodeValue());
                        }
                    }
                }
            } catch (SAXException | IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return unit;
    }

    static String findSubNode(String nodeName, Node parent) {
        NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (nodeName.equals(item.getNodeName())) {
                return item.getTextContent();
            }
        }
        return null;
    }
}