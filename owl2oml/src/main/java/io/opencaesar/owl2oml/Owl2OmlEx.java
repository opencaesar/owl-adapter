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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.HasAnnotations;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.parameters.Imports;

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
		return (i > 0) ? str.substring(i+1) : iri.getFragment();
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
			if (isTBox((OWLOntology)hasAnnotations)) {
				type = OmlConstants.Vocabulary;
			} else {
				type = OmlConstants.Description;
			}
		}
		return type;
	}

	protected List<Ontology> visitOntology(OWLOntology owlOntology) throws IOException, OWLException {
		var type = super.getOmlType(owlOntology);
		var isTbox = isTBox(owlOntology);
		var isAbox = isABox(owlOntology);
		if (type == null && isTbox && isAbox) {
			var ontologies = new ArrayList<Ontology>();
			
			var iri = getIri(owlOntology);
			var imports = owlOntology.getImportsDeclarations();
			var annotations = owlOntology.getAnnotations();
			
			// convert the tbox
			var tbox_iri = IRI.create(iri);
			var tbox = manager.createOntology(tbox_iri);
			for (var i : imports) {
				manager.applyChanges(new AddImport(tbox, i));
			}
			for (var a : annotations) {
				manager.applyChange(new AddOntologyAnnotation(tbox, a));
			}
			tbox.addAxioms(getTBoxAxioms(owlOntology));
			ontologies.addAll(visitOntology(tbox));
						
			// convert the abox
			var abox_iri = IRI.create(iri+"-d");
			var abox = manager.createOntology(abox_iri);
			manager.applyChanges(new AddImport(abox, manager.getOWLDataFactory().getOWLImportsDeclaration(tbox_iri)));
			for (var i : imports) {
				manager.applyChanges(new AddImport(abox, i));
			}
			for (var a : annotations) {
				manager.applyChange(new AddOntologyAnnotation(abox, a));
			}
			abox.addAxioms(getABoxAxioms(owlOntology));
			ontologies.addAll(visitOntology(abox));

			return ontologies;
		}
		return super.visitOntology(owlOntology);
	}
	
	private boolean isTBox(OWLOntology owlOntology) {
		return owlOntology.tboxAxioms(Imports.EXCLUDED).findAny().isPresent() ||
			owlOntology.getAxioms(AxiomType.DECLARATION).stream().filter(i -> {
				var entity = i.getEntity();
				return (entity instanceof OWLClass) || 
					   (entity instanceof OWLDatatype) || 
					   (entity instanceof OWLProperty);
			}).findAny().isPresent();
	}

	private Set<OWLAxiom> getTBoxAxioms(OWLOntology owlOntology) {
		return owlOntology.axioms(Imports.EXCLUDED).filter(i -> {
			var type = i.getAxiomType();
			if (AxiomType.DECLARATION.equals(type)) {
				return ((OWLDeclarationAxiom)i).getEntity() instanceof OWLNamedIndividual;
			} else if (AxiomType.ABoxAxiomTypes.contains(type)) {
				if (AxiomType.CLASS_ASSERTION.equals(type)) {
					return ((OWLClassAssertionAxiom) i).getIndividual() instanceof OWLNamedIndividual;
				}
				if (AxiomType.DATA_PROPERTY_ASSERTION.equals(type)) {
					return ((OWLDataPropertyAssertionAxiom) i).getSubject() instanceof OWLNamedIndividual;
				}
				if (AxiomType.OBJECT_PROPERTY_ASSERTION.equals(type)) {
					return ((OWLObjectPropertyAssertionAxiom) i).getSubject() instanceof OWLNamedIndividual;
				}
			}
			return false;
		}).collect(Collectors.toSet());
	}

	private boolean isABox(OWLOntology owlOntology) {
		return owlOntology.axioms(Imports.EXCLUDED).filter(i -> {
			var type = i.getAxiomType();
			if (AxiomType.DECLARATION.equals(type)) {
				return ((OWLDeclarationAxiom)i).getEntity() instanceof OWLNamedIndividual;
			} else if (AxiomType.ABoxAxiomTypes.contains(type)) {
				if (AxiomType.CLASS_ASSERTION.equals(type)) {
					return ((OWLClassAssertionAxiom) i).getIndividual() instanceof OWLNamedIndividual;
				}
				if (AxiomType.DATA_PROPERTY_ASSERTION.equals(type)) {
					return ((OWLDataPropertyAssertionAxiom) i).getSubject() instanceof OWLNamedIndividual;
				}
				if (AxiomType.OBJECT_PROPERTY_ASSERTION.equals(type)) {
					return ((OWLObjectPropertyAssertionAxiom) i).getSubject() instanceof OWLNamedIndividual;
				}
			}
			return false;
		}).findAny().isPresent();
	}
	
	private Set<OWLAxiom> getABoxAxioms(OWLOntology owlOntology) {
		return owlOntology.axioms(Imports.EXCLUDED).filter(i -> {
			var type = i.getAxiomType();
			if (AxiomType.DECLARATION.equals(type)) {
				return ((OWLDeclarationAxiom)i).getEntity() instanceof OWLNamedIndividual;
			} else if (AxiomType.ABoxAxiomTypes.contains(type)) {
				if (AxiomType.CLASS_ASSERTION.equals(type)) {
					return ((OWLClassAssertionAxiom) i).getIndividual() instanceof OWLNamedIndividual;
				}
				if (AxiomType.DATA_PROPERTY_ASSERTION.equals(type)) {
					return ((OWLDataPropertyAssertionAxiom) i).getSubject() instanceof OWLNamedIndividual;
				}
				if (AxiomType.OBJECT_PROPERTY_ASSERTION.equals(type)) {
					return ((OWLObjectPropertyAssertionAxiom) i).getSubject() instanceof OWLNamedIndividual;
				}
			}
			return false;
		}).collect(Collectors.toSet());
	}
}
