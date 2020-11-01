package io.opencaesar.oml2owl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.io.CharStreams;

import io.opencaesar.oml.DescriptionBundle;
import io.opencaesar.oml.VocabularyBundle;
import io.opencaesar.oml.dsl.OmlStandaloneSetup;
import io.opencaesar.oml.util.OmlCatalog;
import io.opencaesar.oml.util.OmlRead;
import io.opencaesar.oml.util.OmlXMIResourceFactory;
import io.opencaesar.oml2owl.CloseDescriptionBundle.CloseDescriptionBundleToOwl;
import io.opencaesar.oml2owl.CloseVocabularyBundle.CloseVocabularyBundleToOwl;

public class Oml2OwlApp {

	static final String OML = "oml";
	static final String OMLXMI = "omlxmi";

	@Parameter(
			names = { "--input-catalog-path", "-i" }, 
			description = "Path of the input OML catalog (Required)", 
			validateWith = Oml2OwlApp.InputCatalogPath.class, 
			required = true, 
			order = 1)
	private String inputCatalogPath;

	@Parameter(
			names = { "--output-catalog-path", "-o" }, 
			description = "Path of the output OWL catalog (Required)", 
			validateWith = Oml2OwlApp.OutputCatalogPath.class, 
			required = true, 
			order = 2)
	private String outputCatalogPath;

	@Parameter(
			names = { "--disjoint-unions", "-u" },
			description = "Create disjoint union axioms",
			order = 3)
	private boolean disjointUnions = false;

	@Parameter(
			names = { "--annotations-on-axioms", "-a" },
			description = "Emit annotations on axioms",
			order = 4)
	private boolean annotationsOnAxioms = false;

	@Parameter(
			names = { "--debug", "-d" },
			description = "Shows debug logging statements",
			order = 5)
	private boolean debug;

	@Parameter(
			names = { "--help", "-h" },
			description = "Displays summary of options",
			help = true,
			order = 6)
	private boolean help;

	private final Logger LOGGER = LogManager.getLogger(Oml2OwlApp.class);

	public static void main(final String... args) throws Exception {
		final Oml2OwlApp app = new Oml2OwlApp();
		final JCommander builder = JCommander.newBuilder().addObject(app).build();
		builder.parse(args);
		if (app.help) {
			builder.usage();
			return;
		}
		if (app.debug) {
			final Appender appender = LogManager.getRootLogger().getAppender("stdout");
			((AppenderSkeleton) appender).setThreshold(Level.DEBUG);
		}
		app.run();
	}

