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

import static java.util.logging.Level.SEVERE;
import static org.apache.http.HttpStatus.SC_MULTIPLE_CHOICES;
import static org.apache.http.HttpStatus.SC_OK;
import static org.jsoup.Jsoup.parse;
import static org.jsoup.parser.Parser.xmlParser;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;

public class ArquillianBase {
    
    private static final Logger logger = Logger.getLogger(ArquillianBase.class.getName());
    
    private WebClient webClient;
    private String response;

	@ArquillianResource
    private URL base;
	
    @Rule
    public TestWatcher ruleExample = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            super.failed(e, description);
            
            logger.log(SEVERE, 
                "\n\nTest failed: " + 
                description.getClassName() + "." + description.getMethodName() +
                
                "\nMessage: " + e.getMessage() +
                
                "\nLast response: " +
                
                "\n\n"  + formatHTML(response) + "\n\n");
            
        }
    };

    @Before
    public void setUp() {
        response = null;
        webClient = new WebClient() {
            
            private static final long serialVersionUID = 1L;

            @Override
            public void printContentIfNecessary(WebResponse webResponse) {
                int statusCode = webResponse.getStatusCode();
                if (getOptions().getPrintContentOnFailingStatusCode() && !(statusCode >= SC_OK && statusCode < SC_MULTIPLE_CHOICES)) {
                    logger.log(SEVERE, webResponse.getWebRequest().getUrl().toExternalForm());
                }
                super.printContentIfNecessary(webResponse);
            }
        };
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
    }

    @After
    public void tearDown() {
        webClient.getCookieManager().clearCookies();
        webClient.close();
    }
    
    protected String readFromServer(String path) {
        response = "";
        WebResponse localResponse = responseFromServer(path);
        if (localResponse != null) {
            response = localResponse.getContentAsString();
        }
        
    	return response;
    }
    
    protected WebResponse responseFromServer(String path) {
        
        WebResponse webResponse = null;
        
        Page page = pageFromServer(path);
        if (page != null) {
            webResponse = page.getWebResponse();
            if (webResponse != null) {
                response = webResponse.getContentAsString();
            }
        }
        
        return webResponse;
    }
    
    protected <P extends Page> P pageFromServer(String path) {
    	
    	if (base.toString().endsWith("/") && path.startsWith("/")) {
    		path = path.substring(1);
    	}
    	
        try {
            response = "";
            
            P page = webClient.getPage(base + path);
            
            if (page != null) {
                WebResponse localResponse = page.getWebResponse();
                if (localResponse != null) {
                    response = localResponse.getContentAsString();
                }
            }
            
            return page;
            
        } catch (FailingHttpStatusCodeException | IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    protected WebClient getWebClient() {
 		return webClient;
 	}
    
    public static String formatHTML(String html) {
        try {
            return parse(html, "", xmlParser()).toString();
        } catch (Exception e) {
            return html;
        }
    }
    
}
