package org.eclipse.swt.browser;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.mozilla.xpcom.*;

public class MozillaBrowser extends AbstractMozillaBrowser {

	public MozillaBrowser(Composite parent, int style) {
		super(parent, style);
	}

	protected int getHandle() {
		return handle;
	}

	protected void onShow(Event event) {
	}

	static MozillaBrowser findBrowser(long handle) {
		Display display = Display.getCurrent();
		return (MozillaBrowser) display.findWidget((int)handle);
	}
	
	void onResize() {
		Rectangle rect = getClientArea();
		nsIBaseWindow baseWindow = (nsIBaseWindow) webBrowser.queryInterface(nsIBaseWindow.NS_IBASEWINDOW_IID);
		baseWindow.setPositionAndSize(rect.x, rect.y, rect.width, rect.height, true);
	}

}
