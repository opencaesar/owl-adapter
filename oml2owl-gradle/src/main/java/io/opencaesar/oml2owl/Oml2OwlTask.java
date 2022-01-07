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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;

import io.opencaesar.oml.util.OmlCatalog;

public abstract class Oml2OwlTask extends DefaultTask {
	
	@Input
    public String inputCatalogPath;

    public void setInputCatalogPath(String s) {
    	try {
    		inputCatalogPath = s;
    		final OmlCatalog inputCatalog = OmlCatalog.create(URI.createFileURI(s));
    		Collection<File> files = Oml2OwlApp.collectOMLFiles(inputCatalog);
    		files.add(new File(s));
    		getInputFiles().from(files);
    	} catch (Exception e) {
    		System.out.println(e);
    	}
    }

    @Incremental
    @InputFiles
    public abstract ConfigurableFileCollection getInputFiles();

	@Input
	public String rootOntologyIri;

	@Input
	public String outputCatalogPath;

    public void setOutputCatalogPath(String s) {
    	outputCatalogPath = s;
    	getOutputDir().set(new File(s).getParentFile());
    }

    @OutputDirectory
	public abstract DirectoryProperty getOutputDir();

	@Input
	public String outputFileExtension;

	@Input
	public boolean disjointUnions;

	@Input
	public boolean annotationsOnAxioms;

	public boolean debug;

    @TaskAction
    public void run(InputChanges inputChanges) {
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
        if (outputFileExtension != null) {
        	args.add("-f");
        	args.add(outputFileExtension);
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
	    	if (inputChanges.getFileChanges(getInputFiles()).iterator().hasNext()) {
	    		Oml2OwlApp.main(args.toArray(new String[0]));
	    	}
		} catch (Exception e) {
			throw new TaskExecutionException(this, e);
		}
   	}
    
}