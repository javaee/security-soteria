# License Maven Plugin
To treat license headers in a unified and compliant way, this project uses the Mycila Licnse Maven Plugin
http://code.mycila.com/license-maven-plugin/

In a (non-POM) project simply add:

```
<build>
	<plugins>
		<plugin>
			<groupId>com.mycila</groupId>
			<artifactId>license-maven-plugin</artifactId>
		</plugin>
	</plugins>
</build>
```

to the project's POM. 

Templates and settings defined in [license](license) are based on the entire project cloned. 

Should modules have a different hierarchy, it may be necessary to adjust the configuration of ``header`` and ``headerDefinition`` from the parent POM.