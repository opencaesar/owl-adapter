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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogEntry;
import org.apache.xml.resolver.CatalogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

/**
 * An OASIS XML catalog for OWL files
 */
@SuppressWarnings("serial")
public class OwlCatalog {

    /**
     * The singleton catalog manager
     */
    private static CatalogManager manager = new CatalogManager();
    static {
        manager.setUseStaticCatalog(false);
        manager.setIgnoreMissingProperties(true);
    }

    /**
     * The wrapped Apache catalog
     */
    protected CatalogEx catalog;
    
	/**
	 * The file extensions to search ontology files in scope of the catalog.
	 */
    protected final List<String> fileExtensions;

	/**
	 * A registry of document IRIs that have been mapped so far
	 */
    protected Map<String, IRI> documentIris;
	
	
	/**
	 * Construct
	 * @param catalogFile OASIS XML catalog file
	 * @param fileExtensions ontology file extensions
	 * @throws IOException error
	 */
	public OwlCatalog(File catalogFile, List<String> fileExtensions) throws IOException {
		if (null == catalogFile || !catalogFile.isFile() || !catalogFile.isAbsolute())
			throw new IllegalArgumentException("The catalogFile must exist and be an absolute path: " + catalogFile);
		this.fileExtensions = fileExtensions;
		this.documentIris = new HashMap<>();
    	catalog = new CatalogEx(catalogFile.toURI());
        catalog.setCatalogManager(manager);
        catalog.setupReaders();
        catalog.loadSystemCatalogs();
        catalog.parseCatalog(catalogFile.toURI().toURL());
	}

	/**
	 * An ontology IRI mapper based on an OASIS XML catalog.
	 */
	public static class IRIMapper extends OwlCatalog implements OWLOntologyIRIMapper {
		
		/**
		 * Construct
		 * @param catalogFile OASIS XML catalog file
		 * @param fileExtensions ontology file extensions
		 * @throws IOException error
		 */
		public IRIMapper(File catalogFile, List<String> fileExtensions) throws IOException {
			super(catalogFile, fileExtensions);
		}
		
		@Override
		public IRI getDocumentIRI(IRI originalIri) {
			try {
				String documentUri = catalog.resolveURI(originalIri.toString());
				if (documentUri != null && documentUri.startsWith("file:")) {
					File f = new File(new URI(documentUri));
					if (!f.exists() || !f.isFile()) {
						for ( String ext : fileExtensions ) {
							String fileWithExtensionPath = f.toString()+"." + ext;
							File f_ext = new File(fileWithExtensionPath);
							if (f_ext.exists() && f_ext.isFile())
								return createDocumentIRI(documentUri+"."+ext);
						}
					}
				}
				return createDocumentIRI(documentUri);
			} catch (Exception e) {
				System.out.println(e);
				return null;
			}
		}

		private IRI createDocumentIRI(String documentUri) throws URISyntaxException {
			IRI iri = documentIris.get(documentUri);
			if (iri == null) {
				documentIris.put(documentUri, iri = IRI.create(new URI(documentUri).normalize()));
			}
			return iri;
		}
}
	
	/**
	 * Determins if a given iri has been mapped by this catalog before
	 * 
	 * @param iri The given iri
	 * @return true if the iri has been mapped, otherwise false
	 */
	public boolean isDocumentIRIMapped(IRI iri) {
		return documentIris.values().contains(iri);
	}
	
    /**
     * Gets the files that are mapped by this catalog
     * 
     * @return a list of file URIs
     */
    public Collection<File> getFiles() {
		var files = new LinkedHashSet<File>();
		for (final var rewriteRule : getRewriteRules().entrySet()) {
			var rewriteUri = rewriteRule.getValue();
			var path = new File(rewriteUri);
			if (path.isDirectory()) {
				files.addAll(getFiles(path));
			} else { // likely a file name with no extension
				for (String ext : fileExtensions) {
					var file = new File(path.toString()+"."+ext);
					if (file.exists()) {
						files.add(file);
						break;
					}
				}
			}
		}
		return files;
    }

    /**
     * Gets the URIs that are used for rewrite rules in this catalog
     * 
     * @return a map of rewrite URIs
     */
    public Map<String, URI> getRewriteRules() {
		var rewriteUris = new HashMap<String, URI>();
		for (CatalogEntry e : getEntries()) {
			if (e.getEntryType() == Catalog.REWRITE_URI) { // only type of entry supported so far
				var uri = URI.create(normalize(e.getEntryArg(1)));
	    		String s = uri.toString();
				if (s.endsWith("/")) {
		    		try {
						uri = new URI(s.substring(0, s.length()-1));
					} catch (URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
				rewriteUris.put(e.getEntryArg(0), uri);
			}
		}
    	return rewriteUris;
    }

    /**
     * Deresolves the given physical uri to a logical Iri
     * 
     * @param uri The physical URI to deresolve
     * @return The deresolved logical URI
     * @throws IOException if the physical URI cannot be deresolved to a logical URI
     */
    public String deresolveUri(String uri) throws IOException {
		for (CatalogEntry e : catalog.getCatalogEntries()) {
			if (e.getEntryType() == Catalog.REWRITE_URI) {
				String uriStartString = e.getEntryArg(0);
				String rewriteUri = normalize(e.getEntryArg(1));
				int i =  uri.toString().indexOf(rewriteUri);
				if (i != -1) {
					var pathWithNoExt = uri.substring(0, uri.lastIndexOf("."));
					return pathWithNoExt.replace(rewriteUri, uriStartString);
				}
			}
		}
    	return null;
    }

    /**
     * Gets the catalog entries
     * 
     * @return The entries of the catalog
     */
    private List<CatalogEntry> getEntries() {
        return catalog.getCatalogEntries();
    }

	private static class CatalogEx extends Catalog {
    	private URI baseUri;
    	public CatalogEx(URI catalogUri) {
    		String s = catalogUri.toString();
    		int i = s.lastIndexOf("/");
    		try {
				this.baseUri = new URI(s.substring(0, i));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
    	}
        public List<CatalogEntry> getCatalogEntries() {
            List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
            Enumeration<?> en = catalogEntries.elements();
            while (en.hasMoreElements()) {
                entries.add((CatalogEntry) en.nextElement());
            }
            return entries;
        }
        @Override
        protected String makeAbsolute(String sysid) {
            sysid = fixSlashes(sysid);
            return  baseUri.toString()+'/'+sysid;
        }
    }
	
	private List<File> getFiles(File folder) {
		final var files = new LinkedHashSet<File>();
		for (File file : folder.listFiles()) {
			if (file.isFile()) {
				var ext = getFileExtension(file);
				if (fileExtensions.contains(ext)) {
					files.add(file);
				}
			} else if (file.isDirectory()) {
				files.addAll(getFiles(file));
			}
		}
		return new ArrayList<File>(files);
	}
	
	private String getFileExtension(final File file) {
	    String fileName = file.getName();
	    if (fileName.lastIndexOf(".") != -1)
	    	return fileName.substring(fileName.lastIndexOf(".")+1);
	    else 
	    	return "";
	}
	
    private String normalize(String path) {
    	java.net.URI uri = java.net.URI.create(path);
    	java.net.URI normalized = uri.normalize();
    	return path.replaceFirst(uri.getRawPath(), normalized.getRawPath());
    }
    
}
