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

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

public class Oml2OwlTask extends DefaultTask {
	
	public String inputCatalogPath;

	public String rootOntologyIri;

	public String outputCatalogPath;

	public String outputFormat;

	public boolean disjointUnions;

	public boolean annotationsOnAxioms;

	public boolean debug;

    @TaskAction
    public void run() {
        List<String> args = new ArrayList<String>();
        if (inputCatalogPath != null) {
		    args.add("-i");
		    args.add(inputCatalogPath);
        }
        if (rootOntologyIri != null) {
		    args.add("-r");
		    args.add(rootOntologyIri);
        }
        if (outputCatalogPath != null) {
		    args.add("-o");
		    args.add(outputCatalogPath);
        }
        if (outputFormat != null) {
        	args.add("-f");
        	args.add(outputFormat);
		}
	    if (disjointUnions) {
		    args.add("-u");
	    }
	    if (annotationsOnAxioms) {
		    args.add("-a");
	    }
	    if (debug) {
		    args.add("-d");
	    }
	    try {
	    	Oml2OwlApp.main(args.toArray(new String[0]));
		} catch (Exception e) {
			throw new TaskExecutionException(this, e);
		}
   	}
    
}