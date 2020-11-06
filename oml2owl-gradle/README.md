# OML to OWL

[ ![Download](https://api.bintray.com/packages/opencaesar/owl-adapter/oml2owl-gradle/images/download.svg) ](https://bintray.com/opencaesar/owl-adapter/oml2owl-gradle/_latestVersion)

A tool to translate ontologies from an OML to an OWL representation

## Run as Gradle Task
In a build.gradle script, use:
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