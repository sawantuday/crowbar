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

/**
 * This adapter class provides default implementations for the
 * methods described by the {@link MozillaVisibilityWindowListener} interface.
 * <p>
 * Classes that wish to deal with {@link MozillaWindowEvent}'s can
 * extend this class and override only the methods which they are
 * interested in.
 * </p>
 * 
 * @since 3.0
 */
public abstract class MozillaVisibilityWindowAdapter implements MozillaVisibilityWindowListener {

public void hide(MozillaWindowEvent event) {
}
 
public void show(MozillaWindowEvent event) {
}
}
