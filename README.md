# ModeShape performance test framework

This is the official Git repository for the ModeShape performance test framework.

The purpose of this framework is to provide a uniform way of testing performance of JCR 2.0 compliant repositories, together with
a predefined set of performance tests which can be used as benchmarks. It also aims to make it easy to add and run new tests against a variety
of JCR implementations and versions.

## Get the code

The easiest way to get started with the code is to [create your own fork](http://help.github.com/forking/) of this repository and then clone your fork:

	$ git clone git@github.com:<you>/modeshape-performance.git
	$ cd modeshape-performance
	$ git remote add upstream git://github.com/ModeShape/modeshape-performance.git

At any time, you can pull changes from the upstream and merge them onto your master:

	$ git checkout master               # switches to the 'master' branch
	$ git pull upstream master          # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
	$ git push origin                   # pushes all the updates to your fork, which should be in-sync with 'upstream'

The general idea is to keep your local `master` branch in-sync with the `upstream/master`.

## Structure

The framework is a multi-module Maven project. The following modules are part of the test framework:

- `perf-tests-api` - contains the main framework API and in terms of repository dependencies, should only depend on the javax.jcr package
- `perf-tests-report` - contains the code which produces aggregated reports, comparing the runs across all the repositories

The following modules each run the test suite against the latest available [ModeShape](http://modeshape.org) version that matches the pattern in the module name. For example, the first module listed below runs against 3.0.1.Final, while the second runs against 3.1.0.Final.

- `modeshape-3.0.x.Final-tests`
- `modeshape-3.1.x.Final-tests`
- `modeshape-3.2.x.Final-tests`
- `modeshape-3.3.x.Final-tests`
- `modeshape-3.4.x.Final-tests`
- `modeshape-3.5.x.Final-tests`
- `modeshape-3.6.x.Final-tests`
- `modeshape-3.7.x.Final-tests`
- `modeshape-latest-tests` - runs against the current snapshot for the in-development version

The following modules each run the test suite against a specific version of [JackRabbit](http://jackrabbit.apache.org) as denoted in the module name:

- `jackrabbit-2.5.2-tests`
- `jackrabbit-2.7.0-tests`

To test the performance of another JCR 2.0 compliant repository or another version of ModeShape or Jackrabbit, simply copy one of the existing `tests` modules and change it to have the correct dependencies and to initialize the JCR repository. Be sure to add your module to the parent `pom.xml` and as a dependency in `perf-tests-report/pom.xml`.

## Repository configurations

Currently each of the different modules defines two test configurations:

* `local-inmemory` configures and tests a self-contained non-clustered memory-only repository
* `local-filesystem` configures and tests a self-contained non-clustered repository that persists to the local file system

Each repository implementation has support for both configurations, though the file system persistence is implemented quite differently and thus each has different benefits, liabilities, and even configuration settings. Consequently, take caution comparing the performance of these different configurations.

## Usage

To use the framework in its current version, you need to use Maven (3.x or greater). Once you have the source code, you can either run a script that runs all of the tests in each module against all repository configurations, or run the individual Maven command to run all of the tests in each module against a single repository configuration.

To run the script:

    $ bin/run.sh

The following Maven commands are equivalent:

    $ mvn clean install -Plocal-inmemory
    $ mvn clean install -Plocal-filesystem

Note that each Maven command runs a single profile (configuration) at a time.


### Reporting

The reports for each test are placed in `reports/{timestamp}`. This makes it very easy to run lots of tests and keep all of the results. Note that the normal `mvn clean` command does not remove any of the generated reports, so they need to be removed manually.


### Adding new tests

To add a new test, all you need to do is subclass the `org.modeshape.jcr.perftests.AbstractPerformanceTestSuite` class inside the
`perf-tests-api` module.

## Test parameters

There are several files that control how the tests are configured. Under the `perf-tests-api/src/main/resources` are three properties files:

- `runner.properties` - configuration file which controls the global parameters for the test runner

  * `tests.exclude` - a comma separated list of regular expressions, representing the name of the test suites that won't be run. By default, nothing is excluded.
  * `tests.include` - a comma separated list of regular expressions, representing the name of the test suites which are included. By default, all suite are included. Note that this configuration parameter has lower precedence than `tests.exclude`.
  * `repeat.count` - the number of times each test suite is ran against a repository. For meaningful statistical data, this should be greater than 5.
  * `warmup.count` - the number of times each test suite is ran before the performance data will be recorded.

- `testsuite.properties` - configuration file which controls the configuration parameters for the test suites (all of them)

  * `testsuite.config.nodeCount` - the number of nodes which are set-up by default by each suite. Note that there may be suites that ignore this setting.

- `output.properties` - a configuration file that controls where the output for each module is written
  * `test.data.output.folder` - the folder where the raw text data for each test should be placed inside the corresponding test module, relative to the current working directory, which is `${basedir}`. The default is `target/classes/test-data-output`
  * `test.data.output.package` - the package (inside each module jar) where the output data can be located. It is directly related to the above path, and defaults to `test-data-output`
  * `reports.output.folder` - the folder where the graphic reports should be placed, relative to the current working directory, which is `${basedir}`. The default is `reports`.

Then, each module can define a properties file for each configuration, and these are placed in `{module}/src/test/resources` and typically named `{profile-name}.properties`. The content of these files are usually implementation specific, but can also override any of the properties defined above.

