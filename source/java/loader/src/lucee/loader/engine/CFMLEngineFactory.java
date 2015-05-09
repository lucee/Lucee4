/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.loader.engine;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import lucee.VersionInfo;
import lucee.loader.TP;
import lucee.loader.osgi.BundleCollection;
import lucee.loader.osgi.BundleLoader;
import lucee.loader.osgi.BundleUtil;
import lucee.loader.osgi.LoggerImpl;
import lucee.loader.util.ExtensionFilter;
import lucee.loader.util.Util;
import lucee.loader.util.ZipUtil;
import lucee.runtime.config.Identification;
import lucee.runtime.config.Password;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.Logger;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.intergral.fusiondebug.server.FDControllerFactory;

/**
 * Factory to load CFML Engine
 */
public class CFMLEngineFactory extends CFMLEngineFactorySupport {

	// set to false to disable patch loading, for example in major alpha releases
	private static final boolean PATCH_ENABLED = true;
	public static final Version VERSION_ZERO = new Version(0,0,0,"0");
	private static final String UPDATE_LOCATION ="http://stable.lucee.org"; // MUST from server.xml

	private static CFMLEngineFactory factory;
	//private static  CFMLEngineWrapper engineListener;
	private static CFMLEngineWrapper singelton;

	private static File luceeServerRoot;
	
	private Felix felix;
	private BundleCollection bundleCollection;
	//private CFMLEngineWrapper engine;

	private ClassLoader mainClassLoader = new TP().getClass().getClassLoader();
	private Version version;
	private List<EngineChangeListener> listeners = new ArrayList<EngineChangeListener>();
	private File resourceRoot;

	//private PrintWriter out;

	private final LoggerImpl logger;

	protected ServletConfig config;

	/**
	 * Constructor of the class
	 */
	protected CFMLEngineFactory(ServletConfig config) {
		File logFile=null;
		this.config=config;
		try {
			logFile = new File(getResourceRoot(),"context/logs/felix.log");
		} catch (IOException e) {
			e.printStackTrace();
		}
		logFile.getParentFile().mkdirs();
		logger=new LoggerImpl(logFile);
	}

	/**
	 * returns instance of this factory (singelton-> always the same instance)
	 * do auto update when changes occur
	 * 
	 * @param config
	 * @return Singelton Instance of the Factory
	 * @throws ServletException
	 */
	public static CFMLEngine getInstance(ServletConfig config)
			throws ServletException {

		if (singelton != null) {
			if (factory == null) factory = singelton.getCFMLEngineFactory(); // not sure if this ever is done, but it does not hurt
			return singelton;
		}

		if (factory == null) factory = new CFMLEngineFactory(config);

		// read init param from config
		factory.readInitParam(config);

		factory.initEngineIfNecessary();
		singelton.addServletConfig(config);
		
		// add listener for update
		//factory.addListener(singelton);
		return singelton;
	}

	/**
	 * returns instance of this factory (singelton-> always the same instance)
	 * do auto update when changes occur
	 * 
	 * @return Singelton Instance of the Factory
	 * @throws RuntimeException
	 */
	public static CFMLEngine getInstance() throws RuntimeException {
		if (singelton != null)
			return singelton;
		throw new RuntimeException(
				"engine is not initalized, you must first call getInstance(ServletConfig)");
	}

	public static void registerInstance(CFMLEngine engine) {
		if(engine instanceof CFMLEngineWrapper) throw new RuntimeException("that should not happen!");
		setEngine(engine);
	}
	
	/**
	 * used only for internal usage
	 * 
	 * @param engine
	 * @throws RuntimeException
	 */
	/*public static void registerInstance(CFMLEngineWrapper engine) throws RuntimeException {
		if (factory == null)
			factory = engine.getCFMLEngineFactory();
		
		// first update existing listener
		if (singelton != null) {
			if (engineListener.equalTo(engine, true))
				return;
			engineListener.onUpdate(engine);// perhaps this is still refrenced in the code, because of that we update it
			factory.removeListener(engineListener);
		}

		// now register this
		if (engine instanceof CFMLEngineWrapper)
			engineListener = (CFMLEngineWrapper) engine;
		else
			engineListener = new CFMLEngineWrapper(engine);

		factory.addListener(engineListener);
	}*/

