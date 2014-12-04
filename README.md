![Lucee CFML Server](https://bytebucket.org/lucee-foundation/lucee4/raw/c16535635b7be12bae85f2f6c4aebc1b1539e862/misc/lucee.png?token=e6d732606efd7d1ff969aa12639e0f93a7d27573)

Welcome to the Lucee CFML Server source code repository.

Lucee CFML Server, or simply Lucee, is open source software which implements the general-purpose CFML server-side tag and scripting language, often used to create dynamic websites, web applications and intranet systems. CFML is a dynamic language supporting multiple programming paradigms and runs on the Java Virtual Machine (JVM).
Lucee is a open source projec driven by the [Lucee Foundation](http://www.lucee.org) and contributed by multiple companies.

License/Copyright
-----------------
Lucee is a derivate of the Railo Server (c) Project (Version 4.2) that was published under the GNU Lesser General Public License (LGPL) in Version v2.1 . Lucee is licensed as LGPL v2.1.
The "the Railo Company LCC" (TRC) is copyright owner of the intial code in this project, the "Lucee Foundation" (LE) is copyright owner of all the code they added afterwards and with all sigificant changes to the existing code TRC and LE share the copyright.

Plattform
---------
Lucee is a JVM language and runs on the JVM as a servlet and will work with any servlet container (e.g. Apache Tomcat, Eclipse Jetty) or application server (e.g. JBoss AS, GlassFish). 

Philosophy
----------
A primary aim of Lucee is to provide the functionality of ColdFusion(c) using less resources and giving better performance, and the Lucee team continue to "treat slowness as a bug" as a core development philosophy. Many performance tests have shown Lucee to perform faster than other CFML engines. In addition to this, Lucee attempts to resolve many inconsistencies found in traditional CFML. These are either forced changes in behaviour, or configurable options in the Lucee Administrator.
The Lucee team is always open to feedback and active at CFML community events, and are keen to remind people that Lucee is a community project.


Download
--------
You can download binary builds including the necesary enviroment to run [here](http://www.lucee.org/download)



Building from source
--------------------
The following text assumes that you have basic knowlege of how to use git and ant, if not please first consult the documentation for this tools.

### 1. Before you get started

Before you can start building Lucee from source, you will need a few things installed on your machine:

1. **Java JDK** - since you're going to compile Java code you need the JDK and not just the JRE.  Lucee requires JDK 6 or later in order to compile.  http://www.oracle.com/technetwork/java/javase/downloads/

1. **Apache ANT** - the source code contains several build scripts that will automate the build process for you. you will need ANT installed in order to run these build scripts. http://ant.apache.org/bindownload.cgi

### 2. Get the source code

Lucee's source code is version-controlled with GIT and is hosted on bitbucket.com [Bitbucket](https://bitbucket.org/lucee-foundation/lucee4) (chances are that this is where you're reading this right now ;]).

The repository contains a few branches, with the most important ones being "Master" (current release) and "Develop" (alpha and beta releases).

So simply clone the repository to your local drive withthe following command:
$ git clone https://huan83@bitbucket.org/lucee-foundation/lucee4.git


### 3. Edit /lucee-core/src/lucee/runtime/Info.ini

The build process will generate a patch file that you can deploy as an update to Lucee servers. In order for the patch to work, its version must be higher than the current version on the server that you wish to patch.

You should set the version in **/lucee-java/lucee-core/src/lucee/runtime/Info.ini**

The content of this file will look similar to this:

    [version]
    number=4.3.0.003
    level=os
    state=final
    name=Velvet
    name-explanation=http://en.wikipedia.org/wiki/Velvet_(dog)
    release-date=2015/01/01 00:00:00 CET

Simply edit the value of the number property so that it is higher than the version on the server that you plan to patch


### 6. Run ANT

Open a Command Prompt (or Shell) and change the working directory to the root of the project

    ANT

    TIP: ANT's path must be in the system's executables PATH.

The build process should take a less than a minute.  Once it's finished, you can find the newly built patch files in /dist/**


### Congratulations!  You've just built Lucee from source :)
