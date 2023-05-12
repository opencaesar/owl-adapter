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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.HasAnnotations;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLRule;

import io.opencaesar.oml.Description;
import io.opencaesar.oml.DescriptionBundle;
import io.opencaesar.oml.Import;
import io.opencaesar.oml.ImportKind;
import io.opencaesar.oml.Literal;
import io.opencaesar.oml.OmlFactory;
import io.opencaesar.oml.OmlPackage;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.Vocabulary;
import io.opencaesar.oml.VocabularyBundle;
import io.opencaesar.oml.util.OmlBuilder;
import io.opencaesar.oml.util.OmlCatalog;
import io.opencaesar.oml.util.OmlSwitch;

class Owl2Oml extends OmlSwitch<Void> {
	
	private final OWLOntologyManager manager;
	private final OmlBuilder oml;
	private final OmlCatalog catalog;
	private final String outputFileExtension;

	public Owl2Oml(OWLOntologyManager manager, OmlBuilder oml, OmlCatalog catalog, String outputFileExtension) {
		this.manager = manager;
		this.oml = oml;
		this.catalog = catalog;
		this.outputFileExtension = outputFileExtension;
	}

	public List<Ontology> run(OWLOntology owlOntology) throws IOException {
		return createOntologies(owlOntology);
	}

	private List<Ontology> createOntologies(OWLOntology owlOntology) throws IOException {
		var iri = owlOntology.getOntologyID().getOntologyIRI().get();
		var namespace = owlOntology.getFormat().asPrefixOWLDocumentFormat().getDefaultPrefix();
		var prefix = getOntologyNamespacePrefix(owlOntology, namespace);
		var uri = URI.createURI(catalog.resolveURI(iri.getIRIString()+"."+outputFileExtension));
		
		var type = getAnnotationValue(owlOntology, DC.TYPE).toString();
		if (type != null) {
			if (type.equals(OmlConstants.Vocabulary)) {
				return Collections.singletonList(createVocabulary(owlOntology, uri, namespace, prefix)); 
			} else if (type.equals(OmlConstants.VocabularyBundle)) {
				return Collections.singletonList(createVocabularyBundle(owlOntology, uri, namespace, prefix)); 
			} else if (type.equals(OmlConstants.Description)) {
				return Collections.singletonList(createDescription(owlOntology, uri, namespace, prefix)); 
			} else if (type.equals(OmlConstants.DescriptionBundle)) {
				return Collections.singletonList(createDescriptionBundle(owlOntology, uri, namespace, prefix)); 
			}
		}
		return Collections.emptyList();
	}
	
	private Vocabulary createVocabulary(OWLOntology owlOntology, URI uri, String namespace, String prefix) throws IOException {
		var vocabulary = oml.createVocabulary(uri, namespace, prefix);
		createOntologyImports(owlOntology, vocabulary);
		createOntologyAnnotations(owlOntology, vocabulary);
		createAspects(owlOntology, vocabulary);
		createConcepts(owlOntology, vocabulary);
		createRelationEntities(owlOntology, vocabulary);
		createStructures(owlOntology, vocabulary);
		//createScalars(owlOntology, vocabulary);
		creaAnnotationProperties(owlOntology, vocabulary);
		return vocabulary;
	}
	
	private VocabularyBundle createVocabularyBundle(OWLOntology owlOntology, URI uri, String namespace, String prefix) throws IOException {
		var bundle = oml.createVocabularyBundle(uri, namespace, prefix);
		createOntologyImports(owlOntology, bundle);
		createOntologyAnnotations(owlOntology, bundle);
		return bundle;
	}

	private Description createDescription(OWLOntology owlOntology, URI uri, String namespace, String prefix) throws IOException {
		var description = oml.createDescription(uri, namespace, prefix);
		createOntologyImports(owlOntology, description);
		createOntologyAnnotations(owlOntology, description);
		return description;
	}

