# zeebe-cherry-simpleexample
Just create a simple hello word project

This project explains how to create a simple "Hello Word" worker, and execute it via the Cherry Framework

You can create your own project following these different steps.

# Maven.xml

To include the Cherry Framework, add in your pom.xml this library

`````xml
  <dependency>
      <groupId>io.camunda.community</groupId>
      <artifactId>zeebe-cherry-runtime</artifactId>
      <version>${cherry.version}</version>
  </dependency>


`````

You need to add the Connector SDK and Zeebe Client.

`````xml
   <dependency>
      <groupId>io.camunda</groupId>
      <artifactId>spring-zeebe-starter</artifactId>
      <version>${zeebe.version}</version>
    </dependency>
    <dependency>
      <groupId>io.camunda</groupId>
      <artifactId>zeebe-client-java</artifactId>
      <version>${zeebe-client.version}</version>
    </dependency>

    <!-- Accept Camunda Connector -->
    <dependency>
      <groupId>io.camunda.connector</groupId>
      <artifactId>connector-core</artifactId>
      <version>${connector-core.version}</version>
    </dependency>
    <dependency>
      <groupId>io.camunda.connector</groupId>
      <artifactId>connector-validation</artifactId>
      <version>${connector-validation.version}</version>
    </dependency>
`````

Do a 
````
mvn install
````

to retrieve all libraries

# The application.yaml

The project is a SpringBoot application. Configuration can be set in the `src/main/resources/application.yaml` file

Define the connection to the ZeebeEngine

`````yaml
zeebe.client:
broker.gateway-address: 127.0.0.1:26500
# zeebe.client.broker.gateway-address=host.docker.internal:26500
security.plaintext: true

# use a cloud Zeebe engine
# zeebe.client.cloud.region=
# zeebe.client.cloud.clusterId=
# zeebe.client.cloud.clientId=
# zeebe.client.cloud.clientSecret=

`````

The Cherry Runtime uses a database to save statistics and status.
By default, an H2 database is configured.


````yaml

# Database
spring.datasource:
  url: "jdbc:h2:file:./cherry.db"
  driver-class-name: "org.h2.Driver"
  username: "sa"
  password: "password"
spring.jpa:
  hibernate.ddl-auto: update
  generate-ddl: true
  database-platform: "org.hibernate.dialect.H2Dialect"
````


You can use any other SpringBoot variable, for example

`````yaml
server.port: 9091
`````


Note: if the connection to a Zeebe Server is not provided in the configuration, then the Cherry Runtime will ask the administrator to give the information in the UI.


# Your first Worker

Define a new class. Let's review each part of the class.

## Declaration

Define a new class. This class extends AbstractWorker

`````java
package io.camunda.cherry.helloword;


@Component
public class HelloWordWorker extends AbstractWorker 

`````

Note: 

1. Your class must declare @Component ou @Service. This notation is used by Cherry to detect the different connector and worker
2. You can choose between AbstractWorker and AbstractConnector. In this example, AbstractWorker is choosen. Check the Developper manual to see the difference


## Constructor

In the constructor, you have to specify
`````java
  public HelloWordWorker() {
    super( // Type
    "helloword",
    // List of Input
    Arrays.asList(
    RunnerParameter.getInstance(INPUT_COUNTRY, "Country name", String.class, RunnerParameter.Level.OPTIONAL,
    "Country to whom to say hello")
    .addChoice(COUNTRY_V_USA, "United State of America")
    .addChoice(COUNTRY_V_GE, "Germany")
    .addChoice(COUNTRY_V_FR, "France")
    .addChoice(COUNTRY_V_EN, "England")
    .addChoice(COUNTRY_V_IT, "Italy")
    .addChoice(COUNTRY_V_SP, "Spain"),

    RunnerParameter.getInstance(INPUT_STATE, "State", String.class, RunnerParameter.Level.OPTIONAL, "State")
    .addCondition(INPUT_COUNTRY, Collections.singletonList(COUNTRY_V_USA))),

    // list of Output
    Collections.singletonList(
    RunnerParameter.getInstance(OUTPUT_MESSAGE, OUTPUT_MESSAGE, String.class, RunnerParameter.Level.REQUIRED,
    "Welcome Message")),
    // List of BPMN Error
    Collections.singletonList(
    BpmnError.getInstance(BPMN_ERROR_NOTIME, "Sorry, no time to say hello now, I'm busy")));
    }


`````

