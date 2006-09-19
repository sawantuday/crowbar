/*******************************************************************
 *
 * Licensed Materials - Property of IBM
 * 
 * AJAX Toolkit Framework 6-28-496-8128
 * 
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 * 
 * U.S. Government Users Restricted Rights - Use, duplication or 
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 *
 *******************************************************************/
package org.eclipse.swt.browser;

import java.io.File;
import java.io.FileNotFoundException;

import org.mozilla.xpcom.IAppFileLocProvider;

public class LocationProvider implements IAppFileLocProvider {

	File libXULPath;
	
	public LocationProvider(File aBinPath) throws FileNotFoundException {
		libXULPath = aBinPath;
		if (!libXULPath.exists() || !libXULPath.isDirectory())
			throw new FileNotFoundException("libxul directory specified is not valid: " + libXULPath.getAbsolutePath());	
	}
	
	public File getFile(String aProp, boolean[] aPersistent) {
		File file = null;
		if (aProp.equals("GreD") || aProp.equals("GreComsD")) { //$NON-NLS-1$
//			file = new File(grePath);
			file = libXULPath;
			if (aProp.equals("GreComsD")) { //$NON-NLS-1$
				file = new File(file, "components"); //$NON-NLS-1$
			}
		}
		else if (aProp.equals("MozBinD") ||  //$NON-NLS-1$
			aProp.equals("CurProcD") || //$NON-NLS-1$
			aProp.equals("ComsD") || //$NON-NLS-1$
			aProp.equals("ProfD"))  //$NON-NLS-1$
		{
			file = libXULPath;
			if (aProp.equals("ComsD")) { //$NON-NLS-1$
				file = new File(file, "components"); //$NON-NLS-1$
			}
//		}
//		else {
//			System.err.println("LocationProvider::getFile() => unhandled property = " + aProp);
		}

		return file;
	}

	public File[] getFiles(String aProp) {
		File[] files = null;
		if (aProp.equals("APluginsDL")) { //$NON-NLS-1$
			files = new File[1];
			files[0] = new File(libXULPath, "plugins");
//		} else {
//			System.err.println("LocationProvider::getFiles() => unhandled property = " + aProp);
		}

		return files;
	}

}
