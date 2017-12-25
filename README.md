# Violations Gradle Plugin
[![Build Status](https://travis-ci.org/tomasbjerre/violations-gradle-plugin.svg?branch=master)](https://travis-ci.org/tomasbjerre/violations-gradle-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.bjurr.violations/violations-gradle-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.bjurr.violations/violations-gradle-plugin)
[![Bintray](https://api.bintray.com/packages/tomasbjerre/tomasbjerre/se.bjurr.violations%3Aviolations-gradle-plugin/images/download.svg)](https://bintray.com/tomasbjerre/tomasbjerre/se.bjurr.violations%3Aviolations-gradle-plugin/_latestVersion)

This is a Gradle plugin for [Violations Lib](https://github.com/tomasbjerre/violations-lib).

It can parse results from static code analysis and:

 * Report violations in the build log.
 * Optionally fail the build depending on violations found.

A snippet of the output may look like this:
```
...
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

:violations FAILED
:violations (Thread[main,5,main]) completed. Took 0.148 secs.

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':violations'.
> javax.script.ScriptException: To many violations found, max is 2 but found 15
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

Here is an example: 

```
buildscript {
 repositories {
  maven { url 'https://plugins.gradle.org/m2/' }
  jcenter()
 }
 dependencies {
  classpath "se.bjurr.violations:violations-gradle-plugin:1.1"
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
 minSeverity = 'INFO'
 detailLevel = 'VERBOSE' // PER_FILE_COMPACT, COMPACT or VERBOSE
 maxViolations = 99999999
 
 // Many more formats available, see: https://github.com/tomasbjerre/violations-lib
 violations = [
  ["FINDBUGS",   ".", ".*/findbugs/.*\\.xml\$",   "Findbugs"]
 ]
}

check.finalizedBy violations
```