	/**
	 * returns instance of this factory (singelton-> always the same instance)
	 * 
	 * @param config
	 * @param listener
	 * @return Singelton Instance of the Factory
	 * @throws ServletException
	 */
	public static CFMLEngine getInstance(ServletConfig config,
			EngineChangeListener listener) throws ServletException {
		getInstance(config);
		
		// add listener for update
		factory.addListener(listener);

		// read init param from config
		factory.readInitParam(config);

		factory.initEngineIfNecessary();
		singelton.addServletConfig(config);

		// make the FDController visible for the FDClient
		FDControllerFactory.makeVisible();

		return singelton;
	}

	void readInitParam(ServletConfig config) {
		if (luceeServerRoot != null)
			return;

		String initParam = config.getInitParameter("lucee-server-directory");
		if (Util.isEmpty(initParam))
			initParam = config.getInitParameter("lucee-server-root");
		if (Util.isEmpty(initParam))
			initParam = config.getInitParameter("lucee-server-dir");
		if (Util.isEmpty(initParam))
			initParam = config.getInitParameter("lucee-server");
		if (Util.isEmpty(initParam))
			initParam = System.getProperty("lucee.server.dir");
		
		initParam = parsePlaceHolder(removeQuotes(initParam, true));
		try {
			if (!Util.isEmpty(initParam)) {
				File root = new File(initParam);
				if (!root.exists()) {
					if (root.mkdirs()) {
						luceeServerRoot = root.getCanonicalFile();
						return;
					}
				} else if (root.canWrite()) {
					luceeServerRoot = root.getCanonicalFile();
					return;
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * adds a listener to the factory that will be informed when a new engine
	 * will be loaded.
	 * 
	 * @param listener
	 */
	private void addListener(EngineChangeListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}


	/**
	 * @return CFML Engine
	 * @throws ServletException
	 */
	private void initEngineIfNecessary() throws ServletException {
		if (singelton == null) initEngine();
	}

	private void initEngine() throws ServletException {
		Version coreVersion = VersionInfo.getIntVersion();
		long coreCreated = VersionInfo.getCreateTime();

		// get newest lucee version as file
		File patcheDir = null;
		try {
			patcheDir = getPatchDirectory();
			log(Logger.LOG_DEBUG,"lucee-server-root:" + patcheDir.getParent());
		} catch (IOException e) {
			throw new ServletException(e);
		}

		File[] patches = PATCH_ENABLED ? patcheDir
				.listFiles(new ExtensionFilter(new String[] { ".lco" })) : null;
		File lucee = null;
		if (patches != null) {
			for (int i = 0; i < patches.length; i++) {
				if (patches[i].getName().startsWith("tmp.lco")) {
					patches[i].delete();
				} else if (patches[i].lastModified() < coreCreated) {
					patches[i].delete();
				} else if (lucee == null
						|| isNewerThan(toVersion(patches[i].getName(), VERSION_ZERO),
								toVersion(lucee.getName(), VERSION_ZERO))) {
					lucee = patches[i];
				}
			}
		}
		if (lucee != null
				&& isNewerThan(coreVersion, toVersion(lucee.getName(), VERSION_ZERO)))
			lucee = null;

		// Load Lucee
		//URL url=null;
		try {
			// Load core version when no patch available
			if (lucee == null) {
				log(Logger.LOG_DEBUG,"Load Build in Core");
				// 
				String coreExt = "lco";
				setEngine( getCore());

				lucee = new File(patcheDir, singelton.getInfo().getVersion().toString() + "." + coreExt);
				if (PATCH_ENABLED) {
					InputStream bis = new TP().getClass().getResourceAsStream(
							"/core/core." + coreExt);
					OutputStream bos = new BufferedOutputStream(
							new FileOutputStream(lucee));
					copy(bis, bos);
					closeEL(bis);
					closeEL(bos);
				}
			} else {

				bundleCollection = BundleLoader.loadBundles(this,
						getFelixCacheDirectory(), getBundleDirectory(), lucee,
						bundleCollection);
				//bundle=loadBundle(lucee);
				log(Logger.LOG_DEBUG,"loaded bundle:" + bundleCollection.core.getSymbolicName());
				setEngine(getEngine(bundleCollection));
				log(Logger.LOG_DEBUG,"loaded engine:" + singelton);
			}
			version = singelton.getInfo().getVersion();

			log(Logger.LOG_DEBUG,"Loaded Lucee Version " + singelton.getInfo().getVersion());
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			throw new ServletException(e.getTargetException());
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}

		//check updates
		String updateType = singelton.getUpdateType();
		if (updateType == null || updateType.length() == 0)
			updateType = "manuell";

		if (updateType.equalsIgnoreCase("auto")) {
			new UpdateChecker(this,null).start();
		}

	}

	private static CFMLEngineWrapper setEngine(CFMLEngine engine) {
		//new RuntimeException("setEngine").printStackTrace();
		if(singelton==null) singelton=new CFMLEngineWrapper(engine);
		else if(!singelton.isIdentical(engine)) {
			engine.reset();
			singelton.setEngine(engine);
		}
		else {
			//new RuntimeException("useless call").printStackTrace();
		}
		
		return singelton;
	}

	public Felix getFelix(File cacheRootDir, Map<String, Object> config) throws BundleException {
		
		if(config==null)config=new HashMap<String, Object>();
		
		// Log Level
		int logLevel=1; // 1 = error, 2 = warning, 3 = information, and 4 = debug
		String strLogLevel = (String)config.get("felix.log.level");
		if(!Util.isEmpty(strLogLevel)) {
			if("warn".equalsIgnoreCase(strLogLevel) || "warning".equalsIgnoreCase(strLogLevel) || "2".equalsIgnoreCase(strLogLevel)) 
				logLevel=2;
			else if("info".equalsIgnoreCase(strLogLevel) || "information".equalsIgnoreCase(strLogLevel) || "3".equalsIgnoreCase(strLogLevel)) 
				logLevel=3;
			else if("debug".equalsIgnoreCase(strLogLevel) || "4".equalsIgnoreCase(strLogLevel)) 
				logLevel=4;
		}
		config.put("felix.log.level", "" + logLevel);

		// storage clean
		String storageClean = (String)config.get(Constants.FRAMEWORK_STORAGE_CLEAN);
		if (Util.isEmpty(storageClean))
			config.put(Constants.FRAMEWORK_STORAGE_CLEAN,Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

		// parent classLoader
		String parentClassLoader = (String)config.get(Constants.FRAMEWORK_BUNDLE_PARENT);
		if (Util.isEmpty(parentClassLoader))
			config.put(Constants.FRAMEWORK_BUNDLE_PARENT,Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);
		else
			config.put(Constants.FRAMEWORK_BUNDLE_PARENT,BundleUtil.toFrameworkBundleParent(parentClassLoader));
		

		
		// felix.cache.rootdir
		if (!cacheRootDir.exists()) {
			cacheRootDir.mkdirs();
		}
		if (cacheRootDir.isDirectory()) {
			config.put("felix.cache.rootdir", cacheRootDir.getAbsolutePath());
		}
		
		if(logger!=null)config.put("felix.log.logger", logger);
		// TODO felix.log.logger 
		
		// remove any empty record, this can produce trouble
		{
			Iterator<Entry<String, Object>> it = config.entrySet().iterator();
			Entry<String, Object> e;
			Object v;
			while(it.hasNext()){
				e = it.next();
				v=e.getValue();
				if(v==null || v.toString().isEmpty())
					it.remove();
			}
		}
		
		
		StringBuilder sb=new StringBuilder("loading felix with config:");
		Iterator<Entry<String, Object>> it = config.entrySet().iterator();
		Entry<String, Object> e;
		while(it.hasNext()){
			e=it.next();
			sb.append("\n- ").append(e.getKey()).append(':').append(e.getValue());
		}
		log(Logger.LOG_INFO,sb.toString());
		
		
		
		felix = new Felix(config);
		felix.start();

		return felix;
	}

	public void log(Throwable t) {
		if(logger!=null)
			logger.log(Logger.LOG_ERROR, "",t);
	}

	public void log(int level, String msg) {
		if(logger!=null)
			logger.log(level, msg);
	}

	private CFMLEngine getCore() throws IOException, BundleException,
			ClassNotFoundException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		File rc = new File(getTempDirectory(), "tmp_"
				+ System.currentTimeMillis() + ".lco");
		try {
			InputStream is = null;
			OutputStream os = null;
			try {
				is = new TP().getClass().getResourceAsStream("/core/core.lco");
				os = new FileOutputStream(rc);
				copy(is, os);

			} finally {
				closeEL(is);
				closeEL(os);
			}
			bundleCollection = BundleLoader.loadBundles(this, getFelixCacheDirectory(),
					getBundleDirectory(), rc, bundleCollection);
			return getEngine(bundleCollection);
		} finally {
			rc.delete();
		}
	}

	/**
	 * method to initalize a update of the CFML Engine.
	 * checks if there is a new Version and update it whwn a new version is
	 * available
	 * 
	 * @param password
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	public boolean update(Password password, Identification id) throws IOException, ServletException {
		if (!singelton.can(CFMLEngine.CAN_UPDATE, password))
			throw new IOException("access denied to update CFMLEngine");
		//new RunUpdate(this).start();
		return _update(id);
	}

	/**
	 * restart the cfml engine
	 * 
	 * @param password
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	public boolean restart(Password password) throws IOException, ServletException {
		if (!singelton.can(CFMLEngine.CAN_RESTART_ALL, password))
			throw new IOException("access denied to restart CFMLEngine");

		return _restart();
	}

	/**
	 * restart the cfml engine
	 * 
	 * @param password
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	public boolean restart(String configId, Password password)
			throws IOException, ServletException {
		if (!singelton.can(CFMLEngine.CAN_RESTART_CONTEXT, password))// TODO restart single context
			throw new IOException(
					"access denied to restart CFML Context (configId:"
							+ configId + ")");

		return _restart();
	}

	/**
	 * restart the cfml engine
	 * 
	 * @param password
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	private synchronized boolean _restart() throws ServletException {
		//engine.reset();
		initEngine();
		//registerInstance(engine); they all have only the reference to the wrapper and the wrapper does not change
		//callListeners(engine);
		System.gc();
		return true;
	}

	/**
	 * updates the engine when a update is available
	 * 
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	private boolean _update(Identification id) throws IOException, ServletException {
		File newLucee = downloadCore(id);
		if (newLucee == null)
			return false;

		/* happens in setEngine
		 try {
			singelton.reset();
		} catch (Throwable t) {
			t.printStackTrace();
		}*/

		Version v =null;
		try {

			bundleCollection = BundleLoader.loadBundles(this, getFelixCacheDirectory(),
					getBundleDirectory(), newLucee, bundleCollection);
			CFMLEngine e = getEngine(bundleCollection);
			if (e == null)
				throw new IOException("can't load engine");
			version = e.getInfo().getVersion();
			//engine = e;
			setEngine(e);
			//e.reset();
			callListeners(e);
		} catch (Exception e) {
			System.gc();
			try {
				newLucee.delete();
			} catch (Exception ee) {
			}
			log(e);
			e.printStackTrace();
			return false;
		}

		log(Logger.LOG_DEBUG,"Version (" + v + ")installed");
		return true;
	}

	public File downloadBundle(String symbolicName, String symbolicVersion,Identification id) throws IOException {
		File jarDir = getBundleDirectory();
		File jar = new File(jarDir, symbolicName.replace('.', '-') + "-"
				+ symbolicVersion.replace('.', '-') + (".jar"));

		URL updateProvider = getUpdateLocation();
		if(id==null && singelton!=null)id=singelton.getIdentification();
		
		System.out.println("download:"+symbolicName+":"+symbolicVersion); // MUST remove
		URL updateUrl = new URL(updateProvider,
				"/rest/update/provider/download/" + symbolicName + "/"
						+ symbolicVersion + "/"+(id!=null?id.toQueryString():""));

		log(Logger.LOG_DEBUG, "download bundle [" + symbolicName + ":"
				+ symbolicVersion + "] from " + updateUrl);

		int code;
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) updateUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			code = conn.getResponseCode();
		} catch (UnknownHostException e) {
			log(e);
			throw e;
		}

		if (code != 200) {
			String msg = "Lucee is not able do download the bundle for ["
					+ symbolicName + "] in version ["+symbolicVersion+"] from " + updateUrl
					+ ", please donwload manually and copy to [" + jarDir + "]";
			log(Logger.LOG_ERROR, msg);
			throw new IOException(msg);
		}

		//if(jar.createNewFile()) {	
		copy((InputStream) conn.getContent(), new FileOutputStream(jar));
		return jar;
		/*}
		else {
			throw new IOException("File ["+jar.getName()+"] already exists, won't copy new one");
		}*/
	}

	private File downloadCore(Identification id) throws IOException {
		URL updateProvider = getUpdateLocation();
		
		if(id==null && singelton!=null) id=singelton.getIdentification();
		
		
		URL infoUrl = new URL(updateProvider,"/rest/update/provider/update-for/"+version.toString()+(id!=null?id.toQueryString():""));

		log(Logger.LOG_DEBUG,"Check for update at " + updateProvider);

		String strAvailableVersion = toString(
				(InputStream) infoUrl.getContent()).trim();
		log(Logger.LOG_DEBUG,"receive available update version from update provider ("
				+ strAvailableVersion + ") ");

		strAvailableVersion = CFMLEngineFactory.removeQuotes(
				strAvailableVersion, true);
		CFMLEngineFactory.removeQuotes(strAvailableVersion, true); // not necessary but does not hurt

		if (strAvailableVersion.length() == 0
				|| !isNewerThan(toVersion(strAvailableVersion, VERSION_ZERO), version)) {
			log(Logger.LOG_DEBUG,"There is no newer Version available");
			return null;
		}

		log(Logger.LOG_DEBUG,"Found a newer Version \n - current Version "
				+ version.toString() + "\n - available Version "
				+ strAvailableVersion +("/rest/update/provider/download/" + version.toString() +(id!=null?id.toQueryString():"")));
		
		
		URL updateUrl = new URL(updateProvider,
				"/rest/update/provider/download/" + version.toString() +(id!=null?id.toQueryString():""));
		
		log(Logger.LOG_DEBUG,"download update from "+updateUrl);
		
		
		System.out.println(updateUrl);
		File patchDir = getPatchDirectory();
		File newLucee = new File(patchDir, strAvailableVersion + (".lco"));

		if (newLucee.createNewFile()) {
			copy((InputStream) updateUrl.getContent(), new FileOutputStream(
					newLucee));
		} else {
			log(Logger.LOG_DEBUG,"File for new Version already exists, won't copy new one");
			return null;
		}
		return newLucee;
	}

	public URL getUpdateLocation() throws MalformedURLException {
		URL location = singelton == null ? null : singelton.getUpdateLocation();

		// read location directly from xml
		if (location == null) {
			InputStream is = null;

			try {
				File xml = new File(getResourceRoot(),"context/lucee-server.xml");
				if(xml.exists() || xml.length()>0) {
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(xml);
					Element root = doc.getDocumentElement();
	
					NodeList children = root.getChildNodes();
	
					for (int i = children.getLength() - 1; i >= 0; i--) {
						Node node = children.item(i);
						if (node.getNodeType() == Node.ELEMENT_NODE
								&& node.getNodeName().equals("update")) {
							String loc = ((Element) node).getAttribute("location");
							if (!Util.isEmpty(loc)) {
								location = new URL(loc);
							}
						}
					}
				}
				
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				CFMLEngineFactory.closeEL(is);
			}
		}

		// if there is no lucee-server.xml
		if (location == null) location = new URL(UPDATE_LOCATION);
		
		return location;
	}

	/**
	 * method to initalize a update of the CFML Engine.
	 * checks if there is a new Version and update it whwn a new version is
	 * available
	 * 
	 * @param password
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	public boolean removeUpdate(Password password) throws IOException,
			ServletException {
		if (!singelton.can(CFMLEngine.CAN_UPDATE, password))
			throw new IOException("access denied to update CFMLEngine");
		return removeUpdate();
	}

	/**
	 * method to initalize a update of the CFML Engine.
	 * checks if there is a new Version and update it whwn a new version is
	 * available
	 * 
	 * @param password
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	public boolean removeLatestUpdate(Password password) throws IOException,
			ServletException {
		if (!singelton.can(CFMLEngine.CAN_UPDATE, password))
			throw new IOException("access denied to update CFMLEngine");
		return removeLatestUpdate();
	}

	/**
	 * updates the engine when a update is available
	 * 
	 * @return has updated
	 * @throws IOException
	 * @throws ServletException
	 */
	private boolean removeUpdate() throws IOException, ServletException {
		File patchDir = getPatchDirectory();
		File[] patches = patchDir.listFiles(new ExtensionFilter(new String[] {
				"rc", "rcs" }));

		for (int i = 0; i < patches.length; i++) {
			if (!patches[i].delete())
				patches[i].deleteOnExit();
		}
		_restart();
		return true;
	}

	private boolean removeLatestUpdate() throws IOException, ServletException {
		File patchDir = getPatchDirectory();
		File[] patches = patchDir.listFiles(new ExtensionFilter(
				new String[] { ".lco" }));
		File patch = null;
		for (int i = 0; i < patches.length; i++) {
			if (patch == null
					|| isNewerThan(toVersion(patches[i].getName(), VERSION_ZERO),
							toVersion(patch.getName(), VERSION_ZERO))) {
				patch = patches[i];
			}
		}
		if (patch != null && !patch.delete())
			patch.deleteOnExit();

		_restart();
		return true;
	}

	public String[] getInstalledPatches() throws ServletException, IOException {
		File patchDir = getPatchDirectory();
		File[] patches = patchDir.listFiles(new ExtensionFilter(
				new String[] { ".lco" }));

		List<String> list = new ArrayList<String>();
		String name;
		int extLen = "rc".length() + 1;
		for (int i = 0; i < patches.length; i++) {
			name = patches[i].getName();
			name = name.substring(0, name.length() - extLen);
			list.add(name);
		}
		String[] arr = list.toArray(new String[list.size()]);
		Arrays.sort(arr);
		return arr;
	}

	/**
	 * call all registred listener for update of the engine
	 * 
	 * @param engine
	 */
	private void callListeners(CFMLEngine engine) {
		Iterator<EngineChangeListener> it = listeners.iterator();
		while (it.hasNext()) {
			it.next().onUpdate();
		}
	}

	public File getPatchDirectory() throws IOException {
		File pd = new File(getResourceRoot(), "patches");
		if (!pd.exists())
			pd.mkdirs();
		return pd;
	}

	public File getBundleDirectory() throws IOException {
		File bd = new File(getResourceRoot(), "bundles");
		if (!bd.exists())
			bd.mkdirs();
		return bd;
	}

	public File getFelixCacheDirectory() throws IOException {
		return getResourceRoot();
		//File bd = new File(getResourceRoot(),"felix-cache");
		//if(!bd.exists())bd.mkdirs();
		//return bd;
	}

	/**
	 * return directory to lucee resource root
	 * 
	 * @return lucee root directory
	 * @throws IOException
	 */
	public File getResourceRoot() throws IOException {
		if (resourceRoot == null) {
			resourceRoot = new File(_getResourceRoot(), "lucee-server");
			if (!resourceRoot.exists())
				resourceRoot.mkdirs();
		}
		return resourceRoot;
	}

	/**
	 * @return return running context root
	 * @throws IOException
	 * @throws IOException
	 */
	private File _getResourceRoot() throws IOException {
		
		// custom configuration
		if(luceeServerRoot==null) readInitParam(config);
		if (luceeServerRoot != null) return luceeServerRoot;
		
		File root=getDirectoryByProp("lucee.base.dir"); // directory defined by the caller
		
		
		// get the root directory
		if(root==null)root=getDirectoryByProp("jboss.server.home.dir"); // Jboss/Jetty|Tomcat 
		if(root==null)root=getDirectoryByProp("jonas.base"); // Jonas
		if(root==null)root=getDirectoryByProp("catalina.base"); // Tomcat
		if(root==null)root=getDirectoryByProp("jetty.home"); // Jetty
		if(root==null)root=getDirectoryByProp("org.apache.geronimo.base.dir"); // Geronimo
		if(root==null)root=getDirectoryByProp("com.sun.aas.instanceRoot"); // Glassfish
		if(root==null)root=getDirectoryByProp("env.DOMAIN_HOME"); // weblogic
		if(root==null) root=getClassLoaderRoot(mainClassLoader).getParentFile().getParentFile();
		
		System.out.println("root dir:"+root);
		
		if(root==null)throw new IOException(
				"can't locate the root of the servlet container, please define a location (physical path) for the server configuration"
				+ " with help of the servlet init param [lucee-server-directory] in the web.xml where the Lucee Servlet is defined");
		
		File modernDir = new File(root,"lucee-server");
		if(true) {
			// there is a server context in the old lucee location, move that one
			File classicRoot = getClassLoaderRoot(mainClassLoader),classicDir;
			System.out.println("classicRoot:"+classicRoot);
			boolean had=false;
			if(classicRoot.isDirectory() && (classicDir=new File(classicRoot,"lucee-server")).isDirectory()) {
				System.out.println("had lucee-server classic"+classicDir);
				moveContent(classicDir,modernDir);
				had=true;
			}
			// there is a railo context
			if(!had && classicRoot.isDirectory() && (classicDir=new File(classicRoot,"railo-server")).isDirectory()) {
				System.out.println("had railo-server classic"+classicDir);
								// check if there is a Railo context
				copyRecursiveAndRename(classicDir,modernDir);
				// zip the railo-server di and delete it (optional)
				try {
					ZipUtil.zip(classicDir, new File(root,"railo-server-context-old.zip"));
					Util.delete(classicDir);
				}
				catch(Throwable t){t.printStackTrace();}
				//moveContent(classicDir,new File(root,"lucee-server"));
			}
		}

		return root;
	}
	
	private static void copyRecursiveAndRename(File src,File trg) throws IOException {
		if(!src.exists()) return ;
			
		if(src.isDirectory()) {
				if(!trg.exists())trg.mkdirs();
				
				File[] files = src.listFiles();
					for(int i=0;i<files.length;i++) {
						copyRecursiveAndRename(files[i],new File(trg,files[i].getName()));
					}
			}
			else if(src.isFile()) {
				if(trg.getName().endsWith(".rc") || trg.getName().startsWith(".")) {
					return;
				}
					
				if(trg.getName().equals("railo-server.xml")) {
					trg=new File(trg.getParentFile(),"lucee-server.xml");
					// cfLuceeConfiguration
					FileInputStream is = new FileInputStream(src);
						FileOutputStream os = new FileOutputStream(trg);
						try{
							String str=Util.toString(is);
							str=str.replace("<cfRailoConfiguration", "<!-- copy from Railo context --><cfLuceeConfiguration");
							str=str.replace("</cfRailoConfiguration", "</cfLuceeConfiguration");
							
							str=str.replace("<railo-configuration", "<!-- copy from Railo context --><cfLuceeConfiguration");
							str=str.replace("</railo-configuration", "</cfLuceeConfiguration");
							
							
							str=str.replace("{railo-config}", "{lucee-config}");
							str=str.replace("{railo-server}", "{lucee-server}");
							str=str.replace("{railo-web}", "{lucee-web}");
							str=str.replace("\"railo.commons.", "\"lucee.commons.");
							str=str.replace("\"railo.runtime.", "\"lucee.runtime.");
							str=str.replace("\"railo.cfx.", "\"lucee.cfx.");
							str=str.replace("/railo-context.ra", "/lucee-context.lar");
							str=str.replace("/railo-context", "/lucee");
							str=str.replace("railo-server-context", "lucee-server");
							str=str.replace("http://www.getrailo.org", "http://stable.lucee.org");
							str=str.replace("http://www.getrailo.com", "http://stable.lucee.org");
							
							
							ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());
							
							try {
						 		Util.copy(bais, os);
						 		bais.close();
						 	}
						 	finally {
						 		Util.closeEL(is, os);
						 	}
						}
						finally {
							Util.closeEL(is,os);
						}
					return;
				}
		 			
				FileInputStream is = new FileInputStream(src);
				FileOutputStream os = new FileOutputStream(trg);
				try{
					Util.copy(is, os);
				}
				finally {
					Util.closeEL(is, os);
				}
			}
		}

