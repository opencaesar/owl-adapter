/**
 * 
 * Copyright 2019-2024 California Institute of Technology ("Caltech").
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.opencaesar.oml.Argument;
import io.opencaesar.oml.BuiltInPredicate;
import io.opencaesar.oml.DifferentFromPredicate;
import io.opencaesar.oml.Member;
import io.opencaesar.oml.Predicate;
import io.opencaesar.oml.PropertyPredicate;
import io.opencaesar.oml.RelationEntityPredicate;
import io.opencaesar.oml.Rule;
import io.opencaesar.oml.SameAsPredicate;
import io.opencaesar.oml.TypePredicate;
import io.opencaesar.oml.Vocabulary;
import io.opencaesar.oml.VocabularyBundle;
import io.opencaesar.oml.util.OmlRead;

/**
 * Converts Oml Rules to Jena Rules (*.rules) files 
 */
public class Oml2Rules {

	private static final String newLine = System.getProperty("line.separator"); 
	private static final String sourceRelation = "oml:hasSource";
	private static final String targetRelation = "oml:hasTarget";

	private VocabularyBundle bundle;
	private File rulesFile;
	private Map<String, String> prefixes;
	private StringBuilder ruleBuilder;

	/**
	 * Constructs a new instance
	 *  
	 * @param bundle The vocabulary bundle
	 * @param rulesFile The rules file
	 */
	public Oml2Rules(VocabularyBundle bundle, File rulesFile) {
		this.bundle = bundle;
		this.rulesFile = rulesFile;
		this.ruleBuilder = new StringBuilder();
		this.prefixes = new LinkedHashMap<>();
		addDefaultRules();
	}
	
	/**
	 * Runs the converter
	 * 
	 * @return Oml2Rules
	 */
	public Oml2Rules run() {
		var rules = OmlRead.getImportedOntologyClosure(bundle, false).stream()
			.filter(o -> o instanceof Vocabulary)
			.map(o -> (Vocabulary)o)
			.flatMap(v -> v.getOwnedStatements().stream())
			.filter(s -> s instanceof Rule)
			.map(s -> (Rule) s)
			.collect(Collectors.toList());
		for (Rule rule : rules) {
			add(rule);
		}
		return this;
	}

	/**
	 * Saves the files
	 * 
	 * @throws IOException If an IO issue exists
	 */
	public void save() throws IOException {
		BufferedWriter writer = null;
		try {
		    writer = new BufferedWriter(new FileWriter(rulesFile));
		    for (var prefix : prefixes.entrySet()) {
		    	writer.append("@prefix "+prefix.getKey()+": "+prefix.getValue());
		    	writer.newLine();
		    }
	    	writer.newLine();
		    writer.append(ruleBuilder);
		} finally {
		    if (writer != null) {
		    	writer.close();
		    }
		}
	}

	private void add(Rule rule) {
		List<String> antecedents = new ArrayList<>();
		for (Predicate p : rule.getAntecedent()) {
			antecedents.add(addPredicate(p));
		}
		List<String> consequents = new ArrayList<>();
		for (Predicate p : rule.getConsequent()) {
			consequents.add(addPredicate(p));
		}
		ruleBuilder
		.append(newLine)
		.append(newLine)
		.append("["+rule.getName()+": ")
		.append(String.join(", ", antecedents))
		.append(" -> ")
		.append(String.join(", ", consequents))
		.append("]");
	}
		
	private void addDefaultRules() {
		prefixes.put("oml", "http://opencaesar.io/oml#");
		
		ruleBuilder
			.append("# Default Rules:")
			.append(newLine)
			.append(newLine)
			.append("[ClassHierarchy: (?x rdf:type ?c1), (?c1 rdfs:subClassOf ?c2) notBNode(?c2) -> (?x rdf:type ?c2)]")
			.append(newLine)
			.append(newLine)
			.append("[PropertyHierarchy: (?x ?p1 ?y), (?p1 rdfs:subPropertyOf ?p2) notBNode(?p2) -> (?x ?p2 ?y)]")
			.append(newLine)
			.append(newLine)
			.append("[Inverse: (?x ?p1 ?y), (?p1 owl:inverseOf ?p2) notBNode(?p2) -> (?y ?p2 ?x)]")
			.append(newLine)
			.append(newLine)
			.append("[Reification: (?x rdf:type ?r), (?x "+sourceRelation+" ?s), (?x "+targetRelation+" ?t), (?p oml:relationEntity ?r) -> (?s ?p ?t)]")
			.append(newLine)
			.append(newLine)
			.append("# Bundle Rules:");
	}
	
	private String addPredicate(Predicate p) {
		StringBuilder b = new StringBuilder();
		if (p instanceof TypePredicate) {
			TypePredicate pp = (TypePredicate)p;
			b.append("(")
			 .append(addArgument(pp.getArgument()))
			 .append(" rdf:type ")
			 .append(addIri(pp.getType()))
			 .append(")");
		} else if (p instanceof PropertyPredicate) {
			PropertyPredicate pp = (PropertyPredicate)p;
			b.append("(")
			 .append(addArgument(pp.getArgument1()))
			 .append(" "+addIri(pp.getProperty())+" ")
			 .append(addArgument(pp.getArgument2()))
			 .append(")");
		} else if (p instanceof RelationEntityPredicate) {
			RelationEntityPredicate pp = (RelationEntityPredicate)p;
			b.append("(")
			 .append(addArgument(pp.getArgument()))
			 .append(" rdf:type ")
			 .append(addIri(pp.getType()))
			 .append("), (")
			 .append(addArgument(pp.getArgument()))
			 .append(" "+sourceRelation+" ")
			 .append(addArgument(pp.getArgument1()))
			 .append("), (")
			 .append(addArgument(pp.getArgument()))
			 .append(" "+targetRelation+" ")
			 .append(addArgument(pp.getArgument2()))
			 .append(")")
			 ;
		} else if (p instanceof DifferentFromPredicate) {
			DifferentFromPredicate pp = (DifferentFromPredicate)p;
			b.append("(")
			 .append(addArgument(pp.getArgument1()))
			 .append(" owl:differentFrom ")
			 .append(addArgument(pp.getArgument2()))
			 .append(")");
		} else if (p instanceof SameAsPredicate) {
			SameAsPredicate pp = (SameAsPredicate)p;
			b.append("(")
			 .append(addArgument(pp.getArgument1()))
			 .append(" owl:sameAs ")
			 .append(addArgument(pp.getArgument2()))
			 .append(")");
		} else if (p instanceof BuiltInPredicate) {
			BuiltInPredicate pp = (BuiltInPredicate)p;
			b.append(pp.getBuiltIn().getName())
			 .append("(")
			 .append(pp.getArguments().stream().map(a -> addArgument(a)).collect(Collectors.joining(", ")))
			 .append(")");
		}
		return b.toString();
	}

	private String addArgument(Argument a) {
		if (a.getInstance() != null) {
			return addIri(a.getInstance());
		} else if (a.getLiteral() != null) {
			return a.getLiteral().getLexicalValue();
		}
		return "?"+a.getVariable();
	}
	
	private String addIri(Member m) {
		if (!prefixes.containsKey(m.getOntology().getPrefix())) {
			prefixes.put(m.getOntology().getPrefix(), m.getOntology().getNamespace());
		}
		return m.getAbbreviatedIri();
	}
	
}
