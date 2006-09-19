package edu.mit.simile.crowbar;

import java.io.File;

import org.mozilla.xpcom.GREVersionRange;
import org.mozilla.xpcom.Mozilla;
import org.mozilla.xpcom.nsIAppStartup;
import org.mozilla.xpcom.nsIDOMWindow;
import org.mozilla.xpcom.nsIServiceManager;
import org.mozilla.xpcom.nsIWindowCreator;
import org.mozilla.xpcom.nsIWindowWatcher;

public class Crowbar {

    static Mozilla initMozilla(File app) throws Exception {
        Mozilla mozilla = Mozilla.getInstance();
        try {
            GREVersionRange[] range = new GREVersionRange[1];
            range[0] = new GREVersionRange("1.8.0", true, "1.9", false);

            File grePath = Mozilla.getGREPathWithProperties(range, null);
            System.out.println("GRE: " + grePath);
            LocationProvider locProvider = new LocationProvider(app, grePath);
            mozilla.initXPCOM(grePath, locProvider);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return mozilla;
    }

    public static void main(String args[]) throws Exception {
        Mozilla moz = initMozilla(new File("."));
    
        // Now we need to start an XUL application, so we get an instance of the XPCOM service manager
        nsIServiceManager serviceManager = moz.getServiceManager();
    
        // Now we need to get the @mozilla.org/toolkit/app-startup;1 service:
        nsIAppStartup appStartup = (nsIAppStartup) serviceManager.getServiceByContractID("@mozilla.org/toolkit/app-startup;1", nsIAppStartup.NS_IAPPSTARTUP_IID);
    
        // Get the nsIWindowWatcher interface to the above
        nsIWindowCreator windowCreator = (nsIWindowCreator) appStartup.queryInterface(nsIWindowCreator.NS_IWINDOWCREATOR_IID);
    
        // Get the window watcher service
        nsIWindowWatcher windowWatcher = (nsIWindowWatcher) serviceManager.getServiceByContractID("@mozilla.org/embedcomp/window-watcher;1", nsIWindowWatcher.NS_IWINDOWWATCHER_IID);
    
        // Set the window creator (from step 6)
        windowWatcher.setWindowCreator(windowCreator);
    
        // Create the root XUL window:
        nsIDOMWindow win = windowWatcher.openWindow(null, "/Users/stefano/blah.xul", "mywindow", "chrome,resizable,centerscreen", null);
    
        // Set this as the active window
        windowWatcher.setActiveWindow(win);
    
        // Hand over the application to xpcom/xul, this will block:
        appStartup.run();        
    }
}