package io.opencaesar.oml2owl

import com.beust.jcommander.IParameterValidator
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.google.common.io.CharStreams
import io.opencaesar.oml.VocabularyBundle
import io.opencaesar.oml.dsl.OmlStandaloneSetup
import io.opencaesar.oml.util.OmlXMIResourceFactory
import io.opencaesar.oml2owl.utils.CloseBundleToOwl
import io.opencaesar.oml2owl.utils.OwlApi
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.ArrayList
import java.util.Collection
import java.util.LinkedHashMap
import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.resource.XtextResourceSet
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.model.OWLOntology

import static extension io.opencaesar.oml.util.OmlRead.*

class App {

	package static val OML = 'oml'
	package static val OMLXMI = "omlxmi"
	package static val OMLZIP = "omlzip"

	@Parameter(
		names=#["--input","-i"], 
		description="Location of OML input folder (Required)",
		validateWith=InputFolderPath, 
		required=true, 
		order=1)
	String inputPath = null

	@Parameter(
		names=#["--output", "-o"], 
		description="Location of the OWL2 output folder", 
		validateWith=OutputFolderPath, 
		order=2
	)
	String outputPath = "."

	@Parameter(
		names=#["--disjoint-unions", "-u"], 
		description="Create disjoint union axioms", 
		order=3
	)
	boolean disjointUnions = false
	
	@Parameter(
		names=#["--annotations-on-axioms", "-a"], 
		description="Emit annotations on axioms", 
		order=4
	)
	boolean annotationsOnAxioms = false

	@Parameter(
		names=#["--debug", "-d"], 
		description="Shows debug logging statements", 
		order=5
	)
	boolean debug

	@Parameter(
		names=#["--help","-h"], 
		description="Displays summary of options", 
		help=true, 
		order=6)
	boolean help

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
		LOGGER.info("                      OML to Owl "+getAppVersion)
		LOGGER.info("=================================================================")
		LOGGER.info("Input Folder= " + inputPath)
		LOGGER.info("Output Folder= " + outputPath)

		val inputFolder = new File(inputPath)
		val inputFiles = collectOMLFiles(inputFolder)
		
		OmlStandaloneSetup.doSetup()
		OmlXMIResourceFactory.register
		val inputResourceSet = new XtextResourceSet

		val ontologyManager = OWLManager.createOWLOntologyManager()
		val owl2api = new OwlApi(ontologyManager, annotationsOnAxioms)
		val outputFiles = new LinkedHashMap<File, OWLOntology>
		val oml2owl = new LinkedHashMap<Resource, OWLOntology>

		// load the OML otologies
		for (inputFile : inputFiles) {
			val inputURI = URI.createFileURI(inputFile.absolutePath)
			LOGGER.info("Reading: "+inputURI)
			inputResourceSet.getResource(inputURI, true)
		}
		
		// create the equivalent OWL ontologies
		val threads1 = new ArrayList<Thread>
		for (inputFile : inputFiles) {
			val inputURI = URI.createFileURI(inputFile.absolutePath)
			val inputResource = inputResourceSet.getResource(inputURI, true)
			val builtin = Oml2Owl.isBuiltInOntology(inputResource.ontology?.iri)
			if (inputResource !== null && !builtin) {
				var relativePath = outputPath+'/'+inputFolder.toURI().relativize(inputFile.toURI()).getPath()
				val outputFile = new File(relativePath.substring(0, relativePath.lastIndexOf('.')+1)+'owl')
				val thread = new Thread() {
					override run() {
						LOGGER.info("Creating: "+outputFile)
						val owlOntology = new Oml2Owl(inputResource, owl2api).run
						outputFiles.put(outputFile, owlOntology)
						oml2owl.put(inputResource, owlOntology)
						LOGGER.info("Created: "+outputFile)
					}
				}
				threads1.add(thread)
				thread.start
			}
		}
		threads1.forEach[join]
		
