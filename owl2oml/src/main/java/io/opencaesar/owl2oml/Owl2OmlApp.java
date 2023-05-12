/**
 * 
 * Copyright 2019-2021 California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package io.opencaesar.owl2oml;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.N3DocumentFormat;
import org.semanticweb.owlapi.formats.NQuadsDocumentFormat;
import org.semanticweb.owlapi.formats.NQuadsDocumentFormatFactory;
import org.semanticweb.owlapi.formats.NTriplesDocumentFormat;
import org.semanticweb.owlapi.formats.RDFJsonDocumentFormat;
import org.semanticweb.owlapi.formats.RDFJsonLDDocumentFormat;
import org.semanticweb.owlapi.formats.RDFJsonLDDocumentFormatFactory;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RioTurtleDocumentFormat;
import org.semanticweb.owlapi.formats.TrigDocumentFormat;
import org.semanticweb.owlapi.formats.TrigDocumentFormatFactory;
import org.semanticweb.owlapi.formats.TrixDocumentFormat;
import org.semanticweb.owlapi.formats.TrixDocumentFormatFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLDocumentFormatFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLStorer;
import org.semanticweb.owlapi.rio.RioStorer;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import io.opencaesar.oml.dsl.OmlStandaloneSetup;
import io.opencaesar.oml.resource.OmlJsonResourceFactory;
import io.opencaesar.oml.resource.OmlXMIResourceFactory;
import io.opencaesar.oml.util.OmlBuilder;
import io.opencaesar.oml.util.OmlCatalog;

/**
 * An application to transform Owl resources into Oml resources
 */
public class Owl2OmlApp {

	@Parameter(
			names = { "--input-catalog-path", "-i" }, 
			description = "Path of the input OWL catalog", 
			validateWith = InputCatalogPath.class, 
			required = true, 
			order = 1)
	private String inputCatalogPath;

	@Parameter(
			names = { "--output-catalog-path", "-o" }, 
			description = "Path of the output OML catalog", 
			validateWith = OutputCatalogPath.class, 
			required = true, 
			order = 2)
	private String outputCatalogPath;

	@Parameter(
			names = { "--output-writable-path", "-w" }, 
			description = "Path of the output writeable folders", 
			validateWith = OutputFolderPath.class, 
			required = false,
			order = 3)
	private List<String> outputWritablePaths;

	@Parameter(
			names = { "--input-file-extensions", "-if" },
			description = "Extensions for the input OWL files (default=owl, options: owl, rdf, xml, rj, ttl, n3, nt, trig, nq, trix, jsonld, fss)",
			validateWith = FileExtensionValidator.class,
			required = false,
			order = 4)
	private List<String> inputFileExtensions = Arrays.asList("owl");

	@Parameter(
			names = { "--output-file-extension", "-of" },
			description = "Extension for the output OML files (default=oml, options: oml, omlxmmi, omljson)",
			validateWith = FileExtensionValidator.class,
			required = false,
			order = 5)
	private String outputFileExtension = "oml";

	@Parameter(
			names = { "--debug", "-d" },
			description = "Shows debug logging statements",
			order = 6)
	private boolean debug;

	@Parameter(
			names = { "--help", "-h" },
			description = "Displays summary of options",
			help = true,
			order = 7)
	private boolean help;

	private final Logger LOGGER = LogManager.getLogger(Owl2OmlApp.class);

