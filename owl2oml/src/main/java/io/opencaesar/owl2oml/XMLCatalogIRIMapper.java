package io.opencaesar.owl2oml;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogEntry;
import org.apache.xml.resolver.CatalogManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

/**
 * An ontology IRI mapper based on an OASIS XML catalog.
 */
@SuppressWarnings("serial")
public class XMLCatalogIRIMapper implements OWLOntologyIRIMapper {

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
    private CatalogEx catalog;
    
	/**
	 * The file extensions to search ontology files in scope of the catalog.
	 */
	private final List<String> fileExtensions;

	/**
	 * Construct
	 * @param catalogFile OASIS XML catalog file
	 * @param fileExtensions ontology file extensions
	 * @throws IOException error
	 */
	public XMLCatalogIRIMapper(File catalogFile, List<String> fileExtensions) throws IOException {
		if (null == catalogFile || !catalogFile.isFile() || !catalogFile.isAbsolute())
			throw new IllegalArgumentException("The catalogFile must exists and be an absolute path: " + catalogFile);
		this.fileExtensions = fileExtensions;
    	catalog = new CatalogEx(catalogFile.toURI());
        catalog.setCatalogManager(manager);
        catalog.setupReaders();
        catalog.loadSystemCatalogs();
        catalog.parseCatalog(catalogFile.toURI().toURL());
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
							return IRI.create(documentUri+"."+ext);
					}
				}
			}
			return IRI.create(documentUri);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

    /**
     * Gets the URIs of files that are mapped by this catalog
     * 
     * @param fileExtensions a list of file extension
     * @return a list of file URIs
     */
    public Map<String, URI> getFileUriMap(List<String> fileExtensions) {
		var uris = new HashMap<String, URI>();
		for (final var rewriteRule : getRewriteRules().entrySet()) {
			var rewriteUri = rewriteRule.getValue();
			var path = new File(rewriteUri);
			if (path.isDirectory()) {
				for (var file : getFiles(path, fileExtensions)) {
					String relative = path.toURI().relativize(file.toURI()).getPath();
					uris.put(rewriteRule.getKey()+trimFileExtension(relative), URI.create(rewriteUri+"/"+relative));
				}
			} else { // likely a file name with no extension
				for (String ext : fileExtensions) {
					var file = new File(path.toString()+"."+ext);
					if (file.exists()) {
						uris.put(rewriteRule.getKey(), URI.create(path.toString()+"."+ext));
						break;
					}
				}
			}
		}
		return uris;
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
     * Gets the catalog entries
     * 
     * @return The entries of the catalog
     */
    public List<CatalogEntry> getEntries() {
        List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
        Enumeration<?> en = catalog.getCatalogEntries().elements();
        while (en.hasMoreElements()) {
            entries.add((CatalogEntry) en.nextElement());
        }
        return entries;
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
        Vector<?> getCatalogEntries() {
            return catalogEntries;
        }
        @Override
        protected String makeAbsolute(String sysid) {
            sysid = fixSlashes(sysid);
            return  baseUri.toString()+'/'+sysid;
        }
    }
	
	private List<File> getFiles(File folder, List<String> fileExtensions) {
		final var files = new LinkedHashSet<File>();
		for (File file : folder.listFiles()) {
			if (file.isFile()) {
				var ext = getFileExtension(file);
				if (fileExtensions.contains(ext)) {
					files.add(file);
				}
			} else if (file.isDirectory()) {
				files.addAll(getFiles(file, fileExtensions));
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
	
	private String trimFileExtension(final String fileName) {
	    int i = fileName.lastIndexOf('.');
	    if (i != -1)
	    	return fileName.substring(0, i);
	    else 
	    	return fileName;
	}

    private String normalize(String path) {
    	java.net.URI uri = java.net.URI.create(path);
    	java.net.URI normalized = uri.normalize();
    	return path.replaceFirst(uri.getRawPath(), normalized.getRawPath());
    }
    
}
