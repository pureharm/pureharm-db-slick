# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

# unreleased

### breaking changes:

- upgraded to `pureharm-db-testkit` 0.2.0` which replaces scalatest w/ munit. See

### dependency upgrades

- [pureharm-core-anomaly](https://github.com/busymachines/pureharm-core/releases) `0.2.0`
- [pureharm-core-sprout](https://github.com/busymachines/pureharm-core/releases) `0.2.0`
- [pureharm-core-identifiable](https://github.com/busymachines/pureharm-core/releases) `0.2.0`

# 0.1.0

Split out from [pureharm](https://github.com/busymachines/pureharm) as of version `0.0.7`.

- cross compiled to Scala 2.13 -- pending support for scala 3.0.0-RC1

:warning: Breaking changes :warning:

- rename `PureharmDBSlickTypeDefinitions` to `PureharmDBSlickAliases`

Deprecations

- deprecated unsafe methods for `Transactor` creation.

Internals:

- move `.internals` package
- tests that hit an actual postgresql db are now moved to the IT task, and not run as part of CI. This will change in the future.