	/**
	 * Main method
	 * 
	 * @param args command line arguments for the app
	 * @throws Exception when template instantiation has a problem
	 */
	public static void main(final String... args) throws Exception {
		final Owl2OmlApp app = new Owl2OmlApp();
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

	/**
	 * Creates a new Owl2OmlApp object
	 */
	public Owl2OmlApp() {
	}
	
	/**
	 * Runs the application
	 * 
	 * @throws Exception error
	 */
	public void run() throws Exception {
		LOGGER.info("=================================================================");
		LOGGER.info("                        S T A R T");
		LOGGER.info("                      Owl to Oml "+getAppVersion());
		LOGGER.info("=================================================================");
		LOGGER.info("Input catalog path= " + inputCatalogPath);
		LOGGER.info("Output catalog path= " + outputCatalogPath);

        // Create the ontology manager and configure it with catalog-based IRI mapper
        LOGGER.info("create ontology manager");
        final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        if (manager == null) {
            throw new RuntimeException("couldn't create owl ontology manager");
        }
        LOGGER.debug("add location mappers");
        XMLCatalogIRIMapper mapper = new XMLCatalogIRIMapper(new File(inputCatalogPath), inputFileExtensions);
        manager.getIRIMappers().add(mapper);

        // Load the input ontologies in memory
        var iris = mapper.getFileUriMap(inputFileExtensions).keySet();
        iris.forEach(iri -> {
            LOGGER.info("load ontology " + iri);
            try {
                final OWLOntology ont = manager.loadOntology(IRI.create(iri));
                if (ont == null) {
                    throw new RuntimeException("couldn't load ontology at IRI=" + iri);
                }
            } catch (OWLOntologyCreationException e) {
            	throw new RuntimeException(e);
            }
        });
		
		OmlStandaloneSetup.doSetup();
		OmlXMIResourceFactory.register();
		OmlJsonResourceFactory.register();
		final ResourceSet outputResourceSet = new ResourceSetImpl();
		outputResourceSet.eAdapters().add(new ECrossReferenceAdapterEx());
		
		// create Oml catalog
		final OmlCatalog outputCatalog = OmlCatalog.create(URI.createFileURI(outputCatalogPath));		

		// create the Oml builder
		final OmlBuilder builder = new OmlBuilder(outputResourceSet);
		
		// start the Oml Builder
		builder.start();
		
		// create the equivalent OWL ontologies
		List<URI> outputResourceURIs = new ArrayList<>(); 
		manager.ontologies().forEach(owlOntology -> {
			LOGGER.info(("Converting: " + owlOntology.getOntologyID().getOntologyIRI().get()));
			try {
				var omlOntologies = new Owl2Oml(manager, builder, outputCatalog, outputFileExtension).run(owlOntology);
				omlOntologies.forEach(i -> outputResourceURIs.add(i.eResource().getURI()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		// finish the Oml builder
		builder.finish();
		
		// save the output resources here instead of calling builder.save in order to log
		for (URI outputResourceURI : outputResourceURIs) {
			if (outputResourceURI.fileExtension().equals(outputFileExtension)) {
				String filePath = outputResourceURI.toFileString();
				if (!outputWritablePaths.isEmpty()) {
					if (outputWritablePaths.stream().noneMatch(i -> filePath.startsWith(i))) {
						continue;
					}
				}
				LOGGER.info("Saving: "+outputResourceURI);
				final Resource outputResource = outputResourceSet.getResource(outputResourceURI, false);
				outputResource.save(Collections.EMPTY_MAP);
			}
		}
		
		LOGGER.info("=================================================================");
		LOGGER.info("                          E N D");
		LOGGER.info("=================================================================");
	}

	/**
	 * Collects OML files referenced by the given Oml catalog
	 * 
	 * @param inputCatalog The input Oml catalog
	 * @return Collection of Files
	 * @throws MalformedURLException error
	 * @throws URISyntaxException error
	 */
	public static Collection<File> collectOMLFiles(OmlCatalog inputCatalog) throws MalformedURLException, URISyntaxException {
		var fileExtensions = Arrays.asList(OmlConstants.OML_EXTENSIONS);
		
		final var omlFiles = new LinkedHashSet<File>();
		for (URI uri : inputCatalog.getFileUris(fileExtensions)) {
			File file = new File(new URL(uri.toString()).toURI().getPath());
			omlFiles.add(file);
		}
		return omlFiles;
	}
	
	/**
	 * Get application version id from properties file.
	 * 
	 * @return version string from build.properties or UNKNOWN
	 */
	private String getAppVersion() {
    	var version = this.getClass().getPackage().getImplementationVersion();
    	return (version != null) ? version : "<SNAPSHOT>";
	}

	/**
	 * The validator of the input catalog path 
	 */
	public static class InputCatalogPath implements IParameterValidator {
		/**
		 * Creates a new InputCatalogPath object
		 */
		public InputCatalogPath() {
		}
		@Override
		public void validate(final String name, final String value) throws ParameterException {
			final File file = new File(value);
			if (!file.exists() || !file.getName().endsWith("catalog.xml")) {
				throw new ParameterException((("Parameter " + name) + " should be a valid OML catalog path"));
			}
		}
	}

	/**
	 * The validator of the output catalog path 
	 */
	public static class OutputCatalogPath implements IParameterValidator {
		/**
		 * Creates a new OutputCatalogPath object
		 */
		public OutputCatalogPath() {
		}
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

	/**
	 * The validator of the output folder path 
	 */
	public static class OutputFolderPath implements IParameterValidator {
		/**
		 * Creates a new OutputFolderPath object
		 */
		public OutputFolderPath() {
		}
		@Override
		public void validate(final String name, final String value) throws ParameterException {
			final File file = new File(value);
			if (!file.isDirectory() || !file.exists()) {
				throw new ParameterException((("Parameter " + name) + " should be a valid folder path"));
			}
			final File folder = file.getParentFile();
			folder.mkdirs();
		}
	}

	/**
	 * The validator of the file extension 
	 */
	public static class FileExtensionValidator implements IParameterValidator {
		/**
		 * Creates a new FileExtensionValidator object
		 */
		public FileExtensionValidator() {
		}
		@Override
		public void validate(final String name, final String value) throws ParameterException {
			if (!extensions.containsKey(value)) {
				throw new ParameterException((("Parameter " + name) + " should be a valid OWL file extension: " +
						extensions.keySet().stream().reduce( (x,y) -> x + " " + y) ));
			}
		}

		private static Map<String, Class<? extends OWLDocumentFormat>> extensions = new HashMap<>();

		static {
			extensions.put("fss", FunctionalSyntaxDocumentFormat.class);
			// triple formats
			extensions.put("owl", RDFXMLDocumentFormat.class);
			extensions.put("rdf", RDFXMLDocumentFormat.class);
			extensions.put("xml", RDFXMLDocumentFormat.class);
			extensions.put("n3", N3DocumentFormat.class);
			extensions.put("ttl", RioTurtleDocumentFormat.class);
			extensions.put("rj", RDFJsonDocumentFormat.class);
			extensions.put("nt", NTriplesDocumentFormat.class);
			// quad formats
			extensions.put("jsonld", RDFJsonLDDocumentFormat.class);
			extensions.put("trig", TrigDocumentFormat.class);
			extensions.put("trix", TrixDocumentFormat.class);
			extensions.put("nq", NQuadsDocumentFormat.class);
		}

		// Workaround.
		// See https://github.com/owlcs/owlapi/issues/1002
		// See https://github.com/owlcs/owlapi/pull/1003

		private static Map<String, Function<OWLOntology,OWLStorer>> storers = new HashMap<>();

		private static OWLStorer createQuadOntologyStorer(OWLDocumentFormatFactory factory, OWLOntology owlOntology) {
			return owlOntology
					.getOntologyID()
					.getOntologyIRI()
					.map(iri -> new RioStorer(factory, SimpleValueFactory.getInstance().createIRI(iri.toString())))
					.orElse(new RioStorer(factory));
		}

		static {
			// quad formats
			storers.put("jsonld", owlOntology -> createQuadOntologyStorer(new RDFJsonLDDocumentFormatFactory(), owlOntology));
			storers.put("trig", owlOntology -> createQuadOntologyStorer(new TrigDocumentFormatFactory(), owlOntology));
			storers.put("trix", owlOntology -> createQuadOntologyStorer(new TrixDocumentFormatFactory(), owlOntology));
			storers.put("nq", owlOntology -> createQuadOntologyStorer(new NQuadsDocumentFormatFactory(), owlOntology));
		}
	}

	private class ECrossReferenceAdapterEx extends ECrossReferenceAdapter {
		
		private Set<Resource> allResources = Collections.emptySet();
		
		@Override
		public Collection<EStructuralFeature.Setting> getInverseReferences(EObject eObject) {
			var references = super.getInverseReferences(eObject);
			if (!allResources.isEmpty()) {
				for (var i = references.iterator(); i.hasNext();) {
					var setting = i.next();
					if (!allResources.contains(setting.getEObject().eResource())) {
						i.remove();
					}
				}
			}
			return references;
		}
	}
	
}
