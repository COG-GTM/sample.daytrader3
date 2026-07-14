# sample.daytrader3 [![CI](https://github.com/COG-GTM/sample.daytrader3/actions/workflows/ci.yml/badge.svg)](https://github.com/COG-GTM/sample.daytrader3/actions/workflows/ci.yml)

# Java EE6: DayTrader3 Sample

Java EE6 DayTrader3 Sample


This sample contains the DayTrader 3 benchmark, which is an application built around the paradigm of an online stock trading system. The application allows users to login, view their portfolio, lookup stock quotes, and buy or sell stock shares. With the aid of a Web-based load driver such as Apache JMeter, the real-world workload provided by DayTrader can be used to measure and compare the performance of Java Platform, Enterprise Edition (Java EE) application servers offered by a variety of vendors. In addition to the full workload, the application also contains a set of primitives used for functional and performance testing of various Java EE components and common design patterns.

DayTrader is an end-to-end benchmark and performance sample application. It provides a real world Java EE workload. 

## Runtime requirements

This sample builds and runs on **Java 21** (LTS). The build targets Java 21 in both
Maven and Gradle, and the embedded Apache Derby driver has been upgraded to a
current, security-patched line. The application still targets the Java EE 6 (`javax.*`)
programming model on WebSphere Liberty. See [MIGRATION.md](/MIGRATION.md) for the full
Java 8 → Java 21 migration notes.

## Getting Started

Browse the code to see what it does, or build and run it yourself:
* [Building and running on the command line using Maven and Gradle](/docs/Using-cmd-line.md)
* [Building and running using Eclipse and WebSphere Development Tools (WDT)](/docs/Using-WDT.md)
* [Downloading WAS Liberty](/docs/Downloading-WAS-Liberty.md)

### Quick build & test

```bash
# Requires JDK 21 on PATH / JAVA_HOME
mvn clean package
```

This compiles all modules, runs the JUnit 5 regression tests (order creation, quote
retrieval, buy/sell execution math, holdings valuation) and assembles the EAR. It does
not require a Liberty runtime; the Liberty functional tests remain bound to the
integration-test phases.

Once the server has been started, go to [http://localhost:9083/daytrader](http://localhost:9083/daytrader) to interact with the sample.

## Notice

© Copyright IBM Corporation 2015.

## License

```text
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
````
