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

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.mozilla.xpcom.GREVersionRange;
import org.mozilla.xpcom.Mozilla;
import org.mozilla.xpcom.XPCOMException;
import org.mozilla.xpcom.nsIAppShell;
import org.mozilla.xpcom.nsIBaseWindow;
import org.mozilla.xpcom.nsIComponentManager;
import org.mozilla.xpcom.nsIComponentRegistrar;
import org.mozilla.xpcom.nsIContextMenuListener;
import org.mozilla.xpcom.nsIDOMDocument;
import org.mozilla.xpcom.nsIDOMEvent;
import org.mozilla.xpcom.nsIDOMHTMLDocument;
import org.mozilla.xpcom.nsIDOMMouseEvent;
import org.mozilla.xpcom.nsIDOMNode;
import org.mozilla.xpcom.nsIDOMWindow;
import org.mozilla.xpcom.nsIEmbeddingSiteWindow;
import org.mozilla.xpcom.nsIInterfaceRequestor;
import org.mozilla.xpcom.nsIRequest;
import org.mozilla.xpcom.nsIServiceManager;
import org.mozilla.xpcom.nsIStreamListener;
import org.mozilla.xpcom.nsISupports;
import org.mozilla.xpcom.nsITooltipListener;
import org.mozilla.xpcom.nsIURI;
import org.mozilla.xpcom.nsIURIContentListener;
import org.mozilla.xpcom.nsIWebBrowser;
import org.mozilla.xpcom.nsIWebBrowserChrome;
import org.mozilla.xpcom.nsIWebBrowserChromeFocus;
import org.mozilla.xpcom.nsIWebBrowserFocus;
import org.mozilla.xpcom.nsIWebNavigation;
import org.mozilla.xpcom.nsIWebProgress;
import org.mozilla.xpcom.nsIWebProgressListener;
import org.mozilla.xpcom.nsIWindowWatcher;

/**
 * Instances of this class implement the browser user interface
 * metaphor.  It allows the user to visualize and navigate through
 * HTML documents.
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to set a layout on it.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @since 3.0
 */
