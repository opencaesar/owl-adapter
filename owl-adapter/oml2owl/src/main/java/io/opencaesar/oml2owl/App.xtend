package io.opencaesar.oml2owl

import com.beust.jcommander.IParameterValidator
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import java.io.File
import java.util.ArrayList
import java.util.Collection
import java.util.HashMap
import io.opencaesar.oml.dsl.OmlStandaloneSetup
import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.resource.XtextResourceSet
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI

class App {

	@Parameter(
		names=#["--input","-i"], 
		description="Location of OML input folder (Required)",
		validateWith=FolderPath, 
		required=true, 
		order=1)
	package String inputPath = null

	@Parameter(
		names=#["--output", "-o"], 
		description="Location of the OWL2 output folder", 
		validateWith=FolderPath, 
		order=2
	)
	package String outputPath = "."

	@Parameter(
		names=#["-d", "--debug"], 
		description="Shows debug logging statements", 
		order=3
	)
	package boolean debug

	@Parameter(
		names=#["--help","-h"], 
		description="Displays summary of options", 
		help=true, 
		order=4) package boolean help

	val LOGGER = LogManager.getLogger(App)

	def static void main(String ... args) {
		val app = new App
		val builder = JCommander.newBuilder().addObject(app).build()
		builder.parse(args)
		if (app.help) {
			builder.usage()
			return
		}
		if (app.debug) {
			val appender = LogManager.getRootLogger.getAppender("stdout")
			(appender as AppenderSkeleton).setThreshold(Level.DEBUG)
		}
		if (app.inputPath.endsWith('/')) {
			app.inputPath = app.inputPath.substring(0, app.inputPath.length-1)
		}
		if (app.outputPath.endsWith('/')) {
			app.outputPath = app.outputPath.substring(0, app.outputPath.length-1)
		}
		app.run()
	}

	def void run() {
		LOGGER.info("=================================================================")
		LOGGER.info("                        S T A R T")
		LOGGER.info("=================================================================")
		LOGGER.info("Input Folder= " + inputPath)
		LOGGER.info("Output Folder= " + outputPath)

		val inputFolder = new File(inputPath)
		val inputFiles = collectOMLFiles(inputFolder)
		
		OmlStandaloneSetup.doSetup()
		val inputResourceSet = new XtextResourceSet

		val ontologyManager = OWLManager.createOWLOntologyManager()
		val owl2api = new OwlApi(ontologyManager)
		val outputFiles = new HashMap
		
		val oml2owl = new HashMap

		for (inputFile : inputFiles) {
			val inputURI = URI.createFileURI(inputFile.absolutePath)
			val inputResource = inputResourceSet.getResource(inputURI, true)
			if (inputResource !== null) {
				LOGGER.info("Reading: "+inputURI)
				var relativePath = outputPath+'/'+inputFolder.toURI().relativize(inputFile.toURI()).getPath()
				val outputFile = new File(relativePath.substring(0, relativePath.lastIndexOf('.')+1)+'owl2')
				val ontology = new Oml2Owl(inputResource, owl2api).run
				outputFiles.put(outputFile, ontology)
				oml2owl.put(inputResource, ontology)
			}
		}
		
		oml2owl.filter[true].forEach[resource, ontology |
			new CloseBundle(resource, ontology, owl2api).run
		]		
		
		val resourceURL = ClassLoader.getSystemClassLoader().getResource("opencaesar.io/Oml.oml")
		val resource = inputResourceSet.getResource(URI.createURI(resourceURL.toString), true)
		val outputFile = new File(outputPath+"/opencaesar.io/Oml.owl2")
		outputFiles.put(outputFile, new Oml2Owl(resource, owl2api).run)
		

		// save the output resources
		for (outputEntry : outputFiles.entrySet) {
			val file = outputEntry.key
			val ontology = outputEntry.value
			LOGGER.info("Saving: "+file)
			ontologyManager.saveOntology(ontology, /*new TurtleDocumentFormat, */IRI.create(file))
		}

		LOGGER.info("=================================================================")
		LOGGER.info("                          E N D")
		LOGGER.info("=================================================================")
	}

	def Collection<File> collectOMLFiles(File directory) {
		val omlFiles = new ArrayList<File>
		for (file : directory.listFiles()) {
			if (file.isFile) {
				if (getFileExtension(file) == "oml" && !file.canonicalPath.contains("www.w3.org")) {
					omlFiles.add(file)
				}
			} else if (file.isDirectory) {
				omlFiles.addAll(collectOMLFiles(file))
			}
		}
		return omlFiles
	}

	private def String getFileExtension(File file) {
        val fileName = file.getName()
        if(fileName.lastIndexOf(".") != -1)
        	return fileName.substring(fileName.lastIndexOf(".")+1)
        else 
        	return ""
    }

	static class FolderPath implements IParameterValidator {
		override validate(String name, String value) throws ParameterException {
			val directory = new File(value)
			if (!directory.isDirectory) {
				throw new ParameterException("Parameter " + name + " should be a valid folder path");
			}
	  	}
	}
	
}
