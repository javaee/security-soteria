/*
 * Copyright (c) 2015-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.glassfish.soteria.test;

import static java.lang.System.getProperty;
import static org.glassfish.soteria.test.Assert.assertDefaultAuthenticated;
import static org.glassfish.soteria.test.Assert.assertDefaultNotAuthenticated;
import static org.glassfish.soteria.test.ShrinkWrap.mavenWar;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.io.IOException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


@RunWith(Arquillian.class)
public class AppMemCustomFormIT extends ArquillianBase {
    
    // Disabled for Liberty since as of version 16.0.0.3 / 2016.9 it doesn't
    // support request.authenticate(), which is essential for the custom form
    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        if ("liberty".equals(getProperty("arquillian.server"))) {
            return ShrinkWrap.create(WebArchive.class, "test.war")
                             .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        }
        return mavenWar();
    }

    @Before
    public void checkEnabled() {
        assumeFalse("liberty".equals(getProperty("arquillian.server")));
    }

    @Test
    public void testAuthenticated() throws IOException {
        
        
        // 1. Initially request protected page when we're not authenticated
        
        Page loginPage = pageFromServer("/servlet");
        
        
        // 2. Server should forward to login page
        
        assertTrue(
            "The login page should have been displayed, but was not",
            loginPage.getWebResponse().getContentAsString().contains("Login to continue")
        );
        
        
        // 3. Submit the form on the login page with the correct credentials
        
        HtmlForm form = ((HtmlPage) loginPage).getForms().get(0);
        
        form.getInputByName("form:username")
            .setValueAttribute("reza");
        
        form.getInputByName("form:password")
            .setValueAttribute("secret1");
        
        HtmlPage page = form.getInputByValue("Login")
                            .click();
        
        // Has to be authenticted now
        assertDefaultAuthenticated(
            page.getWebResponse()
                .getContentAsString());
        
        
        // 4. Request page again. FORM is stateful (http session bound) so
        // still has to be authenticated.
        
        page = pageFromServer("/servlet");
        
        assertDefaultAuthenticated(
            page.getWebResponse()
                .getContentAsString());
        
        
        // 5. Logout
        
        page = page.getForms()
                   .get(0)
                   .getInputByValue("Logout")
                   .click();
        
        // Has to be logged out now (page will still be rendered, but with 
        // web username null and no roles.
        
        assertDefaultNotAuthenticated(
            page.getWebResponse()
                .getContentAsString());
        
        
        
        // 6. Request page again. Should still be logged out
        // (and will display login to continue again now)
        
        assertDefaultNotAuthenticated(
            readFromServer("/servlet"));
        
    }
    
    @Test
    public void testNotAuthenticatedWrongName() throws IOException {
        
        // 1. Initially request protected page when we're not authenticated
        
        HtmlPage loginPage = pageFromServer("/servlet");
        
        
        // 2. Server should forward to login page
        
        assertTrue(
            "The login page should have been displayed, but was not",
            loginPage.getWebResponse().getContentAsString().contains("Login to continue")
        );
        
        
        // 3. Submit the form on the login page with the wrong credentials
        
        HtmlForm form = loginPage.getForms().get(0);
        
        form.getInputByName("form:username")
            .setValueAttribute("romo");
        
        form.getInputByName("form:password")
            .setValueAttribute("secret1");
        
        HtmlPage page = form.getInputByValue("Login")
                            .click();
        
        assertTrue(
            "An error message should be displayed, but was not",
            page.getWebResponse().getContentAsString().contains("Authentication failed")
        );
        
        // Should not be authenticted now
        assertDefaultNotAuthenticated(
            page.getWebResponse()
                .getContentAsString());
        
    }
    
    @Test
    public void testNotAuthenticatedWrongPassword() throws IOException {
        
        // 1. Initially request protected page when we're not authenticated
        
        HtmlPage loginPage = pageFromServer("/servlet");
        
        
        // 2. Server should forward to login page
        
        assertTrue(
            "The login page should have been displayed, but was not",
            loginPage.getWebResponse().getContentAsString().contains("Login to continue")
        );
        
        
        // 3. Submit the form on the login page with the wrong credentials
        
        HtmlForm form = loginPage.getForms().get(0);
        
        form.getInputByName("form:username")
            .setValueAttribute("reza");
        
        form.getInputByName("form:password")
            .setValueAttribute("wrongpassword");
        
        HtmlPage page = form.getInputByValue("Login")
                            .click();
        
        assertTrue(
            "An error message should be displayed, but was not",
            page.getWebResponse().getContentAsString().contains("Authentication failed")
        );
        
        // Should not be authenticted now
        assertDefaultNotAuthenticated(
            page.getWebResponse()
                .getContentAsString());
       
    }
    
    @Test
    public void testNotAuthenticatedInitiallyWrongNameThenCorrect() throws IOException {
        
        // 1. Initially request protected page when we're not authenticated
        
        HtmlPage loginPage = pageFromServer("/servlet");
        
        
        // 2. Server should forward to login page
        
        assertTrue(
            "The login page should have been displayed, but was not",
            loginPage.getWebResponse().getContentAsString().contains("Login to continue")
        );
        
        
        // 3. Submit the form on the login page with the wrong credentials
        
        HtmlForm form = loginPage.getForms().get(0);
        
        form.getInputByName("form:username")
            .setValueAttribute("reza");
        
        form.getInputByName("form:password")
            .setValueAttribute("wrongpassword");
        
        HtmlPage page = form.getInputByValue("Login")
                            .click();
        
        assertTrue(
            "An error message should be displayed, but was not",
            page.getWebResponse().getContentAsString().contains("Authentication failed")
        );
        
        // Should not be authenticted now
        assertDefaultNotAuthenticated(
            page.getWebResponse()
                .getContentAsString());
        
        
        // 4. Fill out form on page again (note that contrary to app mem form, this sample
        //    app causes an error message to be displayed on the form instead of showing
        //    an error page.
        
        
        form = page.getForms().get(0);
        
        form.getInputByName("form:username")
            .setValueAttribute("reza");
        
        form.getInputByName("form:password")
            .setValueAttribute("secret1");
        
        page = form.getInputByValue("Login")
                            .click();
        
        // Has to be authenticted now
        assertDefaultAuthenticated(
            page.getWebResponse()
                .getContentAsString());
    }

}
