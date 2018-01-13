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
se/bjurr/gitchangelog/internal/settings/Settings.java
╔══════════╤════════════════╤══════════╤══════╤═════════════════════════════════════════════════════════════════════════════════╗
║ Reporter │ Rule           │ Severity │ Line │ Message                                                                         ║
╠══════════╪════════════════╪══════════╪══════╪═════════════════════════════════════════════════════════════════════════════════╣
║ Findbugs │ EI_EXPOSE_REP2 │ INFO     │ 211  │ May expose internal representation by incorporating reference to mutable object ║
║          │                │          │      │                                                                                 ║
║          │                │          │      │                                                                                 ║
║          │                │          │      │   <p> This code stores a reference to an externally mutable object into the     ║
║          │                │          │      │   internal representation of the object.&nbsp;                                  ║
║          │                │          │      │    If instances                                                                 ║
║          │                │          │      │    are accessed by untrusted code, and unchecked changes to                     ║
║          │                │          │      │    the mutable object would compromise security or other                        ║
║          │                │          │      │    important properties, you will need to do something different.               ║
║          │                │          │      │   Storing a copy of the object is better approach in many situations.</p>       ║
╚══════════╧════════════════╧══════════╧══════╧═════════════════════════════════════════════════════════════════════════════════╝

Summary of se/bjurr/gitchangelog/internal/settings/Settings.java
╔══════════╤══════╤══════╤═══════╤═══════╗
║ Reporter │ INFO │ WARN │ ERROR │ Total ║
╠══════════╪══════╪══════╪═══════╪═══════╣
║ Findbugs │ 1    │ 0    │ 0     │ 1     ║
╟──────────┼──────┼──────┼───────┼───────╢
║          │ 1    │ 0    │ 0     │ 1     ║
╚══════════╧══════╧══════╧═══════╧═══════╝


Summary
╔══════════╤══════╤══════╤═══════╤═══════╗
║ Reporter │ INFO │ WARN │ ERROR │ Total ║
╠══════════╪══════╪══════╪═══════╪═══════╣
║ Findbugs │ 27   │ 2    │ 0     │ 29    ║
╟──────────┼──────┼──────┼───────┼───────╢
║          │ 27   │ 2    │ 0     │ 29    ║
╚══════════╧══════╧══════╧═══════╧═══════╝

:violations FAILED
:violations (Thread[main,5,main]) completed. Took 0.148 secs.

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':violations'.
> javax.script.ScriptException: To many violations found, max is 2 but found 29
```

It supports:
 * [_AndroidLint_](http://developer.android.com/tools/help/lint.html)
 * [_Checkstyle_](http://checkstyle.sourceforge.net/)
   * [_Detekt_](https://github.com/arturbosch/detekt) with `--output-format xml`.
   * [_ESLint_](https://github.com/sindresorhus/grunt-eslint) with `format: 'checkstyle'`.
   * [_KTLint_](https://github.com/shyiko/ktlint)
   * [_SwiftLint_](https://github.com/realm/SwiftLint) with `--reporter checkstyle`.
   * [_PHPCS_](https://github.com/squizlabs/PHP_CodeSniffer) with `phpcs api.php --report=checkstyle`.
 * [_CLang_](https://clang-analyzer.llvm.org/)
   * [_RubyCop_](http://rubocop.readthedocs.io/en/latest/formatters/) with `rubycop -f clang file.rb`
 * [_CodeNarc_](http://codenarc.sourceforge.net/)
 * [_CPD_](http://pmd.sourceforge.net/pmd-4.3.0/cpd.html)
 * [_CPPLint_](https://github.com/theandrewdavis/cpplint)
 * [_CPPCheck_](http://cppcheck.sourceforge.net/)
 * [_CSSLint_](https://github.com/CSSLint/csslint)
 * [_DocFX_](http://dotnet.github.io/docfx/)
 * [_Findbugs_](http://findbugs.sourceforge.net/)
 * [_Flake8_](http://flake8.readthedocs.org/en/latest/)
   * [_AnsibleLint_](https://github.com/willthames/ansible-lint) with `-p`
   * [_Mccabe_](https://pypi.python.org/pypi/mccabe)
   * [_Pep8_](https://github.com/PyCQA/pycodestyle)
   * [_PyFlakes_](https://pypi.python.org/pypi/pyflakes)
 * [_FxCop_](https://en.wikipedia.org/wiki/FxCop)
 * [_Gendarme_](http://www.mono-project.com/docs/tools+libraries/tools/gendarme/)
 * [_GoLint_](https://github.com/golang/lint)
   * [_GoVet_](https://golang.org/cmd/vet/) Same format as GoLint.
 * [_JSHint_](http://jshint.com/)
 * _Lint_ A common XML format, used by different linters.
 * [_JCReport_](https://github.com/jCoderZ/fawkez/wiki/JcReport)
 * [_Klocwork_](http://www.klocwork.com/products-services/klocwork/static-code-analysis)
 * [_MyPy_](https://pypi.python.org/pypi/mypy-lang)
 * [_PerlCritic_](https://github.com/Perl-Critic)
 * [_PiTest_](http://pitest.org/)
 * [_PyDocStyle_](https://pypi.python.org/pypi/pydocstyle)
 * [_PyLint_](https://www.pylint.org/)
 * [_PMD_](https://pmd.github.io/)
   * [_Infer_](http://fbinfer.com/) Facebook Infer. With `--pmd-xml`.
   * [_PHPPMD_](https://phpmd.org/) with `phpmd api.php xml ruleset.xml`.
 * [_ReSharper_](https://www.jetbrains.com/resharper/)
 * [_SbtScalac_](http://www.scala-sbt.org/)
 * [_Simian_](http://www.harukizaemon.com/simian/)
 * [_StyleCop_](https://stylecop.codeplex.com/)
 * [_XMLLint_](http://xmlsoft.org/xmllint.html)
 * [_ZPTLint_](https://pypi.python.org/pypi/zptlint)


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
