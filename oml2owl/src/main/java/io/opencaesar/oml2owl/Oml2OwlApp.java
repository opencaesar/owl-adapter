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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.xtext.resource.XtextResource;
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
import org.semanticweb.owlapi.io.WriterDocumentTarget;
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
import io.opencaesar.oml.util.OmlConstants;
import io.opencaesar.oml.util.OmlRead;
import io.opencaesar.oml.util.OmlResolve;
import io.opencaesar.oml.validate.OmlValidator;
import io.opencaesar.oml2owl.CloseVocabularyBundle.CloseVocabularyBundleToOwl;

/**
 * An application to transform Oml resources into Owl resources
 */
public class Oml2OwlApp {

	enum OmlAnnotations {
		generate,
		suppress
	}
	
	@Parameter(
			names = { "--input-catalog-path", "-i" }, 
			description = "Path of the input Oml catalog (Required)", 
			validateWith = InputCatalogPath.class, 
			required = true)
	private String inputCatalogPath;

	@Parameter(
			names= { "--root-ontology-iri", "-r" }, 
			description="Root ontology IRI (Optional)",
			required=false)
	private String rootOntologyIri = null;

	@Parameter(
			names = { "--output-catalog-path", "-o" }, 
			description = "Path of the output OWL catalog (Required)", 
			validateWith = OutputCatalogPath.class, 
			required = true)
	private String outputCatalogPath;

	@Parameter(
			names = { "--output-file-extension", "-f" },
			description = "Extension for the output OWL files (default=owl, options: owl, rdf, xml, rj, ttl, n3, nt, trig, nq, trix, jsonld, fss)",
			validateWith = FileExtensionValidator.class,
			required = false)
	private String outputFileExtension = "owl";

	@Parameter(
			names = { "--disjoint-unions", "-u" },
			description = "Whether to create disjoint union axioms")
	private boolean disjointUnions = false;

	@Parameter(
			names = { "--annotations-on-axioms", "-a" },
			description = "Whether to Emit annotations on axioms")
	private boolean annotationsOnAxioms = false;

	@Parameter(
			names = { "--generate-rules", "-rl" }, 
			description = "Whether to generate Jena rules files (Optional)", 
			required = false)
	private boolean generateRules;
	
	@Parameter(
			names = { "--oml-annotations", "-an" }, 
			description = "How to process OML annotations (Optional, options: generate (default), suppress", 
			required = false)
	private OmlAnnotations omlAnnotations = OmlAnnotations.generate;

	@Parameter(
			names = { "--debug", "-d" },
			description = "Shows debug logging statements")
	private boolean debug;

	@Parameter(
			names = { "--help", "-h" },
			description = "Displays summary of options",
			help = true)
	private boolean help;

	private final Logger LOGGER = LogManager.getLogger(Oml2OwlApp.class);

    /**
     * Main Method
     * @param args Application arguments.
     * @throws Exception Error
     */
    public static void main(final String... args) throws Exception {
    	mainWithDeltas(null, args);
    }

    /**
     * Main Method with Deltas
     * @param deltas The set of changed files
     * @param args Application arguments.
     * @throws Exception Error
     */
    public static void mainWithDeltas(Collection<File> deltas, final String... args) throws Exception {
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
		app.run(deltas);
	}

	/**
	 * Creates a new Oml2OwlApp object
	 */
	public Oml2OwlApp() {
	}
	
