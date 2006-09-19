/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.browser;

import org.eclipse.swt.graphics.Point;
import org.mozilla.xpcom.*;

public class EmbeddingSiteWindow implements nsIEmbeddingSiteWindow {
    AbstractMozillaBrowser browser;

    public EmbeddingSiteWindow(AbstractMozillaBrowser aBrowser) {
        browser = aBrowser;
    }
    
    public nsISupports queryInterface(String riid) {
        if (riid.equals(NS_IEMBEDDINGSITEWINDOW_IID))
            return this;
        else
            return browser.queryInterface(riid);
    }

    public void setDimensions(long flags, int x, int y, int cx, int cy) {
        if (flags == nsIEmbeddingSiteWindow.DIM_FLAGS_POSITION)
            browser.location = new Point(x, y);
    }

    public void getDimensions(long flags, int[] x, int[] y, int[] cx, int[] cy) {
    }   

    public void setFocus() {
        nsIBaseWindow baseWindow = (nsIBaseWindow) browser.webBrowser.queryInterface(nsIBaseWindow.NS_IBASEWINDOW_IID);
        baseWindow.setFocus();
    }

    public boolean getVisibility() {
        return true; //XXX ????
    }

    public void setVisibility(boolean aVisibility) {
        MozillaWindowEvent event = new MozillaWindowEvent(browser);
        event.display = browser.getDisplay();
        event.widget = browser;
        if (aVisibility) {
            /*
            * Bug in Mozilla.  When the JavaScript window.open is executed, Mozilla
            * fires multiple SetVisibility 1 notifications.  The workaround is
            * to ignore subsequent notifications. 
            */
            if (!browser.visible) {
                browser.visible = true;
                event.location = browser.location;
                event.size = browser.size;
                event.addressBar = browser.addressBar;
                event.menuBar = browser.menuBar;
                event.statusBar = browser.statusBar;
                event.toolBar = browser.toolBar;
                for (int i = 0; i < browser.visibilityWindowListeners.length; i++)
                    browser.visibilityWindowListeners[i].show(event);
                browser.location = null;
                browser.size = null;
            }
        } else {
            browser.visible = false;
            for (int i = 0; i < browser.visibilityWindowListeners.length; i++)
                browser.visibilityWindowListeners[i].hide(event);
        }
    }

    public String getTitle() {
        return "";  //XXX ????
    }

    public void setTitle(String aTitle) {
        if (browser.titleListeners.length == 0)
            return;
        TitleEvent event = new TitleEvent(browser);
        event.display = browser.getDisplay();
        event.widget = browser;
        event.title = aTitle;
        for (int i = 0; i < browser.titleListeners.length; i++)
            browser.titleListeners[i].changed(event);
    }

    public long getSiteWindow() {
        /*
        * Note.  The handle is expected to be an HWND on Windows and
        * a GtkWidget* on GTK.  This callback is invoked on Windows
        * when the javascript window.print is invoked and the print
        * dialog comes up. If no handle is returned, the print dialog
        * does not come up on this platform.
        */
        return browser.getHandle();
    }
}