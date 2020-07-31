# OML2OWL

[ ![Download](https://api.bintray.com/packages/opencaesar/owl-adapter/oml2owl/images/download.svg) ](https://bintray.com/opencaesar/owl-adapter/oml2owl/_latestVersion)

A tool to translate ontologies from an Oml to an Owl representation

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
--input-path | -i path/to/oml/folder [Required]
--output-path | -o path/to/owl/folder [Required]
--disjoint-unions | -u [Optional]
--annotations-on-axioms | -a [Optional]
```

## [Run as Gradle Task](../oml2owl-gradle/README.md)