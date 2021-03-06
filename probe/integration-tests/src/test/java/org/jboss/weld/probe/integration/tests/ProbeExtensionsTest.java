/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.probe.integration.tests;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.jboss.weld.probe.Strings.DATA;
import static org.jboss.weld.probe.Strings.DECLARED_OBSERVERS;
import static org.jboss.weld.probe.Strings.KIND;
import static org.jboss.weld.probe.Strings.OBSERVED_TYPE;
import static org.jboss.weld.probe.Strings.QUALIFIERS;
import static org.jboss.weld.probe.integration.tests.JSONTestUtil.BEANS_PATH_ALL;
import static org.jboss.weld.probe.integration.tests.JSONTestUtil.BeanType;
import static org.jboss.weld.probe.integration.tests.JSONTestUtil.getBeanDetail;
import static org.jboss.weld.probe.integration.tests.JSONTestUtil.getPageAsJSONObject;

import java.io.IOException;
import java.net.URL;

import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.probe.integration.tests.annotations.Collector;
import org.jboss.weld.probe.integration.tests.extensions.DummyBean;
import org.jboss.weld.probe.integration.tests.extensions.TestExtension;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Tomas Remes
 */
@RunWith(Arquillian.class)
public class ProbeExtensionsTest extends ProbeIntegrationTest {

    @ArquillianResource
    private URL url;

    private static final String TEST_ARCHIVE_NAME = "probe-extensions-test";

    @Deployment(testable = false)
    public static WebArchive deployExplicitArchive() {
        return ShrinkWrap.create(WebArchive.class, TEST_ARCHIVE_NAME + ".war")
                .addAsWebInfResource(ProbeExtensionsTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(ProbeExtensionsTest.class.getPackage(), "beans.xml", "beans.xml")
                .addPackage(TestExtension.class.getPackage())
                .addPackage(Collector.class.getPackage())
                .addClasses(ProbeExtensionsTest.class, ProbeIntegrationTest.class).addAsServiceProvider(Extension.class, TestExtension.class);
    }

    @Test
    public void testExtensionVisibleInProbe() throws IOException {
        JsonObject beansInTestArchive = getPageAsJSONObject(BEANS_PATH_ALL, url);
        assertNotNull(beansInTestArchive);
        JsonArray beansArray = beansInTestArchive.getJsonArray(DATA);
        assertBeanClassVisibleInProbe(TestExtension.class, beansArray);

        //test extension attributes
        JsonObject extensionBeanDetail = getBeanDetail(BEANS_PATH_ALL, TestExtension.class, url);
        JsonArray declaredObserversInExtension = extensionBeanDetail.getJsonArray(DECLARED_OBSERVERS);
        assertEquals(BeanType.EXTENSION.name(), extensionBeanDetail.getString(KIND));
        assertTrue("Cannot find ProcessAnnotatedType observer method!",
                checkStringInArrayRecursively(ProcessAnnotatedType.class.getName() + "<DummyBean>", OBSERVED_TYPE, declaredObserversInExtension, false));

        //test bean altered by extension
        JsonObject dummyBeanDetail = getBeanDetail(BEANS_PATH_ALL, DummyBean.class, url);
        JsonArray qualifiersOfDummyBean = dummyBeanDetail.getJsonArray(QUALIFIERS);
        assertTrue("Cannot find " + Collector.class + " qualifier on " + DummyBean.class,
                checkStringInArrayRecursively("@" + Collector.class.getName(), null, qualifiersOfDummyBean, false));
    }

}