* The type: this is the type of workers used in the Element-Template

* The list of Inputs

The Runtime uses the list of Inputs to calculate the Element-Template. The second advantage is to reduce your verification.
For example, when you define an Input as REQUIRED, the Runtime will check the existence of this parameter. If the parameter does not exist
the Runtime will throw an error and will not call your method.

See below for a detailed explanation.

* The list of Outputs

Declare this information for the documentation. Attention, if the output is declared as REQUIRED, then the Runtime will verify that you provided the information.
If not, it will throw an error (the worker does not respect the contract).

* The list of Errors

Declare the list of Errors. There are multiple impacts. The first one is the documentation: this list will be visible. The second one is the Element-Template.
When the worker throws a ConnectorExecption, then the ZeebeClient, according to the definition in the Element-Template, will throw a BPMN Error.
Else, the ConnectorException will send a Fail Task.

## Define an Input
The input contains different part
RunnerParameter.getInstance(INPUT_COUNTRY, "Country name", String.class, RunnerParameter.Level.OPTIONAL,
"Country to whom to say hello")

The code. This is the value expected from the worker.
The label. Labels are visible  in the documentation and in the Element-template
The class (String.class here). This is nice for the documentation, and the Runtime checks the type.
Level (OPTIONAL/REQUIRED). A REQUIRED input must be provided by the process.
A description for the documentation and as a hint in the Element-template

## Add Input decorator
Multiple decorators exist for inputs.
AddChoice give a list of choice, then a Select Box is created in the Element-tempalte

    .addChoice(COUNTRY_V_USA, "United State of America")
    .addChoice(COUNTRY_V_GE, "Germany")
    .addChoice(COUNTRY_V_FR, "France")
    .addChoice(COUNTRY_V_EN, "England")
    .addChoice(COUNTRY_V_IT, "Italy")
    .addChoice(COUNTRY_V_SP, "Spain"),


Condition describes a show/hide condition, exploitable in the Element-template
.addCondition(INPUT_COUNTRY, Collections.singletonList(COUNTRY_V_USA))),

Check the documentation for the list of decorator.

## The different information

Different methods, like getName(), getCollectionName(), getDescription(), getLogo() can be overridden in order to give more explanation to your worker.

## The execute method
This is the core of your work!
In the method you have different methods to simplify your work.
getInput...Value() : multiple getInput methods simplify your code. For example, getInputStringValue() returns a String.

Note: if you use a Connector, all Inputs are grouped under an object. This simplifies the approach, except that you have to define an extra class.

The setOuputValue() simplifies the way to produce the result. Attention: any REQUIRED output must be resolved by a call to the setOuputValue() method: this is the implementation for the framework to check your contract.

## How to throw an error?

Just throw a ConnectorException(). If the worker declare some BPMN Error, then this will be transformed by the Connector SDK to a BPMN Error.

# Execute in an IDE

In the IDE like Intellij, the class to start is `io.camunda.CherryApplication`
![Intellij](doc/IntellijConfiguration.png?raw=true)

Execute this project

