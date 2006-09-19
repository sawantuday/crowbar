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

class MozillaPromptService implements nsIPromptService {

  public MozillaPromptService() {
  }

  public nsISupports queryInterface(String aIID) {
      return Mozilla.queryInterface(this, aIID);
  }

  MozillaBrowser getBrowser(nsIDOMWindow aDOMWindow) {
    nsIServiceManager serviceManager = Mozilla.getInstance().getServiceManager();
    nsIWindowWatcher windowWatcher = (nsIWindowWatcher) serviceManager.getServiceByContractID(MozillaBrowser.NS_WINDOWWATCHER_CONTRACTID, nsIWindowWatcher.NS_IWINDOWWATCHER_IID);
    nsIWebBrowserChrome webBrowserChrome = windowWatcher.getChromeForWindow(aDOMWindow);
    nsIEmbeddingSiteWindow embeddingSiteWindow = (nsIEmbeddingSiteWindow) webBrowserChrome.queryInterface(nsIEmbeddingSiteWindow.NS_IEMBEDDINGSITEWINDOW_IID);
    long siteWin = embeddingSiteWindow.getSiteWindow();
    return MozillaBrowser.findBrowser(siteWin);
  }

  String getLabel(long buttonFlag, long index, String buttonTitle) {
    String label = null;
    long flag = (buttonFlag & (0xff * index)) / index;
    if (flag == nsIPromptService.BUTTON_TITLE_CANCEL) {
      label = SWT.getMessage("SWT_Cancel");	//$NON-NLS-1$
    } else if (flag == nsIPromptService.BUTTON_TITLE_NO) {
      label = SWT.getMessage("SWT_No");	//$NON-NLS-1$
    } else if (flag == nsIPromptService.BUTTON_TITLE_OK) {
      label = SWT.getMessage("SWT_OK");	//$NON-NLS-1$
    } else if (flag == nsIPromptService.BUTTON_TITLE_SAVE) {
      label = SWT.getMessage("SWT_Save");	//$NON-NLS-1$
    } else if (flag == nsIPromptService.BUTTON_TITLE_YES) {
      label = SWT.getMessage("SWT_Yes");	//$NON-NLS-1$
    } else if (flag == nsIPromptService.BUTTON_TITLE_IS_STRING) {
      label = buttonTitle;
    }
    return label;
  }

  /* nsIPromptService */

  public void alert(nsIDOMWindow parent, String dialogTitle, String text) {
    MozillaBrowser browser = getBrowser(parent);
    
    MessageBox messageBox = new MessageBox(browser.getShell(), SWT.OK);
    messageBox.setText(dialogTitle);
    messageBox.setMessage(text);
    messageBox.open();
  }

  public void alertCheck(nsIDOMWindow parent, String dialogTitle, String text, String checkMsg, boolean[] checkValue) {
    throw new RuntimeException("Not implemented.");
  }

  public boolean confirm(nsIDOMWindow parent, String dialogTitle, String text) {
    MozillaBrowser browser = getBrowser(parent);

    MessageBox messageBox = new MessageBox(browser.getShell(), SWT.OK | SWT.CANCEL);
    messageBox.setText(dialogTitle);
    messageBox.setMessage(text);
    int id = messageBox.open();

    if (id == SWT.OK)
      return true;
    return false;
  }

  public boolean confirmCheck(nsIDOMWindow parent, String dialogTitle, String text, String checkMsg, boolean[] checkValue) {
    throw new RuntimeException("Not implemented.");
  }

  public int confirmEx(nsIDOMWindow parent, String dialogTitle, String text, long buttonFlags, String button0Title, String button1Title, String button2Title, String checkMsg, boolean[] checkValue) {
    MozillaBrowser browser = getBrowser(parent);

    String button1Label = getLabel(buttonFlags, nsIPromptService.BUTTON_POS_0, button0Title);
    String button2Label = getLabel(buttonFlags, nsIPromptService.BUTTON_POS_1, button0Title);
    String button3Label = getLabel(buttonFlags, nsIPromptService.BUTTON_POS_2, button0Title);

    MozillaPromptDialog dialog = new MozillaPromptDialog(browser.getShell());
    int[] check = new int[1], result = new int[1];
    check[0] = checkValue[0] ? 1 : 0;
    dialog.confirmEx(dialogTitle, text, checkMsg, button1Label, button2Label, button3Label, check, result);
    if (check[0] == 0)
      checkValue[0] = false;
    else
      checkValue[0] = true;
    return result[0];
  }

  public boolean prompt(nsIDOMWindow parent, String dialogTitle, String text, String[] value, String checkMsg, boolean[] checkValue) {
    MozillaBrowser browser = getBrowser(parent);

    MozillaPromptDialog dialog = new MozillaPromptDialog(browser.getShell());
    int[] check = new int[1], result = new int[1];
    if (checkValue[0])
      check[0] = 1;
    else
      check[0] = 0;
    dialog.prompt(dialogTitle, text, checkMsg, value, check, result);

    if (check[0] == 0)
      checkValue[0] = false;
    else
      checkValue[0] = true;
    return result[0] != 0;
  }

  public boolean promptUsernameAndPassword(nsIDOMWindow parent, String dialogTitle, String text, String[] username, String[] password, String checkMsg, boolean[] checkValue) {
    MozillaBrowser browser = getBrowser(parent);

		if (dialogTitle.length() == 0) {
			dialogTitle = SWT.getMessage("SWT_Prompt"); //$NON-NLS-1$
		}

		MozillaPromptDialog dialog = new MozillaPromptDialog(browser.getShell());
		int[] check = new int[1], result = new int[1];
		if (checkValue[0])
			check[0] = 1;
		else
			check[0] = 0;
		dialog.promptUsernameAndPassword(dialogTitle, text, checkMsg, username, password, check, result);

		if (check[0] == 0)
			checkValue[0] = false;
		else
			checkValue[0] = true;
		return result[0] != 0;
	}

	public boolean promptPassword(nsIDOMWindow parent, String dialogTitle, String text, String[] password, String checkMsg, boolean[] checkValue) {
		throw new RuntimeException("Not implemented.");
	}

	public boolean select(nsIDOMWindow parent, String dialogTitle, String text, long count, String[] selectList, int[] outSelection) {
		throw new RuntimeException("Not implemented.");
	}
}