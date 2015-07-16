# HideSwaggerParamsMavenPlugin

**usage**

1. build package: execute maven package goal 
2. integrate into maven repository: `mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=path\to\hide-swagger-params-maven-plugin-0.1-SNAPSHOT.jar`
3. integrate into your project:

```
    		<plugins>
      
      			<plugin>
				<groupId>com.github.arnebinder</groupId>
				<artifactId>hide-swagger-params-maven-plugin</artifactId>
				<version>0.1-SNAPSHOT</version>
				<configuration>
				  <jsonfile>path/to/swagger-json-file(default: target/doc/swagger-ui/swagger.json)</jsonfile>
				  <yamlfile>path/to/yaml/output(default: target/doc/swagger-ui/swagger.yaml)</yamlfile>
				  <excludeKey>name-of-element-key-to-hide(default: name)</excludeKey>
				  <excludeValue>name-of-element-value-to-hide(default: HIDDEN)</excludeValue>
				</configuration>
				<executions>
					<execution>
						<phase>package</phase>
						<goals>
							<goal>hideparams</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		<plugins>
```