The Runtime started
````
"C:\Program Files\Java\jdk-17.0.3.1\bin\java.exe" -agentlib:jdwp=transport=dt_socket,address=127.0.0.1:59003,suspend=y,server=n -javaagent:C:\Users\Pierre-YvesMonnet\AppData\Local\JetBrains\IntelliJIdea2021.3\captureAgent\debugger-agent.jar=file:/C:/Users/Pierre-YvesMonnet/AppData/Local/Temp/capture.props -Dfile.encoding=UTF-8 -classpath "D:\dev\intellij\community\cherry\zeebe-cherry-simpleexample\target\classes;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\boot\spring-boot-starter-web\2.7.4\spring-boot-starter-web-2.7.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\boot\spring-boot-starter\2.7.4\spring-boot-starter-2.7.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\boot\spring-boot\2.7.4\spring-boot-2.7.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\boot\spring-boot-autoconfigure\2.7.4\spring-boot-autoconfigure-2.7.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\boot\spring-boot-starter-logging\2.7.4\spring-boot-starter-logging-2.7.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\ch\qos\logback\logback-classic\1.2.11\logback-classic-1.2.11.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\ch\qos\logback\logback-core\1.2.11\logback-core-1.2.11.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\logging\log4j\log4j-to-slf4j\2.17.2\log4j-to-slf4j-2.17.2.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\logging\log4j\log4j-api\2.17.2\log4j-api-2.17.2.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\slf4j\jul-to-slf4j\1.7.36\jul-to-slf4j-1.7.36.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\jakarta\annotation\jakarta.annotation-api\1.3.5\jakarta.annotation-api-1.3.5.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\spring-core\5.3.23\spring-core-5.3.23.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\spring-jcl\5.3.23\spring-jcl-5.3.23.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\yaml\snakeyaml\1.30\snakeyaml-1.30.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\boot\spring-boot-starter-json\2.7.4\spring-boot-starter-json-2.7.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\fasterxml\jackson\datatype\jackson-datatype-jdk8\2.13.4\jackson-datatype-jdk8-2.13.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\fasterxml\jackson\module\jackson-module-parameter-names\2.13.4\jackson-module-parameter-names-2.13.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\boot\spring-boot-starter-tomcat\2.7.4\spring-boot-starter-tomcat-2.7.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\tomcat\embed\tomcat-embed-core\9.0.65\tomcat-embed-core-9.0.65.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\tomcat\embed\tomcat-embed-el\9.0.65\tomcat-embed-el-9.0.65.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\tomcat\embed\tomcat-embed-websocket\9.0.65\tomcat-embed-websocket-9.0.65.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\spring-web\5.3.23\spring-web-5.3.23.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\spring-beans\5.3.23\spring-beans-5.3.23.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\spring-webmvc\5.3.23\spring-webmvc-5.3.23.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\spring-aop\5.3.23\spring-aop-5.3.23.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\spring-context\5.3.23\spring-context-5.3.23.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\spring-expression\5.3.23\spring-expression-5.3.23.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\camunda\spring\spring-boot-starter-camunda\8.2.0\spring-boot-starter-camunda-8.2.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\camunda\spring\spring-client-zeebe\8.2.0\spring-client-zeebe-8.2.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\camunda\spring\spring-client-common\8.2.0\spring-client-common-8.2.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\camunda\spring\spring-client-annotations\8.2.0\spring-client-annotations-8.2.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\camunda\connector\connector-runtime-util\0.8.0\connector-runtime-util-0.8.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\camunda\feel\feel-engine\1.16.0\feel-engine-1.16.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\scala-lang\scala-library\2.13.10\scala-library-2.13.10.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\lihaoyi\fastparse_2.13\2.3.3\fastparse_2.13-2.3.3.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\lihaoyi\sourcecode_2.13\0.2.3\sourcecode_2.13-0.2.3.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\lihaoyi\geny_2.13\0.6.10\geny_2.13-0.6.10.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\fasterxml\jackson\module\jackson-module-scala_3\2.13.4\jackson-module-scala_3-2.13.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\scala-lang\scala3-library_3\3.0.2\scala3-library_3-3.0.2.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\thoughtworks\paranamer\paranamer\2.8\paranamer-2.8.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\commons-beanutils\commons-beanutils\1.9.4\commons-beanutils-1.9.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\commons-logging\commons-logging\1.2\commons-logging-1.2.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\commons-collections\commons-collections\3.2.2\commons-collections-3.2.2.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\camunda\camunda-operate-client-java\8.1.7.2\camunda-operate-client-java-8.1.7.2.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\httpcomponents\client5\httpclient5\5.1.3\httpclient5-5.1.3.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\httpcomponents\core5\httpcore5\5.1.4\httpcore5-5.1.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\httpcomponents\core5\httpcore5-h2\5.1.4\httpcore5-h2-5.1.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\commons-codec\commons-codec\1.15\commons-codec-1.15.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\httpcomponents\client5\httpclient5-fluent\5.1.3\httpclient5-fluent-5.1.3.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\github\resilience4j\resilience4j-retry\2.0.2\resilience4j-retry-2.0.2.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\github\resilience4j\resilience4j-core\2.0.2\resilience4j-core-2.0.2.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\camunda\zeebe-client-java\8.2.3\zeebe-client-java-8.2.3.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\camunda\zeebe-bpmn-model\8.2.3\zeebe-bpmn-model-8.2.3.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\camunda\bpm\model\camunda-xml-model\7.18.0\camunda-xml-model-7.18.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\fasterxml\jackson\core\jackson-core\2.13.4\jackson-core-2.13.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\fasterxml\jackson\core\jackson-databind\2.13.4\jackson-databind-2.13.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\fasterxml\jackson\core\jackson-annotations\2.13.4\jackson-annotations-2.13.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\camunda\zeebe-gateway-protocol-impl\8.2.3\zeebe-gateway-protocol-impl-8.2.3.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\grpc\grpc-protobuf\1.54.1\grpc-protobuf-1.54.1.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\google\api\grpc\proto-google-common-protos\2.9.0\proto-google-common-protos-2.9.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\grpc\grpc-protobuf-lite\1.54.1\grpc-protobuf-lite-1.54.1.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\google\guava\guava\31.1-jre\guava-31.1-jre.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\google\guava\failureaccess\1.0.1\failureaccess-1.0.1.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\google\guava\listenablefuture\9999.0-empty-to-avoid-conflict-with-guava\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\google\j2objc\j2objc-annotations\1.3\j2objc-annotations-1.3.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\grpc\grpc-stub\1.54.1\grpc-stub-1.54.1.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\google\errorprone\error_prone_annotations\2.18.0\error_prone_annotations-2.18.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\grpc\grpc-core\1.54.1\grpc-core-1.54.1.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\google\android\annotations\4.1.1.4\annotations-4.1.1.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\codehaus\mojo\animal-sniffer-annotations\1.21\animal-sniffer-annotations-1.21.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\perfmark\perfmark-api\0.25.0\perfmark-api-0.25.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\grpc\grpc-api\1.54.1\grpc-api-1.54.1.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\grpc\grpc-context\1.54.1\grpc-context-1.54.1.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\google\code\findbugs\jsr305\3.0.2\jsr305-3.0.2.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\grpc\grpc-netty\1.54.1\grpc-netty-1.54.1.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-codec-http2\4.1.82.Final\netty-codec-http2-4.1.82.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-codec-http\4.1.82.Final\netty-codec-http-4.1.82.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-handler-proxy\4.1.82.Final\netty-handler-proxy-4.1.82.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-codec-socks\4.1.82.Final\netty-codec-socks-4.1.82.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-transport-native-unix-common\4.1.82.Final\netty-transport-native-unix-common-4.1.82.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\google\protobuf\protobuf-java\3.22.3\protobuf-java-3.22.3.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-tcnative-boringssl-static\2.0.54.Final\netty-tcnative-boringssl-static-2.0.54.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-tcnative-classes\2.0.54.Final\netty-tcnative-classes-2.0.54.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-tcnative-boringssl-static\2.0.54.Final\netty-tcnative-boringssl-static-2.0.54.Final-linux-x86_64.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-tcnative-boringssl-static\2.0.54.Final\netty-tcnative-boringssl-static-2.0.54.Final-linux-aarch_64.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-tcnative-boringssl-static\2.0.54.Final\netty-tcnative-boringssl-static-2.0.54.Final-osx-x86_64.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-tcnative-boringssl-static\2.0.54.Final\netty-tcnative-boringssl-static-2.0.54.Final-osx-aarch_64.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-tcnative-boringssl-static\2.0.54.Final\netty-tcnative-boringssl-static-2.0.54.Final-windows-x86_64.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-handler\4.1.82.Final\netty-handler-4.1.82.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-resolver\4.1.82.Final\netty-resolver-4.1.82.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-buffer\4.1.82.Final\netty-buffer-4.1.82.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-transport\4.1.82.Final\netty-transport-4.1.82.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-codec\4.1.82.Final\netty-codec-4.1.82.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\netty\netty-common\4.1.82.Final\netty-common-4.1.82.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\fasterxml\jackson\dataformat\jackson-dataformat-yaml\2.13.4\jackson-dataformat-yaml-2.13.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\camunda\connector\connector-core\0.8.1\connector-core-0.8.1.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\camunda\connector\connector-validation\0.8.1\connector-validation-0.8.1.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\jakarta\validation\jakarta.validation-api\2.0.2\jakarta.validation-api-2.0.2.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\hibernate\validator\hibernate-validator\6.2.5.Final\hibernate-validator-6.2.5.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\jboss\logging\jboss-logging\3.4.3.Final\jboss-logging-3.4.3.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\fasterxml\classmate\1.5.1\classmate-1.5.1.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\camunda\community\zeebe-cherry-runtime\3.0.0\zeebe-cherry-runtime-3.0.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\io\camunda\filestorage\filestorage\1.1.0\filestorage-1.1.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\fasterxml\jackson\datatype\jackson-datatype-jsr310\2.13.4\jackson-datatype-jsr310-2.13.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\google\code\gson\gson\2.9.1\gson-2.9.1.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\boot\spring-boot-starter-data-jpa\2.7.4\spring-boot-starter-data-jpa-2.7.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\boot\spring-boot-starter-aop\2.7.4\spring-boot-starter-aop-2.7.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\aspectj\aspectjweaver\1.9.7\aspectjweaver-1.9.7.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\boot\spring-boot-starter-jdbc\2.7.4\spring-boot-starter-jdbc-2.7.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\zaxxer\HikariCP\4.0.3\HikariCP-4.0.3.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\spring-jdbc\5.3.23\spring-jdbc-5.3.23.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\jakarta\transaction\jakarta.transaction-api\1.3.3\jakarta.transaction-api-1.3.3.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\jakarta\persistence\jakarta.persistence-api\2.2.3\jakarta.persistence-api-2.2.3.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\hibernate\hibernate-core\5.6.11.Final\hibernate-core-5.6.11.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\net\bytebuddy\byte-buddy\1.12.17\byte-buddy-1.12.17.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\antlr\antlr\2.7.7\antlr-2.7.7.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\jboss\jandex\2.4.2.Final\jandex-2.4.2.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\hibernate\common\hibernate-commons-annotations\5.1.2.Final\hibernate-commons-annotations-5.1.2.Final.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\glassfish\jaxb\jaxb-runtime\2.3.6\jaxb-runtime-2.3.6.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\jakarta\xml\bind\jakarta.xml.bind-api\2.3.3\jakarta.xml.bind-api-2.3.3.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\glassfish\jaxb\txw2\2.3.6\txw2-2.3.6.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\sun\istack\istack-commons-runtime\3.0.12\istack-commons-runtime-3.0.12.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\sun\activation\jakarta.activation\1.2.2\jakarta.activation-1.2.2.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\data\spring-data-jpa\2.7.3\spring-data-jpa-2.7.3.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\data\spring-data-commons\2.7.3\spring-data-commons-2.7.3.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\spring-orm\5.3.23\spring-orm-5.3.23.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\spring-tx\5.3.23\spring-tx-5.3.23.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\springframework\spring-aspects\5.3.23\spring-aspects-5.3.23.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\h2database\h2\2.1.214\h2-2.1.214.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\postgresql\postgresql\42.3.7\postgresql-42.3.7.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\checkerframework\checker-qual\3.5.0\checker-qual-3.5.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\chemistry\opencmis\chemistry-opencmis-client-impl\1.1.0\chemistry-opencmis-client-impl-1.1.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\chemistry\opencmis\chemistry-opencmis-client-api\1.1.0\chemistry-opencmis-client-api-1.1.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\chemistry\opencmis\chemistry-opencmis-commons-api\1.1.0\chemistry-opencmis-commons-api-1.1.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\chemistry\opencmis\chemistry-opencmis-commons-impl\1.1.0\chemistry-opencmis-commons-impl-1.1.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\codehaus\woodstox\woodstox-core-asl\4.4.1\woodstox-core-asl-4.4.1.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\codehaus\woodstox\stax2-api\3.1.4\stax2-api-3.1.4.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\chemistry\opencmis\chemistry-opencmis-client-bindings\1.1.0\chemistry-opencmis-client-bindings-1.1.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\cxf\cxf-rt-frontend-jaxws\3.0.12\cxf-rt-frontend-jaxws-3.0.12.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\xml-resolver\xml-resolver\1.2\xml-resolver-1.2.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\asm\asm\3.3.1\asm-3.3.1.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\cxf\cxf-core\3.0.12\cxf-core-3.0.12.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\ws\xmlschema\xmlschema-core\2.2.1\xmlschema-core-2.2.1.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\cxf\cxf-rt-bindings-soap\3.0.12\cxf-rt-bindings-soap-3.0.12.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\cxf\cxf-rt-wsdl\3.0.12\cxf-rt-wsdl-3.0.12.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\cxf\cxf-rt-databinding-jaxb\3.0.12\cxf-rt-databinding-jaxb-3.0.12.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\sun\xml\bind\jaxb-impl\2.1.14\jaxb-impl-2.1.14.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\sun\xml\fastinfoset\FastInfoset\1.2.12\FastInfoset-1.2.12.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\com\sun\xml\bind\jaxb-core\2.1.14\jaxb-core-2.1.14.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\javax\xml\bind\jaxb-api\2.3.1\jaxb-api-2.3.1.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\javax\activation\javax.activation-api\1.2.0\javax.activation-api-1.2.0.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\cxf\cxf-rt-bindings-xml\3.0.12\cxf-rt-bindings-xml-3.0.12.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\cxf\cxf-rt-frontend-simple\3.0.12\cxf-rt-frontend-simple-3.0.12.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\cxf\cxf-rt-ws-addr\3.0.12\cxf-rt-ws-addr-3.0.12.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\cxf\cxf-rt-transports-http\3.0.12\cxf-rt-transports-http-3.0.12.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\cxf\cxf-rt-ws-policy\3.0.12\cxf-rt-ws-policy-3.0.12.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\wsdl4j\wsdl4j\1.6.3\wsdl4j-1.6.3.jar;C:\Users\Pierre-YvesMonnet\.m2\repository\org\apache\neethi\neethi\3.0.3\neethi-3.0.3.jar;D:\atelier\IntelliJ IDEA 2021.3.1\lib\idea_rt.jar" io.camunda.CherryApplication
Connected to the target VM, address: '127.0.0.1:59003', transport: 'socket'

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.7.4)

