/**
 * 
 * Copyright 2024 California Institute of Technology ("Caltech").
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.HasAnnotations;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import io.opencaesar.oml.Description;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.util.OmlBuilder;
import io.opencaesar.oml.util.OmlCatalog;
import io.opencaesar.oml.util.OmlConstants;

/**
 * Converts an OWL ontology to OML ontology  (Do not use, still work in progress) 
 */
class Owl2OmlEx extends Owl2Oml {

	public Owl2OmlEx(OWLOntologyManager manager, OmlBuilder oml, OmlCatalog catalog, String outputFileExtension) {
		super(manager, oml, catalog, outputFileExtension);
	}

	protected String correct(String iri) {
		iri = iri.replace(",", "_");
		iri = iri.replace(";", "_");
		iri = iri.replace("/", "_");
		return iri;
	}

	@Override
	protected String getNamespace(IRI iri) {
		var str = iri.getIRIString();
		int i = str.lastIndexOf('#');
		if (i == -1) {
			i = str.lastIndexOf('/');
		}
		return (i > 0) ? str.substring(0, i+1) : iri.getNamespace();
	}

	@Override
	protected String getFragment(IRI iri) {
		var str = iri.getIRIString();
		int i = str.lastIndexOf('#');
		if (i == -1) {
			i = str.lastIndexOf('/');
		}
		String fragment = (i > 0) ? str.substring(i+1) : iri.getFragment();
		return correct(fragment);
	}

	@Override
	protected String getNamespace(OWLOntology owlOntology) {
		var namespace = super.getNamespace(owlOntology);
		if (namespace != null) {
			return namespace;
		}
		// sometimes the iri is wrongly specified as a namespace
		var iri =  getIri(owlOntology);
		if (iri.endsWith("#") || iri.endsWith("/")) {
			return iri;
		}
		// check if a default namespace is specified
		var prefix = owlOntology.getFormat().asPrefixOWLDocumentFormat().getDefaultPrefix();
		if (prefix != null && prefix.contains(iri)) {
			return prefix;
		}
		// check if one of the prefixed namespaces includes the IRI 
		for (var ns : owlOntology.getFormat().asPrefixOWLDocumentFormat().getPrefixName2PrefixMap().values()) {
			if (ns.startsWith(iri)) {
				return ns;
			}
		}
		// oh well, take a best guess
		return iri + "#";
	}

	@Override
	protected String getPrefix(OWLOntology owlOntology) {
		var prefix = super.getPrefix(owlOntology);
		if (prefix != null) {
			return prefix;
		}
		// check if it's a known ontology prefix
		var iri = owlOntology.getOntologyID().getOntologyIRI().get();
		if (iri.getIRIString().equals("http://purl.org/dc/elements/1.1")) {
			return "dc";
		}
		// check if a prefix is declared
		var namespace = getNamespace(owlOntology);
		for (var e : owlOntology.getFormat().asPrefixOWLDocumentFormat().getPrefixName2PrefixMap().entrySet()) {
			if (e.getValue().equals(namespace) && !e.getKey().equals(":")) {
				prefix = e.getKey();
				prefix = prefix.substring(0, prefix.length()-1);
				return prefix;
			}
		}
		// check the short form
		prefix = iri.getShortForm();
		if (prefix.endsWith("#") || prefix.endsWith("/")) {
			prefix = prefix.substring(0, prefix.length()-1);
		}
		return prefix;
	}

	@Override
	protected String getOmlType(HasAnnotations hasAnnotations) {
		var type = super.getOmlType(hasAnnotations);
		if (type != null) {
			return type;
		}
		if (hasAnnotations instanceof OWLOntology) {
			if (tBoxAxioms((OWLOntology)hasAnnotations).findAny().isPresent()) {
				type = OmlConstants.Vocabulary;
			} else {
				type = OmlConstants.Description;
			}
		}
		return type;
	}

	@Override
	protected String getImportedIri(IRI iri, Ontology ontology) {
		var importedIri = super.getImportedIri(iri, ontology);
		if (importedIri == null) {
			return null;
		}
		var namespace = getNamespace(iri);
		var fragment = getFragment(iri);
		return namespace+fragment;
	}
	
	private boolean isABoxAxiom(OWLOntology owlOntology, OWLAxiom axiom) {
		var type = axiom.getAxiomType();
		if (AxiomType.DECLARATION.equals(type)) {
			return isNamedIndividualOnly(owlOntology, ((OWLDeclarationAxiom)axiom).getEntity().getIRI());
		} else if (AxiomType.ANNOTATION_ASSERTION.equals(type)) {
			var a = (OWLAnnotationAssertionAxiom)axiom;
			if (a.getSubject().isIRI()) {
				return isNamedIndividualOnly(owlOntology, a.getSubject().asIRI().get());
			}
		} else if (AxiomType.CLASS_ASSERTION.equals(type)) {
			if (((OWLClassAssertionAxiom)axiom).getIndividual().isNamed()) {
				var individual = ((OWLClassAssertionAxiom)axiom).getIndividual().asOWLNamedIndividual();
				return isNamedIndividualOnly(owlOntology, individual.getIRI());
			}
		} else if (AxiomType.DATA_PROPERTY_ASSERTION.equals(type)) {
			if (((OWLDataPropertyAssertionAxiom)axiom).getSubject().isNamed()) {
				var individual = ((OWLDataPropertyAssertionAxiom)axiom).getSubject().asOWLNamedIndividual();
				return isNamedIndividualOnly(owlOntology, individual.getIRI());
			};
		} else if (AxiomType.OBJECT_PROPERTY_ASSERTION.equals(type)) {
			if (((OWLObjectPropertyAssertionAxiom)axiom).getSubject().isNamed()) {
				var individual = ((OWLObjectPropertyAssertionAxiom)axiom).getSubject().asOWLNamedIndividual();
				return isNamedIndividualOnly(owlOntology, individual.getIRI());
			};
		}
		return false;
	}

