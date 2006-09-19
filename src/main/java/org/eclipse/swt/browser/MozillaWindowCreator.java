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
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.mozilla.xpcom.*;

public class MozillaWindowCreator implements nsIWindowCreator {
	int refCount = 0;

  public MozillaWindowCreator() {
  }

  public nsISupports queryInterface(String aIID) {
      return Mozilla.queryInterface(this, aIID);
  }

  /* nsIWindowCreator */

  public nsIWebBrowserChrome createChromeWindow(nsIWebBrowserChrome parent, long chromeFlags) {
    if (parent == null) {
      throw new IllegalArgumentException("Parent cannot be null.");
    }

    nsIWebBrowser webBrowser = parent.getWebBrowser();
    nsIBaseWindow baseWindow = (nsIBaseWindow) webBrowser.queryInterface(nsIBaseWindow.NS_IBASEWINDOW_IID);
    long parentNativeWindow = baseWindow.getParentNativeWindow();

    Display display = Display.getCurrent();
    MozillaBrowser src = MozillaBrowser.findBrowser(parentNativeWindow);
    final MozillaBrowser browser;
    if ((chromeFlags & nsIWebBrowserChrome.CHROME_MODAL) != 0) {
      /*
      * Feature on Mozilla.  On platforms that lack a native dialog, Mozilla sends a
      * requests for a new Browser instance in a modal window. e.g. on Windows, Mozilla
      * brings up automatically a native Print Dialog in response to the javascript
      * command window.print() whereas on Linux Mozilla requests a new modal window
      * and a Browser to display an emulated HTML based print dialog. For this reason,
      * modal requests are handled here and not exposed to the user.
      */
      final Shell shell = new Shell(src.getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
      shell.setLayout(new FillLayout());
      browser = new MozillaBrowser(shell, SWT.NONE);
      browser.addVisibilityWindowListener(new MozillaVisibilityWindowListener() {
        public void hide(MozillaWindowEvent event) {
        }
        public void show(MozillaWindowEvent event) {
          if (event.location != null) shell.setLocation(event.location);
          if (event.size != null) {
            Point size = event.size;
            shell.setSize(shell.computeSize(size.x, size.y));
          }
          shell.open();
        }
      });
      browser.addCloseWindowListener(new MozillaCloseWindowListener() {
        public void close(MozillaWindowEvent event) {
          shell.close();
        }
      });
      return browser;
    } else {
      MozillaWindowEvent event = new MozillaWindowEvent(src);
      event.display = display;
      event.widget = src;
      for (int i = 0; i < src.openWindowListeners.length; i++)
        src.openWindowListeners[i].open(event);
      browser = event.browser;
      if (browser != null && !browser.isDisposed()) {
        browser.addressBar = (chromeFlags & nsIWebBrowserChrome.CHROME_LOCATIONBAR) != 0;
        browser.menuBar = (chromeFlags & nsIWebBrowserChrome.CHROME_MENUBAR) != 0;
        browser.statusBar = (chromeFlags & nsIWebBrowserChrome.CHROME_STATUSBAR) != 0;
        browser.toolBar = (chromeFlags & nsIWebBrowserChrome.CHROME_TOOLBAR) != 0;
        return browser;
      }
    }

    throw new RuntimeException("Failed to create browser window.");
  }
}