2023-05-01 17:38:08.148  INFO 35564 --- [           main] io.camunda.CherryApplication             : Starting CherryApplication v3.0.0 using Java 17.0.3.1 on LAPTOP-B6HDDE9H with PID 35564 (C:\Users\Pierre-YvesMonnet\.m2\repository\io\camunda\community\zeebe-cherry-runtime\3.0.0\zeebe-cherry-runtime-3.0.0.jar started by Pierre-YvesMonnet in D:\dev\intellij\community\cherry\zeebe-cherry-simpleexample)
2023-05-01 17:38:08.154  INFO 35564 --- [           main] io.camunda.CherryApplication             : No active profile set, falling back to 1 default profile: "default"
2023-05-01 17:38:15.551  INFO 35564 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2023-05-01 17:38:16.291  INFO 35564 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 711 ms. Found 5 JPA repository interfaces.

.....


2023-05-01 17:38:26.347  INFO 35564 --- [           main] io.camunda.CherryApplication             : Started CherryApplication in 21.314 seconds (JVM running for 24.944)
````

The UI is available via the URL `http://localhost:9091` according the value in the `application.yaml` file

![Cherry Dashboard](doc/CherryDashboard.png?raw=true)

To view only the worker in the dashboard, unselect the toggle "Framework runner"
![Unselect Framework Runner](doc/UnselectFrameworkRunner.png?raw=true)

