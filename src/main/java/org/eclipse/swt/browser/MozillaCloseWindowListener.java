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
 * a {@link MozillaWindowEvent} notification when a {@link MozillaBrowser} is 
 * about to be closed and when its host window should be closed
 * by the application.
 * 
 * @see MozillaBrowser#addCloseWindowListener(MozillaCloseWindowListener)
 * @see MozillaBrowser#removeCloseWindowListener(MozillaCloseWindowListener)
 * @see MozillaOpenWindowListener
 * @see MozillaVisibilityWindowListener
 * 
 * @since 3.0
 */
public interface MozillaCloseWindowListener extends SWTEventListener {

/**
 * This method is called when the window hosting a {@link MozillaBrowser} should be closed.
 * Application would typically close the {@link org.eclipse.swt.widgets.Shell} that
 * hosts the <code>MozillaBrowser</code>. The <code>MozillaBrowser</code> is disposed after this
 * notification.
 *
 * <p>The following fields in the <code>MozillaWindowEvent</code> apply:
 * <ul>
 * <li>(in) widget the <code>MozillaBrowser</code> that is going to be disposed
 * </ul></p>
 *
 * @param event the <code>MozillaWindowEvent</code> that specifies the <code>MozillaBrowser</code>
 * that is going to be disposed
 * 
 * @see org.eclipse.swt.widgets.Shell#close()
 * 
 * @since 3.0
 */ 
public void close(MozillaWindowEvent event);
}
