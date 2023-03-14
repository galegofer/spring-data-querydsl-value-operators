[![Build Status](https://img.shields.io/bitbucket/pipelines/gt_tech/spring-data-querydsl-value-operators.svg)](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/addon/pipelines/home)    [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)   [![Maven Central](https://img.shields.io/maven-central/v/org.bitbucket.gt_tech/spring-data-querydsl-value-operators.svg)](http://search.maven.org/#search|ga|1|g%3A%22org.bitbucket.gt_tech%22%20AND%20a%3A%22spring-data-querydsl-value-operators%22)   [![Javadocs](https://www.javadoc.io/badge/org.bitbucket.gt_tech/spring-data-querydsl-value-operators.svg?label=javadoc)](https://www.javadoc.io/doc/org.bitbucket.gt_tech/spring-data-querydsl-value-operators)

Goal of this component is to provide easy-to-use, reusable SDK [library](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/querydsl-value-operators/) that can be plugged into any Spring framework powered RESTful API which is required to provide a rich Search API supporting complex query clauses.

[Spring Data Querydsl Value Operators](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators) decorates Spring Data's Querydsl integration by providing various value operators to create flexible and rich search (or querying) capabilities on RESTful resources. For folks in hurry, this library decorates on top of **_Spring Data_** and **_Querydsl_** to provide rich, reusable, plug-and-play type of framework for complex searches.

For benefit of readers, [QueryDSL](http://www.querydsl.com/) provides an elegant, unique typesafe queries for variety of persistent stores. This document will not attempt to explain Querydsl and readers are advised to directly visit its website for documentation. Spring framework provides [extensions](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#core.extensions.querydsl) with QueryDSL to allow easier querying mechanism on top if it's powerful [Repository](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#repositories.create-instances.java-config) abstraction, in addition it also provides an easy way for API consumers to follow the API resources object graph and identify searchable input fields. While it's not absolutely mandatory to know those two frameworks before reviewing this documentation or getting started on using this library, but some level of familiarity with those components or the problem domain they operate in would help a lot in appreciating the drivers as well as implementation and usage of this component.

**Other project reports are available _[here](https://gt-tech.bitbucket.io/spring-data-querydsl-value-operators/README.html)_**

# Drivers
Section provides some of the thoughts that went into development of this component both from conceptual and technical perspective.

#### Conceptual perspective
To appreciate the conceptual driver for this component, let's imagine the core business/conceptual problem it attempts to solve for.
Majority of REST APIs in any domain usually required variery of search (or query or lookup) interfaces

* Need for such search API interfaces often starts out small but then grows more complicated as API gains adoption by requiring for inclusion of additional fields into search or requiring additional logical operators like - like, not-like, startsWith etc.

* While highly complicated Search-dedicated systems may utilize an out and out dedicated search platform like [Elastic Search](https://www.elastic.co/) but in the age of microservices, the search functiontioality required on a well defined domain-driven service often may not always require a dedicated search platform.

Most of the usual design/development methods for search interfaces in some shape and form revolves around creating a kind of a domain specific DSL or a query language semantics which is then shared with API consumers. While this can be made to work well but it does come with following additional responsibilities for both consumers and providers:

* API's internal implementation must support search/query DSL and also be backward/forward compatible in event of any future changes to API's schema - e.g. resource model changes.

* If the need for search are non-trivial - for e.g. allows consumers to do and/or clause between different values as well as requires operators like startWith, contains, endsWith, regex etc. the DSL can be relatively complex. In addition to this, the complexity of implementation may also come from security reasons -e.g. black-list or white-list of searchable inputs.

* Since DSL is domain or service specific, every service requires a custom implementation which often is different from scratch reducing the amount of code that can otherwise be re-used.

* API consumers must appreciate the search interface DSL in addition to the responsibility of learning, implementing common REST API, resource structure for business purpose.

To that end, the **core driver** behind this component takes it root from the above and it attempts to solve for following aspects for a general purpose framework for Search API(s):

* Auto-generation of a DSL or Search schema based on the RESTful resource which will be under search.

* Domain agnostic framework that's a plug and play in services while still allowing flexible configurability.

* Write-once but encourage re-use even if search scope grows in a service over a period of time with minimal effort.

* Rich set of operators to allow API consumers to dynamically construct different type of queries on same set of search fields on request-by-request basis.

* Simplicity for API consumers. Since any API consumer is required to appreciate the resource graph and it's internal attributes, a search interface that derives its inputs from the resource graph itself eliminates the need for consumers to understand additional search specific schema/contract as well as also helps API providers in avoiding the responsibility of maintaining a search DSL implementation.


#### Technical perspective

To explain the technical perspective, let's first look at some code snippets to understand examples of different search needs for a simplistic REST resource. Consider following **_User_** resource offered by an API that also exposes a search endpoint on, for e.g. */search/users*.
Can the consumers of API simply use following object graph as search input fields and create variety of searches as explained after the object graph below:
```java
public class User {
  private String id;
  private String username;
  private Profile profile;
  private List<Email> emails;
  // === getter/setters ===
}

public class Email {
  private String address;
  private boolean verified;
  private boolean _default;
  // === getter/setters ===  
}

public class Profile {
  private String firstName;
  private String lastName;
  private String department;
  //=== getter/setters ===
}
```

Some possible example queries on User resource are shown below and they demonstrate different shades of how simple to complex queries can be:
* Find user(s) by userName: */search/users?username=johndoe*

* Find user(s) by email (use the object graph) - */search/users?emails.value=johndoe@awesomemail.com*
* Find users who have emails at either *@company.com* **OR** *@legacycompany.com* domain -
  */search/users?emails.value=endsWith(@company.com)&emails.value=endsWith(@legacycompany.com)*
  This is first example of a value operator coming into play - **_endsWith_**. Also since same search request paramter is provided twice, the underlying querying mechanism should do a Logical OR between the two values while searching persistent store.

* Find users who have both yahoo **AND** gmail emails -
  */search/users?emails.value=endsWith(@yahoo.com)&emails.value=and(endsWith(@gmail.com))*
  This is first instance of how value operators are composed to provide logical AND relationship - **_and(...)_**

* Find all users not in HR department -
  _/search/users?profile.department=ne(hr)_

* Find users in HR department who has last name as "Doe" and have a personal email on their profile -
  */search/users?profile.lastName=eq(Doe)&profile.department=eq(HR)&emails.value=not(contains(@company.com))*.
  _This is a pretty powerful query that performs a **_logical AND_** on three different attributes, however a logical **NOT** operator _composes_ over **_contains_** operator to negate the result and provide all users in HR department having any email other than @company.com domain._

###### Key observations from above query examples are:
1. Search parameter names maps to the Resource graph under search with nested fields utilizing DOT notation *(so no need to explicitly provide documentation on search parameter names with API, consumer has access to API contract and can decipher the search parameter names from it)*
2. The usage of value operators (eq, startsWith, endsWith, contains, ne, not etc.) on search input values and it's subsequent control on the lookup logic.

To accomplish the requirements stated in _Conceptual Perspective_ and example resource/queries mentioned above, the design and implementation of this library builds its foundations on top of following components:

* [Querydsl](http://www.querydsl.com/)
* [Spring Data Querydsl extensions](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#core.extensions.querydsl)

The out of the box capability in the above two frameworks solves for some of the core requirements for this library like automatic generation of search DSL from REST schema, supports more than one underlying datasource, allowing for configurability of search fields but it falls short for few reasons:
* **Static compile time search rules**: The Search rules are defined statically in code. So despite flexibility available for customization, its limitation to compile time only allows for a given search field to be searchable in only one specific way. To understand this better, let's take a look at [this example](https://github.com/spring-projects/spring-data-examples/blob/master/web/querydsl/src/main/java/example/users/UserRepository.java) from Spring data examples. [QuerydslBinderCustomizer](https://docs.spring.io/spring-data/commons/docs/2.0.5.RELEASE/api/org/springframework/data/querydsl/binding/QuerydslBinderCustomizer.html) allows for configurability of search. However, following code _(minor adds on top of what was originally in Spring example for better illustration)_ has statically compiled the logic in code reducing the flexibility. For example, the _2nd binding_ in below code snippet has now blocked the way of allowing API consumers to search for users which are **_not-in_** a specific search code, or else provide first 4 digits of zip code and allow search for **_startsWith_** for that input.
```java
@Override
	default public void customize(QuerydslBindings bindings, QUser root) {

		bindings.bind(String.class).first((StringPath path, String value) -> path.containsIgnoreCase(value));
		bindings.bind(root.address.zip).all((StringPath path, String values) -> path.containsIgnoreCase(value));
		bindings.excluding(root.password);
}
```

* **Operator composition** - Can multiple operators be composed over each other to perform rich searches. For example:
    * _/search?firstName=not(startsWith(Joh))&lastName=not(contains(Doe))_
      Usage of not(..) operator composing over other operators
    * _/search?address.city=eq(Dallas)&address.city=or(eq(Austin))_
      Usage of or(..) in a multi-valued search to indicate to search that user is requesting for either of value to succeed on a resource record to be returned.
      More composition examples were shown earlier in this section and also would be available later in this write-up and/or in example application.

###### Why is this library named as **value-operators or what are value operators?
Since the name of search fields derives from resource graph, using additional prefix/suffix for operators within field names would strip the elegance of Search API to some degree, it was decided to introduce the additional operators within the **input value** of search fields _(e.g.: /uri?**field=operator(value)**....)_ and hence this module is named as _*****value-operators***_


# Features
Library provides various value-operators as defined in further sub-sections.
The syntax of using value operator is shown below. There are also sensible default implicit operators as explained later. The value is wrapped between **operator**(_\<value\>_), for example: _/search_uri/<resource_name>?searchParameter=**_operator_**(value)_

### Value comparison operators
* **eq** - Equal operator
  This operator performs case-sensitive searches (unless the behavior is controlled by collation in underlying data source). _Until v1.0.0, this operator performed case-insensitive searches by default._
  (Default implicit operator for a given field value unless an explicit value operator is specified).

* **ne** - Not-Equal operator.
  Performs case-sensitive searches (unless the behavior is controlled by collation in underlying data source). _Until v1.0.0, this operator performed case-insensitive searches by default._

* **startsWith (also supported in Kebab case starts-with)**
  Operator to perform a search where input value is starting value for the search field/parameter. Performs case-sensitive searches (unless the behavior is controlled by collation in underlying data source). _Until v1.0.0, this operator performed case-insensitive searches by default._

* **endsWith (also supported in Kebab case ends-with)**
  Operator to perform a search where input value is ending value for the search field/parameter. Performs case-sensitive searches (unless the behavior is controlled by collation in underlying data source) _Until v1.0.0, this operator performed case-insensitive searches by default._

* **contains** - *LIKE* operator.
  Operator to perform a search where input value is contained in the target field. Performs case-sensitive searches (unless the behavior is controlled by collation in underlying data source) _Until v1.0.0, this operator performed case-insensitive searches by default._

* **ci** - *Case insensitive* operator.
  Operator can be used to enclose over **eq**, **ne**, **startsWith**, **endsWith**, **contains** operator to affect their behavior to perform case-insensitive searches. For example, ```firstName=ci(contains(john))```

* **matches**
  Operator that takes a regular expression which must be applied while performing lookup/query on target database.

* **gt** *Greater Than*
  Operator applies for for Number fields in resource model and performs a greater-than clause on underlying search records for given field.

* **gte** *Greater Than or Equal*
  Operator applies for for Number fields in resource model and performs a greater-than-or-equal clause on underlying search records for given field.

* **lt** *Less Than*
  Operator applies for for Number fields in resource model and performs a less-than clause on underlying search records for given field.

* **lte** *Less Than or Equal*
  Operator applies for for Number fields in resource model and performs a less-than-or-equal clause on underlying search records for given field.

##### Logical operators
* **or** *OR clause operator*
  If a search parameter is provided multiple times in input with presumably different values, this operator is used to specify that search should have an OR clause between the multiple values. This is the default behavior if an explicit logical operator isn't provided on multi-valued search inputs.

* **and** *AND clause operator*
  If a search parameter is provided multiple input, this operator is used to specify that search should have an AND clause between the multiple values.

* **not** *Negate Operator*
  This operator can be used as composition over single valued comparison operators to invert or negate their result. Query examples earlier in this documentation has demonstrated some possible usage of this operator.

###### Default Operator
* For individual search field, default implicit operator is **eq** if an explicit operator isn't provided with the value, for e.g.: _/search?field=value_ which is as same as _/search/field=**eq**(value)_
* For multi-valued search attribute values, default implicit logical operator between multiple values is **or** unless an explicit operator is provided on second or subsequent values, for e.g.: _/search?field=value1&field=value2_ is same as _/search?field=value1&field=**or**(value2)_

###### Date(time) values
Please note that for enabling operators on date(time) the [Advanced features](#advanced-usage) need to be enabled.

This library relies on the default [ConversionService](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/convert/ConversionService.html) to convert from String parameters to a Date object. The ConversionService uses the [Date.parse(String s)](https://docs.oracle.com/javase/8/docs/api/java/util/Date.html#parse-java.lang.String-) method, which accepts various formats. Please refer to the documentation for the complete overview. A usable notation is shown below.
* MM/DD/YYYY HH:mm:ss zzz

Examples:
* 02/28/2019 15:00:33 UTC
* 03/31/2019 (time can be ommitted, but note that this will be translated to 03/31/2019 00:00:00 UTC, which for equals operations need to exactly match)
* 04/29/2019 16:05:00 CET
###### Supported persistence store/datasources
The core design of this library is offered by introducing an intermediate abstraction layer between two ends of search, **a)** query forming and **b)** query execution in out of box Spring Data Querydsl extensions, hence this library doesn't directly influence the supported persistence types.
**Querydsl** supports _JPA_,  _JDO_, _Lucene_, _Collections_ and _MongoDB_, however the underlying framework of this library - i.e. **_[Spring Data](http://projects.spring.io/spring-data/)_** only supports this on following:

* [NoSQL: MongoDB](https://projects.spring.io/spring-data-mongodb)
* [RDBMS: JPA](https://projects.spring.io/spring-data-jpa)
* [Collections: Key Value](https://github.com/spring-projects/spring-data-keyvalue)

The library has been successfully tested and certified against **JPA** and **MongoDB** based repositories.

###### Black-Listing and/or White-Listing of Search Fields
Using a combination of following methods, users can black-list and/or white-list the search fields (aka Resource attributes) on which they wish to allow searches to be allowed or not. The black-listed attributes are simply ignored from search query formation  at runtime.
* Black-list everything globally on the resource other than what's explicitly included or aliased using [QuerydslBindings.html#excludeUnlistedProperties](https://docs.spring.io/spring-data/commons/docs/2.0.5.RELEASE/api/org/springframework/data/querydsl/binding/QuerydslBindings.html#excludeUnlistedProperties-boolean-)
* Exclude certain search fields, useful when global black-listing of search fields is not enabled. [QuerydslBindings.html#excluding](https://docs.spring.io/spring-data/commons/docs/2.0.5.RELEASE/api/org/springframework/data/querydsl/binding/QuerydslBindings.html#excluding-com.querydsl.core.types.Path...-)
* Include certain search fields, useful when global black-listing of search fields is enabled. [QuerydslBindings.html#including](https://docs.spring.io/spring-data/commons/docs/2.0.5.RELEASE/api/org/springframework/data/querydsl/binding/QuerydslBindings.html#including-com.querydsl.core.types.Path...-). It must be noted that, search path's explicitly aliased using  [AliasingPathBinder#as(alias)](https://docs.spring.io/spring-data/commons/docs/2.0.5.RELEASE/api/org/springframework/data/querydsl/binding/QuerydslBindings.AliasingPathBinder.html#as-java.lang.String-) are automatically included in white-list.

An example of this can also be seen in this library's example application usage of **_customize_** method in [bindings customization](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/examples/mongodb-spring-data-querydsl-value-operators-example/src/main/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/example/dao/EmployeeRepository.java?at=master&fileviewer=file-view-default)
```java
bindings.excluding(root.profile.middleName);
```
# Java version support
*  Java 8+

# Dependencies
The current version of this library builds on top of Spring Boot 2.0.5 release. Following are **_"compile"_** and **_"provided"_** scoped dependency of this library. Additionally there will be more **_"provided"_** scoped dependency which will be required by how default Spring Data sub-modules work. More information on this would be available in later sections as well as couple example applications.

It must be noted that the library is also tested and certified against Spring Boot 1.5.x series by overriding the Spring boot version in example/sample application.

```groovy
com.google.code.findbugs:annotations:jar:2.0.1:compile
org.springframework.data:spring-data-commons:jar:2.0.6.RELEASE:provided
com.google.code.findbugs:jsr305:jar:1.3.9:compile
com.querydsl:querydsl-collections:jar:4.1.4:compile
commons-collections:commons-collections:jar:3.2.1:compile
com.querydsl:querydsl-codegen:jar:4.1.4:compile
commons-lang:commons-lang:jar:2.6:compile
org.apache.commons:commons-lang3:jar:3.7:compile
com.querydsl:querydsl-core:jar:4.1.4:compile
com.mysema.codegen:codegen:jar:0.6.8:compile
com.infradna.tool:bridge-method-annotation:jar:1.13:compile
javax.inject:javax.inject:jar:1:compile
com.mysema.commons:mysema-commons-lang:jar:0.2.4:compile
javax.transaction:jta:jar:1.1:compile
org.springframework:spring-context:jar:5.0.5.RELEASE:provided
org.springframework:spring-core:jar:5.0.5.RELEASE:provided
com.google.guava:guava:jar:18.0:compile
org.springframework:spring-beans:jar:5.0.5.RELEASE:provided
org.slf4j:slf4j-api:jar:1.7.25:compile
org.reflections:reflections:jar:0.9.9:compile
com.querydsl:querydsl-apt:jar:4.1.4:compile
javax.jdo:jdo-api:jar:3.0.1:compile
org.springframework:spring-jcl:jar:5.0.5.RELEASE:provided
javax.servlet:javax.servlet-api:jar:4.0.0:provided
```

# Getting Started & How to Use

### In a hurry or already highly familiar with Querydsl and it's Spring data extensions?
Readers can skip the other sub-sections here and can directly jump to **Example Applications** section below which provides quick-start.

### Including Value Operator SDK library within consuming application
##### Maven
_Spring Data querydsl value operators_ is available through  Maven central repository. Maven users can add below dependency in your POM. Users are always advised to check maven central for latest updates to version.

```xml
<dependency>
    <groupId>org.bitbucket.gt_tech</groupId>
    <artifactId>spring-data-querydsl-value-operators</artifactId>
    <version>x.x.x</version> <!-- 1.0.0 or latest version -->
</dependency>
```

For **snapshot** (or milestone versions), following repository should be added in Maven settings. Users must note that these versions may not be stable due to them being still in active development process.

```xml
<repositories>
    <repository>
        <id>snapshots-repo</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```
##### Building from source
The component can be built directly from source using following commands:
```cmd
$ git clone https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators.git
$ cd querydsl-value-operators
$ mvn -Dskip.checkStyle=true -Dskip.javadocs.generation=true -Pdefault,integration-tests,reporting clean install
```
### Bootstrapping Spring data modules
Most of this SDK's downstream dependency comes from **_[spring-data-common](https://docs.spring.io/spring-data/commons/docs/current/reference/html/)_**, however, it anticipates certain bootstrap dependencies depending on the choice of underlying persistence store. This is also anyway a required step for application's working with Spring data. Following section provides the dependencies required to work with both certified/supported persistence targets:

* **MongoDB**
  Following are required bootstrap dependencies to make Spring data MongoDB as well as Querydsl work. The full working example can be seen in MongoDB based [example application's POM file](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/examples/mongodb-spring-data-querydsl-value-operators-example/pom.xml)
```xml
	<dependency>
		<groupId>com.querydsl</groupId>
		<artifactId>querydsl-mongodb</artifactId>
		<exclusions>
			<exclusion>
				<groupId>org.mongodb</groupId>
				<artifactId>mongo-java-driver</artifactId>
			</exclusion>
		</exclusions>
	</dependency>

	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-data-mongodb</artifactId>
	</dependency>
```
* **JPA**
  Following are required bootstrap dependencies to make Spring data JPA as well as Querydsl work. The full working example can be seen in core libraries E2E Integration test based dependencies Refer [here](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/querydsl-value-operators/pom.xml?at=master&fileviewer=file-view-default) and look for _TEST_ scope only dependencies, the integration test that uses this can be found [here](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/querydsl-value-operators/src/test/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/integration/tests/UsersSearchIT.java?at=master&fileviewer=file-view-default)
```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Actual database driver would replace this in server/production environments -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>com.querydsl</groupId>
        <artifactId>querydsl-jpa</artifactId>
    </dependency>
```

###	Querydsl code generation
Querydsl relies on generating Q-Classes based on target resource model at the time of compilation. These Q-classes allows Querydsl to understand the resource graph and map the search request fields to actual resource to build type-safe queries. The Querydsl [documentation](http://www.querydsl.com/static/querydsl/4.2.1/reference/html_single/) provide information about code-generation through different methods (different annotation and corresponding APT), however for ease for readers, below are two examples for generating code:
* **MongoDB**
  Neither this example and nor generated code is tied to MongoDB, rather it's presumed that the resource/domain model here is annotated with [Document](https://docs.spring.io/spring-data/mongodb/docs/2.0.5.RELEASE/api/org/springframework/data/mongodb/core/mapping/Document.html) annotation since underlying database technology is MongoDB. To that end, there's a APT processor that scans for this annotation and generates the Q-classes. Below is Maven plugin configuration for this, full example can be referred in MongoDB based [example application's POM file](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/examples/mongodb-spring-data-querydsl-value-operators-example/pom.xml?at=master&fileviewer=file-view-default)
```xml
        <plugin>
			<groupId>com.mysema.maven</groupId>
			<artifactId>apt-maven-plugin</artifactId>
			<version>1.1.3</version>
			<dependencies>
				<dependency>
					<groupId>com.querydsl</groupId>
					<artifactId>querydsl-apt</artifactId>
					<!-- Should be aligned with querydsl version at runtime -->
					<version>4.1.4</version>
				</dependency>
			</dependencies>
			<executions>
				<execution>
					<goals>
						<goal>process</goal>
					</goals>
					<phase>generate-sources</phase>
					<configuration>
						<processor>org.springframework.data.mongodb.repository.support.MongoAnnotationProcessor</processor>
						<options>
							<querydsl.logInfo>true</querydsl.logInfo>
							<querydsl.listAccessors>false</querydsl.listAccessors>
							<querydsl.useGetters>true</querydsl.useGetters>
							<querydsl.unknownAsEmbeddable>true</querydsl.unknownAsEmbeddable>
						</options>
					</configuration>
				</execution>
			</executions>
		</plugin>
```

* **JPA or any other target datasource technology**
  This approach uses a general purpose annotation from Querydsl to mark the resource model classes to qualify them for code generation scanning. The annotation utilized is [@QueryEntity](http://www.querydsl.com/static/querydsl/4.0.4/apidocs/com/querydsl/core/annotations/QueryEntity.html) which can be seen as applied on resource model classes [here](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/querydsl-value-operators/src/test/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/integration/model/?at=master). The full working example can be seen in core libraries E2E Integration test based dependencies Refer [here](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/querydsl-value-operators/pom.xml?at=master&fileviewer=file-view-default)  and look for _TEST_ only dependencies, the integration test that uses this can be found [here](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/querydsl-value-operators/src/test/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/integration/tests/UsersSearchIT.java?at=master&fileviewer=file-view-default)
```xml
        <plugin>
            <groupId>com.mysema.maven</groupId>
            <artifactId>apt-maven-plugin</artifactId>
            <version>1.1.3</version>
            <dependencies>
                <dependency>
                    <groupId>com.querydsl</groupId>
                    <artifactId>querydsl-apt</artifactId>
                </dependency>
            </dependencies>
            <executions>
                <execution>
                    <goals>
                        <goal>test-process</goal>
                        <goal>add-test-sources</goal>
                    </goals>
                    <phase>generate-test-sources</phase>
                    <configuration>
                        <outputDirectory>${project.build.directory}/generated-test-sources/querydsl/java
                        </outputDirectory>
                        <!-- Works with QueryEntity annotation -->
                        <processor>com.querydsl.apt.QuerydslAnnotationProcessor</processor>
                        <options>
                            <querydsl.logInfo>true</querydsl.logInfo>
                            <querydsl.listAccessors>false</querydsl.listAccessors>
                            <querydsl.useGetters>true</querydsl.useGetters>
                            <querydsl.unknownAsEmbeddable>true</querydsl.unknownAsEmbeddable>
                        </options>
                    </configuration>
                </execution>
            </executions>
        </plugin>
```

The target output directory for generated code should be added as a source directory to maven build, sample maven definition is shown below:
```xml
	<plugin>
		<groupId>org.codehaus.mojo</groupId>
		<artifactId>build-helper-maven-plugin</artifactId>
		<version>3.0.0</version>
		<executions>
			<execution>
				<id>add-source</id>
				<phase>generate-sources</phase>
				<goals>
					<goal>add-source</goal>
				</goals>
			    <configuration>
					<sources>
						<source>${project.build.directory}/generated-sources/querydsl/java</source>
					</sources>
				</configuration>
			</execution>
		</executions>
	</plugin>
```

##### Advanced: Implication of having a resource model class that directly extends from a type of [java.util.List](https://docs.oracle.com/javase/8/docs/api/java/util/List.html) implementation
Querydsl APT processor in such cases starts to generate [QList](http://www.querydsl.com/static/querydsl/4.0.4/apidocs/com/querydsl/core/types/QList.html) instead of [ListPath](http://www.querydsl.com/static/querydsl/4.0.4/apidocs/com/querydsl/core/types/dsl/ListPath.html) for List-properties in resource model for Q-Classes and there are some undocumented issues [link](https://github.com/querydsl/querydsl/issues/2245) or unknows about how this plays out in end to end search API and field naming conventions. Thus it is encouraged to hide or not annotate those classes of List type so as to have code generation APT processor use ListPath which is well documented as well as works as desired.

##### Advanced: How to apply annotation when resource model is generated from an API contract or modeling like WADL or Open API specification?
* For WADL/XSD based generated sources, custom XJB bindings can be supplied to add annotation for Querydsl APT.
* For OpenAPI specification generated sources, custom mustache template having custom annotation can be supplied to swagger codegen tool.

### Configure Spring Data QueryDSL integration
This section will explain the default integration method that relies on out of box capabilities of Spring Data Querydsl extension, it won't dwell into the integration of this particular library's integration and value it will bring as **_next section_** would explain how this library easily fits on top of out of box integration.

The main components to make a Search API involves:
* **REST controller for search**: User search controller depicited [here - searchUser(..) method](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/examples/mongodb-spring-data-querydsl-value-operators-example/src/main/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/example/dao/EmployeeRepository.java?at=master&fileviewer=file-view-default) provides a view. Though the example here utilizes [Spring Data Rest](https://projects.spring.io/spring-data-rest) but it wasn't necessary. A dedicated [RestController](https://docs.spring.io/spring-framework/docs/5.0.5.RELEASE/javadoc-api/org/springframework/web/bind/annotation/RestController.html) or depending upon nature of application [Controller](https://docs.spring.io/spring-framework/docs/5.0.5.RELEASE/javadoc-api/org/springframework/stereotype/Controller.html) invoking an independent [Repository](https://docs.spring.io/spring-data/commons/docs/2.0.5.RELEASE/api/org/springframework/data/repository/Repository.html) would have done the same trick.
  **Sample snippet** below highlights the method. **Key aspects** of below code snippet is [QuerydslPredicate](https://docs.spring.io/spring-data/commons/docs/2.0.5.RELEASE/api/org/springframework/data/querydsl/binding/QuerydslPredicate.html) annotation which is resolved by [QuerydslPredicateArgumentResolver](https://docs.spring.io/spring-data/commons/docs/2.0.5.RELEASE/api/org/springframework/data/web/querydsl/QuerydslPredicateArgumentResolver.html) to construct the [Predicate](http://www.querydsl.com/static/querydsl/4.1.4/apidocs/com/querydsl/core/types/Predicate.html) with [QueryDslBindings](https://docs.spring.io/spring-data/commons/docs/2.0.5.RELEASE/api/org/springframework/data/querydsl/binding/QuerydslBindings.html) which is dependent on bindings information available to runtime as well as search fields recieved in the request. Note the use of **"root"** property of annotation which indicates the underlying framework on type of resource under search. Underlying framework utilizes this information to locate corresponding Q-classes and utilize the info available in it to construct the predicate.
```java
@RequestMapping(path = {"/search"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = {
            RequestMethod.GET,
            RequestMethod.POST
    })
    default ResponseEntity<Iterable<User>> searchUser(@ApiIgnore @QuerydslPredicate(root = User.class) Predicate
                                                                  predicate,
                                                      Pageable pageable) {
        if (predicate == null || (BooleanBuilder.class.isAssignableFrom(predicate.getClass())
                && !((BooleanBuilder) predicate).hasValue())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(this.findAll(predicate, pageable));
        }
    }
```

* [QuerydslPredicateExecutor](https://docs.spring.io/spring-data/commons/docs/2.0.5.RELEASE/api/org/springframework/data/querydsl/QuerydslPredicateExecutor.html) - This executor provides the ability to execute constructed _Predicate_ in previous step against underlying persistence store. The recommended way to implement this is by extending this interface in corresponding resource's Spring data repository as demonstrated [here](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/examples/mongodb-spring-data-querydsl-value-operators-example/src/main/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/example/dao/EmployeeRepository.java?at=master&fileviewer=file-view-default).

* [QuerydslBinderCustomizer](https://docs.spring.io/spring-data/commons/docs/2.0.5.RELEASE/api/org/springframework/data/querydsl/binding/QuerydslBinderCustomizer.html) - This customizer allows user to specify the bindings to be used while forming and subsequently executing search queries. Binding controls the type of searches that can occur on specific fields as well as configuration of black-listed, white-listed search fields as explained earlier in the document. The recommended way to define this customizer is by extending this interface in corresponding resource's Spring data repository as demonstrated [here](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/examples/mongodb-spring-data-querydsl-value-operators-example/src/main/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/example/dao/EmployeeRepository.java?at=master&fileviewer=file-view-default). Note the use of Q-class corresponding to the resource repository supports in generics for this interface. **Note** that there are default bindings available in the framework which are as follows:
    * Default binding rules:
        * Object on simple properties as **_eq_**.
        * Object on collection like properties as **_contains_**.
        * Collection on simple properties as **_in_**.

These rules can be customized by providing a default implementation of customizer as demonstrated in code snippet below _(within implementation of QuerydslBinderCustomizer)_
```java
@Override
	default public void customize(QuerydslBindings bindings, QUser root) {
		bindings.excluding(root.password);
		bindings.bind(root.userName)
                .all((path, values) -> path.equals(value));
        bindings.bind(root._id)
                .all((path, values) -> path.equals(value));
        bindings.bind(root.profile.firstName)
                .all((path, values) -> path.containsIgnoreCase(value));
        bindings.bind(root.profile.lastName)
                .all((path, values) -> path.containsIgnoreCase(value));
        // ...... more bindings
        // ......
}
```
That's all what's needed for an out of box configuration. As it is visible, above bindings are static in nature and now only supports limited type of search logic on specific fields. Next section will explain how these bindings can be delegated to this library's capability to bring powerful rich value-operators in play.

### Integrate Querydsl Value Operators SDK offered by this library
This section will bring minor modifications on top of previous section code snippet in bindings customization. This will also demonstrate how easy it is to integrate this library and plug-play to make searches more powerful. All that's required is to modify the bindings to delegate to an [ExpressionProvider](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/querydsl-value-operators/src/main/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/ExpressionProvider.java?at=master&fileviewer=file-view-default) from this library as also demonstrated in [example application](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/examples/mongodb-spring-data-querydsl-value-operators-example/src/main/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/example/dao/EmployeeRepository.java?at=master&fileviewer=file-view-default)

```java
    @Override
    default void customize(QuerydslBindings bindings, QUser root) {

        bindings.bind(root.userName)
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
        bindings.bind(root._id)
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));

        bindings.bind(root.profile.firstName)
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
        bindings.bind(root.profile.lastName)
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));

        // Demonstration of how a certain attribute or search parameter can be compared on single-value level for
        // indicating the fact that search interface doesn't expect API consumer to provide multiple values for this
        // parameter and if that happens, it will use only the first value for querying.
        // Since in case of using single-valued the value passed to it is wrapped in Optional monad and
        // ExpressionProvider has a contract that value passed to it must not be Optional so it is unwrapped here.
        bindings.bind(root.profile.age)
                .firstOptional((path, optionalValue) -> ExpressionProviderFactory.getPredicate(path, optionalValue
                        .orElseGet(() -> null)));


        bindings.bind(root.emails.any().address)
                .as("emails.address")
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
        // Not Required for StringPath values but it's recommended to register
        // alias(es) explicitly.
        // ListPath won't work without alias anyway easily or as elegantly..
        ExpressionProviderFactory.registerAlias(root.emails.any().address, "emails.address");
        
        bindings.bind(root.status)
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
        bindings.bind(root.jobData.department)
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
        bindings.bind(root.jobData.location)
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));

        /*
         * Demonstration of black-listing non-searchable parameters/fields
         */
        bindings.excluding(root.profile.middleName);

        /*
         * Another mechanism for black-listing unlisted properties. Note this
         * may require explicit addition of including(..) paths for other than
         * where an explicit alias is provided.
         */
        // bindings.excludeUnlistedProperties(true);
    }
```

And that's it, that's all the application will require to bring in value operators in play for powerful searches. Library currently supports String, Number, Enum and DateTime based property comparisons both in single value property or within Lists. This is achieved by support of [StringPath](http://www.querydsl.com/static/querydsl/4.0.4/apidocs/com/querydsl/core/types/dsl/StringPath.html), [NumberPath](http://www.querydsl.com/static/querydsl/4.0.4/apidocs/com/querydsl/core/types/dsl/NumberPath.html), [EnumPath](http://www.querydsl.com/static/querydsl/4.0.4/apidocs/com/querydsl/core/types/dsl/EnumPath.html) and [DateTimePath](http://www.querydsl.com/static/querydsl/4.0.4/apidocs/com/querydsl/core/types/dsl/DateTimePath.html) as well as their usage within [ListPath](http://www.querydsl.com/static/querydsl/4.0.4/apidocs/com/querydsl/core/types/dsl/ListPath.html) inside list-properties.

*Note* for **Spring Boot 1.x** users, _ExpressionProviderFactory#getPredicate(..)_ method returns an _Optional\<Predicate\>_ which is as per the contract for Spring Boot 2.x. To make this work with Spring Boot 1.x, users will be required to perform a get() operation on returned Optional, as an example:
```java
bindings.bind(root.profile.firstName)
                .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values).orElseGet( null ));
``` 

**Pagination** support is from out of the box capabilities from Spring data and this library's provided functionality doesn't affect it. It's demonstrated in [example application](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/examples/mongodb-spring-data-querydsl-value-operators-example/src/main/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/example/dao/EmployeeRepository.java)

For **List** based properties, it's required to apply an easier alias as demonstrated by **_"emails.address"_** example in above snippet. Otherwise, there's a defect in underlying Spring framework, that prevents the use of these fields as desired in searches. Moreover, it also provides an easier to use search field by end-consumer which also semantically aligns with resource graph.

[ExpressionProviderFactory](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/querydsl-value-operators/src/main/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/ExpressionProviderFactory.java?at=master&fileviewer=file-view-default) is primary entry-point into the SDK library which delegates to appropriate implementation of [ExpressionProvider](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/querydsl-value-operators/src/main/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/ExpressionProvider.java?at=master) based on provided [Path](http://www.querydsl.com/static/querydsl/4.0.4/apidocs/com/querydsl/core/types/Path.html)

**Note** that for more complex querying on _NumberPath_, _EnumPath_ and _DateTimePath_ some **advanced extensions** are offered by this library that are also equally easy to enable and is discussed in next section.

#Advance usage and considerations
Value operators work seemlessly on String based properties/fields. However these operators do not work well with non-string values like Number or Enum since by default [QuerydslPredicateArgumentResolver](https://docs.spring.io/spring-data/data-commons/docs/current/api/org/springframework/data/web/querydsl/QuerydslPredicateArgumentResolver.html) that resolves annotation [QuerydslPredicate](https://docs.spring.io/spring-data/data-commons/docs/current/api/org/springframework/data/querydsl/binding/QuerydslPredicate.html), which is used to annotate search handling method on RESTful method (aka RestController methods), performs **strong-typing** as per the guiding design principle of _Querydsl_, i.e. it attempts to convert the value(s) recieved from HTTP request to exact type defined in corresponding Q-Classes. This works well without value operators and is inline with Querydsl promise of allowing type-safe queries however hinders the path for value-operators to do their trick. This section provides an overview of how to configure certain experimental components from this library to get around this. Users of this library whose search inputs are limited to String properties can ignore reading this section further.

*QuerydslPredicateArgumentResolver* uses [ConversionService](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/convert/ConversionService.html) for type-conversion. Since conversion of String to Enum or String to Integer is core to Spring's dependency injection, it isn't advisable to change those default built-in converters (**_never_** do it). The library provides an experimental combination of a [BeanPostProcessor](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/config/BeanPostProcessor.html) and a [ServletFilter](https://docs.oracle.com/javaee/6/api/javax/servlet/Filter.html) that can be explicitly configured in target application's context to disable the strong type-conversion attempted by *QuerydslPredicateArgumentResolver*.

##### Use case 1: For non-String properties, consuming application requires users to only use limited operators that require absolute property values
\
For example, assume UserStatus is an enum
```java
public class User {
  private String userName;
  private UserStatus status;
  .....
  ......
}

public enum UserStatus {
  ACTIVE,
  LOCKED;
  ....
  ....
}
```

API provider wants its consumers to be able to perform following searches:
*_/search/users?status=LOCKED_* which is same as *_/search/users?status=eq(LOCKED)_* or *_/search/users?status=ne(LOCKED)_* but it **doesn't** prefer it's consumers to be able to perform a search like **_/search/users?status=contains(LOC)_** or **_/search/users?status=startsWith(LOC)_**, then only the experimental filter is required to be configured. The difference between allowed and disallowed example is that allowed search parameters utilizes absolute value that will successfully go through type-conversion, however the disallowed search parameters will fail the type-conversion due to presence of incomplete value (in case of Enum).

Configuring below bean in Spring's *ApplicationContext* would allow for above allowed searches to occur:

```java
    @Bean
    public FilterRegistrationBean querydslHttpRequestContextAwareServletFilter() {
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new QuerydslHttpRequestContextAwareServletFilter
                               (querydslHttpRequestContextAwareServletFilterMappings()));
        bean.setAsyncSupported(false);
        bean.setEnabled(true);
        bean.setName("querydslHttpRequestContextAwareServletFilter");
        /* URI pattern this filter should invoke */
        bean.setUrlPatterns(Arrays.asList(new String[]{"/search/*"}));
        bean.setOrder(Ordered.LOWEST_PRECEDENCE);
        return bean;
    }

    private Map<String, Class<?>> querydslHttpRequestContextAwareServletFilterMappings() {
        Map<String, Class<?>> mappings = new HashMap<>();
        /*
        Mapping of URI to corresponding resource class under Search.
        */
        mappings.put("/api/search/users", User.class);
        return mappings;
    }
```

**[QuerydslHttpRequestContextAwareServletFilter](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/querydsl-value-operators/src/main/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/experimental/QuerydslHttpRequestContextAwareServletFilter.java?at=master&fileviewer=file-view-default)** decorates the incoming HttpServletRequest such that the operators are removed from request by the time it hits the Spring controllers and **QuerydslPredicateArgumentResolver** and thus any type-conversion attempted by resolver will successfully happen provided API consumer has provided a valid value in request.
Internal layers (read, _ExpressionProvider_) of this library during phase of constructing _Predicate_ or while forming querying logic, would consult with a shared storage between *QuerydslHttpRequestContextAwareServletFilter* and _ExpressionProvider_ to retrieve original parameter values _(as supplied by client and containing value operators)_ before forming the actual search expressions.
The shared storage mentioned here uses a ThreadLocal based sharing method so it's critical for DAO/Repository method to execute in same thread as **QuerydslHttpRequestContextAwareServletFilter** executed (classic but rightfully scrutinized thread-per-request model). Otherwise application is required to manually promote the **[QuerydslHttpRequestContext](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/querydsl-value-operators/src/main/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/experimental/QuerydslHttpRequestContext.java?at=master)**. Check **[QuerydslHttpRequestContextHolder](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/a5f95f7ed0d7f5bfe3fbda2dfb672572d8326290/querydsl-value-operators/src/main/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/experimental/QuerydslHttpRequestContextHolder.java?at=master)** and it's available strategy **[QuerydslHttpRequestContextHolderStrategy](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/a5f95f7ed0d7f5bfe3fbda2dfb672572d8326290/querydsl-value-operators/src/main/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/experimental/QuerydslHttpRequestContextHolderStrategy.java?at=master)**.

Example application demonstrates the [usage](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/examples/mongodb-spring-data-querydsl-value-operators-example/src/main/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/example/spring/QueryDslValueOperatorsConfig.java?at=master) of this filter through **_querydslHttpRequestContextAwareServletFilter(..)_** method/bean.


====

##### Use case 2: For non-String properties, consuming application requires users to be able utilize full range of value of operators
\
Building on top of _use-case 1_, this approach solves for use-cases where API provider is flexible to allow API consumers to perform searches like **_/search/users?status=contains(LOC)_** or **_/search/users?status=startsWith(LOC)_** on top of what's mentioned in _use-case 1_. This approach also attempts to work around the limitation of _use-case 1_ where a thread-per-request model or else manual promotion of **QuerydslHttpRequestContext** was required as in case of reactive programming or custom thread-pools.

This approach works in a way that it installs a *BeanPostProcessor* which disables the strong type-conversion attempted by *QuerydslPredicateArgumentResolver* by overriding the default bean created by spring-data-commons and creating one of it's own and in that process, it injects a **no-op** *ConversionService* into overridden *QuerydslPredicateArgumentResolver*. The resolver on not finding appropriate converters in ConversionService simply packs the values as String and passes it further downstream and thus allowing the lower-layers to recieve the value operators and process them. This approach may be frowned upon (reason it's in *experimental* package) as it violates the core strong-typing (type-safe) searches Querydsl promises. However, *no pain no gain*, it's a little cost to pay to obtain advanced value-operators to be usable on non-string properties with it's full range. Also, it's worth be noted that most of persistent stores do not distinguish between Enum and String and treat them equivalently.

For this solution, following bean alone for [QuerydslPredicateArgumentResolverBeanPostProcessor](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/querydsl-value-operators/src/main/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/experimental/QuerydslPredicateArgumentResolverBeanPostProcessor.java) in consuming application Spring ApplicationContext would do the trick. The required dependency *QuerydslBindingsFactory* is already provided by Spring data commons.
```java
    @Bean
    public QuerydslPredicateArgumentResolverBeanPostProcessor querydslPredicateArgumentResolverBeanPostProcessor
            (QuerydslBindingsFactory factory) {
        return new QuerydslPredicateArgumentResolverBeanPostProcessor(factory);
    }
}
```

Note that a delegate ConversionService can also be provided for scenarios when advanced search ability is required on fields of type like Date which can be natively performed by Spring data when proper bindings are defined. See the Spring context configurations in example application for more details. Below is a snippet for such cases:
```java
    /**
	 * Note the use of delegate ConversionService which comes handy for types like
	 * java.util.Date for handling powerful searches natively with Spring data.
	 * @param factory QuerydslBindingsFactory instance
	 * @param conversionServiceDelegate delegate ConversionService
	 * @return
	 */
	@Bean
	public QuerydslPredicateArgumentResolverBeanPostProcessor querydslPredicateArgumentResolverBeanPostProcessor(
			QuerydslBindingsFactory factory, DefaultFormattingConversionService conversionServiceDelegate) {
		return new QuerydslPredicateArgumentResolverBeanPostProcessor(factory, conversionServiceDelegate);
	}
}
```

Note that when this _BeanPostProcessor_ is enabled, _QuerydslHttpRequestContextAwareServletFilter_ is no longer mandatory and can be disabled.

Example application  demonstrates the [usage](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/examples/mongodb-spring-data-querydsl-value-operators-example/src/main/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/example/spring/QueryDslValueOperatorsConfig.java?at=master) of this _BeanPostProcessor_ through **_querydslPredicateArgumentResolverBeanPostProcessor(..)_** method/bean.

# Example applications
* An end to end application built for MongoDB is provided [here](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/examples/mongodb-spring-data-querydsl-value-operators-example/?at=master)

* For JPA based example, readers are advised to look at Integration test - [UsersSearchIT](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/querydsl-value-operators/src/test/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/integration/tests/UsersSearchIT.java?at=master) in core library. The entire supporting classes for integration test are in [this package](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/src/master/querydsl-value-operators/src/test/java/org/bitbucket/gt_tech/spring/data/querydsl/value/operators/integration/?at=master). Integration test can be directly invoked in IDE or if executed via Maven **_integration-tests_** profile must be activated.

# Bugs and feature requests
Have a bug or a feature request? Please use [issue tracker](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/issues?status=new&status=open) to raise a request.

# Contributing
Contributions are highly appreciated, it is encouraged to submit a [PULL request](https://bitbucket.org/gt_tech/spring-data-querydsl-value-operators/pull-requests/).
Contributors must ensure that existing test cases pass (or are modified to adjust to their changes) and appropriate documentation changes are accompanied with pull request.
For integration tests, _integration-tests_ maven profile must be activated during build process.

# LICENSE
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.