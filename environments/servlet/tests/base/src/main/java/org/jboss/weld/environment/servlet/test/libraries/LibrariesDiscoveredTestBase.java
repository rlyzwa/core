package org.jboss.weld.environment.servlet.test.libraries;

import static org.jboss.weld.environment.servlet.test.util.Deployments.baseDeployment;
import static org.junit.Assert.assertNotNull;

import java.util.logging.Logger;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LibrariesDiscoveredTestBase {

    private static final Logger log = Logger.getLogger(LibrariesDiscoveredTestBase.class.getName());
    private static final String DELIMITER = "-------------------------";

    public static WebArchive deployment() {
        JavaArchive library = ShrinkWrap.create(JavaArchive.class, "library.jar").addClass(Camel.class).addAsManifestResource(new BeansXml(), "beans.xml");
        log.fine(DELIMITER);
        log.fine("Library");
        log.fine(DELIMITER);
        log.fine(library.toString(true));
        return baseDeployment().addClasses(Needle.class, LibrariesDiscoveredTestBase.class).addAsLibrary(library);
    }

    @Test
    public void testCamelDeployed(Camel camel, Needle needle) {
        assertNotNull(camel);
        assertNotNull(needle);
    }

}
