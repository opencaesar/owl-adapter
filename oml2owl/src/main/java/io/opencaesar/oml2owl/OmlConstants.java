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

interface OmlConstants extends io.opencaesar.oml.util.OmlConstants {
	
	static final String omlPath = "opencaesar.io/oml";

	static final String type = DC_NS + "type";

	static final String Vocabulary = OML_NS + "Vocabulary";

	static final String VocabularyBundle = OML_NS + "VocabularyBundle";

	static final String Description = OML_NS + "Description";

	static final String DescriptionBundle = OML_NS + "DescriptionBundle";

	static final String Aspect = OML_NS + "Aspect";

	static final String Concept = OML_NS + "Concept";

	static final String RelationEntity = OML_NS + "RelationEntity";

	static final String Structure = OML_NS + "Structure";

	static final String forwardRelation = OML_NS + "forwardRelation";

	static final String reverseRelation = OML_NS + "reverseRelation";

	static final String sourceRelation = OML_NS + "hasSource";

	static final String targetRelation = OML_NS + "hasTarget";

	static final String structuredProperty = OML_NS + "structuredProperty";

	static final String scalarProperty = OML_NS + "scalarProperty";
}