	private void moveContent(File src, File trg) throws IOException {
		if(src.isDirectory()) {
			File[] children = src.listFiles();
			if(children!=null)for(int i=0;i<children.length;i++){
				moveContent(children[i], new File(trg,children[i].getName()));
			}
			src.delete();
		}
		else if(src.isFile()){
			trg.getParentFile().mkdirs();
			src.renameTo(trg);
		}
	}

	private File getDirectoryByProp(String name) {
		String value=System.getProperty(name);
		if(Util.isEmpty(value,true)) return null;
		
		File dir=new File(value);
		dir.mkdirs();
		if (dir.isDirectory()) return dir;
		
		return null;
	}

	/**
	 * returns the path where the classloader is located
	 * 
	 * @param cl ClassLoader
	 * @return file of the classloader root
	 */
	public static File getClassLoaderRoot(ClassLoader cl) {
		String path = "lucee/loader/engine/CFMLEngine.class";
		URL res = cl.getResource(path);
		if(res==null) return null;
		// get file and remove all after !
		String strFile = null;
		try {
			strFile = URLDecoder.decode(res.getFile().trim(), "iso-8859-1");
		} catch (UnsupportedEncodingException e) {

		}
		int index = strFile.indexOf('!');
		if (index != -1)
			strFile = strFile.substring(0, index);

		// remove path at the end
		index = strFile.lastIndexOf(path);
		if (index != -1)
			strFile = strFile.substring(0, index);

		// remove "file:" at start and lucee.jar at the end
		if (strFile.startsWith("file:"))
			strFile = strFile.substring(5);
		if (strFile.endsWith("lucee.jar"))
			strFile = strFile.substring(0, strFile.length() - 9);

		File file = new File(strFile);
		if (file.isFile())
			file = file.getParentFile();

		return file;
	}

