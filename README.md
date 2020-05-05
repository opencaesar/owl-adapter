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
Requirements: java 8, node 8.x 
```
    cd owl-adapter
    ./gradlew build
```

## Run as CLI

MacOS/Linux:
```
    ./gradlew oml2owl:run --args="-i path/to/oml/folder -o path/to/owl/folder"
```
Windows:
```
    gradlew.bat oml2owl:run --args="-i path/to/oml/folder -o path/to/owl/folder"
```

## Run from Gradle
Optionally install it in your local maven repo (if you modified it)
```
    ./gradlew install
```
In a gradle.build script, add the following:
```
buildscript {
	repositories {
		mavenLocal()
		maven { url 'https://dl.bintray.com/opencaesar/owl-adapter' }
	}
	dependencies {
		classpath 'io.opencaesar.owl:oml2owl:+'
	}
}

apply plugin: 'io.opencaesar.oml2owl'

oml2owl {
	inputPath = 'path/to/oml/folder'
	outputPath = 'path/to/owl/folder'
}

task build {
	dependsOn generateOwl
}

task clean(type: Delete) {
	delete 'path/to/owl/folder'
}
```

## Release

Replace \<version\> by the version, e.g., 1.2
```
  git tag -a <version> -m "<version>"
  git push origin <version>
```
