![Lucee](https://bitbucket.org/repo/rX87Rq/images/3392835614-logo-1-color-black-small.png)

---

### MasterControl Fork Specific Information:

#### How to build:
- Import project as a gradle project.
- Add a file name `gradle.properties` to the root of the project with the following contents:
```
artifactory_user=<artifactory_user_name>
artifactory_password=<artifactory_password>
artifactory_contextUrl=<path_to_labs_artifactory>
release=false    
```
- Run the gradle `build` task
- The compiled artifact can be found in the `dist` folder. The particular file you will likely have interest in is the jar that starts with the version number, this can replace the lucee-core jar in your classpath.

#### How to publish:
- Run the `artifactoryPublish` gradle task.

#### Major Differences Between Mainline Lucee and MasterControl's:
- This fork uses a custom version of [Hibernate 3.5](https://github.com/MasterControlInc/hibernate-orm/tree/lucee-hibernate) which has all the hibernate packages changed from `org.hibernate` to `org.luceehibernate`. The purpose for this is so that a different version of Hibernate can also sit on the classpath at the same time. In order to accomodate this all references to Hibernate in this fork reference `org.luceehibernate`.
- Secondary cache fallback option of EHCache has been removed due to incompatibility with previous change.
---


Welcome to the Lucee Server source code repository.

Lucee Server, or simply Lucee, is a dynamic Java based tag and scripting language used for rapid development from simple to highly sophisticated web applications. Lucee simplifies technologies like webservices (REST,SOAP,HTTP), ORM (Hibernate), searching (Lucene), datasources (MSSQl,Oracle,MySQL ...), caching (infinispan,ehcache,memcached ...) and a lot more. It was never easier to integrate any backend technology with the internet.
Lucee is of course open source (LGPL 2.1) and available for free!

### PLEASE NOTE: The issue tracker has moved to [JIRA](http://issues.lucee.org). ###

License/Copyright
-----------------
Lucee is a derivate of the Railo Server (c) Project (Version 4.2) that was published under the GNU Lesser General Public License (LGPL) in Version v2.1 . Lucee is licensed as LGPL v2.1.
The "Railo Company LCC" (TRC) is copyright owner of the initial code in this project, the "Lucee Association Switzerland" (LAS) is copyright owner of all the code added afterwards and with all significant changes to the existing code TRC and LAS share the copyright.

Platform
---------
Lucee is a JVM language and runs on the JVM as a servlet and will work with any servlet container (e.g. [Apache Tomcat](http://tomcat.apache.org/), [Eclipse Jetty](http://eclipse.org/jetty/)) or application server (e.g. [JBoss AS](http://jbossas.jboss.org/), [GlassFish](https://glassfish.java.net/)).

Philosophy
----------
A primary aim of Lucee is to provide the functionality of ColdFusion(c) using less resources and giving better performance, and the Lucee team continues to "treat slowness as a bug" as a core development philosophy. Many performance tests have shown Lucee to perform faster than other CFML engines. In addition to this, Lucee attempts to resolve many inconsistencies found in traditional CFML. These are either forced changes in behavior, or configurable options in the Lucee Administrator.
The Lucee team is always open to feedback and active at CFML community events, and is keen to remind people that Lucee is a community project.

**Please consult our [documentation](http://docs.lucee.org) for any questions you may have.**
