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
package io.opencaesar.oml2owl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
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
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLStorer;
import org.semanticweb.owlapi.rio.RioStorer;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import io.opencaesar.oml.DescriptionBundle;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.VocabularyBundle;
import io.opencaesar.oml.dsl.OmlStandaloneSetup;
import io.opencaesar.oml.resource.OmlJsonResourceFactory;
import io.opencaesar.oml.resource.OmlXMIResourceFactory;
import io.opencaesar.oml.util.OmlCatalog;
import io.opencaesar.oml.util.OmlRead;
import io.opencaesar.oml.validate.OmlValidator;
import io.opencaesar.oml2owl.CloseDescriptionBundle.CloseDescriptionBundleToOwl;
import io.opencaesar.oml2owl.CloseVocabularyBundle.CloseVocabularyBundleToOwl;

/**
 * An application to transform Oml resources into Owl resources
 */
public class Oml2OwlApp {

	@Parameter(
			names = { "--input-catalog-path", "-i" }, 
			description = "Path of the input OML catalog (Required)", 
			validateWith = InputCatalogPath.class, 
			required = true, 
			order = 1)
	private String inputCatalogPath;

	@Parameter(
			names= { "--root-ontology-iri", "-r" }, 
			description="Root OML ontology IRI (Optional)",
			required=false, 
			order=2)
	private String rootOntologyIri = null;

	@Parameter(
			names = { "--output-catalog-path", "-o" }, 
			description = "Path of the output OWL catalog (Required)", 
			validateWith = OutputCatalogPath.class, 
			required = true, 
			order = 3)
	private String outputCatalogPath;

	@Parameter(
			names = { "--output-file-extension", "-f" },
			description = "Extension for the output OWL files (default=owl, options: owl, rdf, xml, rj, ttl, n3, nt, trig, nq, trix, jsonld, fss)",
			validateWith = FileExtensionValidator.class,
			required = false,
			order = 4)
	private String outputFileExtension = "owl";

	@Parameter(
			names = { "--disjoint-unions", "-u" },
			description = "Whether to create disjoint union axioms",
			order = 5)
	private boolean disjointUnions = false;

	@Parameter(
			names = { "--annotations-on-axioms", "-a" },
			description = "Whether to Emit annotations on axioms",
			order = 6)
	private boolean annotationsOnAxioms = false;

	@Parameter(
			names = { "--debug", "-d" },
			description = "Shows debug logging statements",
			order = 7)
	private boolean debug;

	@Parameter(
			names = { "--help", "-h" },
			description = "Displays summary of options",
			help = true,
			order = 8)
	private boolean help;

	private final Logger LOGGER = LogManager.getLogger(Oml2OwlApp.class);

	/**
	 * Main method
	 * 
	 * @param args command line arguments for the app
	 * @throws Exception when template instantiation has a problem
	 */
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

	/**
	 * Creates a new Oml2OwlApp object
	 */
	public Oml2OwlApp() {
	}
	
