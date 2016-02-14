
# Deployment Guide  

<div style="float:right;margin-top:-43px;">
    <img src="../resources/images/blox250.png"/>
</div> 


##  Table of Contents  

1. [Quickstart](#tocQuickstart)
  2. [Get YADA](#tocGetYADA) 
  2. [I downloaded the war file](#tocWarDownload)
  3. [I cloned the github repo](#tocGitClone)
  4. [Sanity check](#tocSanity)
  5. [Additional Filesystem Configuration](#tocFilesys)
  6. [Caveats](#tocQuickstartCaveats)
2. [Advanced Topics](#tocAdvanced)  
  3. [YADA Index](#tocYADAIndex)
  4. [Automation](#tocAutomation)
  5. [Server Configuration](#tocServerConfig)
      6. [Linux Environment](#tocUnix) 
      7. [Apache HTTP](#tocApache)
      8. [Tomcat](#tocTomcat)
  9. [Maven](#tocMaven) 
      10. [Environment Variables](#tocVars)
      11. [Build Properties](#tocProps)
      12. [Profiles](#tocProfiles)
      13. [Examples](#tocExamples)
      


<a name="tocQuickstart"></a>
##  Quickstart  

<a name="tocGetYADA"></a>
###  Get YADA  

First, acquire the [YADA-Quickstart] application in one of the following ways:

* Click any of the [YADA-Quickstart] links throughout this document to download the `YADA-Quickstart-6.2.3-SNAPSHOT.war` file, or
* Clone the YADA-Quickstart github [repo](https://github.com/Novartis/YADA-Quickstart)

Refer to the appropriate section below, either [I downloaded the war file](#tocWarDownload) or [I cloned the github repo](#tocGitClone):

<a name="tocWarDownload"></a>
###  I downloaded the war file 

If you cloned from github, skip to the next section.

The downloadable war is pre-built using default values for context paths and resource configurations. Inspect and modify `META-INF/context.xml` as needed. Documentation to assist this effort is inline, in the file.

After conforming `META-INF/context.xml` for your environment, simply drop it into your existing tomcat container.

The pre-built war uses a local [SQLite®] file for the YADA index. This is not likely to be your final implementation–nor should it–however, it is possible to use it to perform sanity checks and more extensive automated testing. 

Refer to the [Sanity Check](#tocSanity) to validate your install. If you have issues, check [Additional Filesystem Configuration](#tocFilesys) and [Caveats](#tocQuickstartCaveats).

<a name="tocGitClone"></a>
###  I cloned the github repo 

You will soon run the maven `package` goal. 

First, however, you have an opportunity to ensure maven builds the war file with the correct contextual values for your environment. Find the `build.properties` file in the appropriate `YADA-Quickstart/src/main/resources/` subdirectory (i.e., `local` or `dev`,) and modify per your environment. 

If the `local` properties file is left unchanged, the default `local` build will create a warfile identical to the downloadable version referenced in the previous section. It is preferable to modify `build.properties` before building, rather than to modify `context.xml` afterward. This is because in the future, you can automate deployment of the warfile using a variety of methods, including maven-cargo-plugin, custom scripts, or continuous integration (CI) services, and you won't want to, or won't be enabled to modify the     `context.xml` between build and deployment. Information about the settings in `build.properties` can be found inline, in the file.

After making your desired changes, simply run `mvn package` in the `YADA-Quickstart` directory.  This will result in a `YADA-Quickstart-6.2.3-SNAPSHOT.war` file in the `target` subdirectory.  This warfile can now be copied to your tomcat container.



<a name="tocSanity"></a>
###  Sanity check  

To confirm YADA is running, access the following url:
`http://host.domain:port/yada.jsp?q=YADA+default&py=true`

Obviously, substitute `host.domain:port` with your environment's values. `q` is the alias for `qname` which points to the named YADA query you will execute. `py` is the alias for `pretty` which "pretty-prints" the JSON string.

Your result should be the a JSON object string containing `YADA is alive!` in your browser.

```
{
  "RESULTSET": {
    "total": 1,
    "ROWS": [{"'YADAISALIVE!'": "YADA is alive!"}],
    "qname": "YADA default",
    "page": "1",
    "records": 1
  },
  "version": "5.1.2_SNAPSHOT"
}
```
<a name="tocFilesys"></a>
###  Additional Filesystem Configuration  

As alluded to above, there are a few filesystem touchpoints that need to be configured. If you've read the comments embedded in `build.properties` and `context.xml` you already know what to expect. The easiest (and default) configuration is to create a filesystem path `/apps/yada` and deploy, create, or link all other relevent directories beneath.

If you are deploying multiple instances on the same server, e.g., for dev and test, you can append the "env" value to the path, e.g., `/apps/yada/local` or `/apps/yada/dev`. This can also be a symlink to it's own directory. 

* `files/in`: this is the base directory for uploads
* `files/out`: this is the base directory for i/o output
* `utils`: this is where YADA service scripts are deployed, e.g., `curlerWS.sh`
* `bin`: this is where script plugins should be deployed
* `tomcat`: this is usually a softlink to `$TOMCAT_HOME`
* `web`: if using apache, this can be a softlnk to `$APACHE_HOME`, or a separate config altogether which shares the binary

Your filesystem should could like this:

```
/
|
+- apps
    |
    +- yada 
        |
        +- (optional env subdir, e.g., local or dev, or symlink to .)
             |
             +- bin
             |
             +- utils
             |
             +- files
             |   |
             |   +- in
             |   |
             |   +- out 
             |
             +- tomcat (probably symlink to $TOMCAT_HOME)
             |
             +- web (probably symlink to $APACHE_HOME)          
```

<a name="tocQuickstartCaveats"></a>
###  Caveats  

####  JDBC Drivers  

[YADA-Quickstart] contains all the necessary jars in `WEB-INF/lib`. However, some versions of Tomcat require JDBC-driver jars to be installed in `$TOMCAT_HOME/lib` instead, probably so they are loaded by a different class loader (not sure why this matters.) In any event, if you're having driver issues, this could be why.

####  Proxies and Authentication  

Some networks use proxy servers and others require the passing of credentials. It is highly unlikely in the initial setup and testing of JDBC queries, that you've encountered any proxy or authentication issues.  Nevertheless, YADA has features to enable the use of proxies to access third party resources via REST requests on a per-request basis.  YADA also enables the inclusion (pass-thru) of named cookies to internally authenticated resources on a per-request basis. If you experience proxy or authentication related issues, consult the [YADA Parameters Reference](params.md), in particular the sections on the [proxy](params.md#proxy) and [cookies](params.md#cookies) params.


<a name="tocAdvanced"></a>
##  Advanced Topics  

<a name="tocYADAIndex"></a>
###  The YADA Index database  

The [YADA-Quickstart] default settings include a [SQLite®] pre-populated implementation of the YADA Index. If you want to use a different database engine, namely [MySQL®], [PostgreSQL®], or [Oracle®], you must have an instance of your desired engine at the ready, and run the appropriate database script. 

> NOTE: An imminent future version of YADA will prefer [ElasticSearch®] for the YADA Index.

If you cloned the YADA-Quickstart github repo you'll find configuration scripts for Oracle®, MySQL®, and PostgreSQL®, and scripts for inserting the essential queries to enable YADA to work, and queries to run the TestNG tests in the following directory:

```
YADA-Quickstart
  |
  +-- src
       |
       +-- main
            |
            +-- resources
                 |
                 +-- YADA_db_MySQL.sql
                 +-- YADA_db_Oracle.sql
                 +-- YADA_db_PostgreSQL.sql
                 +-- YADA_db_SQLite.sql
                 +-- YADA_query_essentials.sql
                 +-- YADA_query_tests.sql
                 +-- YADA.db
```
In addition to the vendor-specific configuration script, one must run `YADA_query_essentials.sql` as well. `YADA_query_tests.sql` need only be executed if it is intended to run the TestNG tests.

<a name="tocAutomation"></a>
###  Automation  

YADA is a versatile platform which can be installed in a variety of environments with a range of options and dependency configurations. For example, an admin may wish to deploy YADA 

* in different Tomcat versions
* in multiple instances on one machine (e.g., dev and test environments)
* remotely, or locally to the build environment
* via scp, or psi-probe, or tomcat-manager 
* using a continuous integration (CI) service
* using tomcat's provided shell scripts to restart the container, or instead, the linux `service` command
* with or without `sudo` protection
* with or without interactive password prompting
* with or without an apache front-end to Tomcat

Just these eight options elicit hundreds of different possibilities.

The deployment script included in the [YADA-Quickstart] provides facilities for a variety of configuration scenarios. This script can be extended to support many other scenarios.  Pull-requests for mods are encouraged.

<table>
  <thead>
    <tr>
      <th colspan="2">env</th>
      <th colspan="4">warfile deployment</th>
      <th colspan="3">container restart</th>
    </tr>
    <tr>
      <th>Local</th>
      <th>Remote</th>
      <th>scp</th>
      <th>cp</th>
      <th>psi-probe</th>
      <th>ssh</th>
      <th>sudo</th>
      <th>script</th>
      <th>service</th>
    </tr>
  </thead>
<tbody>
    <tr>
        <th>√</th>
        <th></th>
        <th></th>
        <th>√</th>
        <th></th>
        <th></th>
        <th>√</th>
        <th>√</th>
        <th></th>
    </tr>
    <tr>
        <th>√</th>
        <th></th>
        <th></th>
        <th>√</th>
        <th></th>
        <th></th>
        <th>√</th>
        <th></th>
        <th>√</th>
    </tr>


    <tr>
        <th>√</th>
        <th></th>
        <th></th>
        <th>√</th>
        <th></th>
        <th></th>
        <th></th>
        <th>√</th>
        <th></th>
    </tr>
    <tr>
        <th>√</th>
        <th></th>
        <th></th>
        <th>√</th>
        <th></th>
        <th></th>
        <th></th>
        <th></th>
        <th>√</th>
    </tr>
    <tr>
        <th></th>
        <th>√</th>
        <th></th>
        <th></th>
        <th>√</th>
        <th>√</th>
        <th>√</th>
        <th>√</th>
        <th></th>
    </tr>
    <tr>
        <th></th>
        <th>√</th>
        <th></th>
        <th></th>
        <th>√</th>
        <th>√</th>
        <th>√</th>
        <th></th>
        <th>√</th>
    </tr>
    <tr>
        <th></th>
        <th>√</th>
        <th></th>
        <th></th>
        <th>√</th>
        <th>√</th>
        <th></th>
        <th>√</th>
        <th></th>
    </tr>
    <tr>
        <th></th>
        <th>√</th>
        <th></th>
        <th></th>
        <th>√</th>
        <th>√</th>
        <th></th>
        <th></th>
        <th>√</th>
    </tr>


    <tr>
        <th></th>
        <th>√</th>
        <th>√</th>
        <th></th>
        <th></th>
        <th>√</th>
        <th>√</th>
        <th>√</th>
        <th></th>
    </tr>
    <tr>
        <th></th>
        <th>√</th>
        <th>√</th>
        <th></th>
        <th></th>
        <th>√</th>
        <th>√</th>
        <th></th>
        <th>√</th>
    </tr>
    <tr>
        <th></th>
        <th>√</th>
        <th>√</th>
        <th></th>
        <th></th>
        <th>√</th>
        <th></th>
        <th>√</th>
        <th></th>
    </tr>
    <tr>
        <th></th>
        <th>√</th>
        <th>√</th>
        <th></th>
        <th></th>
        <th>√</th>
        <th></th>
        <th></th>
        <th>√</th>
    </tr>
</tbody>
</table>

The deployment script is typically executed by the `exec-maven-plugin`. You can change the arguments passed to it by modifying the pom in the `exec-maven-plugin` configuration section:

```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>exec-maven-plugin</artifactId>
  <version>1.3.2</version>
  <configuration>
    <skip>${skip.war.deploy}</skip>
    <executable>/bin/bash</executable>
    <arguments>
      <!-- default arguments are equivalent to '-d psiprobe -r script'-->
      <argument>${project.build.directory}/deploy.sh</argument>
      <!-- uncomment the following to use scp to deploy the war file remotely -->
      <!-- <argument>-d</argument>
      <argument>scp</argument> -->
      <!-- uncomment the following to use cp to deploy the war file locally -->
      <!-- <argument>-d</argument>
      <argument>cp</argument> -->
      <!-- uncomment the following to use linux 'service' command instead of catalina script -->
      <!-- <argument>-r</argument>
      <argument>service</argument> -->
      <!-- uncomment the following 'sudo' arg to use sudo locally or over ssh -->
      <!-- <argument>-s</argument> -->
    </arguments>
  </configuration>
  <executions>
    <execution>
      <id>restart-tomcat</id>
      <phase>pre-integration-test</phase>
      <goals>
        <goal>exec</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

<a name="tocServerConfig"></a>
##  Server Configuration  

<a name="tocUnix"></a>
###  The *nix Environment  

Your environment may need to be conformed or prepared, even with the [YADA-Quickstart].  Here is a typical sequence of steps:

> Note: You may need to `sudo` all these commands.

```sh
# Another reminder to 'sudo'

# if you're using amazon EC2, you may need to install tomcat and maven
yum info tomcat7
yum install tomcat7
yum install tomcat7
yum info maven
wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
yum install -y apache-maven
mvn --version

# create the yada *nix group
groupadd yada

# add the yada user 
useradd -g yada -d /home/yada yada

# set the yada user password
passwd yada # yada-adm1n by default

# add the current user, yada, apache, and tomcat 
# to eachothers' groups
usermod -G tomcat,yada apache
usermod -G apache,yada tomcat
usermod -G apache,tomcat yada
usermod -G apache,tomcat,yada <current_user>

# create the app dir and adjust privs
mkdir -p /apps/yada
chgrp yada /apps/yada
chmod 775 /apps/yada

# switch to the 'yada' user
su - yada

# create the subdirs
cd /apps/yada
mkdir -p bin files/in files/out lib log util

# create symlinks to your env, apache, and tomcat (your tomcat path may differ)
ln -s . local
ln -s /path/to/tomcat7 tomcat
ln -s /path/to/httpd web

# Now it's a good idea to provide sudo NOPASSWD access 
# to the 'yada' group to enable automated execution of 
# tomcat and apache restart.  If you are unfamiliar with
# editing sudoers, please ask for help or consult the
# google.

```

<a name="tocApache"></a>
###  Configuring Apache HTTP  

As stated in the `yada.httpd.conf` file itself:

This a virtual host httpd.conf file for YADA, showing some of the options that one can use when fielding yada requests with apache. 

The main advantage here, other than the standard advantages of fronting tomcat with apache httpd, is support for yada i/o, mapping directories and headers for file transfers without having to rely on symlinks inside the webapp.

You'll notice there are also configs for CORS support, which tomcat could support on its own with the CORS filter, and support for path-style parameters, which tomcat supports with the URLRewrite filter.

Edit the values for your environment and drop it into /path/to/httpd/conf.d and restart apache.

Remember depending on the httpd version, config, etc. the env vars
may be ignored during startup. You may need to replace them with 
absolute values. You also might need to build and install [mod_jk](http://tomcat.apache.org/connectors-doc/webserver_howto/apache.html).

```sh
# copy yada.httpd.conf to deployment directory
cd /apps/yada/web/conf.d
cp ~/YADA-Quickstart-6.2.3-SNAPSHOT/src/main/resources/dev/yada.httpd.conf .

# Edit yada.httpd.conf for your environment.
# Look for 'SET THIS' comments, in particular.  
# In some apache instances, 'SetEnv' directives are honored during 
# configuration time. In others they are not. In some instances, 
# variables are ignored only for certain settings.  See the 
# comments in the file to figure out what needs setting, and check
# your apache error_log as well. Also, apache might not startup if 
# the path to DocumentRoot doesn't exist yet.
vi /apps/yada/web/conf.d/yada.httpd.conf
```

<a name="tocTomcat"></a>
###  Configuring Tomcat  

####  psi-probe  

[psi-probe] is a great Tomcat manager. YADA's `deploy.sh` script supports it–by default, in fact. It is not required for YADA warfile deployment.

Get [psi-probe] at [https://github.com/psi-probe/psi-probe](https://github.com/psi-probe/psi-probe) and follow the directions there. [psi-probe] requires modifications to `tomcat-users.xml`.  This file can be found in the YADA-Quickstart github repo at `src/main/resources/dev`

Assuming you've got `maven` installed and working, here are some commands for getting `psi-probe` working on an Amazon EC2 instance:

```sh
# install git
yum install git

# clone the psi-probe repo
git clone https://github.com/psi-probe/psi-probe

# run the maven package goal
cd psi-probe
mvn package

# copy the war artifact to the webapps dir
cp psi-probe/web/target/probe.war /apps/yada/tomcat/webapps
```

#####  tomcat-users.xml  

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<tomcat-users>
<!--
  NOTE:  By default, no user is included in the "manager-gui" role required
  to operate the "/manager/html" web application.  If you wish to use this app,
  you must define such a user - the username and password are arbitrary.
-->
<!--
  NOTE:  The sample user and role entries below are wrapped in a comment
  and thus are ignored when reading this file. Do not forget to remove
  <!.. ..> that surrounds them.
-->
<!--
  <role rolename="tomcat"/>
  <role rolename="role1"/>
  <user username="tomcat" password="tomcat" roles="tomcat"/>
  <user username="both" password="tomcat" roles="tomcat,role1"/>
  <user username="role1" password="tomcat" roles="role1"/>
-->
  <role rolename="admin" />
  <role rolename="manager" />
  <role rolename="manager-gui"/>
  <role rolename="probeuser" />
  <role rolename="poweruser" />
  <role rolename="poweruserplus" />

  <user username="admin" password="yadapr0be" roles="admin,manager,manager-gui" />
</tomcat-users>
```

<a name="tocMaven"></a>
###  Maven  

####  Intro  

You can effectively just download the [YADA-Quickstart] and drop it into your running Tomcat instance to get started. Though you will likely want to set some custom values for properties, the defaults may work for you. 

If instead, you cloned YADA-Quickstart from the github repo, you likely know it is a [maven] project which relies on the [maven war plugin], and specifically, it's [overlay] feature, to create a [YADA] installation that is specific to your environment. 

Once the repo is cloned to local filesystem, simply update the properties with your environment-specific values and build the project.  

Detailed instructions follow. 

<a name="tocVars"></a>
####  Environment Variables

As elucidated in the `build.properties` and `context.xml` files, there are a few environment variables which can (and should) be used to more securely pass values to the build. These include usernames, password, et al.

In `build.properties`, referenced by deployment and test scripts

```sh
user=${env.YADA_USER}         # the user associated to the deployment
YADA.user=${env.YADA_USER}    # the proxy user if proxy authentication is required (usu only for testing)
YADA.pass=${env.YADA_PASS}    # the proxy password if proxy authentication is required (usu only for testing)
YADA.proxy=${env.YADA_PROXY}  # the proxy host:port, if necessary (usu only for testing)
```

In `src/main/resources/local/build.properties`, the default values specific to the local SQLite® implementation of YADA Index. This is not a secured system.  When a more robust database solution is implemented that requires authentication, the property values below should replace the defaults.

The environment variable substitutions are provided already in `src/main/resources/dev/build.properties` (see below.) 

> If you look at the test configuration in the `yada-war` submodule of the `YADA` project, you'll see multiple configurations for different database engines. That may add clarity to this config, or it may confuse you more.

The properties values are substituted in `src/main/webapp/META-INF/context.xml` during the resources build phase.

```sh
YADA.index.adaptor=${env.YADA_INDEX_ADAPTOR}
YADA.index.url=${env.YADA_INDEX_URL}
YADA.index.validationQuery=${env.YADA_INDEX_VALIDATION_QUERY}
YADA.index.driverClassName=${env.YADA_INDEX_DRIVER_CLASSNAME}
YADA.index.username=${env.YADA_INDEX_USERNAME}
YADA.index.password=${env.YADA_INDEX_PASSWORD}
```

<a name="tocProps"></a>
####  build.properties  

The `build.properties` file is well-documented. Please refer to it for required and optional property settings, necessary for building.

<a name="tocProfiles"></a>
####  Build Profiles

The [pom.xml] is liberally documented. Below is the `profiles` section to illuminate the build logic. The [pom.xml] has a lot more documentation than what is shown below. A key takeaway here is that regarding the `war-deploy` profile: 

- When included, in concert with any profile, will execute the `deploy.sh` script with options as configured in the `exec-maven-plugin` section
- When omitted, the build can be configured to use the `maven-cargo-plugin` instead (quite easily in the `local` profile–good for testing.) 

```xml
<profiles>
  
    <!-- 'local' -->
    <!-- The 'local' profile is active by default. -->
    <!-- Remember that passing any other profile name to maven will override 
      activeByDefault settings, so you will need to include the 'local' profile 
      name if you want to also use 'war-deploy' -->
    <!-- The 'local' profile sets the ${env} property to 'local', which ultimately 
      is translated into the path '/resources/conf/local' in order to use the environment-specific 
      'build.properties' file. 'local' can also turn on 'cargo-deploy' (off by default) and turns off 
      auth testing, assuming that you want to deploy to a local tomcat instance, 
      and that it won't be behind your authentication gateway, so you'll just do 
      noauth, if any, testing. -->
    <profile>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <id>local</id>
      <properties>
        <env>local</env>
        <skip.cargo.deploy>true</skip.cargo.deploy>
        <skip.auth.test>true</skip.auth.test>
      </properties>
    </profile>

    <!-- 'dev' -->
    <!-- The 'dev' profile sets the ${env} property to 'dev', which ultimately 
      is translated into the path '/resources/conf/dev' in order to use the environment-specific 
      'build.properties' file and 'deploy.sh' script. 'dev' does not automatically 
      deploy, in case you want to just run tests without wasting time deploying 
      the same version of the war file -->
    <profile>
      <id>dev</id>
      <properties>
        <env>dev</env>
      </properties>
    </profile>

    <!-- 'test' -->
    <!-- The 'test' profile sets the ${env} property to 'test', which ultimately 
      is translated into the path '/resources/conf/test' in order to use the environment-specific 
      'build.properties' file and 'deploy.sh' script. 'test' does not automatically 
      deploy, in case you want to just run tests without wasting time deploying 
      the same version of the war file -->
    <profile>
      <id>test</id>
      <properties>
        <env>test</env>
      </properties>
    </profile>

    <!-- 'load' -->
    <!-- Maybe you need a second internal implementation pointing to your 
      production environment just for ETL? This is here to illustrate that "sure, 
      you can do that!" -->
    <profile>
      <id>load</id>
      <properties>
        <env>load</env>
        <skip.auth.test>true</skip.auth.test>
        <skip.noauth.test>true</skip.noauth.test>
      </properties>
    </profile>

    <!-- 'prod' -->
    <!-- The 'prod' profile sets the ${env} property to 'prod', which ultimately 
      is translated into the path '/resources/conf/prod' in order to use the environment-specific 
      'build.properties' file for filtering and 'deploy.sh' script for deployment. 
      Unlike other environment settings, no tests are run by default. -->
    <profile>
      <id>prod</id>
      <properties>
        <env>prod</env>
        <skip.auth.test>true</skip.auth.test>
        <skip.noauth.test>true</skip.noauth.test>
      </properties>
    </profile>

    <!-- When war-deploy is active, the exec plugin will execute with designated 
      environment settings. For example the command 'mvn -pdev,war-deploy' will 
      build your warfile, run your /resources/conf/dev/deploy.sh script, and then 
      run both auth and no-auth testing -->
    <profile>
      <id>war-deploy</id>
      <properties>
        <skip.war.deploy>false</skip.war.deploy>
      </properties>
    </profile>
  </profiles>
```

<a name="tocExecOptions"></a>
####  exec-maven-plugin Configuration

This is the section of the pom concerning automated deployment via `deploy.sh`

```xml
      <!-- If you choose to use the included deployment script for non-local 
        environments, this is where it becomes relevant, to redeploy the war file 
        via curl/psiprobe and restart Tomcat. -->
      <!-- To deploy without testing, use 'pre-integration-test' as the goal -->
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>1.3.2</version>
        <configuration>
          <skip>${skip.war.deploy}</skip>
          <executable>/bin/bash</executable>
          <arguments>
            <!-- default arguments are equivalent to '-d psiprobe -r script'-->
            <argument>${project.build.directory}/deploy.sh</argument>
            <!-- uncomment the following to use scp to deploy the war file remotely via scp -->
            <!-- <argument>-d scp</argument> -->
            <!-- uncomment the following to use cp to deploy the war file locally -->
            <!-- <argument>-d cp</argument> -->
            <!-- uncomment the following to use linux 'service' command instead of catalina script -->
            <!-- <argument>-r service</argument> -->
            <!-- uncomment the following 'sudo' arg to use sudo locally or over ssh -->
            <!-- <argument>s</argument> -->
          </arguments>
        </configuration>
        <executions>
          <execution>
            <id>restart-tomcat</id>
            <phase>pre-integration-test</phase>
            <goals>
              <goal>exec</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
```

<a name="tocExamples"></a>
####  Examples  

```c
# build the default 'local' package. This will not deploy anywhere.
mvn package

# build the 'dev' package. 
mvn package -P dev

# build the 'local' package and deploy 
# This uses 'deploy.sh' via 'exec-maven-plugin' (requires configuration–see the pom)
mvn pre-integration-test -P local,war-deploy

# build and deploy to the 'dev' server (or at least, using the 'dev'
# configuration). Again, it uses 'deploy.sh' via 'exec-maven-plugin' (requires configuration–see the pom)
mvn pre-integration-test -P dev,war-deploy

# build, deploy to the 'dev' server and run custom integration tests
mvn verify -P dev,war-deploy

```



[YADA]: URL
[YADA-Quickstart]: ../resources/downloads/YADA-Quickstart-6.2.3-SNAPSHOT.war
[warfile]: ../resources/downloads/YADA-Quickstart-6.2.3-SNAPSHOT.zip
[pom.xml]: ../resources/downloads/pom.xml
[SQLite®]: https://www.sqlite.org/
[MySQL®]: http://dev.mysql.com/
[PostgreSQL®]: http://www.postgresql.org/
[Oracle®]: http://www.oracle.com/index.html
[maven]: https://maven.apache.org/
[maven war plugin]: https://maven.apache.org/plugins/maven-war-plugin/
[overlay]: https://maven.apache.org/plugins/maven-war-plugin/overlays.html
[sparse checkout]: http://schacon.github.io/git/git-read-tree.html#_sparse_checkout
[tutorial]: http://jasonkarns.com/blog/subdirectory-checkouts-with-git-sparse-checkout/
[eclipse]: http://eclipse.org
[ElasticSearch®]: https://www.elastic.co/products/elasticsearch
[psi-probe]: https://github.com/psi-probe/psi-probe
