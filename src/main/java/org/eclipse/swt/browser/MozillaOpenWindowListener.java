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

import org.eclipse.swt.internal.SWTEventListener;

/** 
 * This listener interface may be implemented in order to receive
 * a {@link MozillaWindowEvent} notification when a new {@link MozillaBrowser}
 * needs to be provided by the application.
 * 
 * @see MozillaBrowser#addOpenWindowListener(MozillaOpenWindowListener)
 * @see MozillaBrowser#removeOpenWindowListener(MozillaOpenWindowListener)
 * @see MozillaCloseWindowListener
 * @see MozillaVisibilityWindowListener
 * 
 * @since 3.0
 */
public interface MozillaOpenWindowListener extends SWTEventListener {

/**
 * This method is called when a new window needs to be created.
 * <p>
 * A particular <code>MozillaBrowser</code> can be passed to the event.browser
 * field to host the content of a new window.
 * <p>
 * A standalone system browser is used to host the new window
 * if the event.required field value is false and if the event.browser 
 * field is left <code>null</code>. The event.required field
 * is true on platforms that don't support a standalone system browser for
 * new window requests. 
 * <p>
 * The navigation is cancelled if the event.required field is set to
 * true and the event.browser field is left <code>null</code>.
 * <p>
 * <p>The following fields in the <code>MozillaWindowEvent</code> apply:
 * <ul>
 * <li>(in/out) required true if the platform requires the user to provide a
 * <code>MozillaBrowser</code> to handle the new window or false otherwise.
 * <li>(out) browser the new <code>MozillaBrowser</code> that will host the 
 * content of the new window.
 * <li>(in) widget the <code>MozillaBrowser</code> that is requesting to open a 
 * new window
 * </ul>
 * 
 * @param event the <code>MozillaWindowEvent</code> that needs to be passed a new
 * <code>MozillaBrowser</code> to handle the new window request
 * 
 * @since 3.0
 */ 
public void open(MozillaWindowEvent event);
}
