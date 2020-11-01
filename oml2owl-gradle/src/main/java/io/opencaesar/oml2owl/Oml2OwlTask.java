package io.opencaesar.oml2owl;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

public class Oml2OwlTask extends DefaultTask {
	
	public String inputCatalogPath;

	public String outputCatalogPath;

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
        if (outputCatalogPath != null) {
		    args.add("-o");
		    args.add(outputCatalogPath);
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