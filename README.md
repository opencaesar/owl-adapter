# OML Adapter for OWL

[![Build Status](https://travis-ci.org/opencaesar/owl-adapter.svg?branch=master)](https://travis-ci.org/opencaesar/owl-adapter)
[ ![Download](https://api.bintray.com/packages/opencaesar/owl-adapter/oml2owl/images/download.svg) ](https://bintray.com/opencaesar/owl-adapter/oml2owl/_latestVersion)

An [OML](https://github.com/opencaesar/oml) adapter for [OWL2-DL](https://www.w3.org/TR/owl2-syntax/) + [SWRL Rules](https://www.w3.org/Submission/SWRL/)

## Clone
```
    git clone https://github.com/opencaesar/owl-adapter.git
    cd owl-adapter
```
      
## Build
Requirements: java 8, node 8.x, 
```
    cd owl-adapter
    ./gradlew build
```

## Run

MacOS/Linux:
```
    ./gradlew oml2owl:run --args="-i path/to/oml/folder -o path/to/owl/folder"
```
Windows:
```
    gradlew.bat oml2owl:run --args="-i path/to/oml/folder -o path/to/owl/folder"
```

## Release

Replace \<version\> by the version, e.g., 1.2
```
  git tag -a v<version> -m "v<version>"
  git push origin v<version>
```