	private boolean isNamedIndividualOnly(OWLOntology owlOntology, IRI iri) {
		var entities = owlOntology.getEntitiesInSignature(iri, Imports.INCLUDED);
		return entities.size() == 1 && entities.iterator().next() instanceof OWLNamedIndividual;
	}
	
	private Stream<OWLAxiom> tBoxAxioms(OWLOntology owlOntology) {
		return owlOntology.axioms(Imports.EXCLUDED).filter(i -> !isABoxAxiom(owlOntology, i));
	}

	private Stream<OWLAxiom> aBoxAxioms(OWLOntology owlOntology) {
		return owlOntology.axioms(Imports.EXCLUDED).filter(i -> isABoxAxiom(owlOntology, i));
	}

	private void changeIndividualsNamespace(OWLOntology owlOntology, String oldNS, String newNS) {
		var map = new HashMap<OWLEntity, IRI>();

		owlOntology.axioms(AxiomType.DECLARATION, Imports.EXCLUDED)
			.filter(i -> i.getEntity() instanceof OWLNamedIndividual)
			.map(i -> i.getEntity())
			.filter(i -> owlOntology.getEntitiesInSignature(i.getIRI()).size() == 1)
			.forEach(i -> map.put(i, IRI.create(i.getIRI().getIRIString().replace(oldNS, newNS))));

		var renamer = new OWLEntityRenamer(manager, Collections.singletonList(owlOntology));
		manager.applyChanges(renamer.changeIRI(map));
	}

	protected List<Ontology> visitOntology(OWLOntology owlOntology) throws IOException, OWLException {
		var type = super.getOmlType(owlOntology);
		var isTBox = tBoxAxioms(owlOntology).findAny().isPresent();
		var isABox = aBoxAxioms(owlOntology).findAny().isPresent();
		if (type == null && isTBox && isABox) {
			var ontologies = new ArrayList<Ontology>();

			// change individuals to a different namespace derived from this one
			var tbox_ns = getNamespace(owlOntology);
			var tbox_iri = tbox_ns.substring(0, tbox_ns.length()-1);
			var abox_iri = tbox_iri+"-d";
			var abox_ns = abox_iri+tbox_ns.substring(tbox_ns.length()-1);
			changeIndividualsNamespace(owlOntology, tbox_ns, abox_ns);

			// remove the abox axioms from tbox
			var aboxAxioms = aBoxAxioms(owlOntology).collect(Collectors.toList());
			owlOntology.removeAxioms(aboxAxioms);

			// create new abox ontology and add the axioms to it
			var abox = manager.createOntology(IRI.create(abox_iri));
			manager.applyChanges(new AddImport(abox, manager.getOWLDataFactory().getOWLImportsDeclaration(IRI.create(tbox_iri))));
			for (var i : owlOntology.getImportsDeclarations()) {
				manager.applyChanges(new AddImport(abox, i));
			}
			for (var a : owlOntology.getAnnotations()) {
				manager.applyChange(new AddOntologyAnnotation(abox, a));
			}
			abox.addAxioms(aboxAxioms);
			
			// convert the tbox
			ontologies.addAll(super.visitOntology(owlOntology));
			
			//convert the abox
			ontologies.addAll(super.visitOntology(abox));

			return ontologies;
		}
		return super.visitOntology(owlOntology);
	}

	/**
	 * If the annotation IRI value is not of an importable member, treat it as a literal
	 */
	protected boolean visitAnnotationAssertionAxiom(OWLAnnotationAssertionAxiom axiom, OWLOntology owlOntology, Ontology ontology) {
		var value = axiom.getValue();
		if (value.isIRI()) {
			IRI valueIri = value.asIRI().get();
			String namespace = getNamespace(valueIri);
			String fragment = (valueIri.getIRIString().length() != namespace.length()) ? valueIri.getIRIString().substring(namespace.length()) : "";
			IRI ontologyIri = IRI.create(namespace.substring(0, namespace.length()-1));
			OWLOntology importedOntology = manager.getOntology(ontologyIri);
			boolean isNamespaceImportable = (importedOntology != null) || isStandardNamespace(namespace);
			if (!isNamespaceImportable || fragment.equals("")) {
				var subjectIri = getImportedIri(axiom.getSubject().asIRI().get(), ontology);
				var propertyIri = getImportedIri(axiom.getProperty().getIRI(), ontology);
				if (subjectIri != null && propertyIri != null) {
					oml.addAnnotation(
							ontology,
							subjectIri,
							propertyIri, 
							oml.createQuotedLiteral(ontology, value.asIRI().get().toString(), null, null),
							null);
				}
				return true;
			}
		}
		return super.visitAnnotationAssertionAxiom(axiom, owlOntology, ontology);
	}

	protected boolean visitObjectPropertyAssertionAxiom(Object subject, OWLObjectPropertyAssertionAxiom axiom, OWLOntology owlOntology, Ontology ontology) {
		if (ontology instanceof Description) {
			var property = axiom.getProperty();
			if (property instanceof OWLObjectProperty) {
				var propertyIri = ((OWLObjectProperty)property).getIRI().getIRIString();
				if (!OmlConstants.sourceRelation.equals(propertyIri) && !OmlConstants.targetRelation.equals(propertyIri)) {
					var object = axiom.getObject();
					if (object.isNamed()) {
						if (!isNamedIndividualOnly(owlOntology, object.asOWLNamedIndividual().getIRI())) {
							return false;
						}
					}
				}
			}
		}
		return super.visitObjectPropertyAssertionAxiom(subject, axiom, owlOntology, ontology);
	}
}
