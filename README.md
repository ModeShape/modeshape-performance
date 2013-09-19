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
- `modeshape-latest-tests` - runs against the current snapshot for the in-development version

The following modules each run the test suite against a specific version of [JackRabbit](http://jackrabbit.apache.org) as denoted in the module name:

- `jackrabbit-2.5.2-tests`
- `jackrabbit-2.7.0-tests`

To test the performance of another JCR 2.0 compliant repository or another version of ModeShape or Jackrabbit, simply copy one of the existing `tests` modules and change it to have the correct dependencies and to initialize the JCR repository. Be sure to add your module to the parent `pom.xml` and as a dependency in `perf-tests-report/pom.xml`.


## Usage

To use the framework in its current version, you need to use Maven (2.x or greater). Once you have the source code, all you need
to run is `mvn clean install` from the parent module (which means that all the tests against all the repositories will be run).

### Reporting

Once the test have been run, the following reports are generated:

- `<module-name>/target/classes/test-data-output/perf-report.txt` - a plain text file, which contains some statistical information (5 number summary and standard deviation) for each test run
- `perf-tests-report/reports/yyyy-MM-dd_hh-mm-ss - a set of box charts for each test run, generated using http://informationandvisualization.de/blog/box-plot.

## Tests

The performance tests are grouped into several subpackages inside `org.modeshape.jcr.perftests`:

- init - contains the tests which measure initialization performance
- query - contains the tests which measure query performance
- read - contains the tests which measure read performance
- write - contains the tests which measure write performance

### Adding new tests

To add a new test, all you need to do is subclass the `org.modeshape.jcr.perftests.AbstractPerformanceTestSuite` class inside the
`perf-tests-api` module.

## Configuration

The following configuration options are available for tweaking the framework:

- `runner.properties` - configuration file which controls the global parameters for the test runner

  * `tests.exclude` - a comma separated list of regular expressions, representing the name of the test suites that won't be run. By default, nothing is excluded.
  * `tests.include` - a comma separated list of regular expressions, representing the name of the test suites which are included. By default, all suite are included. Note that this configuration parameter has lower precedence than `tests.exclude`.
  * `repeat.count` - the number of times each test suite is ran against a repository. For meaningful statistical data, this should be greater than 5.
  * `warmup.count` - the number of times each test suite is ran before the performance data will be recorded.

- `testsuite.properties` - configuration file which controls the configuration parameters for the test suites (all of them)

  * `testsuite.config.nodeCount` - the number of nodes which are set-up by default by each suite. Note that there may be suites that ignore this setting.
