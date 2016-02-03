# Soteria

Java EE Security (JSR 375) upstream API and RI

Notes
-----

The API in the /api module is the *upstream API*, which means development takes place there. At set times a version of this will be synced to java.net representing "the truth". 
From java.net the API will be mirrored back to https://github.com/javaee-security-spec/spec-api

Currently Soteria is working towards an EDR1 release.

Compatibility
-------------

Soteria currently fully runs on Payara 4.1.1.161 and JBoss WildFly 10, and mostly on GlassFish 4.1.1. Soteria unfortunately does not run on Liberty 8 or 9 at the moment due to missing support for CDI in server authentication modules. Soteria does not run at all on WebLogic 12.2.1 due to a problem with server authentication modules working at all.