	/**
	 * check left value against right value
	 * 
	 * @param left
	 * @param right
	 * @return returns if right is newer than left
	 */
	private boolean isNewerThan(Version left, Version right) {
		return left.compareTo(right) > 0;
	}

	/**
	 * Load CFMl Engine Implementation (lucee.runtime.engine.CFMLEngineImpl)
	 * from a Classloader
	 * 
	 * @param bundle
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private CFMLEngine getEngine(BundleCollection bc) throws ClassNotFoundException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		
		log(Logger.LOG_DEBUG,"state:" + BundleUtil.bundleState(bc.core.getState(), ""));
		//bundle.getBundleContext().getServiceReference(CFMLEngine.class.getName());
		log(Logger.LOG_DEBUG,Constants.FRAMEWORK_BOOTDELEGATION
				+ ":"
				+ bc.getBundleContext().getProperty(
						Constants.FRAMEWORK_BOOTDELEGATION));
		log(Logger.LOG_DEBUG,"felix.cache.rootdir:"
				+ bc.getBundleContext().getProperty("felix.cache.rootdir"));

		//log(Logger.LOG_DEBUG,bc.master.loadClass(TP.class.getName()).getClassLoader().toString());
		Class<?> clazz = bc.core
				.loadClass("lucee.runtime.engine.CFMLEngineImpl");
		log(Logger.LOG_DEBUG,"class:" + clazz.getName());
		Method m = clazz.getMethod("getInstance", new Class[] {
				CFMLEngineFactory.class, BundleCollection.class });
		return (CFMLEngine) m.invoke(null, new Object[] { this, bc });

	}

	private class UpdateChecker extends Thread {
		private CFMLEngineFactory factory;
		private Identification id;

		private UpdateChecker(CFMLEngineFactory factory, Identification id) {
			this.factory = factory;
			this.id=id;
		}

		public void run() {
			long time = 10000;
			while (true) {
				try {
					sleep(time);
					time = 1000 * 60 * 60 * 24;
					factory._update(id);

				} catch (Exception e) {

				}
			}
		}
	}

}