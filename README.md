# HideSwaggerParamsMavenPlugin

usage:

    <plugins>
      
      <plugin>
				<groupId>com.github.arnebinder</groupId>
				<artifactId>hide-swagger-params-maven-plugin</artifactId>
				<version>0.1-SNAPSHOT</version>
				<configuration>
				  <jsonfile>path/to/file(default: target/doc/swagger-ui/swagger.json)</jsonfile>
				  <hiddenNameValue>name-of-parameter-to-hide(default: HIDDEN)</hiddenNameValue>
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
