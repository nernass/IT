# Exemplary project structure with separated unit and integration tests using Maven

## Motivation

Separating unit tests from integration tests helps in better, logical organization of tests within a project.

The goal is to have the following structure:

```
.
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   └── resources
    ├── test
    │   ├── java
    │   └── resources
    └── test-it
        ├── java
        └── resources
```

Directories `src/main` and `src/test`are [standard directories](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html)
for maven project.

Directory `src/test-it` is a custom directory containing integration tests. Name of the directory can be adjusted in the configuration (see next section).

Repository contains exemplary Spring-based project, to demonstrate on real-life example how unit and integration tests can be structured. 

## Requirements

Three plugins are required to make it work

* [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/) for running unit tests,
* [Maven Failsafe Plugin](https://maven.apache.org/surefire/maven-failsafe-plugin/) for running integration tests,
* [Build Helper Maven Plugin](https://www.mojohaus.org/build-helper-maven-plugin/) for setting up additional directories
  with integration tests' code and resources.

## Setup

### Configuration of Build Helper Maven Plugin:

Configuration of a `builder-helper-maven-plugin` consists of two steps:

#### Configuration of an integration tests source code location:

```xml

<execution>
    <id>add-integration-test-source</id>
    <phase>generate-test-sources</phase>
    <goals>
        <goal>add-test-source</goal>
    </goals>
    <configuration>
        <sources>
            <source>src/test-it/java</source>
        </sources>
    </configuration>
</execution> 
```

#### Configuration of an integration tests resources location:

```xml

<execution>
    <id>add-integration-test-resource</id>
    <phase>generate-test-resources</phase>
    <goals>
        <goal>add-test-resource</goal>
    </goals>
    <configuration>
        <resources>
            <resource>
                <directory>src/test-it/resources</directory>
            </resource>
        </resources>
    </configuration>
</execution>
```

For full build configuration refer to the [pom.xml](pom.xml) file.

## Build and run
### Build project
```bash
mvn clean compile
```

### Run unit tests
```bash
mvn test
```

### Run integration tests
```bash
mvn failsafe:integration-test
```

### Run unit tests and integration tests
```bash
mvn verify
```