	private DescriptionBundle createDescriptionBundle(OWLOntology owlOntology, URI uri, String namespace, String prefix) throws IOException {
		var bundle = oml.createDescriptionBundle(uri, namespace, prefix);
		createOntologyImports(owlOntology, bundle);
		createOntologyAnnotations(owlOntology, bundle);
		return bundle;
	}
	
	private void createAspects(OWLOntology owlOntology, Vocabulary ontology) {
		listDeclaredEntitiesofType(owlOntology, OWLClass.class).forEach(e -> {			
			var type = getAnnotationValue(owlOntology, e.getIRI(), DC.TYPE);
			if (type != null && type.toString().equals(OmlConstants.Aspect)) {
				oml.addAspect(ontology, e.getIRI().getFragment());
				createAnnotations(owlOntology, e.getIRI(), ontology);
			}
		});
	}
	
	private void createConcepts(OWLOntology owlOntology, Vocabulary ontology) {
		listDeclaredEntitiesofType(owlOntology, OWLClass.class).forEach(e -> {			
			var type = getAnnotationValue(owlOntology, e.getIRI(), DC.TYPE);
			if (type != null && type.toString().equals(OmlConstants.Concept)) {
				oml.addConcept(ontology, e.getIRI().getFragment());
				createAnnotations(owlOntology, e.getIRI(), ontology);
			}
		});
	}

	private void createRelationEntities(OWLOntology owlOntology, Vocabulary ontology) {
		listDeclaredEntitiesofType(owlOntology, OWLClass.class).forEach(e -> {			
			var type = getAnnotationValue(owlOntology, e.getIRI(), DC.TYPE);
			if (type != null && type.toString().equals(OmlConstants.RelationEntity)) {
				var forwardIri = getForwardRelationIri(owlOntology, e.getIRI());
				if (forwardIri != null) {
					var forwardProperty = manager.getOWLDataFactory().getOWLObjectProperty(forwardIri);
					var sourceIris = new ArrayList<String>(owlOntology.getObjectPropertyDomainAxioms(forwardProperty).stream()
						.map(i -> i.getDomain())
						.filter(i -> i.isNamed())
						.map(i -> i.asOWLClass().getIRI().toString())
						.collect(Collectors.toList()));
					var targetIris = new ArrayList<String>(owlOntology.getObjectPropertyRangeAxioms(forwardProperty).stream()
							.map(i -> i.getRange())
							.filter(i -> i.isNamed())
							.map(i -> i.asOWLClass().getIRI().toString())
							.collect(Collectors.toList()));
					var functional = !owlOntology.getFunctionalObjectPropertyAxioms(forwardProperty).isEmpty(); 
					var inverseFunctional = !owlOntology.getInverseFunctionalObjectPropertyAxioms(forwardProperty).isEmpty();
					var symmetric = !owlOntology.getSymmetricObjectPropertyAxioms(forwardProperty).isEmpty();
					var asymmetric = !owlOntology.getAsymmetricObjectPropertyAxioms(forwardProperty).isEmpty();
					var reflexive = !owlOntology.getReflexiveObjectPropertyAxioms(forwardProperty).isEmpty();
					var irreflexive = !owlOntology.getIrreflexiveObjectPropertyAxioms(forwardProperty).isEmpty();
					var transitive = !owlOntology.getTransitiveObjectPropertyAxioms(forwardProperty).isEmpty();
					var entity = oml.addRelationEntity(ontology, e.getIRI().getFragment(), sourceIris, targetIris, functional, inverseFunctional, symmetric, asymmetric, reflexive, irreflexive, transitive);
					createAnnotations(owlOntology, e.getIRI(), ontology);
					if (!(forwardIri.getFragment().startsWith("has") && forwardIri.getFragment().endsWith("Forward"))) {
						oml.addForwardRelation(entity, forwardIri.getFragment());
						createAnnotations(owlOntology, forwardIri, ontology);
					}
					var reverseProperty = forwardProperty.getInverseProperty();
					if (reverseProperty != null && reverseProperty.isNamed()) {
						oml.addReverseRelation(entity, reverseProperty.asOWLObjectProperty().getIRI().getFragment());
						createAnnotations(owlOntology, reverseProperty.asOWLObjectProperty().getIRI(), ontology);
					}
				}
			}
		});
	}