	/**
	 * Runs the application
	 * 
	 * @throws Exception error
	 */
	public void run() throws Exception {
		LOGGER.info("=================================================================");
		LOGGER.info("                        S T A R T");
		LOGGER.info("                      OML to Owl "+getAppVersion());
		LOGGER.info("=================================================================");
		LOGGER.info("Input catalog path= " + inputCatalogPath);
		LOGGER.info("Output catalog path= " + outputCatalogPath);

		OmlStandaloneSetup.doSetup();
		OmlXMIResourceFactory.register();
		OmlJsonResourceFactory.register();
		final ResourceSet inputResourceSet = new ResourceSetImpl();
		inputResourceSet.eAdapters().add(new ECrossReferenceAdapterEx());
		
		final File inputCatalogFile = new File(inputCatalogPath);
		final OmlCatalog inputCatalog = OmlCatalog.create(URI.createFileURI(inputCatalogFile.toString()));

		// load the OML ontologies
		List<Ontology> inputOntologies = new ArrayList<>(); 
		if (rootOntologyIri != null) {
			URI rootUri = resolveRootOntologyIri(rootOntologyIri, inputCatalog);
			LOGGER.info(("Reading: " + rootUri));
			Ontology rootOntology = OmlRead.getOntology(inputResourceSet.getResource(rootUri, true));
			inputOntologies.addAll(OmlRead.getAllImportedOntologies(rootOntology, true));
		} else {
			final Collection<File> inputFiles = collectOMLFiles(inputCatalog);
			for (File inputFile : inputFiles) {
				final URI ontologyUri = URI.createFileURI(inputFile.getAbsolutePath());
				LOGGER.info(("Reading: " + ontologyUri));
				Ontology ontology = OmlRead.getOntology(inputResourceSet.getResource(ontologyUri, true));
				inputOntologies.add(ontology);  
			}
		}
		
		// validate resources
		StringBuffer problems = new StringBuffer();
		for (Resource resource : inputResourceSet.getResources()) {
			String results = OmlValidator.validate(resource);
	        if (results.length()>0) {
	        	if (problems.length()>0)
	        		problems.append("\n\n");
	        	problems.append(results);
	        }
		}
		if (problems.length()>0) {
			throw new IllegalStateException("\n"+problems.toString());
		}
		
		// create OWL manager
		final OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		final OwlApi owl2api = new OwlApi(ontologyManager, annotationsOnAxioms);
		final Map<File, OWLOntology> outputFiles = new LinkedHashMap<>();
		final Map<Resource, OWLOntology> oml2owl = new LinkedHashMap<>();
		
		// get the output OWL folder
		final File outputCatalogFile = new File(outputCatalogPath);
		final String outputFolderPath = outputCatalogFile.getParent();
		final Set<String> outputSchemes = new HashSet<>();
		
		// create the equivalent OWL ontologies
        for (Ontology ontology : inputOntologies) {
			if (ontology != null && !Oml2Owl.isBuiltInOntology(ontology.getIri())) {
	            var uri = URI.createURI(ontology.getIri());
	            if (uri.scheme() != null) { 
	            	outputSchemes.add(uri.scheme());
	            }
	            var relativePath = uri.authority()+uri.path();
				final File outputFile = new File(outputFolderPath+File.separator+relativePath+"."+outputFileExtension);
				LOGGER.info(("Creating: " + outputFile));
				final OWLOntology owlOntology = new Oml2Owl(ontology.eResource(), owl2api).run();
				outputFiles.put(outputFile, owlOntology);
				oml2owl.put(ontology.eResource(), owlOntology);
				LOGGER.info(("Created: " + outputFile));
			}
		}
		
		// run the vocabulary bundle closure algorithm
		oml2owl.entrySet().stream().filter(e -> OmlRead.getOntology(e.getKey()) instanceof VocabularyBundle).forEach(entry -> {
			LOGGER.info("Closing vocabulary bundle: "+entry.getKey().getURI());
			switchECrossReferenceAdapter(entry.getKey());
			new CloseVocabularyBundleToOwl(entry.getKey(), entry.getValue(), disjointUnions, owl2api).run();
		});
		
		// run the description bundle closure algorithm
		oml2owl.entrySet().stream().filter(e -> OmlRead.getOntology(e.getKey()) instanceof DescriptionBundle).forEach(entry -> {
			LOGGER.info("Closing description bundle: "+entry.getKey().getURI());
			switchECrossReferenceAdapter(entry.getKey());
			new CloseDescriptionBundleToOwl(entry.getKey(), entry.getValue(), owl2api).run();
		});
		
		// save the output resources
		outputFiles.forEach((file, owlOntology) -> {
			LOGGER.info("Saving: "+file);
			try {
				OWLDocumentFormat format = FileExtensionValidator.extensions.get(outputFileExtension).getDeclaredConstructor().newInstance();
				if (format instanceof PrefixDocumentFormat) {
					format.asPrefixOWLDocumentFormat().copyPrefixesFrom(owlOntology.getFormat().asPrefixOWLDocumentFormat());
				}
				IRI documentIRI = IRI.create(file);
				// Workaround
				// See https://github.com/owlcs/owlapi/issues/1002
				// See https://github.com/owlcs/owlapi/pull/1003
				if (FileExtensionValidator.storers.get(outputFileExtension) != null) {
					OWLStorer storer = FileExtensionValidator.storers.get(outputFileExtension).apply(owlOntology);
					storer.storeOntology(owlOntology, documentIRI, format);
				} else {
					ontologyManager.saveOntology(owlOntology, format, documentIRI);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		// create the output OWL catalog
		createOutputCatalog(outputCatalogFile, outputSchemes);
		
		LOGGER.info("=================================================================");
		LOGGER.info("                          E N D");
		LOGGER.info("=================================================================");
	}

	private void switchECrossReferenceAdapter(Resource r) {
		final var allResoruces = OmlRead.getAllImportedOntologies(OmlRead.getOntology(r), true).stream()
				.map(i -> i.eResource())
				.collect(Collectors.toSet());
        final var adapter = (ECrossReferenceAdapterEx) ECrossReferenceAdapter.getCrossReferenceAdapter(r.getResourceSet());
        adapter.setAllResources(allResoruces);
	}
	
	private URI resolveRootOntologyIri(String rootOntologyIri, OmlCatalog catalog) throws IOException {
		final URI resolved = URI.createURI(catalog.resolveURI(rootOntologyIri));
		
		if (resolved.isFile()) {
			final String filename = resolved.toFileString();
			if (new File(filename).isFile()) {
				return resolved;
			}
			var fileExtensions = Arrays.asList(OmlConstants.OML_EXTENSIONS);
			for (String ext : fileExtensions) {
				if (new File(filename+'.'+ext).isFile()) {
					return URI.createFileURI(filename+'.'+ext);
				}
			}
		}
		
		return resolved;
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
	
	private void createOutputCatalog(final File outputCatalogFile, Set<String> schemes) throws Exception {
		LOGGER.info(("Saving: " + outputCatalogFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputCatalogFile));
        bw.write("<?xml version='1.0'?>\n");
        bw.write("<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" prefer=\"public\">\n");
        for (String scheme : schemes) {
        	bw.write("\t<rewriteURI uriStartString=\""+scheme+"://\" rewritePrefix=\"./\" />\n");
        }
        bw.write("</catalog>");
        bw.close();
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
		
		public void setAllResources(Set<Resource> allResources) {
			this.allResources = allResources;
		}

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
