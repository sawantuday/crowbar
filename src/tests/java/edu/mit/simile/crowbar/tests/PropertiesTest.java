package edu.mit.simile.crowbar.tests;

import org.mozilla.xpcom.*;

public class PropertiesTest extends JavaXPCOMTest {

    public static final String NS_PROPERTIES_CONTRACTID = "@mozilla.org/properties;1";

    public void testArray() throws Exception {
        Mozilla mozilla = initMozilla();

        nsIComponentManager componentManager = mozilla.getComponentManager();
        nsIProperties props = (nsIProperties) componentManager.createInstanceByContractID(NS_PROPERTIES_CONTRACTID, null, nsIProperties.NS_IPROPERTIES_IID);
        if (props == null) {
            throw new RuntimeException("Failed to create nsIProperties.");
        }

        // create the nsISupports objects we will use
        nsILocalFile localFile1 = mozilla.newLocalFile("/user/local/share", false);
        nsILocalFile localFile2 = mozilla.newLocalFile("/home/foo", false);
        nsILocalFile localFile3 = mozilla.newLocalFile("/home/foo/bar", false);

        // set the properties and associate with the created objects
        props.set("File One", localFile1);
        props.set("File Two", localFile2);
        props.set("File One Repeated", localFile1);
        props.set("File Three", localFile3);

        // test the "has" method
        boolean hasProp = props.has("File One");
        if (hasProp == false) throw new Exception("Could not find property 'File One'.");
        hasProp = props.has("File One Repeated");
        if (hasProp == false) throw new Exception("Could not find property 'File One Repeated'.");
        hasProp = props.has("Nonexistant Property");
        if (hasProp == true) throw new Exception("Found property that doesn't exist.");

        // test the "get" method
        nsILocalFile tempLocalFile = (nsILocalFile) props.get("File One Repeated", nsILocalFile.NS_ILOCALFILE_IID);
        if (tempLocalFile == null) throw new Exception("Property 'File One Repeated' not found.");
        if (tempLocalFile != localFile1)
            throw new Exception("Object returned by 'get' not the same as object passed to 'set'.");

        // test the "undefine" method
        hasProp = props.has("File Two");
        if (hasProp == false) throw new Exception();
        props.undefine("File Two");
        hasProp = props.has("File Two");
        if (hasProp == true) throw new Exception();

        // test the "getKeys" method
        long[] count = new long[1];
        String[] keys = props.getKeys(count);
        if (keys == null || keys.length != 3) {
            System.out.println("getKeys returned incorrect array.");
        }
        for (int i = 0; i < keys.length; i++) {
            System.out.println("key " + i + ": " + keys[i]);
        }

        mozilla.shutdownXPCOM(null);
    }
}
