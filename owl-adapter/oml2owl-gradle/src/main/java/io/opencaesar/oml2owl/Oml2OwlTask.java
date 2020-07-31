package io.opencaesar.oml2owl;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class Oml2OwlTask extends DefaultTask {
	
	public String inputPath;

	public String outputPath;

	public boolean disjointUnions;

	public boolean annotationsOnAxioms;

	public boolean debug;

    @TaskAction
    public void run() {
        List<String> args = new ArrayList<String>();
        if (inputPath != null) {
		    args.add("-i");
		    args.add(inputPath);
        }
        if (outputPath != null) {
		    args.add("-o");
		    args.add(outputPath);
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
        Oml2OwlApp.main(args.toArray(new String[0]));
   	}
    
}