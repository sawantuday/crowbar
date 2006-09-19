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

import org.eclipse.swt.internal.SWTEventListener;

/** 
 * This listener interface may be implemented in order to receive
 * a {@link MozillaWindowEvent} notification when a window hosting a
 * {@link MozillaBrowser} needs to be displayed or hidden.
 * 
 * @see MozillaBrowser#addVisibilityWindowListener(MozillaVisibilityWindowListener)
 * @see MozillaBrowser#removeVisibilityWindowListener(MozillaVisibilityWindowListener)
 * @see MozillaOpenWindowListener
 * @see MozillaCloseWindowListener
 * 
 * @since 3.0
 */
public interface MozillaVisibilityWindowListener extends SWTEventListener {
	
/**
 * This method is called when the window hosting a <code>MozillaBrowser</code> 
 * is requested to be hidden. Application would typically hide the
 * {@link org.eclipse.swt.widgets.Shell} that hosts the <code>MozillaBrowser</code>.
 * <p>
 *
 * <p>The following fields in the <code>MozillaWindowEvent</code> apply:
 * <ul>
 * <li>(in) widget the <code>MozillaBrowser</code> that needs to be hidden
 * </ul>
 *
 * @param event the <code>MozillaWindowEvent</code> that specifies the
 * <code>MozillaBrowser</code> that needs to be hidden
 * 
 * @see org.eclipse.swt.widgets.Shell#setVisible(boolean)
 * 
 * @since 3.0
 */ 
public void hide(MozillaWindowEvent event);

/**
 * This method is called when the window hosting a <code>MozillaBrowser</code>
 * is requested to be displayed. Application would typically set the 
 * location and the size of the {@link org.eclipse.swt.widgets.Shell} 
 * that hosts the <code>MozillaBrowser</code>, if a particular location and size
 * are specified. The application would then open that <code>Shell</code>.
 * <p>
 *
 * <p>The following fields in the <code>MozillaWindowEvent</code> apply:
 * <ul>
 * <li>(in) widget the <code>MozillaBrowser</code> to display
 * <li>(in) location the requested location for the <code>Shell</code> 
 * hosting the browser. It is <code>null</code> if no location is set. 
 * <li>(in) size the requested size for the <code>MozillaBrowser</code>.
 * The client area of the <code>Shell</code> hosting the
 * <code>MozillaBrowser</code> should be large enough to accomodate that size.
 * It is <code>null</code> if no size is set.
 * <li>(in) addressBar <code>true</code> if the <code>Shell</code> 
 * hosting the <code>MozillaBrowser</code> should display an address bar or
 * <code>false</code> otherwise
 * <li>(in) menuBar <code>true</code> if the <code>Shell</code> 
 * hosting the <code>MozillaBrowser</code> should display a menu bar or
 * <code>false</code> otherwise
 * <li>(in) statusBar <code>true</code> if the <code>Shell</code> 
 * hosting the <code>MozillaBrowser</code> should display a status bar or
 * <code>false</code> otherwise
 * <li>(in) toolBar <code>true</code> if the <code>Shell</code> 
 * hosting the <code>MozillaBrowser</code> should display a tool bar or
 * <code>false</code> otherwise
 * </ul>
 *
 * @param event the <code>MozillaWindowEvent</code> that specifies the
 * <code>MozillaBrowser</code> that needs to be displayed
 * 
 * @see org.eclipse.swt.widgets.Control#setLocation(org.eclipse.swt.graphics.Point)
 * @see org.eclipse.swt.widgets.Control#setSize(org.eclipse.swt.graphics.Point)
 * @see org.eclipse.swt.widgets.Shell#open()
 * 
 * @since 3.0
 */ 
public void show(MozillaWindowEvent event);

}
