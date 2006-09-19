package edu.mit.simile.crowbar.tests;

import java.io.File;

import junit.framework.TestCase;

import org.mozilla.xpcom.GREVersionRange;
import org.mozilla.xpcom.Mozilla;
import org.mozilla.xpcom.nsIServiceManager;
import org.mozilla.xpcom.nsISupports;

import edu.mit.simile.crowbar.LocationProvider;

public abstract class JavaXPCOMTest extends TestCase {

    Mozilla initMozilla() throws Exception {
        Mozilla mozilla = Mozilla.getInstance();
        try {
            GREVersionRange[] range = new GREVersionRange[1];
            range[0] = new GREVersionRange("1.8.0", true, "1.9", false);

            File grePath = Mozilla.getGREPathWithProperties(range, null);
            System.out.println("GRE: " + grePath);
            LocationProvider locProvider = new LocationProvider(new File("."), grePath);
            mozilla.initXPCOM(grePath, locProvider);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return mozilla;
    }

    void shutdownMozilla(Mozilla mozilla) {
        shutdownMozilla(mozilla, null);
    }

    void shutdownMozilla(Mozilla mozilla, nsIServiceManager sm) {
        mozilla.shutdownXPCOM(sm);
    }

}

class Foo implements nsISupports {

    static int gCount;

    int mID;

    public Foo(int aID) {
        mID = aID;
        ++gCount;
        System.out.println("init: " + mID + " (" + Integer.toHexString(this.hashCode()) + "), " + gCount + " total");
    }

    public nsISupports queryInterface(String aIID) {
        return Mozilla.queryInterface(this, aIID);
    }

    public int getId() {
        return mID;
    }

    protected void finalize() throws Throwable {
        --gCount;
        System.out.println("destruct: " + mID + " (" + Integer.toHexString(this.hashCode()) + "), " + gCount
                + " remain");
    }
}