	private void createStructures(OWLOntology owlOntology, Vocabulary ontology) {
		listDeclaredEntitiesofType(owlOntology, OWLClass.class).forEach(e -> {			
			var type = getAnnotationValue(owlOntology, e.getIRI(), DC.TYPE);
			if (type != null && type.toString().equals(OmlConstants.Structure)) {
				oml.addStructure(ontology, e.getIRI().getFragment());
				createAnnotations(owlOntology, e.getIRI(), ontology);
			}
		});
	}

	/*private void createScalars(OWLOntology owlOntology, Vocabulary ontology) {
		listDeclaredEntitiesofType(owlOntology, OWLClass.class).forEach(e -> {			
			var type = getAnnotationValue(owlOntology, e.getIRI(), DC.TYPE);
			if (type != null && type.toString().equals(OmlConstants.Structure)) {
				oml.addStructure(ontology, e.getIRI().getFragment());
				createAnnotations(owlOntology, e.getIRI(), ontology);
			}
		});
	}*/

	private void creaAnnotationProperties(OWLOntology owlOntology, Vocabulary ontology) {
		listDeclaredEntitiesofType(owlOntology, OWLAnnotationProperty.class).forEach(e -> {
			oml.addAnnotationProperty(ontology, e.getIRI().getFragment());
			createAnnotations(owlOntology, e.getIRI(), ontology);
		});
	}

	/*private void creaScalarProperties(OWLOntology owlOntology, Vocabulary ontology) {
		listDeclaredEntitiesofType(owlOntology, OWLDataProperty.class).forEach(e -> {
			oml.addAnnotationProperty(ontology, e.getIRI().getFragment());
			createAnnotations(owlOntology, e.getIRI(), ontology);
		});
	}

	private void creaStructuredProperties(OWLOntology owlOntology, Vocabulary ontology) {
		listDeclaredEntitiesofType(owlOntology, OWLObjectProperty.class).forEach(e -> {
			oml.addAnnotationProperty(ontology, e.getIRI().getFragment());
			createAnnotations(owlOntology, e.getIRI(), ontology);
		});
	}

	private void creaReifiedRelations(OWLOntology owlOntology, Vocabulary ontology) {
		listDeclaredEntitiesofType(owlOntology, OWLObjectProperty.class).forEach(e -> {
			oml.addAnnotationProperty(ontology, e.getIRI().getFragment());
			createAnnotations(owlOntology, e.getIRI(), ontology);
		});
	}*/

	// -----------

