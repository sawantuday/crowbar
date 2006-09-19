/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.browser;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.mozilla.xpcom.*;

class MozillaHelperAppLauncherDialog implements nsIHelperAppLauncherDialog {

  public MozillaHelperAppLauncherDialog() {
  }

  public nsISupports queryInterface(String aIID) {
      return Mozilla.queryInterface(this, aIID);
  }

  /* nsIHelperAppLauncherDialog */

  public void show(nsIHelperAppLauncher aLauncher, nsISupports aContext, long aForced) {
    aLauncher.saveToDisk(null, false);
  }

  public nsILocalFile promptForSaveToFile(nsIHelperAppLauncher aLauncher, nsISupports aWindowContext, String aDefaultFile, String aSuggestedFileExtension) {
    Shell shell = new Shell();
    FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
    fileDialog.setFileName(aDefaultFile);
    fileDialog.setFilterExtensions(new String[] {aSuggestedFileExtension});
    String name = fileDialog.open();
    shell.close();

    if (name == null) {
      if (aLauncher != null) {
        // XXX NS_BINDING_ABORTED should be defined in XPCOM class
        aLauncher.cancel(0x804b0002 /*NS_BINDING_ABORTED*/);
        return null;
      }
      throw new RuntimeException("helperAppLauncher is null.");
    }
    return Mozilla.getInstance().newLocalFile(name, true);
  }
}