		// run the bundle closure algorithm
		oml2owl.entrySet.filter[e|e.key.ontology instanceof VocabularyBundle].forEach[entry|
			LOGGER.info("Closing bundle: "+entry.key.URI)
			new CloseBundleToOwl(entry.key, entry.value, disjointUnions, owl2api).run
		]
		
		// save the output resources
		val threads2 = new ArrayList<Thread>
		outputFiles.forEach[file, owlOntology |
			val thread = new Thread() {
				override run() {
					LOGGER.info("Saving: "+file)
					ontologyManager.saveOntology(owlOntology, /*new TurtleDocumentFormat, */IRI.create(file))
				}
			}
			threads2.add(thread)
			thread.start
		]
		threads2.forEach[join]
		
		// copy catalog files
		val catalogFiles = collectCatalogFiles(inputFolder)
		if (catalogFiles.empty) {
			LOGGER.info("Saving: "+outputPath+'/catalog.xml')
			val catalog = new BufferedWriter(new FileWriter(outputPath+'/catalog.xml'))
			val baseURI = new File(outputPath).toURI
			catalog.write('''
				<?xml version='1.0'?>
				<catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog" prefer="public">
					 «FOR entry : outputFiles.entrySet»
					 «val uri = entry.value.ontologyID.ontologyIRI.get()»
					 «val prefix = baseURI.relativize(entry.key.toURI()).getPath().replace('.owl', '')»
					 <rewriteURI uriStartString="«uri»" rewritePrefix="«prefix»" />
					 «ENDFOR»
				</catalog>
			''')
			catalog.close()
		} else {
			for (catalogFile : catalogFiles) {
				val inputCatalog = Paths.get(catalogFile.path)
				val outputCatalog = Paths.get(catalogFile.path.replaceFirst(inputPath, outputPath))
				LOGGER.info("Saving: "+outputCatalog)
				Files.copy(inputCatalog, outputCatalog, StandardCopyOption.REPLACE_EXISTING)
			}
		}

		LOGGER.info("=================================================================")
		LOGGER.info("                          E N D")
		LOGGER.info("=================================================================")
	}

	def Collection<File> collectOMLFiles(File directory) {
		val omlFiles = new ArrayList<File>
		for (file : directory.listFiles()) {
			if (file.isFile) {
				if ((getFileExtension(file) == OML ||
					getFileExtension(file) == OMLXMI)) {
					omlFiles.add(file)
				}
			} else if (file.isDirectory) {
				omlFiles.addAll(collectOMLFiles(file))
			}
		}
		return omlFiles
	}

	def Collection<File> collectCatalogFiles(File directory) {
		val catalogFiles = new ArrayList<File>
		for (file : directory.listFiles()) {
			if (file.isFile) {
				if (file.name == 'catalog.xml') {
					catalogFiles.add(file)
				}
			} else if (file.isDirectory) {
				catalogFiles.addAll(collectCatalogFiles(file))
			}
		}
		return catalogFiles
	}

	private def String getFileExtension(File file) {
        val fileName = file.getName()
        if(fileName.lastIndexOf(".") != -1)
        	return fileName.substring(fileName.lastIndexOf(".")+1)
        else 
        	return ""
    }

	/**
	 * Get application version id from properties file.
	 * @return version string from build.properties or UNKNOWN
	 */
	def String getAppVersion() {
		var version = "UNKNOWN"
		try {
			val input = Thread.currentThread().getContextClassLoader().getResourceAsStream("version.txt")
			val reader = new InputStreamReader(input)
			version = CharStreams.toString(reader);
		} catch (IOException e) {
			val errorMsg = "Could not read version.txt file." + e
			LOGGER.error(errorMsg, e)
		}
		version
	}

	static class InputFolderPath implements IParameterValidator {
		override validate(String name, String value) throws ParameterException {
			val directory = new File(value)
			if (!directory.isDirectory) {
				throw new ParameterException("Parameter " + name + " should be a valid folder path");
			}
	  	}
	}

	static class OutputFolderPath implements IParameterValidator {
		override validate(String name, String value) throws ParameterException {
			val directory = new File(value)
			if (!directory.exists) {
				directory.mkdir
			}
	  	}
	}
	
}
