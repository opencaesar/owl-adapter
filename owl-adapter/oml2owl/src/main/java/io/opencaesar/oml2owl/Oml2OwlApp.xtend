package io.opencaesar.oml2owl

import com.beust.jcommander.IParameterValidator
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.google.common.io.CharStreams
import io.opencaesar.oml.DescriptionBundle
import io.opencaesar.oml.VocabularyBundle
import io.opencaesar.oml.dsl.OmlStandaloneSetup
import io.opencaesar.oml.util.OmlCatalog
import io.opencaesar.oml.util.OmlXMIResourceFactory
import io.opencaesar.oml2owl.CloseDescriptionBundle.CloseDescriptionBundleToOwl
import io.opencaesar.oml2owl.CloseVocabularyBundle.CloseVocabularyBundleToOwl
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
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

class Oml2OwlApp {

	package static val OML = 'oml'
	package static val OMLXMI = "omlxmi"

	@Parameter(
		names=#["--input-catalog-path","-i"], 
		description="Path of the input OML catalog (Required)",
		validateWith=InputCatalogPath, 
		required=true, 
		order=1)
	String inputCatalogPath

	@Parameter(
		names=#["--output-catalog-path", "-o"], 
		description="Path of the output OWL catalog (Required)", 
		validateWith=OutputCatalogPath, 
		required=true, 
		order=2
	)
	String outputCatalogPath

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

	val LOGGER = LogManager.getLogger(Oml2OwlApp)

	def static void main(String ... args) {
		val app = new Oml2OwlApp
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
		app.run()
	}

	def void run() {
		LOGGER.info("=================================================================")
		LOGGER.info("                        S T A R T")
		LOGGER.info("                      OML to Owl "+getAppVersion)
		LOGGER.info("=================================================================")
		LOGGER.info("Input catalog path= " + inputCatalogPath)
		LOGGER.info("Output catalog path= " + outputCatalogPath)
		
		OmlStandaloneSetup.doSetup()
		OmlXMIResourceFactory.register
		val inputResourceSet = new XtextResourceSet

		// collect OML files
		val inputCatalogFile = new File(inputCatalogPath)
		val inputFolder = inputCatalogFile.parentFile
		val inputFiles = collectOMLFiles(inputFolder)

		// load the OML otologies
		for (inputFile : inputFiles) {
			val inputURI = URI.createFileURI(inputFile.absolutePath)
			LOGGER.info("Reading: "+inputURI)
			inputResourceSet.getResource(inputURI, true)
		}
		
		// create OWL manager
		val ontologyManager = OWLManager.createOWLOntologyManager()
		val owl2api = new OwlApi(ontologyManager, annotationsOnAxioms)
		val outputFiles = new LinkedHashMap<File, OWLOntology>
		val oml2owl = new LinkedHashMap<Resource, OWLOntology>

		// get the output OWL folder
		val outputCatalogFile = new File(outputCatalogPath);
		val outputFolderPath = outputCatalogFile.parent

		// create the equivalent OWL ontologies
		val throwables = new ArrayList<Throwable>
		val threads = new ArrayList<Thread>
		for (inputFile : inputFiles) {
			val inputURI = URI.createFileURI(inputFile.absolutePath)
			val inputResource = inputResourceSet.getResource(inputURI, true)
			val builtin = Oml2Owl.isBuiltInOntology(inputResource.ontology?.iri)
			if (inputResource !== null && !builtin) {
				var relativePath = outputFolderPath+File.separator+inputFolder.toURI().relativize(inputFile.toURI()).getPath()
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
				threads.add(thread)
				thread.uncaughtExceptionHandler = [th, ex |
					System.out.println(ex)
					throwables.add(ex)
				]
				thread.start
			}
		}
		threads.forEach[join]
		if (!throwables.isEmpty) {
			throw new RuntimeException("Exception(s) thrown by one or more threads")
		}
		
		// run the vocabulary bundle closure algorithm
		oml2owl.entrySet.filter[e|e.key.ontology instanceof VocabularyBundle].forEach[entry|
			LOGGER.info("Closing vocabulary bundle: "+entry.key.URI)
			new CloseVocabularyBundleToOwl(entry.key, entry.value, disjointUnions, owl2api).run
		]
		
		// run the description bundle closure algorithm
		oml2owl.entrySet.filter[e|e.key.ontology instanceof DescriptionBundle].forEach[entry|
			LOGGER.info("Closing description bundle: "+entry.key.URI)
			new CloseDescriptionBundleToOwl(entry.key, entry.value, owl2api).run
		]
		
		// save the output resources
		val threads2 = new ArrayList<Thread>
		outputFiles.forEach[file, owlOntology |
			val thread = new Thread() {
				override run() {
					LOGGER.info("Saving: "+file)
					ontologyManager.saveOntology(owlOntology, /*new TurtleDocumentFormat,*/ IRI.create(file))
				}
			}
			threads2.add(thread)
			thread.start
		]
		threads2.forEach[join]
		
		// create the equivalent OWL catalog
		copyCatalog(inputCatalogFile, outputCatalogFile) 

		LOGGER.info("=================================================================")
		LOGGER.info("                          E N D")
		LOGGER.info("=================================================================")
	}

	private def Collection<File> collectOMLFiles(File directory) {
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

	private def void copyCatalog(File inputCatalogFile, File outputCatalogFile) {
		LOGGER.info("Saving: "+inputCatalogFile)
		Files.copy(Paths.get(inputCatalogFile.path), Paths.get(outputCatalogFile.path), StandardCopyOption.REPLACE_EXISTING)
		val inputCatalog = OmlCatalog.create(inputCatalogFile.toURI.toURL)
		for (c : inputCatalog.nestedCatalogs) {
			val uri = new URL(c).toURI
			val nestedInputCatalogFile = new File(uri);
			var relativePath = inputCatalogFile.parentFile.toURI().relativize(uri).getPath()
			val nestedOutputCatalogFile = new File(outputCatalogFile.parent+File.separator+relativePath)
			copyCatalog(nestedInputCatalogFile, nestedOutputCatalogFile)
		}
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
	private def String getAppVersion() {
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

	static class InputCatalogPath implements IParameterValidator {
		override validate(String name, String value) throws ParameterException {
			val file = new File(value)
			if (!file.getName().endsWith("catalog.xml")) {
				throw new ParameterException("Parameter " + name + " should be a valid OWL catalog path")
			}
	  	}
	}

	static class OutputCatalogPath implements IParameterValidator {
		override validate(String name, String value) throws ParameterException {
			val file = new File(value)
			if (!file.getName().endsWith("catalog.xml")) {
				throw new ParameterException("Parameter " + name + " should be a valid OWL catalog path")
			}
			val folder = file.parentFile
			folder.mkdirs
	  	}
	}
	
}
