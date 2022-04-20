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
import java.util.Collections;
import java.util.List;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogEntry;
import org.eclipse.emf.common.util.URI;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.work.Incremental;

import io.opencaesar.oml.util.OmlCatalog;

public abstract class Oml2OwlTask extends DefaultTask {
	
	@Input
    public abstract Property<File> getInputCatalogPath();

    @Optional
    @Input
    public abstract Property<String> getRootOntologyIri();

	@Input
    public abstract Property<File> getOutputCatalogPath();

    @Optional
    @Input
    public abstract Property<String> getOutputFileExtension();

    @Optional
    @Input
    public abstract Property<Boolean> getDisjointUnions();

    @Optional
    @Input
    public abstract Property<Boolean> getAnnotationsOnAxioms();

    @Optional
    @Input
    public abstract Property<Boolean> getDebug();

	@Incremental
	@InputFiles
    @SuppressWarnings("deprecation")
    protected ConfigurableFileCollection getInputFiles() {
    	try {
    		final OmlCatalog inputCatalog = OmlCatalog.create(URI.createFileURI(getInputCatalogPath().get().getAbsolutePath()));
    		Collection<File> inputFiles = Oml2OwlApp.collectOMLFiles(inputCatalog);
    		inputFiles.add(getInputCatalogPath().get());
    		return getProject().files(inputFiles);
    	} catch (Exception e) {
			throw new GradleException(e.getLocalizedMessage(), e);
    	}
    }
    
   @OutputFiles
   @SuppressWarnings("deprecation")
   protected ConfigurableFileCollection getOutputFiles() {
    	try {
    		if (getInputCatalogPath().isPresent() && getOutputCatalogPath().isPresent()) {
        		final OmlCatalog inputCatalog = OmlCatalog.create(URI.createFileURI(getInputCatalogPath().get().getAbsolutePath()));
        		final String outputFolderPath = getOutputCatalogPath().get().getParent();
	    		Collection<File> outputFiles = new ArrayList<>(getInputFiles().getFiles().size()+1);
	    		for (File inputFile : getInputFiles().getFiles()) {
	    			String inputFileUri = inputFile.toURI().toString();
	    			if (!inputFile.equals(getInputCatalogPath().get())) {
	    				for (CatalogEntry e : inputCatalog.getEntries()) {
	    					if (e.getEntryType() == Catalog.REWRITE_URI) {
	    						String uriStartString = e.getEntryArg(0);
	    						String rewriteUri = e.getEntryArg(1);
	    						int i =  inputFileUri.indexOf(rewriteUri);
	    						if (i != -1) {
	    							String relativePath = inputFileUri.replace(rewriteUri, uriStartString);
	    							relativePath = relativePath.substring(7); // remove http://
	    							int j = relativePath.lastIndexOf('.');
	    							if (j != -1) {
	    								relativePath = relativePath.substring(0, j);
	    							}
	    							relativePath = relativePath+"."+getOutputFileExtension().getOrElse("owl");
	    							outputFiles.add(new File(outputFolderPath+File.separator+relativePath));
	    						}
	    					}
	    				}
	    			}
	    		}
	    		outputFiles.add(getOutputCatalogPath().get());
	    		return getProject().files(outputFiles);
    		}
    		return getProject().files(Collections.EMPTY_LIST);
    	} catch (Exception e) {
			throw new GradleException(e.getLocalizedMessage(), e);
    	}
    }
    
    @TaskAction
    public void run() {
        List<String> args = new ArrayList<>();
        if (getInputCatalogPath().isPresent()) {
		    args.add("-i");
		    args.add(getInputCatalogPath().get().getAbsolutePath());
        }
        if (getRootOntologyIri().isPresent()) {
		    args.add("-r");
		    args.add(getRootOntologyIri().get());
        }
        if (getOutputCatalogPath().isPresent()) {
		    args.add("-o");
		    args.add(getOutputCatalogPath().get().getAbsolutePath());
        }
        if (getOutputFileExtension().isPresent()) {
        	args.add("-f");
        	args.add(getOutputFileExtension().get());
		}
	    if (getDisjointUnions().isPresent()) {
	    	if (getDisjointUnions().get()) {
	    		args.add("-u");
	    	}
	    }
	    if (getAnnotationsOnAxioms().isPresent()) {
	    	if (getAnnotationsOnAxioms().get()) {
	    		args.add("-a");
	    	}
	    }
		if (getDebug().isPresent() && getDebug().get()) {
		    args.add("-d");
	    }
	    try {
    		Oml2OwlApp.main(args.toArray(new String[0]));
		} catch (Exception e) {
			throw new TaskExecutionException(this, e);
		}
   	}
    
}