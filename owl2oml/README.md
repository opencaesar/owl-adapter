# OWL to OML

[![Release](https://img.shields.io/github/v/tag/opencaesar/owl-adapter?label=release)](https://github.com/opencaesar/owl-adapter/releases/latest)

A tool to translate ontologies from an OWL to an OML representation

## Run as CLI

MacOS/Linux:
```
./gradlew owl2oml:run --args="..."
```
Windows:
```
gradlew.bat owl2oml:run --args="..."
```
Args:
```
--input-catalog-path | -i path/to/input/oml/catalog.xml [Required]
--output-catalog-path | -o path/to/output/owl/catalog.xml [Required]
--source-path | -s path/to/oml/folder [Required]
--input-file-extension | -if [optional, default=ttl, options: owl, rdf, xml, rj, ttl, n3, nt, trig, nq, trix, jsonld]
--output-file-extension | -of [optional, default=oml, options: oml, omlxmi, omljson]
```

## Run as Gradle Task
```
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'io.opencaesar.adapters:owl2oml-gradle:+'
    }
}
task owl2oml(type:io.opencaesar.owl2oml.Owl2OmlTask) {
    inputCatalogPath = file('path/to/input/oml/catalog.xml') [Required]
    outputCatalogPath = file('path/to/output/owl/catalog.xml') [Required]
    sourcePaths = [ file('src/oml') ] [Required]
    inputFileExtensions = ['ttl'] [Optional, default=ttl, options: owl, rdf, xml, rj, ttl, n3, nt, trig, nq, trix, jsonld]
    outputFileExtension = 'oml' [Optional, default=oml, options: oml, omlxmi, omljson]
}
