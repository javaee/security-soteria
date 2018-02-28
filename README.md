# Soteria

[![Build Status](https://travis-ci.org/javaee/security-soteria.svg?branch=master)](https://travis-ci.org/javaee/security-soteria)

Java EE Security (JSR 375) RI

Building
--------

Soteria can be built by executing the following from the project root:

``mvn clean package``

The API and combined API/implementation jars can then be found in /api and /impl respectively.

Sample applications
-------------------

In /test a number of sample applications are located that each demonstrate a specific feature of JSR 375. The folder is called
/test since these double as integration tests.

The sample applications are build when the main build as shown above is executed. By default these applications are build for a
target server that is *not* assumed to already provide a JSR 375 implementation (like a Java EE 8 server would). In that case the Soteria jars are included in the application archive.

Alternatively the sample applications can be build for a server that does provide a JSR 375 implementation. In that case the Soteria jars are not included in the application archive. This can be done using the ``provided`` profile as follows:

``mvn clean package -Pprovided``

There are 4 CI targets provided to test Soteria against:

* payara
* wildfly
* tomee

Testing against any of these is done by activating the maven profile with the same name. E.g.

``mvn clean install -Ptomee,bundled``

Testing against glassfish (which provides soteria integration):
``mvn clean verify -Pglassfish,provided``

Compatibility
-------------

Soteria currently fully runs on Payara 4.1.1.161, JBoss WildFly 10 and TomEE 7.0.2-SNAPSHOT from 12-10 (with Tomcat 8.5.6). It runs mostly on Liberty 16.0.0.3/2016.9, TomEE 7.0.1 and GlassFish 4.1.1. "Mostly" means here that some features don't work because of bugs in the servers. For instance on Liberty 16.0.0.3 `request.authenticate` isn't supported. These bugs are likely going to be fixed in newer versions.

Soteria does not run at all on WebLogic 12.2.1 due to a problem with server authentication modules working at all.

