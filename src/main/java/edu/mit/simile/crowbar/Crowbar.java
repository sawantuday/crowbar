package edu.mit.simile.crowbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.MozillaBrowser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.mozilla.xpcom.nsIDOMDocument;

public class Crowbar {

    public static void main(String args[]) {
        Display display = new Display();
        Shell shell = new Shell(display);

        final MozillaBrowser browser = new MozillaBrowser(shell,SWT.BORDER);
        browser.setUrl("http://www.google.com");
        browser.addProgressListener(new ProgressListener() {
            public void changed(ProgressEvent event) {
            }

            public void completed(ProgressEvent event) {
                nsIDOMDocument doc = browser.getDocument();
                System.out.println(doc);
            }
        });

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

}