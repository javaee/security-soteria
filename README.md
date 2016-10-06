# Soteria

[![Build Status](https://travis-ci.org/javaee-security-spec/soteria.svg?branch=master)](https://travis-ci.org/javaee-security-spec/soteria)

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
target server that is *not* assumed to already provide a JSR 375 implementation (like a Java EE 8 server would). In that case the Soteria jars are included in the application archive.

Alternatively the sample applications can be build for a server that does provide a JSR 375 implementation. In that case the Soteria jars are not included in the application archive. This can be done using the ``provided`` profile as follows:

``mvn clean package -Pprovided``


Compatibility
-------------

Soteria currently fully runs on Payara 4.1.1.161 and JBoss WildFly 10. It runs mostly on Liberty 16.0.0.3/2016.9, TomEE 7.0.1 and GlassFish 4.1.1. "Mostly" means here that some features don't work because of bugs in the servers. For instance in TomEE 7.0.1 `request.authenticate` isn't supported and that server doesn't set the right flag to indicate where a resource is protected. These bugs are likely going to be fixed in newer versions.

Soteria does not run at all on WebLogic 12.2.1 due to a problem with server authentication modules working at all.

