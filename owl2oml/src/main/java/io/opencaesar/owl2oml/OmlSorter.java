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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import io.opencaesar.oml.Annotation;
import io.opencaesar.oml.AnonymousConceptInstance;
import io.opencaesar.oml.Argument;
import io.opencaesar.oml.Element;
import io.opencaesar.oml.EntityEquivalenceAxiom;
import io.opencaesar.oml.Import;
import io.opencaesar.oml.InstanceEnumerationAxiom;
import io.opencaesar.oml.KeyAxiom;
import io.opencaesar.oml.Literal;
import io.opencaesar.oml.LiteralEnumerationAxiom;
import io.opencaesar.oml.Member;
import io.opencaesar.oml.OmlPackage;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.PropertyCardinalityRestrictionAxiom;
import io.opencaesar.oml.PropertyEquivalenceAxiom;
import io.opencaesar.oml.PropertyRangeRestrictionAxiom;
import io.opencaesar.oml.PropertySelfRestrictionAxiom;
import io.opencaesar.oml.PropertyValueAssertion;
import io.opencaesar.oml.PropertyValueRestrictionAxiom;
import io.opencaesar.oml.ScalarEquivalenceAxiom;
import io.opencaesar.oml.SpecializationAxiom;
import io.opencaesar.oml.TypeAssertion;

/**
 * Canonically sorts the content of an OML element 
 */
class OmlSorter {
	
	/**
	 * Sorts in place the contents of the given element
	 * 
	 * @param element The given element
	 */
	public static void sort(Element element) {
		EClass eClass = element.eClass();
		for (EStructuralFeature feature : eClass.getEAllStructuralFeatures()) {
			if (feature.isMany() && feature.isChangeable()) {
				List<?> values = (List<?>) element.eGet(feature);
				if (feature instanceof EReference && 
					((EReference)feature).isContainment() &&
					OmlPackage.Literals.ELEMENT.isSuperTypeOf(((EReference)feature).getEReferenceType())) {
					values.stream().forEach(v -> sort((Element)v));
				}
				if (!shouldNotSort(feature)) {
					sort(values, Comparator.comparing(i -> toString(i)));
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <T> void sort(List<T> aList, Comparator<T> c) {
        Object[] array = aList.toArray();
        Arrays.sort(array, (Comparator) c);
        aList.clear();
        for (var a : array) {
        	aList.add((T)a);
        }
    }

	private static boolean shouldNotSort(EStructuralFeature feature) {
		return feature == OmlPackage.Literals.RULE__ANTECEDENT ||
				feature == OmlPackage.Literals.RULE__CONSEQUENT;
	}
	
	private static String toString(Object o) {
		if (o instanceof Ontology) {
			return ((Ontology)o).getNamespace();
		} else if (o instanceof Member) {
			return ((Member)o).getAbbreviatedIri();
		} else if (o instanceof Literal) {
			return ((Literal)o).getLexicalValue();
		} else if (o instanceof Import) {
			return ((Import)o).getKind() + ((Import)o).getNamespace();
		} else if (o instanceof Argument) {
			return toString(((Argument)o).getValue());
		} else if (o instanceof Annotation) {
			return toString(((Annotation)o).getProperty()) + toString(((Annotation)o).getValue());
		} else if (o instanceof AnonymousConceptInstance) {
			return toString(((AnonymousConceptInstance)o).getType());
		} else if (o instanceof TypeAssertion) {
			return toString(((TypeAssertion)o).getType());
		} else if (o instanceof PropertyValueAssertion) {
			return toString(((PropertyValueAssertion)o).getProperty()) + toString(((PropertyValueAssertion)o).getObject());
		} else if (o instanceof SpecializationAxiom) {
			return toString(((SpecializationAxiom)o).getSuperTerm());
		} else if (o instanceof PropertyRangeRestrictionAxiom) {
			return toString(((PropertyRangeRestrictionAxiom)o).getProperty()) + toString(((PropertyRangeRestrictionAxiom)o).getRange());
		} else if (o instanceof PropertyCardinalityRestrictionAxiom) {
			return toString(((PropertyCardinalityRestrictionAxiom)o).getProperty()) + toString(((PropertyCardinalityRestrictionAxiom)o).getCardinality()) + toString(((PropertyCardinalityRestrictionAxiom)o).getRange());
		} else if (o instanceof PropertyValueRestrictionAxiom) {
			return toString(((PropertyValueRestrictionAxiom)o).getProperty()) + toString(((PropertyValueRestrictionAxiom)o).getValue());
		} else if (o instanceof PropertySelfRestrictionAxiom) {
			return toString(((PropertySelfRestrictionAxiom)o).getProperty());
		} else if (o instanceof KeyAxiom) {
			return ((KeyAxiom)o).getProperties().stream().map(p -> toString(p)).collect(Collectors.joining());
		} else if (o instanceof InstanceEnumerationAxiom) {
			return ((InstanceEnumerationAxiom)o).getInstances().stream().map(l -> toString(l)).collect(Collectors.joining());
		} else if (o instanceof LiteralEnumerationAxiom) {
			return ((LiteralEnumerationAxiom)o).getLiterals().stream().map(l -> toString(l)).collect(Collectors.joining());
		} else if (o instanceof EntityEquivalenceAxiom) {
			return ((EntityEquivalenceAxiom)o).getSuperEntities().stream().map(l -> toString(l)).collect(Collectors.joining());
		} else if (o instanceof PropertyEquivalenceAxiom) {
			return toString(((PropertyEquivalenceAxiom)o).getSuperProperty());
		} else if (o instanceof ScalarEquivalenceAxiom) {
			return toString(((ScalarEquivalenceAxiom)o).getSuperScalar());
		} else if (o != null) {
			return o.toString();
		}
		return null;
	}
	
}
