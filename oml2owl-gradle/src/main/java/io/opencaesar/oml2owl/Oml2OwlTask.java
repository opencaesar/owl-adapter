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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;

import groovy.lang.Closure;
import io.opencaesar.oml.util.OmlCatalog;

/**
 * A gradle task to invoke the Oml2Owl tool 
 */
public abstract class Oml2OwlTask extends DefaultTask {
	
	/**
	 * Creates a new Oml2OwlTask object
	 */
	public Oml2OwlTask() {
	}

    @SuppressWarnings("rawtypes")
	@Override
    public Task configure(Closure closure) {
        Task task = super.configure(closure);
		try {
			final OmlCatalog inputCatalog = OmlCatalog.create(URI.createFileURI(getInputCatalogPath().get().getAbsolutePath()));
			Collection<File> inputFiles = inputCatalog.getRewriteUris().stream()
					.map(i -> new File(java.net.URI.create(i)))
					.collect(Collectors.toList());
			getInputFiles().setFrom(inputFiles);
		} catch (Exception e) {
			throw new GradleException(e.getLocalizedMessage(), e);
		}
        return task;
    }

	/**
	 * Path of the input Oml catalog.
	 * 
	 * @return File Property
	 */
	@Input
    public abstract Property<File> getInputCatalogPath();

	/**
	 * Root Oml ontology IRI.
	 * 
	 * @return String Property
	 */
    @Optional
    @Input
    public abstract Property<String> getRootOntologyIri();

	/**
	 * Path of the output OWL catalog.
	 * 
	 * @return File Property
	 */
	@Input
    public abstract Property<File> getOutputCatalogPath();

	/**
	 * Extension for the output OWL files (default=owl, options: owl, rdf, xml, rj, ttl, n3, nt, trig, nq, trix, jsonld, fss).
	 * 
	 * @return String Property
	 */
    @Optional
    @Input
    public abstract Property<String> getOutputFileExtension();

	/**
	 * Whether to create disjoint union axioms.
	 * 
	 * @return Boolean Property
	 */
    @Optional
    @Input
    public abstract Property<Boolean> getDisjointUnions();

	/**
	 * Whether to Emit annotations on axioms.
	 * 
	 * @return Boolean Property
	 */
    @Optional
    @Input
    public abstract Property<Boolean> getAnnotationsOnAxioms();

	/**
	 * Whether to generate Jena rules files.
	 * 
	 * @return Boolean Property
	 */
    @Optional
    @Input
    public abstract Property<Boolean> getGenerateRules();

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
	 * The collection of output Owl files referenced by the output Owl catalog
	 * 
	 * @return ConfigurableFileCollection
	 */
	@OutputDirectory
	protected File getOutputFiles() {
		try {
			if (getOutputCatalogPath().isPresent()) {
				return getProject().file(getOutputCatalogPath().get().getParentFile());
			}
			return null;
		} catch (Exception e) {
			throw new GradleException(e.getLocalizedMessage(), e);
		}
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
	    if (getGenerateRules().isPresent()) {
	    	if (getGenerateRules().get()) {
	    		args.add("-rl");
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
	    	if (inputChanges.isIncremental()) {
	    		final Set<File> deltas = new HashSet<>();
	        	inputChanges.getFileChanges(getInputFiles()).forEach(f -> deltas.add(f.getFile()));
	        	Oml2OwlApp.mainWithDeltas(deltas, args.toArray(new String[0]));
	    	} else {
	        	Oml2OwlApp.main(args.toArray(new String[0]));
	    	}
		} catch (Exception e) {
			throw new TaskExecutionException(this, e);
		}
   	}
    
}