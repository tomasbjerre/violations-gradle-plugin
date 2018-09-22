# Violations Gradle Plugin
[![Build Status](https://travis-ci.org/tomasbjerre/violations-gradle-plugin.svg?branch=master)](https://travis-ci.org/tomasbjerre/violations-gradle-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.bjurr.violations/violations-gradle-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.bjurr.violations/violations-gradle-plugin)
[![Bintray](https://api.bintray.com/packages/tomasbjerre/tomasbjerre/se.bjurr.violations%3Aviolations-gradle-plugin/images/download.svg)](https://bintray.com/tomasbjerre/tomasbjerre/se.bjurr.violations%3Aviolations-gradle-plugin/_latestVersion)

This is a Gradle plugin for [Violations Lib](https://github.com/tomasbjerre/violations-lib). There is also a [Maven plugin](https://github.com/tomasbjerre/violations-maven-plugin) for this.

It can parse results from static code analysis and:

 * Report violations in the build log.
 * Optionally fail the build depending on violations found.

A snippet of the output may look like this:
```
...
se/bjurr/violations/lib/example/OtherClass.java
╔══════════╤════════════╤══════════╤══════╤════════════════════════════════════════════════════╗
║ Reporter │ Rule       │ Severity │ Line │ Message                                            ║
╠══════════╪════════════╪══════════╪══════╪════════════════════════════════════════════════════╣
║ Findbugs │ MS_SHOULD_ │ INFO     │ 7    │ Field isn't final but should be                    ║
║          │ BE_FINAL   │          │      │                                                    ║
║          │            │          │      │                                                    ║
║          │            │          │      │    <p>                                             ║
║          │            │          │      │ This static field public but not final, and        ║
║          │            │          │      │ could be changed by malicious code or              ║
║          │            │          │      │         by accident from another package.          ║
║          │            │          │      │         The field could be made final to avoid     ║
║          │            │          │      │         this vulnerability.</p>                    ║
╟──────────┼────────────┼──────────┼──────┼────────────────────────────────────────────────────╢
║ Findbugs │ NM_FIELD_N │ INFO     │ 6    │ Field names should start with a lower case letter  ║
║          │ AMING_CONV │          │      │                                                    ║
║          │ ENTION     │          │      │                                                    ║
║          │            │          │      │   <p>                                              ║
║          │            │          │      │ Names of fields that are not final should be in mi ║
║          │            │          │      │ xed case with a lowercase first letter and the fir ║
║          │            │          │      │ st letters of subsequent words capitalized.        ║
║          │            │          │      │ </p>                                               ║
╚══════════╧════════════╧══════════╧══════╧════════════════════════════════════════════════════╝

Summary of se/bjurr/violations/lib/example/OtherClass.java
╔══════════╤══════╤══════╤═══════╤═══════╗
║ Reporter │ INFO │ WARN │ ERROR │ Total ║
╠══════════╪══════╪══════╪═══════╪═══════╣
║ Findbugs │ 2    │ 0    │ 0     │ 2     ║
╟──────────┼──────┼──────┼───────┼───────╢
║          │ 2    │ 0    │ 0     │ 2     ║
╚══════════╧══════╧══════╧═══════╧═══════╝


Summary
╔════════════╤══════╤══════╤═══════╤═══════╗
║ Reporter   │ INFO │ WARN │ ERROR │ Total ║
╠════════════╪══════╪══════╪═══════╪═══════╣
║ Checkstyle │ 4    │ 1    │ 1     │ 6     ║
╟────────────┼──────┼──────┼───────┼───────╢
║ Findbugs   │ 2    │ 2    │ 5     │ 9     ║
╟────────────┼──────┼──────┼───────┼───────╢
║            │ 6    │ 3    │ 6     │ 15    ║
╚════════════╧══════╧══════╧═══════╧═══════╝
```

Example of supported reports are available [here](https://github.com/tomasbjerre/violations-lib/tree/master/src/test/resources).

A number of **parsers** have been implemented. Some **parsers** can parse output from several **reporters**.

| Parser             | Reporter                                                           | Notes
| ---                | ---                                                                | ---
| `ANDROIDLINT`      | [_AndroidLint_](http://developer.android.com/tools/help/lint.html) |
| `CHECKSTYLE`       | [_Checkstyle_](http://checkstyle.sourceforge.net/)                 |
|                    | [_Detekt_](https://github.com/arturbosch/detekt)                   | with `--output-format xml`.
|                    | [_ESLint_](https://github.com/sindresorhus/grunt-eslint)           | with `format: 'checkstyle'`.
|                    | [_KTLint_](https://github.com/shyiko/ktlint)                       |
|                    | [_SwiftLint_](https://github.com/realm/SwiftLint)                  | with `--reporter checkstyle`.
|                    | [_TSLint_](https://palantir.github.io/tslint/usage/cli/)           | with `-t checkstyle`
|                    | [_PHPCS_](https://github.com/squizlabs/PHP_CodeSniffer)            | with `phpcs api.php --report=checkstyle`.
| `CLANG`            | [_CLang_](https://clang-analyzer.llvm.org/)                        |
|                    | [_RubyCop_](http://rubocop.readthedocs.io/en/latest/formatters/)   | with `rubycop -f clang file.rb`
|                    |  [_GCC_](https://gcc.gnu.org/)
|                    | [_ARM-GCC_](https://developer.arm.com/open-source/gnu-toolchain/gnu-rm)
|                    | [_Doxygen_](https://www.stack.nl/~dimitri/doxygen/)
| `CODENARC`         | [_CodeNarc_](http://codenarc.sourceforge.net/)
| `CPD`              | [_CPD_](http://pmd.sourceforge.net/pmd-4.3.0/cpd.html)
| `CPPLINT`          | [_CPPLint_](https://github.com/theandrewdavis/cpplint)
| `CPPCHECK`         | [_CPPCheck_](http://cppcheck.sourceforge.net/)
| `CSSLINT`          | [_CSSLint_](https://github.com/CSSLint/csslint)
| `DOCFX`            | [_DocFX_](http://dotnet.github.io/docfx/)
| `FINDBUGS`         | [_Findbugs_](http://findbugs.sourceforge.net/)
|                    | [_Spotbugs_](https://spotbugs.github.io/)
| `FLAKE8`           | [_Flake8_](http://flake8.readthedocs.org/en/latest/)
|                    | [_AnsibleLint_](https://github.com/willthames/ansible-lint)        | with `-p`
|                    | [_Mccabe_](https://pypi.python.org/pypi/mccabe)
|                    | [_Pep8_](https://github.com/PyCQA/pycodestyle)
|                    |  [_PyFlakes_](https://pypi.python.org/pypi/pyflakes)
| `FXCOP`            | [_FxCop_](https://en.wikipedia.org/wiki/FxCop)
| `GENDARME`         | [_Gendarme_](http://www.mono-project.com/docs/tools+libraries/tools/gendarme/)
| `GOLINT`           | [_GoLint_](https://github.com/golang/lint)
|                    |  [_GoVet_](https://golang.org/cmd/vet/)                            | Same format as GoLint.
| `GOOGLEERRORPRONE` | [_GoogleErrorProne_](https://github.com/google/error-prone)
|                    |  [_NullAway_](https://github.com/uber/NullAway)                    | Same format as Google Error Prone.
| `JSHINT`           | [_JSHint_](http://jshint.com/)
| `LINT`             | _Lint_                                                             | A common XML format, used by different linters.
| `JCREPORT`         | [_JCReport_](https://github.com/jCoderZ/fawkez/wiki/JcReport)
| `KLOCWORK`         | [_Klocwork_](http://www.klocwork.com/products-services/klocwork/static-code-analysis)
| `KOTLINMAVEN`      | [_KotlinMaven_](https://github.com/JetBrains/kotlin)               | Output from Kotlin Maven Plugin.
| `KOTLINGRADLE`     | [_KotlinGradle_](https://github.com/JetBrains/kotlin)              | Output from Kotlin Gradle Plugin.
| `MYPY`             | [_MyPy_](https://pypi.python.org/pypi/mypy-lang)
| `PCLINT`           | [_PCLint_](http://www.gimpel.com/html/pcl.htm)                     | PC-Lint using the same output format as the Jenkins warnings plugin, [_details here_](https://wiki.jenkins.io/display/JENKINS/PcLint+options)
| `PERLCRITIC`       | [_PerlCritic_](https://github.com/Perl-Critic)
| `PITEST`           | [_PiTest_](http://pitest.org/)
| `PYDOCSTYLE`       | [_PyDocStyle_](https://pypi.python.org/pypi/pydocstyle)
| `PYLINT`           | [_PyLint_](https://www.pylint.org/)                                | with `pylint --output-format=parseable`.
| `PMD`              | [_PMD_](https://pmd.github.io/)
|                    |  [_Infer_](http://fbinfer.com/)                                    | Facebook Infer. With `--pmd-xml`.
|                    |  [_PHPPMD_](https://phpmd.org/)                                    | with `phpmd api.php xml ruleset.xml`.
| `RESHARPER`        | [_ReSharper_](https://www.jetbrains.com/resharper/)
| `SBTSCALAC`        | [_SbtScalac_](http://www.scala-sbt.org/)
| `SIMIAN`           | [_Simian_](http://www.harukizaemon.com/simian/)
| `STYLECOP`         | [_StyleCop_](https://stylecop.codeplex.com/)
| `XMLLINT`          | [_XMLLint_](http://xmlsoft.org/xmllint.html)
| `YAMLLINT`         | [_YAMLLint_](https://yamllint.readthedocs.io/en/stable/index.html) | with `-f parsable`
| `ZPTLINT`          | [_ZPTLint_](https://pypi.python.org/pypi/zptlint)

Missing a format? Open an issue [here](https://github.com/tomasbjerre/violations-lib/issues)!

## Usage ##
There is a running example [here](https://github.com/tomasbjerre/violations-gradle-plugin/tree/master/violations-gradle-plugin-example).

Having the following in the build script will make the plugin run with `./gradlew build -i`.

```
buildscript {
 repositories {
  maven { url 'https://plugins.gradle.org/m2/' }
  jcenter()
 }
 dependencies {
  classpath "se.bjurr.violations:violations-gradle-plugin:X"
 }
}

apply plugin: "se.bjurr.violations.violations-gradle-plugin"
apply plugin: 'findbugs'

findbugs {
 ignoreFailures = true
 effort = "max"
 showProgress = true
 reportLevel = "low"
}

task violations(type: se.bjurr.violations.gradle.plugin.ViolationsTask) {
 //
 // Optional config
 //
 maxReporterColumnWidth = 0 // 0 is disabled
 maxRuleColumnWidth = 10
 maxSeverityColumnWidth = 0
 maxLineColumnWidth = 0
 maxMessageColumnWidth = 50


 //
 // Global configuration, remove if you dont want to report violations for
 // the entire repo.
 //
 minSeverity = 'INFO' // INFO, WARN or ERROR
 detailLevel = 'VERBOSE' // PER_FILE_COMPACT, COMPACT or VERBOSE
 maxViolations = 99999999 // Will fail the build if total number of found violations is higher
 printViolations = true // Will print violations found in diff


 //
 // Diff configuration, remove if you dont want to report violations for
 // files changed between specific revisions.
 //
 // diff-properties can be supplied with something like:
 //
 // ./gradlew violations -i -PdiffFrom=e4de20e -PdiffTo=HEAD
 //
 // And in Travis, you could add:
 //
 //  script:
 //   - 'if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then bash ./gradlew check -PdiffFrom=$TRAVIS_PULL_REQUEST_BRANCH -PdiffTo=$TRAVIS_BRANCH ; fi'
 //
 diffFrom = project.properties.diffFrom // Can be empty (ignored), Git-commit or any Git-reference
 diffTo = project.properties.diffTo // Same as above
 diffMinSeverity = 'INFO' // INFO, WARN or ERROR
 diffDetailLevel = 'VERBOSE' // PER_FILE_COMPACT, COMPACT or VERBOSE
 diffMaxViolations = 99 // Will fail the build if number of violations, in the diff within from/to, is higher
 diffPrintViolations = true // Will print violations found in diff
 gitRepo = file('.') // Where to look for Git


 //
 // This is mandatory regardless of if you want to report violations between
 // revisions or the entire repo.
 //
 // Many more formats available, see: https://github.com/tomasbjerre/violations-lib
 violations = [
  ["FINDBUGS",   ".", ".*/findbugs/.*\\.xml\$",   "Findbugs"]
 ]
}

check.finalizedBy violations
```
