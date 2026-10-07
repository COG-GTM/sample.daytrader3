## Building and running the sample using the command line

### Clone Git Repo
:pushpin: [Switch to IDE example](/docs/Using-Liberty-Tools.md/#clone-git-repo)

```bash

$ git clone https://github.com/COG-GTM/sample.daytrader3.git
$ cd sample.daytrader3

```

### Building the sample
:pushpin: [Switch to IDE example](/docs/Using-Liberty-Tools.md/#building-the-sample-in-eclipse)

This sample can be built using either [Gradle](#gradle-commands) or [Maven](#apache-maven-commands). Both builds need JDK 8 or later. The Maven and Gradle wrappers in the repository download the right Maven or Gradle version for you.

Both builds use the Open Liberty [Maven][maven-plugin] or [Gradle][gradle-plugin] plugin. The plugin downloads the Open Liberty kernel (`io.openliberty:openliberty-kernel`) from Maven Central, installs the features listed in `server.xml`, deploys the ear, and starts and stops the server to check that the application starts.

###### [Gradle](https://gradle.org/) commands

```bash
$ ./gradlew build
```

###### [Apache Maven](https://maven.apache.org/) commands

```bash
$ ./mvnw install
```

To build the ear without starting Open Liberty, add `-DskipFVT=true` to the Maven command.

To use an Open Liberty install that you already have instead of downloading one, add `-Dliberty.install.dir=/path/to/wlp` to the Maven command, or set `libertyRoot=/path/to/wlp` in `gradle.properties` for Gradle.

The built ear file is copied into the apps directory of the server configuration located in the daytrader3-ee6-wlpcfg directory:

```text
daytrader3-ee6-wlpcfg
 +- servers
     +- daytrader3_Sample                      <-- specific server configuration
        +- server.xml                          <-- server configuration
        +- apps                                <- directory for applications
           +- daytrader3-ee6.ear                <- sample application
        +- logs                                <- created by running the server locally
        +- workarea                            <- created by running the server locally
```

### Running the application locally
:pushpin: [Switch to IDE example](/docs/Using-Liberty-Tools.md/#running-the-application-locally)

#### Using the Maven or Gradle plugin

After building, start Open Liberty with the plugin. The runtime that was downloaded during the build is reused.

```bash
# Maven
$ cd daytrader3-ee6
$ ../mvnw liberty:start
$ ../mvnw liberty:stop

# Gradle
$ ./gradlew libertyStart
$ ./gradlew libertyStop
```

Use `liberty:run` (Maven) or `libertyRun` (Gradle) to run the server in the foreground instead.

#### Using an Open Liberty install

Pre-requisite: [Download Open Liberty](/docs/Downloading-Open-Liberty.md)

Use the following to start the server and run the application:

```bash
$ export WLP_USER_DIR=/path/to/sample.daytrader3/daytrader3-ee6-wlpcfg
$ /path/to/wlp/bin/featureUtility installServerFeatures daytrader3_Sample
$ /path/to/wlp/bin/server start daytrader3_Sample
```

You only need `featureUtility` if you downloaded the Open Liberty *Kernel* package. The *All GA Features* package already contains the features.

#### Creating the DayTrader database

1.  Open a web browser at "http://localhost:9083/daytrader".
2.  In the web browser, click on the configuration tab.
3.  Click on '(Re)-create  DayTrader Database Tables and Indexes' to create the database.
4.  Click on '(Re)-populate  DayTrader Database' to populate the database.
5.  Restart the server. The application is now ready for use.

```bash
$ /path/to/wlp/bin/server stop daytrader3_Sample
$ /path/to/wlp/bin/server start daytrader3_Sample
```

* `start` runs the server in the background. Look in the logs directory for console.log to see what's going on.
* `stop` stops the server that is running in the background.
* `run` runs the server in the foreground.

```bash
$ tail -f ${WLP_USER_DIR}/servers/daytrader3_Sample/logs/console.log
```

See the [server command reference](https://openliberty.io/docs/latest/reference/command/server-commands.html) for more.

[maven-plugin]: https://github.com/OpenLiberty/ci.maven
[gradle-plugin]: https://github.com/OpenLiberty/ci.gradle
