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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.mozilla.xpcom.*;

class MozillaDownload implements nsIDownload, nsIProgressDialog, nsIWebProgressListener {
    nsIHelperAppLauncher helperAppLauncher;

    Shell shell;
    Label status;
    Button cancel;

    static String DOWNLOAD = SWT.getMessage("SWT_Download")+" ";
    static String SAVING = SWT.getMessage("SWT_Saving")+" ";
    static String FROM = " "+SWT.getMessage("SWT_from")+" ";
    static String KO = SWT.getMessage("SWT_KB");
    static String OF = " "+SWT.getMessage("SWT_of")+" ";

  public MozillaDownload() {
  }

  public nsISupports queryInterface(String aIID) {
      return Mozilla.queryInterface(this, aIID);
  }

  /* nsITransfer */

  /* Note. The argument startTime is defined as a PRInt64. This translates into two java ints. */
  public void init(nsIURI aSource, nsIURI aTarget, String aDisplayName, nsIMIMEInfo aMIMEInfo, double startTime, nsILocalFile aTempFile, nsICancelable aCancelable) {
    String url = aSource.getHost();

    // get the local file corresponding to the given target URI
    String file_loc = "";
    nsIFileURL fileURL = (nsIFileURL) aTarget.queryInterface(nsIFileURL.NS_IFILEURL_IID);
    if (fileURL != null) {
      nsIFile file = fileURL.getFile();
      if (file != null) {
        nsILocalFile localfile = (nsILocalFile) file.queryInterface(nsILocalFile.NS_ILOCALFILE_IID);
        // XXX Do we really need to use getNativeLeafName()?  Why not getLeafName()?
        // file_loc = localfile.getNativeLeafName();
        file_loc = localfile.getLeafName();
      }
    }

    Listener listener = new Listener() {
      public void handleEvent(Event event) {
        if (event.widget == cancel) {
          shell.close();
        }
        if (helperAppLauncher != null) {
          // XXX NS_BINDING_ABORTED should be defined in XPCOM class
          helperAppLauncher.cancel(0x804b0002 /*NS_BINDING_ABORTED*/);
        }
        shell = null;
        helperAppLauncher = null;
      }
    };

    shell = new Shell(SWT.DIALOG_TRIM);
    shell.setText(DOWNLOAD+file_loc);
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginHeight = 15;
    gridLayout.marginWidth = 15;
    gridLayout.verticalSpacing = 20;
    shell.setLayout(gridLayout);
    new Label(shell, SWT.SIMPLE).setText(SAVING+file_loc+FROM+url);
    status = new Label(shell, SWT.SIMPLE);
    status.setText(DOWNLOAD);
    GridData data = new GridData ();
    data.grabExcessHorizontalSpace = true;
    data.grabExcessVerticalSpace = true;
    status.setLayoutData (data);

    cancel = new Button(shell, SWT.PUSH);
    cancel.setText(SWT.getMessage("SWT_Cancel"));
    data = new GridData ();
    data.horizontalAlignment = GridData.CENTER;
    cancel.setLayoutData (data);
    cancel.addListener(SWT.Selection, listener);
    shell.addListener(SWT.Close, listener);
    shell.pack();
    shell.open();
  }

  public nsIObserver getObserver() {
    throw new RuntimeException("Not implemented.");
  }

  public void setObserver(nsIObserver aObserver) {
    if (aObserver != null) {
      helperAppLauncher = (nsIHelperAppLauncher) aObserver.queryInterface(nsIHelperAppLauncher.NS_IHELPERAPPLAUNCHER_IID);
    }
    return;
  }

  public void onProgressChange64(nsIWebProgress arg1, nsIRequest arg2, long arg3, long arg4, long arg5, long arg6) {
    throw new RuntimeException("Not implemented.");
  }

  /* nsIDownload */

  public nsILocalFile getTargetFile() {
    throw new RuntimeException("Not implemented.");
  }

  public int getPercentComplete() {
    throw new RuntimeException("Not implemented.");
  }

  public double getAmountTransferred() {
    throw new RuntimeException("Not implemented.");
  }

  public double getSize() {
    throw new RuntimeException("Not implemented.");
  }

  public nsIURI getSource() {
    throw new RuntimeException("Not implemented.");
  }

  public nsIURI getTarget() {
    throw new RuntimeException("Not implemented.");
  }

  public nsICancelable getCancelable() {
    throw new RuntimeException("Not implemented.");
  }

  public String getDisplayName() {
    throw new RuntimeException("Not implemented.");
  }

  public long getStartTime() {
    throw new RuntimeException("Not implemented.");
  }

  public nsIMIMEInfo getMIMEInfo() {
    throw new RuntimeException("Not implemented.");
  }

  /* nsIProgressDialog */

  public void open(nsIDOMWindow aParent) {
    throw new RuntimeException("Not implemented.");
  }

  public boolean getCancelDownloadOnClose() {
    throw new RuntimeException("Not implemented.");
  }

  public void setCancelDownloadOnClose(boolean aCancelDownloadOnClose) {
    throw new RuntimeException("Not implemented.");
  }

  public nsIDOMWindow getDialog() {
    throw new RuntimeException("Not implemented.");
  }

  public void setDialog(nsIDOMWindow aDialog) {
    throw new RuntimeException("Not implemented.");
  }

  /* nsIWebProgressListener */

  public void onStateChange(nsIWebProgress aWebProgress, nsIRequest aRequest, long aStateFlags, long aStatus) {
    if ((aStateFlags & nsIWebProgressListener.STATE_STOP) != 0) {
      helperAppLauncher = null;
      if (shell != null && !shell.isDisposed())
        shell.dispose();
      shell = null;
    }
    return;
  } 

  public void onProgressChange(nsIWebProgress aWebProgress, nsIRequest aRequest, int aCurSelfProgress, int aMaxSelfProgress, int aCurTotalProgress, int aMaxTotalProgress) {
    int currentBytes = aCurTotalProgress / 1024;
    int totalBytes = aMaxTotalProgress / 1024;
    if (shell != null & !shell.isDisposed()) {
      status.setText(DOWNLOAD+currentBytes+KO+OF+totalBytes+KO);
      shell.layout(true);
      shell.getDisplay().update();
    }
    return;
  }

  public void onLocationChange(nsIWebProgress aWebProgress, nsIRequest aRequest, nsIURI aLocation) {
    return;
  }

  public void onStatusChange(nsIWebProgress aWebProgress, nsIRequest aRequest, long aStatus, String aMessage) {
    return;
  }

  public void onSecurityChange(nsIWebProgress aWebProgress, nsIRequest aRequest, long state) {
    return;
  }
}