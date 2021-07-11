# OML to OWL

[![Release](https://img.shields.io/github/v/tag/opencaesar/owl-adapter?label=release)](https://github.com/opencaesar/owl-adapter/releases/latest)

A tool to translate ontologies from an OML to an OWL representation

## Run as CLI

MacOS/Linux:
```
./gradlew oml2owl:run --args="..."
```
Windows:
```
gradlew.bat oml2owl:run --args="..."
```
Args:
```
--input-catalog-path | -i path/to/input/oml/catalog.xml [Required]
--output-catalog-path | -o path/to/output/owl/catalog.xml [Required]
--output-file-extension | -f [optional, default=owl, options: owl, rdf, xml, rj, ttl, n3, nt, trig, nq, trix, fss]
--disjoint-unions | -u [Optional]
--annotations-on-axioms | -a [Optional]
```

## Run as Gradle Task
```
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'io.opencaesar.owl:oml2owl-gradle:+'
    }
}
task oml2owl(type:io.opencaesar.oml2owl.Oml2OwlTask) {
    inputCatalogPath = file('path/to/input/oml/catalog.xml') [Required]
    outputCatalogPath = file('path/to/output/owl/catalog.xml') [Required]
    outputFileExtension = 'owl' [Optional, default=owl, options: owl, rdf, xml, rj, ttl, n3, nt, trig, nq, trix, fss]
    disjointUnions = true [Optional, false by default]
    annotationsOnAxioms = true [Optional, false by default]
}
