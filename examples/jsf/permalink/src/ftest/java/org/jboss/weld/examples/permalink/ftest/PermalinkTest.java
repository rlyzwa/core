/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.examples.permalink.ftest;

import java.net.MalformedURLException;
import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import static org.jboss.arquillian.graphene.Graphene.*;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Tests permalink example in Weld. The first test just adds comment on the first
 * topic and returns back. Second test tests permanent link from jsf2.
 *
 * @author maschmid
 * @author mgencur
 * @author plenyi
 */
@RunWith(Arquillian.class)
@RunAsClient
public class PermalinkTest {
    protected String MAIN_PAGE = "/home.jsf";
    protected String PAGE_TITLE = "Direct links to the news you crave";
    protected By VIEW_ENTRY_LINK = By.xpath("//a[contains(text(),'View Entry')][1]");
    protected String TOPIC_TITLE = "Mojarra == RI";
    protected By PERMALINK_LINK = By.xpath("//a[@title='A bookmarkable link for this entry.'][1]");
    protected By AUTHOR_INPUT = By.id("author");
    protected By COMMENT_INPUT = By.id("body");
    protected By SUBMIT_BUTTON = By.id("post");
    protected By BACK_BUTTON = By.xpath("//input[@type='button'][@value='Back to main page']");
    protected String COMMENT_TEXT = "This is my first comment on Mojarra project";
    protected String AUTHOR_NAME = "Martin";
    protected String TEXT_ON_HOME_PAGE = "Annotation nation";
    protected By BODY = By.tagName("body");

    @Drone
    WebDriver driver;
    
    @ArquillianResource
    private URL contextPath;
    
    @Deployment(testable = false)
    public static WebArchive createTestDeployment1() {
        return Deployments.createDeployment();
    }
    
    @Before
    public void openStartUrl() throws MalformedURLException {
        driver.navigate().to(new URL(contextPath.toString() + "home.jsf"));
        waitModel(driver).until(element(VIEW_ENTRY_LINK).isPresent());
    }   

    @Test
    @InSequence(1)
    public void addCommentOnTopicTest() {
        guardHttp(driver.findElement(VIEW_ENTRY_LINK)).click();
        assertTrue("Topic title expected on the page", isTextOnPage(TOPIC_TITLE));
        
        driver.findElement(AUTHOR_INPUT).clear();
        driver.findElement(AUTHOR_INPUT).sendKeys(AUTHOR_NAME);
        
        driver.findElement(COMMENT_INPUT).clear();
        driver.findElement(COMMENT_INPUT).sendKeys(COMMENT_TEXT);
        
        guardHttp(driver.findElement(SUBMIT_BUTTON)).click();
        assertTrue("A name of comment's author expected", isTextOnPage(AUTHOR_NAME));
        assertTrue("A text of entered comment expected", isTextOnPage(COMMENT_TEXT));
        
        guardHttp(driver.findElement(BACK_BUTTON)).click();
        assertTrue("Home page expected", isTextOnPage(TEXT_ON_HOME_PAGE));
    }

    @Test
    @InSequence(2)
    public void permanentLinkTest() {
        guardHttp(driver.findElement(PERMALINK_LINK)).click();
        assertTrue("A name of comment's author expected", isTextOnPage(AUTHOR_NAME));
        assertTrue("A text of entered comment expected", isTextOnPage(COMMENT_TEXT));
    }

    private boolean isTextOnPage(String text) {
        return element(BODY).textContains(text).apply(driver);
    }
}
