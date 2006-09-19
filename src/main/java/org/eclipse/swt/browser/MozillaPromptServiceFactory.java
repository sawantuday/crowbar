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

import org.mozilla.xpcom.*;

class MozillaPromptServiceFactory implements nsIFactory {

  public MozillaPromptServiceFactory() {
  }

  public nsISupports queryInterface(String aIID) {
      return Mozilla.queryInterface(this, aIID);
  }

  /* nsIFactory */

  public nsISupports createInstance(nsISupports aOuter, String iid) {
    MozillaPromptService promptService = new MozillaPromptService();
    return promptService;
  }

  public void lockFactory(boolean lock) {
  }
}