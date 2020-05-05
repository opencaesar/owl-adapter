package io.opencaesar.oml2owl

import java.util.ArrayList
import org.gradle.api.Plugin
import org.gradle.api.Project

class GradlePlugin implements Plugin<Project> {
	
    override apply(Project project) {
    	val ^extension = project.extensions.create('oml2owl', Oml2OwlExtension)
        project.getTasks().create("generateOwl").doLast [
	        val args = new ArrayList
		    args += #["-i", project.file(^extension.inputPath).absolutePath]
		    args += #["-o", project.file(^extension.outputPath).absolutePath]
		    if (^extension.disjointUnions) {
		    	args += "-u" 
		    }
		    if (^extension.annotationsOnAxioms) {
		    	args += "-a" 
		    }
		    if (^extension.debug) {
		    	args += "-d" 
		    }
	        App.main(args)
        ]
   }
    
}

class Oml2OwlExtension {
	public var String inputPath
	public var String outputPath
	public var boolean disjointUnions
	public var boolean annotationsOnAxioms
	public var boolean debug
}