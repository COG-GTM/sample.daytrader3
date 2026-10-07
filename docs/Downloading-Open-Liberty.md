There are lots of ways to get your hands on [Open Liberty](https://openliberty.io/). This sample needs an Open Liberty runtime with the following features, which are listed in the `featureManager` block of [server.xml](/daytrader3-ee6-wlpcfg/servers/daytrader3_Sample/server.xml):

* `ejbLite-3.2`
* `jsf-2.2`
* `jaxrs-2.0`
* `jpa-2.1`
* `jmsMdb-3.2`
* `wasJmsServer-1.0`
* `wasJmsClient-2.0`

The application is a Java EE 6 application; these are the Java EE 7 level features that Open Liberty provides, and they run Java EE 6 applications unchanged.

### Let the Maven or Gradle build download Open Liberty (recommended)

You do not need to download anything by hand. The [Liberty Maven plugin][maven-plugin] and the [Liberty Gradle plugin][gradle-plugin] used by this sample download the Open Liberty kernel (`io.openliberty:openliberty-kernel`) from Maven Central, then install the features listed in `server.xml`. The Open Liberty version is set by the `openliberty.version` property in the root [pom.xml](/pom.xml) and by `openLibertyVersion` in [gradle.properties](/gradle.properties).

### Download Open Liberty yourself

To download just the Open Liberty runtime, go to the [Open Liberty downloads page][ol-download]. Choose one of these:

* The *All GA Features* package (also on Maven Central as `io.openliberty:openliberty-runtime`), which already contains every feature the sample needs, or
* The *Kernel* package (`io.openliberty:openliberty-kernel`), then add the sample's features with [featureUtility][featureUtility]:

  ```bash
  $ export WLP_USER_DIR=/path/to/sample.daytrader3/daytrader3-ee6-wlpcfg
  $ /path/to/wlp/bin/featureUtility installServerFeatures daytrader3_Sample
  ```

The *Jakarta EE* and *Web Profile* convenience packages do not contain the Java EE 7 features listed above, so they won't work for this sample.

To build against a Liberty install that you downloaded yourself, run `mvn install -Dliberty.install.dir=/path/to/wlp` or set `libertyRoot=/path/to/wlp` in `gradle.properties`.

You can also get Open Liberty through the IDE using [Liberty Tools](/docs/Using-Liberty-Tools.md).

[ol-download]: https://openliberty.io/start/
[featureUtility]: https://openliberty.io/docs/latest/reference/command/featureUtility-commands.html
[maven-plugin]: https://github.com/OpenLiberty/ci.maven
[gradle-plugin]: https://github.com/OpenLiberty/ci.gradle

## Tips

* The [Open Liberty feature overview](https://openliberty.io/docs/latest/reference/feature/feature-overview.html) lists every available feature and version.
* The [server command reference](https://openliberty.io/docs/latest/reference/command/server-commands.html) describes `server start`, `stop`, `run`, `status` and the other server commands.
