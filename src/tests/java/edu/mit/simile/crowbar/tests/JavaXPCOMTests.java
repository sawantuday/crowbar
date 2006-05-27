package edu.mit.simile.crowbar.tests;

import java.io.File;

import junit.framework.TestCase;

import org.mozilla.xpcom.GREVersionRange;
import org.mozilla.xpcom.Mozilla;
import org.mozilla.xpcom.nsIFile;
import org.mozilla.xpcom.nsILocalFile;
import org.mozilla.xpcom.nsISimpleEnumerator;
import org.mozilla.xpcom.nsISupports;

import edu.mit.simile.crowbar.LocationProvider;

public class JavaXPCOMTests extends TestCase {

    Mozilla mozilla = Mozilla.getInstance();
    
    public JavaXPCOMTests() throws Exception {
        try {
            GREVersionRange[] range = new GREVersionRange[1];
            range[0] = new GREVersionRange("1.8.0", true, "1.9", false);
            
            File grePath = Mozilla.getGREPathWithProperties(range, null);
            System.out.println("GRE: " + grePath);
            LocationProvider locProvider = new LocationProvider(grePath);
            mozilla.initXPCOM(grePath, locProvider);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void testFileReading() throws Exception {
        nsILocalFile directory = mozilla.newLocalFile("/usr", false);
        nsISimpleEnumerator entries = (nsISimpleEnumerator) directory.getDirectoryEntries();
        while (entries.hasMoreElements()) {
            nsISupports supp = entries.getNext();
            nsIFile file = (nsIFile) supp.queryInterface(nsIFile.NS_IFILE_IID);
            System.out.println(file.getPath());
        }
    }
    
}
