package edu.mit.simile.crowbar.tests;

import org.mozilla.xpcom.Mozilla;
import org.mozilla.xpcom.nsIFile;
import org.mozilla.xpcom.nsILocalFile;
import org.mozilla.xpcom.nsISimpleEnumerator;
import org.mozilla.xpcom.nsISupports;

public class FileTest extends JavaXPCOMTest {

    public void testFileReading() throws Exception {
        Mozilla mozilla = initMozilla();
        nsILocalFile directory = mozilla.newLocalFile("/usr", false);
        nsISimpleEnumerator entries = (nsISimpleEnumerator) directory.getDirectoryEntries();
        while (entries.hasMoreElements()) {
            nsISupports supp = entries.getNext();
            nsIFile file = (nsIFile) supp.queryInterface(nsIFile.NS_IFILE_IID);
            System.out.println(file.getPath());
        }
        shutdownMozilla(mozilla);
    }

}
