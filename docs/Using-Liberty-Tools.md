## IDE / Liberty Tools

[Liberty Tools][liberty-tools] are the Open Liberty developer tools for Eclipse, Visual Studio Code and IntelliJ IDEA. They find the Maven and Gradle projects in your workspace that use the Liberty plugins, and let you start, stop and debug Open Liberty in [dev mode][dev-mode] from the IDE.

Liberty Tools also provides:

* content-assist for server configuration (`server.xml`, `server.env` and `bootstrap.properties`), which helps you find the features and settings you need and spots typos.
* hot deployment of application and configuration changes in dev mode, so you can test your changes locally without a full build and server restart.

This guide uses [Liberty Tools for Eclipse][liberty-tools-eclipse], which you can install from the [Eclipse Marketplace][liberty-tools-marketplace]. See [Liberty Tools for VS Code][liberty-tools-vscode] and [Liberty Tools for IntelliJ IDEA][liberty-tools-intellij] for the other IDEs.

*Note: Start Eclipse with a full JDK, not a JRE.*

[liberty-tools]: https://openliberty.io/docs/latest/develop-liberty-tools.html
[dev-mode]: https://openliberty.io/docs/latest/development-mode.html
[liberty-tools-eclipse]: https://github.com/OpenLiberty/liberty-tools-eclipse
[liberty-tools-marketplace]: https://marketplace.eclipse.org/content/liberty-tools
[liberty-tools-vscode]: https://github.com/OpenLiberty/liberty-tools-vscode
[liberty-tools-intellij]: https://github.com/OpenLiberty/liberty-tools-intellij

### Clone Git Repo
:pushpin: [Switch to cmd line example](/docs/Using-cmd-line.md/#clone-git-repo)

If the sample git repository hasn't been cloned yet, Eclipse has git tools integrated into the IDE:

1.  Open the Git repositories view
    * *Window -> Show View -> Other*
    * Type "git" in the filter box, and select *Git Repositories*
2.  Copy Git repo url by finding the textbox under "HTTPS clone URL" at the top of this page, and select *Copy to clipboard*
3.  In the Git repositories view, select the hyperlink `Clone a Git repository`
4.  The git repo url should already be filled in.  Select *Next -> Next -> Finish*
5.  The "sample.daytrader3 [master]" repo should appear in the view

### Building the sample in Eclipse
:pushpin: [Switch to cmd line example](/docs/Using-cmd-line.md/#building-the-sample)

This sample can be built using either [Gradle](#building-with-gradle) or [Maven](#building-with-maven).

#building-with-gradle
#### Building with [Gradle](http://gradle.org/)

###### Import Gradle projects into Eclipse

This assumes you have the Gradle [Buildship](https://projects.eclipse.org/projects/tools.buildship) tools installed into Eclipse.

1. In the Git Repository view, expand the daytrader3 repo to see the "Working Directory" folder
2. Right-click on this folder, and select *Copy path to Clipboard*
3. Select menu *File -> Import -> Gradle -> Gradle Project*
4. In the *Project root directory* folder textbox, Paste in the repository directory.
5. Click *Next* twice
6. Five projects should be listed in the *Gradle project structure* click *Finish*
7. This will create 6 projects in Eclipse: sample.daytrader3, daytrader3-ee6, daytrader3-ee6-ejb, daytrader3-ee6-rest, daytrader3-ee6-web, daytrader3-ee6-wlpcfg
8. Go to the *Gradle Tasks* view in Eclipse and navigate to the *sample.daytrader3* project
9. Double click on the *eclipse* task to generate all the Eclipse files
10. In the *Enterprise Explorer* view in Eclipse right click on the five projects mentioned in step 7 and click refresh

:star: *Note:* If you did not use Eclipse to clone the git repository, follow from step 3, but navigate to the cloned repository directory rather than pasting its name in step 4.

###### Run Gradle build

1. Go to the *Gradle Tasks* view in Eclipse and navigate to the *sample.daytrader3* project
2. Double click: build

#building-with-maven
#### Building with [Maven](http://maven.apache.org/)

###### Import Maven projects into Eclipse

1.  In the Git Repository view, expand the daytrader3 repo to see the "Working Directory" folder
2.  Right-click on this folder, and select *Copy path to Clipboard*
3.  Select menu *File -> Import -> Maven -> Existing Maven Projects*
4.  In the Root Directory textbox, Paste in the repository directory.
5.  Select *Browse...* button and select *Finish* (confirm it finds 6 pom.xml files)
6.  This will create 6 projects in Eclipse: sample.daytrader3, daytrader3-ee6, daytrader3-ee6-ejb, daytrader3-ee6-rest, daytrader3-ee6-web, daytrader3-ee6-wlpcfg

:star: *Note:* If you did not use Eclipse to clone the git repository, follow from step 3, but navigate to the cloned repository directory rather than pasting its name in step 4.

###### Run Maven install

1. Right-click on sample.daytrader3/pom.xml
2. *Run As > Maven build...*
3. In the *Goals* section enter "install"
4. Click *Run*

### Running the application locally
:pushpin: [Switch to cmd line example](/docs/Using-cmd-line.md/#running-the-application-locally)

The Liberty Maven and Gradle plugins download Open Liberty for you, so you don't need to download a runtime first. If you want to use an Open Liberty install that you already have, see [Downloading Open Liberty](/docs/Downloading-Open-Liberty.md).

###### Running Liberty from the Liberty Tools view

1. Build the sample once (see above), so the ejb, web and rest modules are built for the ear project.
2. Open the Liberty Tools view by clicking the Liberty icon on the toolbar.
3. The `daytrader3-ee6` project is listed because it uses the Liberty Maven or Gradle plugin.
4. Right-click `daytrader3-ee6` and select *Start*. Liberty Tools starts Open Liberty in dev mode in a terminal in Eclipse, using the `daytrader3_Sample` server configuration from the `daytrader3-ee6-wlpcfg` project.
5. To stop the server, right-click `daytrader3-ee6` and select *Stop*, or type `q` and press Enter in the dev mode terminal.

You can also right-click the `daytrader3-ee6` project in the Project Explorer and use *Run As* to find the Liberty launch shortcuts.

###### Running Liberty with Maven or Gradle goals

If you don't want to use dev mode, you can start and stop the server with the plugin goals instead:

* Maven: right-click `daytrader3-ee6/pom.xml`, select *Run As > Maven build...*, and enter `liberty:start` (or `liberty:stop`) as the goal.
* Gradle: in the *Gradle Tasks* view, run `libertyStart` (or `libertyStop`) in the `daytrader3-ee6` project.

###### Running the sample application and populating the database

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

1.  Start the server as described above.
2.  Open a web browser at "http://localhost:9083/daytrader/".
3.  In the web browser, click on the configuration tab.
4.  Click on '(Re)-create  DayTrader Database Tables and Indexes' to create the database.
5.  Click on '(Re)-populate  DayTrader Database' to populate the database.
6.  Restart the server. The application is now ready for use.

#### Tips

* When importing the existing maven project into Eclipse, Eclipse will (by default) "helpfully" add this project to an (extraneous) ear. To turn this off, go to Preferences -> Java EE -> Project, and uncheck "Add project to an EAR" before you import the project. If you forgot to do this, just delete the ear project; no harm.
