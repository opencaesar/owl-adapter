# OML to OWL

[ ![Download](https://api.bintray.com/packages/opencaesar/adapters/oml2owl/images/download.svg) ](https://bintray.com/opencaesar/adapters/oml2owl/_latestVersion)

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

## Run as Gradle Task
```
buildscript {
    repositories {
        maven { url 'https://dl.bintray.com/opencaesar/owl-adapter' }
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath 'io.opencaesar.owl:oml2owl-gradle:+'
    }
}
task oml2owl(type:io.opencaesar.oml2owl.Oml2OwlTask) {
    inputCatalogPath = file('path/to/input/oml/catalog.xml') [Required]
    outputCatalogPath = file('path/to/output/owl/catalog.xml') [Required]
    disjointUnions = true [Optional, false by default]
    annotationsOnAxioms = true [Optional, false by default]
}               
```
