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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.emf.common.util.URI;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;

import groovy.lang.Closure;
import io.opencaesar.oml.util.OmlCatalog;

/**
 * A gradle task to invoke the Owl2Oml tool 
 */
public abstract class Owl2OmlTask extends DefaultTask {
	
	/**
	 * Creates a new Owl2OmlTask object
	 */
	public Owl2OmlTask() {
	}

    @SuppressWarnings("rawtypes")
	@Override
    public Task configure(Closure closure) {
        Task task = super.configure(closure);
		try {
			var inputFiles = new ArrayList<File>();
			if (getInputCatalogPath().isPresent()) {
				final var inputCatalogPath = getInputCatalogPath().get();
				if (inputCatalogPath.exists()) {
					final var inputExtensions = getInputFileExtensions().isPresent()? getInputFileExtensions().get() : Arrays.asList("ttl"); 
					final var inputCatalog = new OwlCatalog(inputCatalogPath, inputExtensions);
					inputFiles.addAll(inputCatalog.getFiles());
				}
			}
			getInputFiles().setFrom(inputFiles);
		} catch (Exception e) {
			throw new GradleException(e.getLocalizedMessage(), e);
		}
        return task;
    }

    /**
	 * Path of the input Oml catalog (Required)
	 * 
	 * @return File Property
	 */
	@InputFile
    public abstract Property<File> getInputCatalogPath();

	/**
	 * Path of the output OWL catalog (Required)
	 * 
	 * @return File Property
	 */
	@InputFile
    public abstract Property<File> getOutputCatalogPath();

	/**
	 * Paths of OML folders that should not be updated in the conversion
	 * 
	 * @return String List Property
	 */
    @Optional
    @Input
    public abstract ListProperty<File> getDoNotUpdatePaths();

	/**
	 * Extension for the input OWL files (default=ttl, options: owl, rdf, xml, rj, ttl, n3, nt, trig, nq, trix, jsonld).
	 * 
	 * @return String Property
	 */
    @Optional
    @Input
    public abstract ListProperty<String> getInputFileExtensions();

    /**
	 * Extension for the output OML files (default=oml, options: oml, omlxmi, omljson)
	 * 
	 * @return String Property
	 */
    @Optional
    @Input
    public abstract Property<String> getOutputFileExtension();

	/**
	 * The debug flag
	 * 
	 * @return Boolean Property
	 */
    @Optional
    @Input
    public abstract Property<Boolean> getDebug();

	/**
	 * The collection of input Oml files referenced by the input Oml catalog
	 * 
	 * @return ConfigurableFileCollection
	 */
	@Incremental
	@InputFiles
	protected abstract ConfigurableFileCollection getInputFiles();
    
	/**
	 * The collection of output Oml files referenced by the output Oml catalog
	 * 
	 * @return ConfigurableFileCollection
	 */
   @OutputFiles
   protected ConfigurableFileCollection getOutputFiles() {
	   return getProject().files().from(new Callable<Collection<File>>() {
			@Override
			public Collection<File> call() throws Exception {
				if (getInputCatalogPath().isPresent() && getOutputCatalogPath().isPresent()) {
					final var inputCatalogPath = getInputCatalogPath().get();
					final var outputCatalogPath = getOutputCatalogPath().get();
					if (inputCatalogPath.exists() && outputCatalogPath.exists()) {
						final var inputExtensions = getInputFileExtensions().isPresent()? getInputFileExtensions().get() : Arrays.asList("ttl"); 
						final var inputCatalog = new OwlCatalog(inputCatalogPath, inputExtensions);
						final var inputFiles = inputCatalog.getFiles();
		
						final var outputExtension = getOutputFileExtension().isPresent()? getOutputFileExtension().get() : "oml"; 
			    		final var outputCatalogUri = URI.createFileURI(outputCatalogPath.getAbsolutePath());
			    		final var outputCatalog = OmlCatalog.create(outputCatalogUri, Arrays.asList(outputExtension));
			    		
			    		Collection<File> outputFiles = new ArrayList<>();
			    		for (File inputFile : inputFiles) {
							var iri = inputCatalog.deresolveUri(URI.createFileURI(inputFile.toString()).toString());
							var outputUri = outputCatalog.resolveUri(URI.createURI(iri));
							if (Owl2OmlApp.canUpdateUri(outputUri.toFileString(), getDoNotUpdatePaths().get())) {
								outputFiles.add(new File(outputUri.toFileString()));
							}
			    		}
			    		return outputFiles;
					}
				}
				return Collections.emptyList();
			}
		});
   }
    
   /**
    * The gradle task action logic.
    * 
    * @param inputChanges The input changes
    */
    @TaskAction
    public void run(InputChanges inputChanges) {
        List<String> args = new ArrayList<>();
        if (getInputCatalogPath().isPresent()) {
		    args.add("-i");
		    args.add(getInputCatalogPath().get().getAbsolutePath());
        }
        if (getOutputCatalogPath().isPresent()) {
		    args.add("-o");
		    args.add(getOutputCatalogPath().get().getAbsolutePath());
        }
        if (getDoNotUpdatePaths().isPresent()) {
        	getDoNotUpdatePaths().get().forEach(path -> {
                args.add("-u");
                args.add(path.getAbsolutePath());
            });
		}
        if (getInputFileExtensions().isPresent()) {
        	getInputFileExtensions().get().forEach(ext -> {
                args.add("-if");
                args.add(ext);
            });
		}
        if (getOutputFileExtension().isPresent()) {
        	args.add("-of");
        	args.add(getOutputFileExtension().get());
		}
		if (getDebug().isPresent() && getDebug().get()) {
		    args.add("-d");
	    }
	    try {
	    	
	    	if (inputChanges.isIncremental()) {
	    		final Set<File> deltas = new HashSet<>();
	        	inputChanges.getFileChanges(getInputFiles()).forEach(f -> deltas.add(f.getFile()));
	        	if (deltas.remove(getInputCatalogPath().get())) { // catalog changes
		        	Owl2OmlApp.main(args.toArray(new String[0]));
	        	} else {
	        		Owl2OmlApp.mainWithDeltas(deltas, args.toArray(new String[0]));
	        	}
	    	} else {
	        	Owl2OmlApp.main(args.toArray(new String[0]));
	    	}
		} catch (Exception e) {
			throw new TaskExecutionException(this, e);
		}
   	}
    
}