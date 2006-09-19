package edu.mit.simile.crowbar;

import java.io.File;
import java.io.FileNotFoundException;

import org.mozilla.xpcom.IAppFileLocProvider;

public class LocationProvider implements IAppFileLocProvider {

    File _app;
    File _gre;

    public LocationProvider(File app, File gre) throws FileNotFoundException {
        if (!app.exists() || !app.isDirectory()) {
            throw new FileNotFoundException("The specified application directory is not valid: " + app.getAbsolutePath());
        } else {
            _app = app;
        }
        if (!gre.exists() || !gre.isDirectory()) { 
            throw new FileNotFoundException("The specified XULRunner directory is not valid: " + gre.getAbsolutePath());
        } else {
            _gre = gre;
        }
    }

    public File getFile(String p, boolean[] aPersistent) {
        System.out.println("[LocationProvider.getFile()] get " + p);
        File file = null;
        if (p.equals("GreD") || p.equals("MozBinD") || p.equals("CurProcD") || p.equals("ProfD")) {
            file = _gre;
        } else if (p.equals("GreComsD") || p.equals("ComsD")) {
            file = new File(file, "components");
        } else {
            System.err.println("[LocationProvider.getFile()] unhandled property = " + p);
        }

        return file;
    }

    public File[] getFiles(String p) {
        System.out.println("[LocationProvider.getFiles()] get " + p);
        File[] files = null;
        if (p.equals("APluginsDL")) {
            files = new File[1];
            files[0] = new File(_gre, "plugins");
        } if (p.equals("ComsDL")) {
            files = new File[2];
            files[0] = _app;
            files[1] = _gre;
        } else {
            System.err.println("[LocationProvider.getFiles()] unhandled property = " + p);
        }

        return files;
    }

}
