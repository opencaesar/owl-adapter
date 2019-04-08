# OML Adapter for OWL

[![Gitpod - Code Now](https://img.shields.io/badge/Gitpod-code%20now-blue.svg?longCache=true)](https://gitpod.io#https://github.com/opencaesar/owl-adapter)
[![Build Status](https://travis-ci.org/opencaesar/owl-adapter.svg?branch=master)](https://travis-ci.org/opencaesar/owl-adapter)
[ ![Download](https://api.bintray.com/packages/opencaesar/owl-adapter/io.opencaesar.owl2oml/images/download.svg) ](https://bintray.com/opencaesar/owl-adapter/io.opencaesar.owl2oml/_latestVersion)

An [OML](https://github.com/opencaesar/oml-language-server) adapter for [OWL2-DL](https://www.w3.org/TR/owl2-syntax/) + [SWRL Rules](https://www.w3.org/Submission/SWRL/)

## Clone
```
    git clone https://github.com/opencaesar/owl-adapter.git
```
      
## Build
Requirements: java 8, node 8.x, 
```
    cd owl-adapter
    cd io.opencaesar.owl.adapter/
	./gradlew clean build
```

## Run

MacOS/Linux:
```
    cd owl-adapter
    cd io.opencaesar.owl.adapter/
    ./gradlew io.opencaesar.oml2owl:run --args="-i path/to/oml/folder -o path/to/owl/folder"
```
Windows:
```
    cd owl-adapter
    cd io.opencaesar.owl.adapter/
    gradlew.bat io.opencaesar.oml2owl:run --args="-i path/to/oml/folder -o path/to/owl/folder"
```
