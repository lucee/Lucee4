![Lucee CFML Server](https://bitbucket.org/lucee/lucee/downloads/logo.png)

Welcome to the Lucee Server source code repository.

Lucee Server, or simply Lucee, is a dynamic Java based tag and scripting language used for rapid development from simple to highly sophisticated web applications. Lucee simplifies technologies like webservices (REST,SOAP,HTTP), ORM (Hibernate), searching (Lucene), datasources (MSSQl,Oracle,MySQL ...), caching (infinispan,ehcache,memcached ...) and a lot more. It was never easier to integrate any backend technology with the internet.
Lucee is of course open source (LGPL 2.1) and available for free!

License/Copyright
-----------------
Lucee is a derivate of the Railo Server (c) Project (Version 4.2) that was published under the GNU Lesser General Public License (LGPL) in Version v2.1 . Lucee is licensed as LGPL v2.1.
The "the Railo Company LCC" (TRC) is copyright owner of the initial code in this project, the "Lucee Association Switzerland" (LAS) is copyright owner of all the code added afterwards and with all significant changes to the existing code TRC and LAS share the copyright.

Plattform
---------
Lucee is a JVM language and runs on the JVM as a servlet and will work with any servlet container (e.g. Apache Tomcat, Eclipse Jetty) or application server (e.g. JBoss AS, GlassFish). 

Philosophy
----------
A primary aim of Lucee is to provide the functionality of ColdFusion(c) using less resources and giving better performance, and the Lucee team continue to "treat slowness as a bug" as a core development philosophy. Many performance tests have shown Lucee to perform faster than other CFML engines. In addition to this, Lucee attempts to resolve many inconsistencies found in traditional CFML. These are either forced changes in behavior, or configurable options in the Lucee Administrator.
The Lucee team is always open to feedback and active at CFML community events, and are keen to remind people that Lucee is a community project.


Download
--------
You can download binary builds including the necessary environment to run [here](https://bitbucket.org/lucee/lucee/downloads)



Building from source
--------------------
The following text assumes that you have basic knowlege of how to use git and ant, if not please first consult the documentation for this tools.

### 1. Before you get started

Before you can start building Lucee from source, you will need a few things installed on your machine:

1. **Java JDK** - since you're going to compile Java code you need the JDK and not just the JRE.  Lucee requires JDK 6 or later in order to compile.  http://www.oracle.com/technetwork/java/javase/downloads/

1. **Apache ANT** - the source code contains several build scripts that will automate the build process for you. you will need ANT installed in order to run these build scripts. http://ant.apache.org/bindownload.cgi

### 2. Get the source code

Lucee's source code is version-controlled with GIT and is hosted on bitbucket.com [Bitbucket](https://bitbucket.org/lucee/lucee) (chances are that this is where you're reading this right now ;]).

The repository contains a few branches, with the most important ones being "Master" (current release) and "Develop" (alpha and beta releases).

So simply clone the repository to your local drive with the following command:
$ git clone https://huan83@bitbucket.org/lucee/lucee.git


### 3. Edit /lucee-core/src/lucee/runtime/Info.ini

The build process will generate a patch file that you can deploy as an update to Lucee servers. In order for the patch to work, its version must be higher than the current version on the server that you wish to patch.

You should set the version in **/lucee-java/lucee-core/src/lucee/runtime/Info.ini**

The content of this file will look similar to this:

    [version]
    number=4.5.0.042
    level=os
    state=final
    name=Neo
    name-explanation=https://www.facebook.com/neo.cfm
    release-date=2015/01/01 00:00:00 CET

Simply edit the value of the number property so that it is higher than the version on the server that you plan to patch.
the "release-date" is set by the build script automaticly, so you dont have to set that.

It is not necessary to set the release date, this is made by the build process. 

### 6. Run ANT

Open a Command Prompt (or Shell) and change the working directory to the root of the project

    ANT

    TIP: ANT's path must be in the system's executables PATH.

The build process should take a less than a minute.  Once it's finished, you can find the newly built patch files in /dist/**


### Congratulations!  You've just built Lucee from source :)