	private void createOntologyImports(OWLOntology owlOntology, Ontology ontology) {
		owlOntology.directImports().forEach(i -> {
			var namespace = i.getFormat().asPrefixOWLDocumentFormat().getDefaultPrefix();
			var importingType = getAnnotationValue(owlOntology, DC.TYPE).toString();
			var importedType = getAnnotationValue(i, DC.TYPE).toString();
			
			if (importingType.equals(OmlConstants.DescriptionBundle)) {
				if (importedType.equals(OmlConstants.DescriptionBundle)) {
					oml.addImport(ontology, ImportKind.EXTENSION, namespace, null);
				} else if (importedType.equals(OmlConstants.Description)) {
					oml.addImport(ontology, ImportKind.INCLUSION, namespace, null);
				} else if (importedType.equals(OmlConstants.VocabularyBundle)) {
					oml.addImport(ontology, ImportKind.USAGE, namespace, null);
				} else if (importedType.equals(OmlConstants.Vocabulary)) {
					oml.addImport(ontology, ImportKind.USAGE, namespace, null);
				}
			} else if (importingType.equals(OmlConstants.VocabularyBundle)) {
				if (importedType.equals(OmlConstants.VocabularyBundle)) {
					oml.addImport(ontology, ImportKind.EXTENSION, namespace, null);
				} else if (importedType.equals(OmlConstants.Vocabulary)) {
					oml.addImport(ontology, ImportKind.INCLUSION, namespace, null);
				}
			} else if (importingType.equals(OmlConstants.Vocabulary)) {
				if (importedType.equals(OmlConstants.Vocabulary)) {
					oml.addImport(ontology, ImportKind.EXTENSION, namespace, null);
				} else if (importedType.equals(OmlConstants.Description)) {
					oml.addImport(ontology, ImportKind.USAGE, namespace, null);
				}
			} else if (importingType.equals(OmlConstants.Description)) {
				if (importedType.equals(OmlConstants.Description)) {
					oml.addImport(ontology, ImportKind.EXTENSION, namespace, null);
				} else if (importedType.equals(OmlConstants.Vocabulary)) {
					oml.addImport(ontology, ImportKind.USAGE, namespace, null);
				}
			}
		});
	}
	
	private void createOntologyAnnotations(OWLOntology owlOntology, Ontology ontology) {
		var axioms = owlOntology.annotationsAsList();
		axioms.forEach(a -> {
			var propertyIri = a.getProperty().getIRI();
			var value = a.getValue();
			if (!(propertyIri.toString().equals(DC.TYPE.toString()) && value.toString().startsWith(OmlConstants.OML_NS))) {
				oml.addAnnotation(
					ontology,
					getIriAndImportIfNeeded(ontology, propertyIri), 
					value.isLiteral() ? createLiteral(value.asLiteral().get(), ontology) : null,
					value.isIRI() ? getIriAndImportIfNeeded(ontology, (IRI)value) : null);
			}
		});
	}

	private void createAnnotations(OWLOntology owlOntology, IRI subjectIri, Ontology ontology) {
        owlOntology.annotationAssertionAxioms(subjectIri).forEach(axiom -> {
			var a = axiom.getAnnotation();
			var propertyIri = a.getProperty().getIRI();
			var value = a.getValue();
			if (!(propertyIri.toString().equals(DC.TYPE.toString()) && value.toString().startsWith(OmlConstants.OML_NS))) {
				oml.addAnnotation(
					ontology,
					subjectIri.toString(),
					getIriAndImportIfNeeded(ontology, propertyIri), 
					value.isLiteral() ? createLiteral(value.asLiteral().get(), ontology) : null,
					value.isIRI() ? getIriAndImportIfNeeded(ontology, (IRI)value) : null);
			}
		});
	}

	private Literal createLiteral(OWLLiteral literal, Ontology ontology) {
		if (literal.isBoolean()) {
			return oml.createBooleanLiteral(literal.parseBoolean());
		} else if (literal.isInteger()) {
			return oml.createIntegerLiteral(literal.parseInteger());
		} else if (literal.isDouble()) {
			return oml.createDoubleLiteral(literal.parseDouble());
		} else {
			var datatype = literal.getDatatype();
			if (XSD.DECIMAL.toString().equals(datatype.getIRI().toString())) {
				var value = (BigDecimal) OmlFactory.eINSTANCE.createFromString(
						OmlPackage.Literals.DECIMAL, literal.toString());
				return oml.createDecimalLiteral(value);
			} else {
				var lang = literal.getLang();
				var type = datatype.getIRI();
				return oml.createQuotedLiteral(
					ontology,
					literal.getLiteral(),
					!type.toString().equals(XSD.STRING.toString()) 
						? getIriAndImportIfNeeded(ontology, type) 
						: null,
					lang.length() > 0 ? lang : null);
			}
		}
	}

