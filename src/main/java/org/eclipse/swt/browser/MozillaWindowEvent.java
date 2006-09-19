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

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

/**
 * A <code>MozillaWindowEvent</code> is sent by a {@link MozillaBrowser} when
 * a new window needs to be created or when an existing window needs to be
 * closed. This notification occurs when a javascript command such as
 * <code>window.open</code> or <code>window.close</code> gets executed by
 * a <code>MozillaBrowser</code>.
 *
 * <p>
 * The following example shows how <code>MozillaWindowEvent</code>'s are typically
 * handled.
 * 
 * <code><pre>
 *	public static void main(String[] args) {
 *		Display display = new Display();
 *		Shell shell = new Shell(display);
 *		shell.setText("Main Window");
 *		shell.setLayout(new FillLayout());
 *		MozillaBrowser browser = new MozillaBrowser(shell, SWT.NONE);
 *		initialize(display, browser);
 *		shell.open();
 *		browser.setUrl("http://www.eclipse.org");
 *		while (!shell.isDisposed()) {
 *			if (!display.readAndDispatch())
 *				display.sleep();
 *		}
 *		display.dispose();
 *	}
 *
 *	static void initialize(final Display display, MozillaBrowser browser) {
 *		browser.addOpenWindowListener(new MozillaOpenWindowListener() {
 *			public void open(MozillaWindowEvent event) {
 *				// Certain platforms can provide a default full browser.
 *				// simply return in that case if the application prefers
 *				// the default full browser to the embedded one set below.
 *				if (!event.required) return;
 *
 *				// Embed the new window
 *				Shell shell = new Shell(display);
 *				shell.setText("New Window");
 *				shell.setLayout(new FillLayout());
 *				MozillaBrowser browser = new MozillaBrowser(shell, SWT.NONE);
 *				initialize(display, browser);
 *				event.browser = browser;
 *			}
 *		});
 *		browser.addVisibilityWindowListener(new MozillaVisibilityWindowListener() {
 *			public void hide(MozillaWindowEvent event) {
 *				MozillaBrowser browser = (MozillaBrowser)event.widget;
 *				Shell shell = browser.getShell();
 *				shell.setVisible(false);
 *			}
 *			public void show(MozillaWindowEvent event) {
 *				MozillaBrowser browser = (MozillaBrowser)event.widget;
 *				Shell shell = browser.getShell();
 *				if (event.location != null) shell.setLocation(event.location);
 *				if (event.size != null) {
 *					Point size = event.size;
 *					shell.setSize(shell.computeSize(size.x, size.y));
 *				}
 *				if (event.addressBar || event.menuBar || event.statusBar || event.toolBar) {
 *					// Create widgets for the address bar, menu bar, status bar and/or tool bar
 *					// leave enough space in the Shell to accomodate a MozillaBrowser of the size
 *					// given by event.size
 *				}
 *				shell.open();
 *			}
 *		});
 *		browser.addCloseWindowListener(new MozillaCloseWindowListener() {
 *			public void close(MozillaWindowEvent event) {
 *				MozillaBrowser browser = (MozillaBrowser)event.widget;
 *				Shell shell = browser.getShell();
 *				shell.close();
 *			}
 *		});
 *	}
 * </pre></code>
 * 
 * The following notifications are emitted when the user selects a hyperlink that targets a new window
 * or as the result of a javascript that executes window.open. 
 * 
 * <p>Main Browser
 * <ul>
 *    <li>User selects a link that opens in a new window or javascript requests a new window</li>
 *    <li>MozillaOpenWindowListener.open() notified</li>
 *    <ul>
 *    		<li>Application creates a new Shell and a second MozillaBrowser inside that Shell</li>
 *    		<li>Application registers window listener's on that second Mozilla Browser, 
 *              such as MozillaVisibilityWindowListener</li>
 *	    	<li>Application returns the second MozillaBrowser as the host for the new window content</li>
 *    </ul>
 * </ul>
 * 
 * <p>Second Browser
 * <ul>
 *    <li>MozillaVisibilityWindowListener.show() notified</li>
 *    <ul>
 *    		<li>Application sets navigation tool bar, status bar, menu bar and Shell size
 *    		<li>Application makes the Shell hosting the second MozillaBrowser visible
 *    		<li>User now sees the new window
 *    </ul> 
 * </ul>
 * 
 * @see MozillaCloseWindowListener
 * @see MozillaOpenWindowListener
 * @see MozillaVisibilityWindowListener
 * 
 * @since 3.0
 */
public class MozillaWindowEvent extends TypedEvent {

	/** 
	 * Specifies whether the platform requires the user to provide a
	 * <code>MozillaBrowser</code> to handle the new window.
	 * 
	 * @since 3.1
	 */
	public boolean required;
	
	
	/** 
	 * <code>MozillaBrowser</code> provided by the application.
	 */
	public MozillaBrowser browser;

	/** 
	 * Requested location for the <code>Shell</code> hosting the <code>MozillaBrowser</code>.
	 * It is <code>null</code> if no location has been requested.
	 */
	public Point location;

	/** 
	 * Requested <code>MozillaBrowser</code> size. The client area of the <code>Shell</code> 
	 * hosting the <code>MozillaBrowser</code> should be large enough to accomodate that size. 
	 * It is <code>null</code> if no size has been requested.
	 */
	public Point size;
	
	/**
	 * Specifies whether the <code>Shell</code> hosting the <code>MozillaBrowser</code> should
	 * display an address bar.
	 * 
	 * @since 3.1
	 */
	public boolean addressBar;

	/**
	 * Specifies whether the <code>Shell</code> hosting the <code>MozillaBrowser</code> should
	 * display a menu bar.
	 * 
	 * @since 3.1
	 */
	public boolean menuBar;
	
	/**
	 * Specifies whether the <code>Shell</code> hosting the <code>MozillaBrowser</code> should
	 * display a status bar.
	 * 
	 * @since 3.1
	 */
	public boolean statusBar;
	
	/**
	 * Specifies whether the <code>Shell</code> hosting the <code>MozillaBrowser</code> should
	 * display a tool bar.
	 * 
	 * @since 3.1
	 */
	public boolean toolBar;
	
	static final long serialVersionUID = 3617851997387174969L;
	
MozillaWindowEvent(Widget w) {
	super(w);
}
}