	public void run() throws Exception {
		LOGGER.info("=================================================================");
		LOGGER.info("                        S T A R T");
		LOGGER.info("                      OML to Owl "+getAppVersion());
		LOGGER.info("=================================================================");
		LOGGER.info("Input catalog path= " + inputCatalogPath);
		LOGGER.info("Output catalog path= " + outputCatalogPath);

		OmlStandaloneSetup.doSetup();
		OmlXMIResourceFactory.register();
		final XtextResourceSet inputResourceSet = new XtextResourceSet();
		
		// collect OML files
		final File inputCatalogFile = new File(inputCatalogPath);
		final File inputFolder = inputCatalogFile.getParentFile();
		final Collection<File> inputFiles = collectOMLFiles(inputFolder);
		
		// load the OML otologies
		for (final File inputFile : inputFiles) {
			final URI inputURI = URI.createFileURI(inputFile.getAbsolutePath());
			LOGGER.info(("Reading: " + inputURI));
			inputResourceSet.getResource(inputURI, true);
		}
		
		// create OWL manager
		final OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		final OwlApi owl2api = new OwlApi(ontologyManager, annotationsOnAxioms);
		final Map<File, OWLOntology> outputFiles = new LinkedHashMap<>();
		final Map<Resource, OWLOntology> oml2owl = new LinkedHashMap<>();
		
		// get the output OWL folder
		final File outputCatalogFile = new File(outputCatalogPath);
		final String outputFolderPath = outputCatalogFile.getParent();
		
		// create the equivalent OWL ontologies
		final ArrayList<Callable<Void>> callables = new ArrayList<>();
		for (final File inputFile : inputFiles) {
			final URI inputURI = URI.createFileURI(inputFile.getAbsolutePath());
			final Resource inputResource = inputResourceSet.getResource(inputURI, true);
			final boolean builtin = Oml2Owl.isBuiltInOntology(OmlRead.getOntology(inputResource).getIri());
			if (inputResource != null && !builtin) {
				String relativePath = outputFolderPath+File.separator+inputFolder.toURI().relativize(inputFile.toURI()).getPath();
				final File outputFile = new File(relativePath.substring(0, relativePath.lastIndexOf('.')+1)+"owl");
				callables.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						LOGGER.info(("Creating: " + outputFile));
						final OWLOntology owlOntology = new Oml2Owl(inputResource, owl2api).run();
						outputFiles.put(outputFile, owlOntology);
						oml2owl.put(inputResource, owlOntology);
						LOGGER.info(("Created: " + outputFile));
						return null;
					}
				});
			}
		}
		
		ForkJoinPool forkJoinPool = new ForkJoinPool();
		forkJoinPool.invokeAll(callables);
		
		// run the vocabulary bundle closure algorithm
		oml2owl.entrySet().stream().filter(e -> OmlRead.getOntology(e.getKey()) instanceof VocabularyBundle).forEach(entry -> {
			LOGGER.info("Closing vocabulary bundle: "+entry.getKey().getURI());
			new CloseVocabularyBundleToOwl(entry.getKey(), entry.getValue(), disjointUnions, owl2api).run();
		});
		
		// run the description bundle closure algorithm
		oml2owl.entrySet().stream().filter(e -> OmlRead.getOntology(e.getKey()) instanceof DescriptionBundle).forEach(entry -> {
			LOGGER.info("Closing description bundle: "+entry.getKey().getURI());
			new CloseDescriptionBundleToOwl(entry.getKey(), entry.getValue(), owl2api).run();
		});
		
		// save the output resources
		final ArrayList<Callable<Void>> callables2 = new ArrayList<>();
		outputFiles.forEach((file, owlOntology) -> {
			callables2.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					LOGGER.info("Saving: "+file);
					ontologyManager.saveOntology(owlOntology, /*new TurtleDocumentFormat,*/ IRI.create(file));
					return null;
				}
			});
		});
		forkJoinPool.invokeAll(callables2);
		
		// create the equivalent OWL catalog
		copyCatalog(inputCatalogFile, outputCatalogFile);
		
		LOGGER.info("=================================================================");
		LOGGER.info("                          E N D");
		LOGGER.info("=================================================================");
	}

	private Collection<File> collectOMLFiles(final File directory) {
		final ArrayList<File> omlFiles = new ArrayList<File>();
		for (final File file : directory.listFiles()) {
			if (file.isFile()) {
				if (OML.equals(getFileExtension(file)) || OMLXMI.equals(getFileExtension(file))) {
					omlFiles.add(file);
				}
			} else if (file.isDirectory()) {
				omlFiles.addAll(collectOMLFiles(file));
			}
		}
		return omlFiles;
	}

	private void copyCatalog(final File inputCatalogFile, final File outputCatalogFile) throws Exception {
		LOGGER.info(("Saving: " + inputCatalogFile));
		Files.copy(Paths.get(inputCatalogFile.getPath()), Paths.get(outputCatalogFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
		final OmlCatalog inputCatalog = OmlCatalog.create(inputCatalogFile.toURI().toURL());
		List<String> _nestedCatalogs = inputCatalog.getNestedCatalogs();
		for (final String c : _nestedCatalogs) {
			final java.net.URI uri = new URL(c).toURI();
			final File nestedInputCatalogFile = new File(uri);
			String relativePath = inputCatalogFile.getParentFile().toURI().relativize(uri).getPath();
			final File nestedOutputCatalogFile = new File(outputCatalogFile.getParent()+File.separator+relativePath);
			copyCatalog(nestedInputCatalogFile, nestedOutputCatalogFile);
		}
	}

	private String getFileExtension(final File file) {
        final String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1)
        	return fileName.substring(fileName.lastIndexOf(".")+1);
        else 
        	return "";
	}

	/**
	 * Get application version id from properties file.
	 * 
	 * @return version string from build.properties or UNKNOWN
	 */
	private String getAppVersion() {
		String version = "UNKNOWN";
		try {
			InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("version.txt");
			InputStreamReader reader = new InputStreamReader(input);
			version = CharStreams.toString(reader);
		} catch (IOException e) {
			String errorMsg = "Could not read version.txt file." + e;
			LOGGER.error(errorMsg, e);
		}
		return version;
	}

	public static class InputCatalogPath implements IParameterValidator {
		@Override
		public void validate(final String name, final String value) throws ParameterException {
			final File file = new File(value);
			if (!file.getName().endsWith("catalog.xml")) {
				throw new ParameterException((("Parameter " + name) + " should be a valid OWL catalog path"));
			}
		}
	}

	public static class OutputCatalogPath implements IParameterValidator {
		@Override
		public void validate(final String name, final String value) throws ParameterException {
			final File file = new File(value);
			if (!file.getName().endsWith("catalog.xml")) {
				throw new ParameterException((("Parameter " + name) + " should be a valid OWL catalog path"));
			}
			final File folder = file.getParentFile();
			folder.mkdirs();
		}
	}

}