public abstract class AbstractMozillaBrowser extends Composite implements nsIWebProgressListener,
        nsIWebBrowserChrome, nsIWebBrowserChromeFocus, nsIInterfaceRequestor,
        nsIContextMenuListener, nsIURIContentListener, nsITooltipListener
{

    /* CID constants */
    public static final String NS_APPSHELL_CID = "{2d96b3df-c051-11d1-a827-0040959a28c9}"; //$NON-NLS-1$

    public static final String NS_IOSERVICE_CID = "{9ac9e770-18bc-11d3-9337-00104ba0fd40}"; //$NON-NLS-1$

    public static final String NS_INPUTSTREAMCHANNEL_CID = "{6ddb050c-0d04-11d4-986e-00c04fa0cf4a}"; //$NON-NLS-1$

    public static final String NS_LOADGROUP_CID = "{e1c61582-2a84-11d3-8cce-0060b0fc14a3}"; //$NON-NLS-1$

    public static final String NS_PROMPTSERVICE_CID = "{a2112d6a-0e28-421f-b46a-25c0b308cbd0}"; //$NON-NLS-1$

    public static final String NS_HELPERAPPLAUNCHERDIALOG_CID = "{f68578eb-6ec2-4169-ae19-8c6243f0abe1}"; //$NON-NLS-1$

    public static final String NS_DOWNLOAD_CID = "{e3fa9D0a-1dd1-11b2-bdef-8c720b597445}"; //$NON-NLS-1$

    public static final String NS_CATEGORYMANAGER_CONTRACTID = "@mozilla.org/categorymanager;1"; //$NON-NLS-1$

    public static final String NS_MEMORY_CONTRACTID = "@mozilla.org/xpcom/memory-service;1"; //$NON-NLS-1$

    public static final String NS_PROMPTSERVICE_CONTRACTID = "@mozilla.org/embedcomp/prompt-service;1"; //$NON-NLS-1$

    public static final String NS_WINDOWWATCHER_CONTRACTID = "@mozilla.org/embedcomp/window-watcher;1"; //$NON-NLS-1$

    public static final String NS_HELPERAPPLAUNCHERDIALOG_CONTRACTID = "@mozilla.org/helperapplauncherdialog;1"; //$NON-NLS-1$

    public static final String NS_DOWNLOAD_CONTRACTID = "@mozilla.org/download;1"; //$NON-NLS-1$

    nsIWebBrowser webBrowser;

    nsIEmbeddingSiteWindow embeddingSiteWin;

    long chromeFlags = nsIWebBrowserChrome.CHROME_DEFAULT;
    nsIRequest request;
    Point location;
    Point size;
    boolean addressBar, menuBar, statusBar, toolBar;
    boolean visible;
    Shell tip = null;

    /* External Listener management */
    MozillaCloseWindowListener[] closeWindowListeners = new MozillaCloseWindowListener[0];
    LocationListener[] locationListeners = new LocationListener[0];
    MozillaOpenWindowListener[] openWindowListeners = new MozillaOpenWindowListener[0];
    ProgressListener[] progressListeners = new ProgressListener[0];
    StatusTextListener[] statusTextListeners = new StatusTextListener[0];
    TitleListener[] titleListeners = new TitleListener[0];
    MozillaVisibilityWindowListener[] visibilityWindowListeners = new MozillaVisibilityWindowListener[0];

    static nsIAppShell AppShell;
    static MozillaWindowCreator WindowCreator;
    static int BrowserCount;
    static boolean mozilla;

    /* Package Name */
    static final String PACKAGE_PREFIX = "org.eclipse.swt.browser."; //$NON-NLS-1$


    /**
     * Constructs a new instance of this class given its parent and a style
     * value describing its behavior and appearance.
     * <p>
     * The style value is either one of the style constants defined in class
     * <code>SWT</code> which is applicable to instances of this class, or
     * must be built by <em>bitwise OR</em> 'ing together (that is, using the
     * <code>int</code> "|" operator) two or more of those <code>SWT</code>
     * style constants. The class description lists the style constants that are
     * applicable to the class. Style bits are also inherited from superclasses.
     * </p>
     * 
     * @param parent
     *            a widget which will be the parent of the new instance (cannot
     *            be null)
     * @param style
     *            the style of widget to construct
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     *                </ul>
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                thread that created the parent</li>
     *                </ul>
     * @exception SWTError
     *                <ul>
     *                <li>ERROR_NO_HANDLES if a handle could not be obtained
     *                for browser creation</li>
     *                </ul>
     * 
     * @see #getStyle
     * 
     * @since 3.0
     */
  public AbstractMozillaBrowser(Composite parent, int style) {
    super(parent,style);

    Mozilla Moz = Mozilla.getInstance();
    if (!mozilla) {
      GREVersionRange[] range = new GREVersionRange[1];
      range[0] = new GREVersionRange("1.7.*", false, "1.8.*", true);
        
      File grePath = null;
      try {
          grePath = Mozilla.getGREPathWithProperties(range, null);
          if (!grePath.isDirectory())
            grePath = grePath.getParentFile();

          LocationProvider locProvider = new LocationProvider(grePath);
          Moz.initEmbedding(grePath, grePath, locProvider);
      } catch (Throwable t) {
          dispose();
          SWT.error(SWT.ERROR_FAILED_LOAD_LIBRARY);
      }

      nsIComponentManager componentManager = Moz.getComponentManager();

      AppShell = (nsIAppShell) componentManager.createInstance(
          NS_APPSHELL_CID, null, nsIAppShell.NS_IAPPSHELL_IID);
      AppShell.create(null, null);
      AppShell.spinup();
  
      WindowCreator = new MozillaWindowCreator();

      nsIServiceManager serviceManager = Moz.getServiceManager();

      nsIWindowWatcher windowWatcher = (nsIWindowWatcher) serviceManager
          .getServiceByContractID(NS_WINDOWWATCHER_CONTRACTID,
              nsIWindowWatcher.NS_IWINDOWWATCHER_IID);
      windowWatcher.setWindowCreator(WindowCreator);

      MozillaPromptServiceFactory factory = new MozillaPromptServiceFactory();

      nsIComponentRegistrar componentRegistrar = Moz.getComponentRegistrar();
      componentRegistrar.registerFactory(NS_PROMPTSERVICE_CID,
          "Prompt Service", NS_PROMPTSERVICE_CONTRACTID, factory);

      MozillaHelperAppLauncherDialogFactory dialogFactory = new MozillaHelperAppLauncherDialogFactory();
      componentRegistrar.registerFactory(NS_HELPERAPPLAUNCHERDIALOG_CID,
          "Helper App Launcher Dialog",
          NS_HELPERAPPLAUNCHERDIALOG_CONTRACTID, dialogFactory);

      MozillaDownloadFactory downloadFactory = new MozillaDownloadFactory();
      componentRegistrar.registerFactory(NS_DOWNLOAD_CID, "Download",
          NS_DOWNLOAD_CONTRACTID, downloadFactory);

      mozilla = true;
    }

    BrowserCount++;

    nsIComponentManager componentManager = Moz.getComponentManager();

    String NS_IWEBBROWSER_CID = "F1EAC761-87E9-11d3-AF80-00A024FFC08C"; //$NON-NLS-1$
    webBrowser = (nsIWebBrowser) componentManager.createInstance(
        NS_IWEBBROWSER_CID, null, nsIWebBrowser.NS_IWEBBROWSER_IID);
    webBrowser.setContainerWindow(this);

    nsIBaseWindow baseWindow = (nsIBaseWindow) webBrowser
        .queryInterface(nsIBaseWindow.NS_IBASEWINDOW_IID);
  
    Rectangle rect = getClientArea();
    if (rect.isEmpty()) {
      rect.width = 1;
      rect.height = 1;
    }

    baseWindow.initWindow(getHandle(), 0, 0, 0, rect.width, rect.height);
    baseWindow.create();
    baseWindow.setVisibility(true);

    webBrowser.addWebBrowserListener(this,
        nsIWebProgressListener.NS_IWEBPROGRESSLISTENER_IID);
    webBrowser.setParentURIContentListener(this);

    Listener listener = new Listener() {
      public void handleEvent(Event event) {
        switch (event.type) {
          case SWT.Dispose:
            onDispose();
            break;
          case SWT.Resize:
            onResize();
            break;
          case SWT.FocusIn:
            activate();
            break;
          case SWT.Deactivate: {
            Display display = event.display;
            if (AbstractMozillaBrowser.this == display.getFocusControl())
              deactivate();
            break;
          }
          case SWT.Show: {
            onShow(event);
            break;
          }
        }
      }
    };
    int[] folderEvents = new int[] {
      SWT.Dispose,
      SWT.Resize,
      SWT.FocusIn,
      SWT.KeyDown,
      SWT.Deactivate,
      SWT.Show
    };
    for (int i = 0; i < folderEvents.length; i++) {
      addListener(folderEvents[i], listener);
    }
  }

  protected abstract int getHandle();
  protected abstract void onShow(Event event);

    /**
     * Adds the listener to receive events.
     * <p>
     * 
     * @param listener
     *            the listener
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @since 3.0
     */
  public void addCloseWindowListener(MozillaCloseWindowListener listener) {
    checkWidget();
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    MozillaCloseWindowListener[] newCloseWindowListeners = new MozillaCloseWindowListener[closeWindowListeners.length + 1];
    System.arraycopy(closeWindowListeners, 0, newCloseWindowListeners, 0, closeWindowListeners.length);
    closeWindowListeners = newCloseWindowListeners;
    closeWindowListeners[closeWindowListeners.length - 1] = listener;
  }

    /**
     * Adds the listener to receive events.
     * <p>
     * 
     * @param listener
     *            the listener
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @since 3.0
     */
  public void addLocationListener(LocationListener listener) {
    checkWidget();
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    LocationListener[] newLocationListeners = new LocationListener[locationListeners.length + 1];
    System.arraycopy(locationListeners, 0, newLocationListeners, 0, locationListeners.length);
    locationListeners = newLocationListeners;
    locationListeners[locationListeners.length - 1] = listener;
  }

    /**
     * Adds the listener to receive events.
     * <p>
     * 
     * @param listener
     *            the listener
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @since 3.0
     */
  public void addOpenWindowListener(MozillaOpenWindowListener listener) {
    checkWidget();
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    MozillaOpenWindowListener[] newOpenWindowListeners = new MozillaOpenWindowListener[openWindowListeners.length + 1];
    System.arraycopy(openWindowListeners, 0, newOpenWindowListeners, 0, openWindowListeners.length);
    openWindowListeners = newOpenWindowListeners;
    openWindowListeners[openWindowListeners.length - 1] = listener;
  }

    /**
     * Adds the listener to receive events.
     * <p>
     * 
     * @param listener
     *            the listener
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @since 3.0
     */
  public void addProgressListener(ProgressListener listener) {
    checkWidget();
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    ProgressListener[] newProgressListeners = new ProgressListener[progressListeners.length + 1];
    System.arraycopy(progressListeners, 0, newProgressListeners, 0, progressListeners.length);
    progressListeners = newProgressListeners;
    progressListeners[progressListeners.length - 1] = listener;
  }

    /**
     * Adds the listener to receive events.
     * <p>
     * 
     * @param listener
     *            the listener
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @since 3.0
     */
  public void addStatusTextListener(StatusTextListener listener) {
    checkWidget();
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    StatusTextListener[] newStatusTextListeners = new StatusTextListener[statusTextListeners.length + 1];
    System.arraycopy(statusTextListeners, 0, newStatusTextListeners, 0, statusTextListeners.length);
    statusTextListeners = newStatusTextListeners;
    statusTextListeners[statusTextListeners.length - 1] = listener;
  }

    /**
     * Adds the listener to receive events.
     * <p>
     * 
     * @param listener
     *            the listener
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @since 3.0
     */
  public void addTitleListener(TitleListener listener) {
    checkWidget();
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    TitleListener[] newTitleListeners = new TitleListener[titleListeners.length + 1];
    System.arraycopy(titleListeners, 0, newTitleListeners, 0, titleListeners.length);
    titleListeners = newTitleListeners;
    titleListeners[titleListeners.length - 1] = listener;
  }

    /**
     * Adds the listener to receive events.
     * <p>
     * 
     * @param listener
     *            the listener
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @since 3.0
     */
  public void addVisibilityWindowListener(MozillaVisibilityWindowListener listener) {
    checkWidget();
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    MozillaVisibilityWindowListener[] newVisibilityWindowListeners = new MozillaVisibilityWindowListener[visibilityWindowListeners.length + 1];
    System.arraycopy(visibilityWindowListeners, 0, newVisibilityWindowListeners, 0, visibilityWindowListeners.length);
    visibilityWindowListeners = newVisibilityWindowListeners;
    visibilityWindowListeners[visibilityWindowListeners.length - 1] = listener;
  }

    /**
     * Navigate to the previous session history item.
     * 
     * @return <code>true</code> if the operation was successful and
     *         <code>false</code> otherwise
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @see #forward
     * 
     * @since 3.0
     */
  public boolean back() {
    checkWidget();
    nsIWebNavigation webNavigation = (nsIWebNavigation) webBrowser
        .queryInterface(nsIWebNavigation.NS_IWEBNAVIGATION_IID);
    webNavigation.goBack();
    return true;
  }

  protected void checkSubclass() {
    String name = getClass().getName();
    int index = name.lastIndexOf('.');
    if (!name.substring(0, index + 1).equals(PACKAGE_PREFIX)) {
      SWT.error(SWT.ERROR_INVALID_SUBCLASS);
    }
  }

  public boolean execute(String aScript) {
    checkWidget();
    if (aScript == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);

    String url = "javascript:"+aScript+";void(0);";//$NON-NLS-1$ //$NON-NLS-2$
    nsIWebNavigation webNavigation = (nsIWebNavigation) webBrowser
              .queryInterface(nsIWebNavigation.NS_IWEBNAVIGATION_IID);
    if (webNavigation == null)
      error(Mozilla.NS_ERROR_NO_INTERFACE);

    webNavigation.loadURI(url, nsIWebNavigation.LOAD_FLAGS_NONE, null, null, null);
    return true;
  }

    /**
     * Navigate to the next session history item.
     * 
     * @return <code>true</code> if the operation was successful and
     *         <code>false</code> otherwise
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @see #back
     * 
     * @since 3.0
     */
  public boolean forward() {
    checkWidget();
    nsIWebNavigation webNavigation = (nsIWebNavigation) webBrowser
        .queryInterface(nsIWebNavigation.NS_IWEBNAVIGATION_IID);
    webNavigation.goForward();
    return true;
  }

    /**
     * Returns the current URL.
     * 
     * @return the current URL or an empty <code>String</code> if there is no
     *         current URL
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @see #setUrl
     * 
     * @since 3.0
     */
  public String getUrl() {
    checkWidget();

    nsIWebNavigation webNavigation = (nsIWebNavigation) webBrowser
        .queryInterface(nsIWebNavigation.NS_IWEBNAVIGATION_IID);
    nsIURI uri = webNavigation.getCurrentURI();
    String spec = "";
    if (uri != null) {
      spec = uri.getSpec();
    }
    return spec;
  }

    /**
     * Returns <code>true</code> if the receiver can navigate to the previous
     * session history item, and <code>false</code> otherwise.
     * 
     * @return the receiver's back command enabled state
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                thread that created the receiver</li>
     *                </ul>
     * 
     * @see #back
     */
  public boolean isBackEnabled() {
    checkWidget();
    nsIWebNavigation webNavigation = (nsIWebNavigation) webBrowser
        .queryInterface(nsIWebNavigation.NS_IWEBNAVIGATION_IID);
    boolean aCanGoBack = webNavigation.getCanGoBack();
    return aCanGoBack;
  }

    /**
     * Returns <code>true</code> if the receiver can navigate to the next
     * session history item, and <code>false</code> otherwise.
     * 
     * @return the receiver's forward command enabled state
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                thread that created the receiver</li>
     *                </ul>
     * 
     * @see #forward
     */
  public boolean isForwardEnabled() {
    checkWidget();
    nsIWebNavigation webNavigation = (nsIWebNavigation) webBrowser
        .queryInterface(nsIWebNavigation.NS_IWEBNAVIGATION_IID);
    boolean aCanGoForward = webNavigation.getCanGoForward();
    return aCanGoForward;
  }

  static String error(long code) {
    throw new SWTError("XPCOM error " + code); //$NON-NLS-1$
  }

  void onDispose() {
    webBrowser.removeWebBrowserListener(this,
        nsIWebProgressListener.NS_IWEBPROGRESSLISTENER_IID);
    webBrowser.setParentURIContentListener(null);

    nsIBaseWindow baseWindow = (nsIBaseWindow) webBrowser
        .queryInterface(nsIBaseWindow.NS_IBASEWINDOW_IID);
    baseWindow.destroy();

    if (tip != null && !tip.isDisposed())
      tip.dispose();
    tip = null;

    BrowserCount--;
    /*
         * This code is intentionally commented. It is not possible to
         * reinitialize Mozilla once it has been terminated. NS_InitEmbedding
         * always fails after NS_TermEmbedding has been called. The workaround
         * is to call NS_InitEmbedding once and never call NS_TermEmbedding.
    */
/*
        if (BrowserCount == 0) {
            if (AppShell != null) {
                // Shutdown the appshell service.
                AppShell.spindown();
                AppShell = null;
            }
            MozillaWindowCreator = null;
            embeddingSiteWin = null;
            XUL.termEmbedding();
            mozilla = false;
        }
*/
  }

  void activate() {
    nsIWebBrowserFocus webBrowserFocus = (nsIWebBrowserFocus) webBrowser
              .queryInterface(nsIWebBrowserFocus.NS_IWEBBROWSERFOCUS_IID);
    webBrowserFocus.activate();
  }

  void deactivate() {
    nsIWebBrowserFocus webBrowserFocus = (nsIWebBrowserFocus) webBrowser
              .queryInterface(nsIWebBrowserFocus.NS_IWEBBROWSERFOCUS_IID);
    webBrowserFocus.deactivate();
  }

  void setFocusAtFirstElement() {
    nsIWebBrowserFocus webBrowserFocus = (nsIWebBrowserFocus) webBrowser
              .queryInterface(nsIWebBrowserFocus.NS_IWEBBROWSERFOCUS_IID);
    webBrowserFocus.setFocusAtFirstElement();
  }

  abstract void onResize();

    /**
     * Refresh the current page.
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @since 3.0
     */
  public void refresh() {
    checkWidget();
    nsIWebNavigation webNavigation = (nsIWebNavigation) webBrowser
        .queryInterface(nsIWebNavigation.NS_IWEBNAVIGATION_IID);

    try {
      webNavigation.reload(nsIWebNavigation.LOAD_FLAGS_NONE);
    } catch (XPCOMException e) {
      /*
       * Feature in Mozilla. Reload returns an error code
       * NS_ERROR_INVALID_POINTER when it is called immediately after a
       * request to load a new document using LoadURI. The workaround is to
       * ignore this error code.
       */
      if (e.errorcode != Mozilla.NS_ERROR_INVALID_POINTER)
        throw e;
    }
  }

    /**
     * Removes the listener.
     * 
     * @param listener
     *            the listener
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @since 3.0
     */
  public void removeCloseWindowListener(MozillaCloseWindowListener listener) {
    checkWidget();
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    if (closeWindowListeners.length == 0)
      return;
    int index = -1;
    for (int i = 0; i < closeWindowListeners.length; i++) {
      if (listener == closeWindowListeners[i]) {
        index = i;
        break;
      }
    }
    if (index == -1)
      return;
    if (closeWindowListeners.length == 1) {
      closeWindowListeners = new MozillaCloseWindowListener[0];
      return;
    }

    MozillaCloseWindowListener[] newCloseWindowListeners = new MozillaCloseWindowListener[closeWindowListeners.length - 1];
    System.arraycopy(closeWindowListeners, 0, newCloseWindowListeners, 0, index);
    System.arraycopy(closeWindowListeners, index + 1, newCloseWindowListeners, index, closeWindowListeners.length - index - 1);
    closeWindowListeners = newCloseWindowListeners;
  }

    /**
     * Removes the listener.
     * 
     * @param listener
     *            the listener
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @since 3.0
     */
public void removeLocationListener(LocationListener listener) {
    checkWidget();
    if (listener == null)
    SWT.error(SWT.ERROR_NULL_ARGUMENT);
    if (locationListeners.length == 0)
    return;
    int index = -1;
    for (int i = 0; i < locationListeners.length; i++) {
        if (listener == locationListeners[i]) {
            index = i;
            break;
        }
    }
    if (index == -1)
    return;
    if (locationListeners.length == 1) {
        locationListeners = new LocationListener[0];
        return;
    }

    LocationListener[] newLocationListeners = new LocationListener[locationListeners.length - 1];
    System.arraycopy(locationListeners, 0, newLocationListeners, 0, index);
    System.arraycopy(locationListeners, index + 1, newLocationListeners, index, locationListeners.length - index - 1);
    locationListeners = newLocationListeners;
}

    /**
     * Removes the listener.
     * 
     * @param listener
     *            the listener
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @since 3.0
     */
  public void removeOpenWindowListener(MozillaOpenWindowListener listener) {
    checkWidget();
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    if (openWindowListeners.length == 0)
      return;
    int index = -1;
    for (int i = 0; i < openWindowListeners.length; i++) {
      if (listener == openWindowListeners[i]) {
        index = i;
        break;
      }
    }
    if (index == -1)
      return;
    if (openWindowListeners.length == 1) {
      openWindowListeners = new MozillaOpenWindowListener[0];
      return;
    }

    MozillaOpenWindowListener[] newOpenWindowListeners = new MozillaOpenWindowListener[openWindowListeners.length - 1];
    System.arraycopy(openWindowListeners, 0, newOpenWindowListeners, 0, index);
    System.arraycopy(openWindowListeners, index + 1, newOpenWindowListeners, index, openWindowListeners.length - index - 1);
    openWindowListeners = newOpenWindowListeners;
  }

    /**
     * Removes the listener.
     * 
     * @param listener
     *            the listener
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @since 3.0
     */
  public void removeProgressListener(ProgressListener listener) {
    checkWidget();
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    if (progressListeners.length == 0)
      return;
    int index = -1;
    for (int i = 0; i < progressListeners.length; i++) {
      if (listener == progressListeners[i]) {
        index = i;
        break;
      }
    }
    if (index == -1)
      return;
    if (progressListeners.length == 1) {
      progressListeners = new ProgressListener[0];
      return;
    }

    ProgressListener[] newProgressListeners = new ProgressListener[progressListeners.length - 1];
    System.arraycopy(progressListeners, 0, newProgressListeners, 0, index);
    System.arraycopy(progressListeners, index + 1, newProgressListeners, index, progressListeners.length - index - 1);
    progressListeners = newProgressListeners;
  }

    /**
     * Removes the listener.
     * 
     * @param listener
     *            the listener
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @since 3.0
     */
  public void removeStatusTextListener(StatusTextListener listener) {
    checkWidget();
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    if (statusTextListeners.length == 0)
      return;
    int index = -1;
    for (int i = 0; i < statusTextListeners.length; i++) {
      if (listener == statusTextListeners[i]) {
        index = i;
        break;
      }
    }
    if (index == -1)
      return;
    if (statusTextListeners.length == 1) {
      statusTextListeners = new StatusTextListener[0];
      return;
    }

    StatusTextListener[] newStatusTextListeners = new StatusTextListener[statusTextListeners.length - 1];
    System.arraycopy(statusTextListeners, 0, newStatusTextListeners, 0, index);
    System.arraycopy(statusTextListeners, index + 1, newStatusTextListeners, index, statusTextListeners.length - index - 1);
    statusTextListeners = newStatusTextListeners;
  }

    /**
     * Removes the listener.
     * 
     * @param listener
     *            the listener
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @since 3.0
     */
  public void removeTitleListener(TitleListener listener) {
    checkWidget();
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    if (titleListeners.length == 0)
      return;
    int index = -1;
    for (int i = 0; i < titleListeners.length; i++) {
      if (listener == titleListeners[i]) {
        index = i;
        break;
      }
    }
    if (index == -1)
      return;
    if (titleListeners.length == 1) {
      titleListeners = new TitleListener[0];
      return;
    }

    TitleListener[] newTitleListeners = new TitleListener[titleListeners.length - 1];
    System.arraycopy(titleListeners, 0, newTitleListeners, 0, index);
    System.arraycopy(titleListeners, index + 1, newTitleListeners, index, titleListeners.length - index - 1);
    titleListeners = newTitleListeners;
  }

    /**
     * Removes the listener.
     * 
     * @param listener
     *            the listener
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @since 3.0
     */
  public void removeVisibilityWindowListener(MozillaVisibilityWindowListener listener) {
    checkWidget();
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    if (visibilityWindowListeners.length == 0)
      return;
    int index = -1;
    for (int i = 0; i < visibilityWindowListeners.length; i++) {
      if (listener == visibilityWindowListeners[i]) {
        index = i;
        break;
      }
    }
    if (index == -1)
      return;
    if (visibilityWindowListeners.length == 1) {
      visibilityWindowListeners = new MozillaVisibilityWindowListener[0];
      return;
    }

    MozillaVisibilityWindowListener[] newVisibilityWindowListeners = new MozillaVisibilityWindowListener[visibilityWindowListeners.length - 1];
    System.arraycopy(visibilityWindowListeners, 0, newVisibilityWindowListeners, 0, index);
    System.arraycopy(visibilityWindowListeners, index + 1, newVisibilityWindowListeners, index, visibilityWindowListeners.length - index - 1);
    visibilityWindowListeners = newVisibilityWindowListeners;
  }

    /**
     * Renders HTML.
     * 
     * @param html
     *            the HTML content to be rendered
     * 
     * @return true if the operation was successful and false otherwise.
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the html is null</li>
     *                </ul>
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @see #setUrl
     * 
     * @since 3.0
     */
  public boolean setText(String html) {
    checkWidget();
    if (html == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);

    /*
    *  Feature in Mozilla.  The focus memory of Mozilla must be
    *  properly managed through the nsIWebBrowserFocus interface.
    *  In particular, nsIWebBrowserFocus.deactivate must be called
    *  when the focus moves from the browser (or one of its children
    *  managed by Mozilla to another widget.  We currently do not
    *  get notified when a widget takes focus away from the MozillaBrowser.
    *  As a result, deactivate is not properly called. This causes
    *  Mozilla to retake focus the next time a document is loaded.
    *  This breaks the case where the HTML loaded in the MozillaBrowser
    *  varies while the user enters characters in a text widget. The text
    *  widget loses focus every time new content is loaded.
    *  The current workaround is to call deactivate everytime if
    *  the browser currently does not have focus. A better workaround
    *  would be to have a mean to call deactivate when the MozillaBrowser
    *  or one of its children loses focus.
    */
    if (this != getDisplay().getFocusControl())
      deactivate();

    nsIWebNavigation webNavigation = (nsIWebNavigation) webBrowser
      .queryInterface(nsIWebNavigation.NS_IWEBNAVIGATION_IID);

    /*
     * Note. Stop any pending request. This is required to avoid displaying
     * a blank page as a result of consecutive calls to setUrl and/or
     * setText. The previous request would otherwise render the new html
     * content and reset the html field before the browser actually
     * navigates to the blank page as requested below.
     */
    webNavigation.stop(nsIWebNavigation.STOP_ALL);
    webNavigation.loadURI("data:text/html," + html, nsIWebNavigation.LOAD_FLAGS_NONE, null, null, null); //$NON-NLS-1$

    return true;
  }

    /**
     * Loads a URL.
     * 
     * @param url
     *            the URL to be loaded
     * 
     * @return true if the operation was successful and false otherwise.
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the url is null</li>
     *                </ul>
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @see #getUrl
     * 
     * @since 3.0
     */
  public boolean setUrl(String url) {
    checkWidget();
    if (url == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);

    nsIWebNavigation webNavigation = (nsIWebNavigation) webBrowser
      .queryInterface(nsIWebNavigation.NS_IWEBNAVIGATION_IID);
    webNavigation.loadURI(url, nsIWebNavigation.LOAD_FLAGS_NONE, null, null, null);
    return true;
  }

    /**
     * Stop any loading and rendering activity.
     * 
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS when called from the
     *                wrong thread</li>
     *                <li>ERROR_WIDGET_DISPOSED when the widget has been
     *                disposed</li>
     *                </ul>
     * 
     * @since 3.0
     */
  public void stop() {
    checkWidget();
    nsIWebNavigation webNavigation = (nsIWebNavigation) webBrowser
      .queryInterface(nsIWebNavigation.NS_IWEBNAVIGATION_IID);
    webNavigation.stop(nsIWebNavigation.STOP_ALL);
  }

  /* nsISupports */

  public nsISupports queryInterface(String aIID) {
    // Browser cannot implement nsIEmbeddingSiteWindow since both it and the
    // Composite class have a setFocus() method.
    if (aIID.equals(nsIEmbeddingSiteWindow.NS_IEMBEDDINGSITEWINDOW_IID)) {
      if (embeddingSiteWin == null)
        embeddingSiteWin = new EmbeddingSiteWindow(this);
      return embeddingSiteWin;
    } else {
      return Mozilla.queryInterface(this, aIID);
    }
  }

  /* nsIWeakReference */

  public nsISupports queryReferent(String riid) {
    return queryInterface(riid);
  }

  /* nsIInterfaceRequestor */

  public nsISupports getInterface(String riid) {
    if (riid.equals(nsIDOMWindow.NS_IDOMWINDOW_IID)) {
      nsIDOMWindow contentDOMWindow = webBrowser.getContentDOMWindow();
      return contentDOMWindow;
    }
    return queryInterface(riid);
  }

  /* nsIWebProgressListener */

  public void onStateChange(nsIWebProgress aWebProgress, nsIRequest aRequest, long aStateFlags, long aStatus) {
    if ((aStateFlags & nsIWebProgressListener.STATE_IS_DOCUMENT) == 0)
      return;
    if ((aStateFlags & nsIWebProgressListener.STATE_START) != 0) {
      if (request == null)
        request = aRequest;
    } else if ((aStateFlags & nsIWebProgressListener.STATE_REDIRECTING) != 0) {
            // XXX if (request == aRequest)
            if (request != null
                    && request.getName().equals(aRequest.getName()))
        request = null;
    } else if ((aStateFlags & nsIWebProgressListener.STATE_STOP) != 0) {
      /*
             * Feature on Mozilla. When a request is redirected
             * (STATE_REDIRECTING), it never reaches the state STATE_STOP and it
             * is replaced with a new request. The new request is received when
             * it is in the state STATE_STOP. To handle this case, the variable
             * request is set to 0 when the corresponding request is redirected.
             * The following request received with the state STATE_STOP - the
             * new request resulting from the redirection - is used to send the
             * ProgressListener.completed event.
      */
            // XXX if (request == req || request == null) {
            if (request == null || request.getName().equals(aRequest.getName())) {
        request = null;
        StatusTextEvent event = new StatusTextEvent(this);
        event.display = getDisplay();
        event.widget = this;
        event.text = ""; //$NON-NLS-1$
        for (int i = 0; i < statusTextListeners.length; i++)
          statusTextListeners[i].changed(event);
  
        ProgressEvent event2 = new ProgressEvent(this);
        event2.display = getDisplay();
        event2.widget = this;
        for (int i = 0; i < progressListeners.length; i++)
          progressListeners[i].completed(event2);
      }
    }
  }

  public void onProgressChange(nsIWebProgress aWebProgress, nsIRequest aRequest,
      int aCurSelfProgress, int aMaxSelfProgress, int aCurTotalProgress,
      int aMaxTotalProgress)
  {
    if (progressListeners.length == 0)
      return;
  
    int  total = aMaxTotalProgress;
    if (total <= 0)
      total = Integer.MAX_VALUE;
    ProgressEvent event = new ProgressEvent(this);
    event.display = getDisplay();
    event.widget = this;
    event.current = aCurTotalProgress;
    event.total = aMaxTotalProgress;
    for (int i = 0; i < progressListeners.length; i++)
      progressListeners[i].changed(event);
  }

  public void onLocationChange(nsIWebProgress aWebProgress,
      nsIRequest aRequest, nsIURI aLocation)
  {
    /*
    * Feature on Mozilla.  When a page is loaded via setText before a previous
    * setText page load has completed, the expected OnStateChange STATE_STOP for the
    * original setText never arrives because it gets replaced by the OnStateChange
    * STATE_STOP for the new request.  This results in the request field never being
    * cleared because the original request's OnStateChange STATE_STOP is still expected
    * (but never arrives).  To handle this case, the request field is updated to the new
    * overriding request since its OnStateChange STATE_STOP will be received next.
    */
    if (request != null && request != aRequest)
      request = aRequest;

    if (locationListeners.length == 0)
      return;

    nsIDOMWindow domWindow = aWebProgress.getDOMWindow();
    nsIDOMWindow topWindow = domWindow.getTop();

    String spec = aLocation.getSpec();

    LocationEvent event = new LocationEvent(this);
    event.display = getDisplay();
    event.widget = this;
    event.location = spec;
    event.top = topWindow == domWindow;
    for (int i = 0; i < locationListeners.length; i++)
      locationListeners[i].changed(event);
  }

  public void onStatusChange(nsIWebProgress aWebProgress, nsIRequest aRequest,
      long aStatus, String aMessage)
  {
    /*
    * Feature in Mozilla.  In Mozilla 1.7.5, navigating to an
    * HTTPS link without a user profile set causes a crash.
    * Most requests for HTTPS pages are aborted in OnStartURIOpen.
    * However, https page requests that do not initially specify
    * https as their protocol will get past this check since they
    * are resolved afterwards.  The workaround is to check the url
    * whenever there is a status change, and to abort any https
    * requests that are detected.
    */
    String reqName = aRequest.getName();
    if (reqName.startsWith("https:")) { //$NON-NLS-1$
      // XXX NS_BINDING_ABORTED should be defined in XPCOM class
      aRequest.cancel(0x804b0002 /*XPCOM.NS_BINDING_ABORTED*/);
      return;
    }

    if (statusTextListeners.length == 0)
      return;

    StatusTextEvent event = new StatusTextEvent(this);
    event.display = getDisplay();
    event.widget = this;
    event.text = aMessage;
    for (int i = 0; i < statusTextListeners.length; i++)
      statusTextListeners[i].changed(event);
  }

  public void onSecurityChange(nsIWebProgress aWebProgress, nsIRequest aRequest, long state) {
  }

  /* nsIWebBrowserChrome */

  public void setStatus(long statusType, String status) {
    StatusTextEvent event = new StatusTextEvent(this);
    event.display = getDisplay();
    event.widget = this;
    event.text = status;
    for (int i = 0; i < statusTextListeners.length; i++)
      statusTextListeners[i].changed(event);
  }

  public nsIWebBrowser getWebBrowser() {
    return webBrowser;
  }

  public void setWebBrowser(nsIWebBrowser aWebBrowser) {
    webBrowser = aWebBrowser;
  }

  public long getChromeFlags() {
    return chromeFlags;
  }

  public void setChromeFlags(long aChromeFlags) {
    chromeFlags = aChromeFlags;
  }

  public void destroyBrowserWindow() {
    MozillaWindowEvent newEvent = new MozillaWindowEvent(this);
    newEvent.display = getDisplay();
    newEvent.widget = this;
    for (int i = 0; i < closeWindowListeners.length; i++)
      closeWindowListeners[i].close(newEvent);
    /*
         * Note on Mozilla. The DestroyBrowserWindow notification cannot be
         * cancelled. The browser widget cannot be used after this notification
         * has been received. The application is advised to close the window
         * hosting the browser widget. The browser widget must be disposed in
         * all cases.
    */
    dispose();
  }

  public void sizeBrowserTo(int aCX, int aCY) {
    size = new Point(aCX, aCY);
  }

  public void showAsModal() {
    throw new RuntimeException("Not implemented.");
  }

  public boolean isWindowModal() {
    // no modal loop
    return false;
  }

  public void exitModalEventLoop(long aStatus) {
  }

  /* nsIWebBrowserChromeFocus */

  public void focusNextElement() {
    /*
         * Bug in Mozilla embedding API. Mozilla takes back the focus after
         * sending this event. This prevents tabbing out of Mozilla. This
         * behaviour can be reproduced with the Mozilla application
         * TestGtkEmbed. The workaround is to send the traversal notification
         * after this callback returns.
    */
    getDisplay().asyncExec(new Runnable() {
      public void run() {
        traverse(SWT.TRAVERSE_TAB_NEXT);
      }
    });
  }

  public void focusPrevElement() {
    /*
         * Bug in Mozilla embedding API. Mozilla takes back the focus after
         * sending this event. This prevents tabbing out of Mozilla. This
         * behaviour can be reproduced with the Mozilla application
         * TestGtkEmbed. The workaround is to send the traversal notification
         * after this callback returns.
    */
    getDisplay().asyncExec(new Runnable() {
      public void run() {
        traverse(SWT.TRAVERSE_TAB_PREVIOUS);
      }
    });
  }

  /* nsIContextMenuListener */

  public void onShowContextMenu(long aContextFlags, nsIDOMEvent aEvent,
      nsIDOMNode aNode)
  {
    nsIDOMMouseEvent domMouseEvent = (nsIDOMMouseEvent) aEvent
      .queryInterface(nsIDOMMouseEvent.NS_IDOMMOUSEEVENT_IID);
    int screenX = domMouseEvent.getScreenX();
    int screenY = domMouseEvent.getScreenY();

    Event event = new Event();
    event.x = screenX;
    event.y = screenY;
    notifyListeners(SWT.MenuDetect, event);
    if (!event.doit)
      return;

    Menu menu = getMenu();
    if (menu != null && !menu.isDisposed()) {
      if (screenX != event.x || screenY != event.y) {
        menu.setLocation(event.x, event.y);
      }
      menu.setVisible(true);
    }
  }

  /* nsIURIContentListener */

  public boolean onStartURIOpen(nsIURI aURI) {
    String spec = aURI.getSpec();

    /*
    * Feature in Mozilla.  In Mozilla 1.7.5, navigating to an 
    * HTTPS link without a user profile set causes a crash. 
    * HTTPS requires a user profile to be set to persist security
    * information.  This requires creating a new user profile
    * (i.e. creating a new folder) or locking an existing Mozilla 
    * user profile.  The Mozilla Profile API is not frozen and it is not 
    * currently implemented.  The workaround is to not load 
    * HTTPS resources to avoid the crash.
    */
    boolean isHttps = spec.startsWith("https:"); //$NON-NLS-1$
    if (locationListeners.length == 0) {
      return isHttps;
    }

    boolean doit = !isHttps;
    if (request == null) {
      LocationEvent event = new LocationEvent(this);
      event.display = getDisplay();
      event.widget = this;
      event.location = spec;
      event.doit = doit;
      for (int i = 0; i < locationListeners.length; i++)
        locationListeners[i].changing(event);
      if (!isHttps)
        doit = event.doit;
    }
    return !doit;
  }

  public boolean doContent(String aContentType, boolean aIsContentPreferred, nsIRequest aRequest, nsIStreamListener[] aContentHandler) {
    throw new RuntimeException("Not implemented.");
  }

  public boolean isPreferred(String aContentType, String[] aDesiredContentType) {
    if (aContentType.length() > 0) {
      /*
      * Feature in Mozilla. Implementing IsPreferred properly would require the use of
      * unfrozen API such as nsICategoryManeger.GetCategoryEntry("gecko-content-viewer")
      * in order to determine which content can be handled.  The workaround is to always
      * accept content except for known problematic types.
      */
      if (aContentType.equals("application/x-vnd.mozilla.maybe-text")) //$NON-NLS-1$
        return false;
      if (aContentType.equals("multipart/x-mixed-replace")) //$NON-NLS-1$
        return false;
    }
    return true;
  }

  public boolean canHandleContent(String aContentType, boolean aIsContentPreferred, String[] aDesiredContentType) {
    throw new RuntimeException("Not implemented.");
  }

  public nsISupports getLoadCookie() {
    throw new RuntimeException("Not implemented.");
  }

  public void setLoadCookie(nsISupports aLoadCookie) {
    throw new RuntimeException("Not implemented.");
  }

  public nsIURIContentListener getParentContentListener() {
    throw new RuntimeException("Not implemented.");
  }

  public void setParentContentListener(nsIURIContentListener aParentContentListener) {
    throw new RuntimeException("Not implemented.");
  }

  /* nsITooltipListener */

  public void onShowTooltip(int aXCoords, int aYCoords, String aTipText) {
    if (tip != null && !tip.isDisposed())
      tip.dispose();
    Display display = getDisplay();
    Shell parent = getShell();
    tip = new Shell(parent, SWT.ON_TOP);
    tip.setLayout(new FillLayout());
    Label label = new Label(tip, SWT.CENTER);
    label.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
    label.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    label.setText(aTipText);
    /*
     * Bug in Mozilla embedded API.  Tooltip coordinates are wrong for
     * elements inside an inline frame (IFrame tag).  The workaround is
     * to position the tooltip based on the mouse cursor location.
     */
    Point point = display.getCursorLocation();
    /*
     * Assuming cursor is 21x21 because this is the size of
     * the arrow cursor on Windows
     */
    point.y += 21;
    tip.setLocation(point);
    tip.pack();
    tip.setVisible(true);
  }

  public void onHideTooltip() {
    if (tip != null && !tip.isDisposed())
      tip.dispose();
    tip = null;
  }
  
  public nsIDOMHTMLDocument getDocument() {
        nsIDOMWindow dw = webBrowser.getContentDOMWindow();

        nsIDOMDocument nsDoc = dw.getDocument();

        nsIDOMHTMLDocument htmldoc = (nsIDOMHTMLDocument)nsDoc.queryInterface(nsIDOMHTMLDocument.NS_IDOMHTMLDOCUMENT_IID);

        return htmldoc;
    } 
}
