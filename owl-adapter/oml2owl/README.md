# OML to OWL

[ ![Download](https://api.bintray.com/packages/opencaesar/owl-adapter/oml2owl/images/download.svg) ](https://bintray.com/opencaesar/owl-adapter/oml2owl/_latestVersion)

A tool to translate ontologies from an OML to an OWL representation

## Run as CLI

MacOS/Linux:
```
cd owl-adapter
./gradlew oml2owl:run --args="..."
```
Windows:
```
cd owl-adapter
gradlew.bat oml2owl:run --args="..."
```
Args:
```
--input-catalog-path | -i path/to/input/oml/catalog.xml [Required]
--output-catalog-path | -o path/to/output/owl/catalog.xml [Required]
--disjoint-unions | -u [Optional]
--annotations-on-axioms | -a [Optional]
```

## [Run as Gradle Task](../oml2owl-gradle/README.md)