Only the worker is part of the list
![Cherry Dashboard Filtered](doc/CherryDashboardOnlyWorker.png?raw=true)

# Generate a Docker image

Thanks to Spring, the command generate a Docker image.

## Generate the image 
The project contain a Dockerfile

`````
# docker build -t zeebe-cherry-officepdf:1.0.0 .
FROM openjdk:17-alpine
EXPOSE 8080
COPY target/zeebe-cherry-simpleexample-*-jar-with-dependencies.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar", "io.camunda.CherryApplication"]
`````

To generate the Docker image, execute
````
mvn install
docker build -t zeebe-cherry-simpleexample:1.0.0 .
````

Run it with 
````
docker run -p 8888:9091 zeebe-cherry-simpleexample:1.0.0 .
````
Note: the 9091 is part of the `application.yaml` file. If you change the port number, change it here.

# An execution
To verify the execution, check the process `src/main/resources/SayHello.bpmn`

![Say Hello process](doc/SayHelloProcess.png?raw=true)

This process execute the worker.

Deploy it in your Cluster.
Adapt the `application.yaml` to connect to your Zeebe cluster.
Create a process instance, for the Modeler for example.

Access the dashboard. You see that one execution was processed

![Cherry Dashboard](doc/OneExecutionDashboard.png?raw=true)

In Operation, the execution is visible
![Cherry Operation](doc/OneExecutionOperation.png?raw=true)

Accessing Operate, the result is visible too, and the variable is calculated
![Operate](doc/OneExecutionOperate.png?raw=true)