	private void run(Collection<File> deltas) throws Exception {
		LOGGER.info("=================================================================");
		LOGGER.info("                        S T A R T");
		LOGGER.info("                      Oml to Owl "+getAppVersion());
		LOGGER.info("=================================================================");
		LOGGER.info("Input catalog path= " + inputCatalogPath);
		LOGGER.info("Output catalog path= " + outputCatalogPath);

		OmlStandaloneSetup.doSetup();
		OmlXMIResourceFactory.register();
		OmlJsonResourceFactory.register();
		final ResourceSet inputResourceSet = new ResourceSetImpl();
		inputResourceSet.eAdapters().add(new ECrossReferenceAdapter());
		
		final URI inputCatalogUri = URI.createFileURI(inputCatalogPath);

		// load the Oml ontologies
		Set<String> inputIris = new LinkedHashSet<>(); 
		if (rootOntologyIri != null) {
			URI rootUri = resolveRootOntologyIri(rootOntologyIri, inputCatalogUri);
			LOGGER.info(("Reading: " + rootUri));
			Ontology rootOntology = OmlRead.getOntology(inputResourceSet.getResource(rootUri, true));
			inputIris.addAll(OmlRead.getImportedOntologyClosure(rootOntology, true).stream().map(i -> i.getIri()).collect(Collectors.toList()));
		} else {
			final Collection<File> inputFiles = collectOMLFiles(inputCatalogUri);
			for (File inputFile : inputFiles) {
				final URI ontologyUri = URI.createFileURI(inputFile.getAbsolutePath());
				LOGGER.info(("Reading: " + ontologyUri));
				Ontology ontology = OmlRead.getOntology(inputResourceSet.getResource(ontologyUri, true));
				inputIris.add(ontology.getIri());  
				LOGGER.debug("Detected encoding: "+((XtextResource)ontology.eResource()).getEncoding());
			}
		}
		
		// validate resources
		StringBuffer problems = new StringBuffer();
		for (Resource resource : inputResourceSet.getResources()) {
			LOGGER.info(("Validating: " + resource.getURI()));
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

		// remove builtin ontologies
		inputIris = inputIris.stream().filter(i -> !Oml2Owl.isBuiltInOntology(i)).collect(Collectors.toSet());

		// Calculate the delta IRIs
		Set<String> changed_iris = (deltas == null) ? inputIris
			: deltas.stream()
					.map(i -> URI.createFileURI(i.getAbsolutePath()))
					.map(i -> inputResourceSet.getResource(i, false))
					.filter(Objects::nonNull)
					.map(i -> OmlRead.getOntology(i))
					.map(i -> i.getIri())
					.collect(Collectors.toSet());
		System.out.println(changed_iris.size()+" oml file(s) have changed");

		// create OWL manager
		final OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		
		// create OWL API
		final OwlApi owl2api = new OwlApi(ontologyManager, annotationsOnAxioms);
		final Map<File, OWLOntology> outputFiles = new LinkedHashMap<>();
		final Map<Resource, OWLOntology> oml2owl = new LinkedHashMap<>();
		
		// get the output OWL folder
		final File outputCatalogFile = new File(outputCatalogPath);
		final String outputFolderPath = outputCatalogFile.getParent();
		final Set<String> outputSchemes = new HashSet<>();

		// the Jena rule files
		final Set<Oml2Rules> rules = new LinkedHashSet<>();
		
		// create the equivalent OWL ontologies
        for (String inputIri : inputIris) {
            var uri = URI.createURI(inputIri);
            if (uri.scheme() != null) { 
            	outputSchemes.add(uri.scheme());
            }
            var relativePath = uri.authority()+uri.path();
			final File outputFile = new File(outputFolderPath+File.separator+relativePath+"."+outputFileExtension);
			
			LOGGER.info(("Creating: " + outputFile));
			final Ontology ontology = OmlRead.getOntologyByIri(inputResourceSet, inputIri);
			final OWLOntology owlOntology = new Oml2Owl(ontology.eResource(), owl2api, omlAnnotations).run();

			// Only save resources when needed
			boolean needToSave = changed_iris == null || changed_iris.contains(ontology.getIri());
			if (!needToSave && (ontology instanceof VocabularyBundle || ontology instanceof DescriptionBundle)) {
				// any change in the import closure?
				var importedIris = OmlRead.getImportedOntologyClosure(ontology, false).stream()
						.map(i -> i.getIri()).collect(Collectors.toSet());
				importedIris.retainAll(changed_iris);
				if (!importedIris.isEmpty()) {
					needToSave = true;
				}
			}
			if (needToSave) {
				oml2owl.put(ontology.eResource(), owlOntology);
				outputFiles.put(outputFile, owlOntology);
				
				// generate rule files
				if (generateRules && ontology instanceof VocabularyBundle) {
					final File ruleFile = new File(outputFolderPath+File.separator+relativePath+".rules");
					rules.add(new Oml2Rules((VocabularyBundle)ontology, ruleFile).run());
				}
			}
		}
		
		// run the vocabulary bundle closure algorithm
		oml2owl.entrySet().stream().filter(e -> OmlRead.getOntology(e.getKey()) instanceof VocabularyBundle).forEach(entry -> {
			LOGGER.info("Closing vocabulary bundle: "+entry.getKey().getURI());
			new CloseVocabularyBundleToOwl((VocabularyBundle) OmlRead.getOntology(entry.getKey()), entry.getValue(), disjointUnions, owl2api).run();
		});
		
		// save the output OWL ontologies
		System.out.println(outputFiles.size()+" owl file(s) are saved");
		outputFiles.keySet().parallelStream().forEach(file -> {
			var owlOntology = outputFiles.get(file);
			LOGGER.info("Saving: "+file);
			try {
				OWLDocumentFormat format = FileExtensionValidator.extensions.get(outputFileExtension).getDeclaredConstructor().newInstance();
				if (format instanceof PrefixDocumentFormat) {
					format.asPrefixOWLDocumentFormat().copyPrefixesFrom(owlOntology.getFormat().asPrefixOWLDocumentFormat());
				}
	            file.getParentFile().mkdirs();
	            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
	            	WriterDocumentTarget documentTarget = new WriterDocumentTarget(writer);
	            	if (FileExtensionValidator.storers.get(outputFileExtension) != null) {
						OWLStorer storer = FileExtensionValidator.storers.get(outputFileExtension).apply(owlOntology);
						storer.storeOntology(owlOntology, documentTarget, format);
					} else {
		            	ontologyManager.saveOntology(owlOntology, format, documentTarget);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		// create the output OWL catalog
		createOutputCatalog(outputCatalogFile, outputSchemes);
				
		// create rule files
		if (generateRules) {
			System.out.println(rules.size()+" rules file(s) are saved");
		}
		rules.parallelStream().forEach(r -> {
			try {
				r.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		LOGGER.info("=================================================================");
		LOGGER.info("                          E N D");
		LOGGER.info("=================================================================");
	}

	private URI resolveRootOntologyIri(String rootOntologyIri, URI catalogUri) throws IOException {
		final URI resolved = OmlResolve.resolveOmlFileUri(catalogUri, rootOntologyIri);
		
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
	 * Collects Oml files referenced by the given Oml catalog
	 * 
	 * @param inputCatalogUri The URI of the input Oml catalog
	 * @return Collection of Files
	 * @throws MalformedURLException error
	 * @throws IOException error
	 */
	public static Collection<File> collectOMLFiles(URI inputCatalogUri) throws IOException {
		return OmlResolve.resolveOmlFileUris(inputCatalogUri).stream()
				.map(i -> new File(i.toFileString()))
				.collect(Collectors.toList());
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
				throw new ParameterException((("Parameter " + name) + " should be a valid Oml catalog path"));
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
	
}