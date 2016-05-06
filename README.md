# Soteria

Java EE Security (JSR 375) upstream API and RI

Notes
-----

The API in the /api module is the *upstream API*, which means development takes place there. At set times a version of this will be synced to java.net representing "the truth". 
From java.net the API will be mirrored back to https://github.com/javaee-security-spec/spec-api

Currently Soteria is working towards an EDR1 release.

Building
--------

Soteria can be build by executing the following from the project root:

``mvn clean package``

The API and combined API/implementation jars can then be found in /api and /impl respectively.

Sample applications
-------------------

In /test a number of sample applications are located that each demonstrate a specific feature of JSR 375. The folder is called
/test since these are intended to become RI tests at some point.

The sample applications are build when the main build as shown above is executed. By default these applications are build for a
target server that is assumed to provide a JSR 375 implementation (like a Java EE 8 server would). In that case the Soteria jars
are *not* included in the application archive.

Alternatively the sample applications can be build for a server that does not provide a JSR 375 implementation. In that case the Soteria jars *are* included in the application archive. This can be done using the ``bundled`` profile as follows:

``mvn clean package -Pbundled``


Compatibility
-------------

Soteria currently fully runs on Payara 4.1.1.161 and JBoss WildFly 10, and mostly on GlassFish 4.1.1. Soteria unfortunately does not run on Liberty 8 or 9 at the moment due to missing support for CDI in server authentication modules. Soteria does not run at all on WebLogic 12.2.1 due to a problem with server authentication modules working at all.