	private String getIriAndImportIfNeeded(Ontology ontology, IRI termIRI) {
		String namespace = termIRI.getNamespace();
		if (!namespace.equals(ontology.getNamespace())) {
			var owlOntology = manager.getOntology(IRI.create(ontology.getIri()));
			var prefix = getOntologyNamespacePrefix(owlOntology, namespace);
			Optional<Import> imp = ontology.getOwnedImports().stream().filter(i -> i.getNamespace().equals(namespace)).findAny();
			if (imp.isPresent()) {
				if (imp.get().getPrefix() == null) {
					imp.get().setPrefix(prefix);
				}
			} else {
				if (ontology instanceof Vocabulary) {
					oml.addImport(ontology, ImportKind.EXTENSION, namespace, prefix);
				} else if (ontology instanceof VocabularyBundle) {
					oml.addImport(ontology, ImportKind.INCLUSION, namespace, prefix);
				} else if (ontology instanceof Description || ontology instanceof DescriptionBundle) {
					oml.addImport(ontology, ImportKind.USAGE, namespace, prefix);
				}
			}
		}
		
		return termIRI.getIRIString();
	}

	// -----------

    private static String pattern = "(\\w+) derivation";
    private static Pattern derivationPattern = Pattern.compile(pattern);

	private IRI getForwardRelationIri(OWLOntology owlOntology, IRI iri) {
		for (SWRLRule rule : owlOntology.axioms(AxiomType.SWRL_RULE).collect(Collectors.toList())) {
			var value = (OWLLiteral) getAnnotationValue(rule, RDFS.LABEL);
			if (value != null) {
				Matcher matcher = derivationPattern.matcher(value.getLiteral());
		    	if (matcher.find()) {
					var body = rule.getBody().iterator().next();
					if (body != null) {
						if (body.getPredicate() instanceof OWLClass) {
							if (((OWLClass)body.getPredicate()).getIRI().equals(iri)) {
								String name = matcher.group(1);
								return IRI.create(iri.getNamespace()+name);
							}
						}
					}
		    	}
			}
		}
		return null;
	}
	
	private <T extends OWLEntity> Stream<T> listDeclaredEntitiesofType(OWLOntology owlOntology, Class<T> type) {
		return owlOntology.axioms(AxiomType.DECLARATION)
				.filter(i -> type.isInstance(i.getEntity()))
				.map(i -> type.cast(i.getEntity()));
	}
	
	private String getOntologyNamespacePrefix(OWLOntology owlOntology, String namespace) {
		Map<String, String> map = owlOntology.getFormat().asPrefixOWLDocumentFormat().getPrefixName2PrefixMap();
		String prefix = reverse(map).get(namespace);
		prefix = prefix.substring(0, prefix.length()-1);
		if (prefix.length() == 0) {
			prefix = URI.createURI(namespace).lastSegment();
		}
		return prefix;
	}
	
	private OWLAnnotationValue getAnnotationValue(HasAnnotations objectWithAnnotations, org.eclipse.rdf4j.model.IRI propertyIri) {
		var property = manager.getOWLDataFactory().getOWLAnnotationProperty(propertyIri.stringValue());
		var value = objectWithAnnotations.annotations(property).findFirst();
		return (value.isPresent() ? value.get().getValue() : null);
	}

	private OWLAnnotationValue getAnnotationValue(OWLOntology owlOntology, OWLAnnotationSubject subject, org.eclipse.rdf4j.model.IRI propertyIri) {
		var property = manager.getOWLDataFactory().getOWLAnnotationProperty(propertyIri.stringValue());
		var value = owlOntology.annotationAssertionAxioms(subject).filter(i -> i.getProperty().equals(property)).findFirst();
		return (value.isPresent() ? value.get().getValue() : null);
	}
	
	private <K, V> Map<V, K> reverse(Map<K, V> map) {
		Map<V, K> mapInv=new HashMap<>();
		for (K key : map.keySet()) 
	        mapInv.put(map.get(key), key);
		return mapInv;
	}
}
