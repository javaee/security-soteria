# tests / samples

This sub-repo contains working applications that demonstrate various aspects of JSR 375 / Soteria

* **app-mem** - Uses the embedded in-memory identity store. The application sets the data to be used by means of an annotation
  * Test URL: http://localhost:8080/app-mem/servlet?name=reza&password=secret1
* **app-db**  - Uses the database identity store. The applications defines an embedded datasource and binds this to the identity store definition via an annotation. The data to be used is inserted in the datasoource by the application during startup.
  * Test URL: http://localhost:8080/app-db/servlet?name=reza&password=secret1
* **app-ldap** - Uses the LDAP identity store. The application instantiates an embedded LDAP server and binds its URL to the identity store definition via an annotation. The data to be used is inserted in the LDAP server by the application during startup.
  * Test URL: http://localhost:8080/app-ldap/servlet?name=reza&password=secret1
* **app-custom** - Uses an identity store that's fully provided by the application. Just for the example, this store does the caller name/credential check internally.
  * Test URL: http://localhost:8080/app-custom/servlet?name=reza&password=secret1
* **app-custom-session** - As app-custom, but uses a JSR 375 provided interceptor to automatically establish an authentication session when authenticated. This means that the identity store is only consulted once per session.
  * Check initially not authenticated: http://localhost:8080/app-custom-session/servlet
  * authenticate: http://localhost:8080/app-custom-session/servlet?name=reza&password=secret1
  * Check authentication remembered: http://localhost:8080/app-custom-session/servlet
  * logout: http://localhost:8080/app-custom-session/servlet?logout
* **app-custom-rememberme** - As app-custom-session, but uses a JSR 375 provided interceptor to conditionally remember the caller by writing a cookie and storing the details in an application provided special purpose identity store
  * Check initially not authenticated: http://localhost:8080/app-custom-rememberme/servlet
  * authenticate: http://localhost:8080/app-custom-rememberme/servlet?name=reza&password=secret1
  * Check authentication NOT remembered: http://localhost:8080/app-custom-rememberme/servlet
  * authenticate with remember me: http://localhost:8080/app-custom-rememberme/servlet?name=reza&password=secret1&rememberme=true
  * Check authentication remembered: http://localhost:8080/app-custom-rememberme/servlet
  * logout: http://localhost:8080/app-custom-session/servlet?logout
* **app-mem-basic** - As app-mem but uses the JSR 375 provided BASIC authentication mechanism
  * Test URL: http://localhost:8080/app-mem-basic/servlet (then provide "reza" and "secret1" in the dialog presented by the browser)
  * Note that /servlet is a protected resource and the dialog presented comes from the browser itself and not from the application
* **app-mem-form** - As app-mem but uses the JSR 375 provided FORM authentication mechanism.
  * Test URL: http://localhost:8080/app-mem-form/servlet (then provide "reza" and "secret1" in the form presented)
  * Note that /servlet is a protected resource. The authentication mechanism forwards to /login-servlet, which posts back to j_security_check. The authentication mechanism listens to this URL and if authentication succeeds a redirect back to /servlet is send.
* **app-mem-customform** - As app-mem but uses the JSR 375 provided CUSTOM FORM authentication mechanism.
  * Test URL: http://localhost:8080/app-mem-customform/servlet (then provide "reza" and "secret1" in the form presented)
  * Note that /servlet is a protected resource. The authentication mechanism forwards to /login.xhtml, which posts back to itself. A backing bean then programmatically resumes the authentication dialog and if authentication succeeds a redirect back to /servlet is send.


  


    