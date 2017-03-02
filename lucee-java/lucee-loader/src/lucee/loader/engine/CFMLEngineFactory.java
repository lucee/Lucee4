/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.loader.engine;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import lucee.Version;
import lucee.loader.TP;
import lucee.loader.classloader.LuceeClassLoader;
import lucee.loader.util.ExtensionFilter;
import lucee.loader.util.Util;
import lucee.loader.util.ZipUtil;

import com.intergral.fusiondebug.server.FDControllerFactory;

/**
 * Factory to load CFML Engine
 */
public class CFMLEngineFactory {
	
	 // set to false to disable patch loading, for example in major alpha releases
	 private static final boolean PATCH_ENABLED = true;
	 
	private static CFMLEngineFactory factory;
	 private static CFMLEngineWrapper engineListener;
	 private CFMLEngine engine;
	 private ClassLoader mainClassLoader=new TP().getClass().getClassLoader();
	 private int version;
	 private List<EngineChangeListener> listeners=new ArrayList<EngineChangeListener>();
	 
	 
	 private static File luceeServerRoot;
	 private File resourceRoot;

	private PrintWriter out;
	 
	 
	 /**
	* Constructor of the class
	*/
	 protected CFMLEngineFactory(){
	 }

	 /**
	* returns instance of this factory (singelton-> always the same instance)
	* do auto update when changes occur
	* @param config 
	* @return Singelton Instance of the Factory
	* @throws ServletException 
	*/
	 public static CFMLEngine getInstance(ServletConfig config) throws ServletException {
		
		if(engineListener!=null) {
			if(factory==null) factory=engineListener.getCFMLEngineFactory();
			return engineListener;
		}
		
		if(factory==null) factory=new CFMLEngineFactory();
		
		
		// read init param from config
		factory.setInitParam(config);
		
		CFMLEngine engine = factory.getEngine();
		engine.addServletConfig(config);
		engineListener = new CFMLEngineWrapper(engine);
		
		// add listener for update
		factory.addListener(engineListener);
		return engineListener;
	 }

	 /**
	* returns instance of this factory (singelton-> always the same instance)
	* do auto update when changes occur
	* @return Singelton Instance of the Factory
	* @throws RuntimeException 
	*/
	 public static CFMLEngine getInstance() throws RuntimeException {
		if(engineListener!=null) return engineListener;
		throw new RuntimeException("engine is not initalized, you must first call getInstance(ServletConfig)");
	 }

	 /**
	* used only for internal usage
	* @param engine
	* @throws RuntimeException
	*/
	 public static void registerInstance(CFMLEngine engine) throws RuntimeException {
	 	if(factory==null) factory=engine.getCFMLEngineFactory();
	 	
	 	// first update existing listener
	 	if(engineListener!=null) {
	 		if(engineListener.equalTo(engine, true)) return;
	 		engineListener.onUpdate(engine);// perhaps this is still refrenced in the code, because of that we update it
	 		factory.removeListener(engineListener);
	 	}
	 	
	 	// now register this
	 	if(engine instanceof CFMLEngineWrapper) 
	 		engineListener=(CFMLEngineWrapper) engine;
	 	else 
	 		engineListener = new CFMLEngineWrapper(engine);
	 	
	 	factory.addListener(engineListener);	
	 }
	 
	 
	 /**
	* returns instance of this factory (singelton-> always the same instance)
	* @param config
	* @param listener 
	* @return Singelton Instance of the Factory
	* @throws ServletException 
	*/
	 public static CFMLEngine getInstance(ServletConfig config, EngineChangeListener listener) throws ServletException {
		getInstance(config);
		
		// add listener for update
		factory.addListener(listener);
		
		// read init param from config
		factory.setInitParam(config);
		
		CFMLEngine e = factory.getEngine();
		e.addServletConfig(config);
		
		// make the FDController visible for the FDClient
		FDControllerFactory.makeVisible();
		
		return e;
	 }
	 
	 void setInitParam(ServletConfig config) {
		if(luceeServerRoot!=null) return;
		
		String initParam=config.getInitParameter("lucee-server-directory");
		if(Util.isEmpty(initParam))initParam=config.getInitParameter("lucee-server-root");
		if(Util.isEmpty(initParam))initParam=config.getInitParameter("lucee-server-dir");
		if(Util.isEmpty(initParam))initParam=config.getInitParameter("lucee-server");
		
		if(Util.isEmpty(initParam))initParam=config.getInitParameter("railo-server-directory");
		if(Util.isEmpty(initParam))initParam=config.getInitParameter("railo-server-root");
		if(Util.isEmpty(initParam))initParam=config.getInitParameter("railo-server-dir");
		if(Util.isEmpty(initParam))initParam=config.getInitParameter("railo-server");
		
		
		
		
		initParam=Util.parsePlaceHolder(Util.removeQuotes(initParam,true));
		
		try {
				if(!Util.isEmpty(initParam)) {
					 File root=new File(initParam);
					 if(!root.exists()) {
						if(root.mkdirs()) {
								luceeServerRoot=root.getCanonicalFile();
								return;
						}
					 }
					 else if(root.canWrite()) {
						luceeServerRoot=root.getCanonicalFile();
						return;
					 }
				}
		}
		catch(IOException ioe){}
	 }
	 

	/**
	* adds a listener to the factory that will be informed when a new engine will be loaded.
	* @param listener
	*/
	 private void addListener(EngineChangeListener listener) {
		 if(!listeners.contains(listener)) {
			listeners.add(listener);
		 }
	 }
	 
	 private void removeListener(EngineChangeListener listener) {
	 	listeners.remove(listener);
	}

	 /**
	* @return CFML Engine
	* @throws ServletException
	*/
	 private CFMLEngine getEngine() throws ServletException {
		if(engine==null)initEngine();
		return engine;
	 }

	 private void initEngine() throws ServletException {
		
		int coreVersion=Version.getIntVersion();
		long coreCreated=Version.getCreateTime();
		
		
		// get newest lucee version as file
		File patcheDir=null;
		try {
				patcheDir = getPatchDirectory();
				log("lucee-server-root:"+patcheDir.getParent());
		} 
		catch (IOException e) {
			throw new ServletException(e);
		}
		
		File[] patches=PATCH_ENABLED?patcheDir.listFiles(new ExtensionFilter(new String[]{"."+getCoreExtension()})):null;
		File lucee=null;
		if(patches!=null) {
				for(int i=0;i<patches.length;i++) {
					 if(patches[i].getName().startsWith("tmp.lco")) {
						patches[i].delete();
					 }
					 else if(patches[i].lastModified()<coreCreated) {
						patches[i].delete();
					 }
					 else if(lucee==null || isNewerThan(Util.toInVersion(patches[i].getName()),Util.toInVersion(lucee.getName()))) {
						lucee=patches[i];
					 }
				}
		}
		
		
		if(lucee!=null && isNewerThan(coreVersion,Util.toInVersion(lucee.getName())))lucee=null;
		
		// Load Lucee
		//URL url=null;
		try {
				// Load core version when no patch available
				if(lucee==null) {
					tlog("Load Build in Core");
					 // 
					 String coreExt=getCoreExtension();
					 engine=getCore(coreExt);
					
					 
					 lucee=new File(patcheDir,engine.getVersion()+"."+coreExt);
					if(PATCH_ENABLED) {
						 InputStream bis = new TP().getClass().getResourceAsStream("/core/core."+coreExt);
						 OutputStream bos=new BufferedOutputStream(new FileOutputStream(lucee));
						 Util.copy(bis,bos);
						 Util.closeEL(bis,bos);
					 }
				}
				else {
					try {
						engine=getEngine(new LuceeClassLoader(lucee,mainClassLoader));
					}
					catch(EOFException e) {
						System.err.println("Lucee patch file "+lucee+" is invalid, please delete it");
						engine=getCore(getCoreExtension());
					}
				}
				version=Util.toInVersion(engine.getVersion());
				
				tlog("Loaded Lucee Version "+engine.getVersion());
		}
		catch(InvocationTargetException e) {
				e.getTargetException().printStackTrace();
				throw new ServletException(e.getTargetException());
		}
		catch(Exception e) {
				e.printStackTrace();
				throw new ServletException(e);
		}
		
		//check updates
		String updateType=engine.getUpdateType();
		if(updateType==null || updateType.length()==0)updateType="manuell";
		
		if(updateType.equalsIgnoreCase("auto")) {
				new UpdateChecker(this).start();
		}
		
	 }
	 

	 private String getCoreExtension()  {
	 	return "lco";
	}

	private CFMLEngine getCore(String ext) throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
	 	InputStream is = null;
	 	try {
	 		is = new TP().getClass().getResourceAsStream("/core/core."+ext);
	 		LuceeClassLoader classLoader=new LuceeClassLoader(is,mainClassLoader,ext.equalsIgnoreCase("rcs"));
	 		return getEngine(classLoader);
	 	}
	 	finally {
	 		Util.closeEL(is);
	 	}
	}

	/**
	* method to initalize a update of the CFML Engine.
	* checks if there is a new Version and update it whwn a new version is available
	* @param password
	* @return has updated
	* @throws IOException
	* @throws ServletException 
	*/
	 public boolean update(String password) throws IOException, ServletException {
		if(!engine.can(CFMLEngine.CAN_UPDATE,password))
				throw new IOException("access denied to update CFMLEngine");
		//new RunUpdate(this).start();
		return update();
	 }

	 /**
	* restart the cfml engine
	* @param password
	* @return has updated
	* @throws IOException 
	* @throws ServletException 
	*/
	 public boolean restart(String password) throws IOException, ServletException {
		if(!engine.can(CFMLEngine.CAN_RESTART_ALL,password))
				throw new IOException("access denied to restart CFMLEngine");
		
		return _restart();
	 }

	 /**
	* restart the cfml engine
	* @param password
	* @return has updated
	* @throws IOException 
	* @throws ServletException 
	*/
	 public boolean restart(String configId, String password) throws IOException, ServletException {
		if(!engine.can(CFMLEngine.CAN_RESTART_CONTEXT,password))// TODO restart single context
				throw new IOException("access denied to restart CFML Context (configId:"+configId+")");
		
		return _restart();
	 }
	 
	 /**
	* restart the cfml engine
	* @param password
	* @return has updated
	* @throws IOException 
	* @throws ServletException 
	*/
	 private synchronized boolean _restart() throws ServletException {
		engine.reset();
		initEngine();
		registerInstance(engine);
		callListeners(engine);
		System.gc(); 
		System.gc();
		return true;
	 }

	/**
	* updates the engine when a update is available
	* @return has updated
	* @throws IOException
	* @throws ServletException
	*/
	 private boolean update() throws IOException, ServletException {
	 	
		URL hostUrl=getEngine().getUpdateLocation();
		if(hostUrl==null)hostUrl=new URL("http://stable.lucee.org");
		URL infoUrl=new URL(hostUrl,"/lucee/remote/version/info.cfm?ext="+getCoreExtension()+"&version="+version);// FUTURE replace with Info.cfc or better move the functionality to core if possible. something like engine.getUpdater a class provided by the core and defined (interface) by the loader.
		
		tlog("Check for update at "+hostUrl);
		
		String availableVersion = Util.toString((InputStream)infoUrl.getContent()).trim();
		
		if(availableVersion.length()!=9) throw new IOException("can't get update info from ["+infoUrl+"]");
		if(!isNewerThan(Util.toInVersion(availableVersion),version)) {
				tlog("There is no newer Version available");
				return false;
		}
		
		tlog("Found a newer Version \n - current Version "+Util.toStringVersion(version)+"\n - available Version "+availableVersion);
		
		URL updateUrl=new URL(hostUrl,"/lucee/remote/version/update.cfm?ext="+getCoreExtension()+"&version="+availableVersion);
		File patchDir=getPatchDirectory();
		File newLucee=new File(patchDir,availableVersion+("."+getCoreExtension()));
		
		if(newLucee.createNewFile()) {
				Util.copy((InputStream)updateUrl.getContent(),new FileOutputStream(newLucee));
		}
		else {
				tlog("File for new Version already exists, won't copy new one");
				return false;
		}
		try {
		engine.reset();
		}
		catch(Throwable t) {
			if(t instanceof ThreadDeath) throw (ThreadDeath)t;
			t.printStackTrace();
		}
		
		// Test new lucee version valid
		//FileClassLoader classLoader=new FileClassLoader(newLucee,mainClassLoader);
		LuceeClassLoader classLoader=new LuceeClassLoader(newLucee,mainClassLoader);
		//URLClassLoader classLoader=new URLClassLoader(new URL[]{newLucee.toURL()},mainClassLoader);
		String v="";
		try {
				CFMLEngine e = getEngine(classLoader);
				if(e==null)throw new IOException("can't load engine");
				v=e.getVersion();
				engine=e;
				version=Util.toInVersion(v);
				//e.reset();
				callListeners(e);
		}
		catch (Exception e) {
				classLoader=null;
				System.gc();
				try {
					 newLucee.delete();
				}
				catch(Exception ee){}
				tlog("There was a Problem with the new Version, can't install ("+e+":"+e.getMessage()+")");
				e.printStackTrace();
				return false;
		}
		
		tlog("Version ("+v+")installed");
		return true;
	 }
	 
	 
	 /**
	* method to initalize a update of the CFML Engine.
	* checks if there is a new Version and update it whwn a new version is available
	* @param password
	* @return has updated
	* @throws IOException
	* @throws ServletException 
	*/
	 public boolean removeUpdate(String password) throws IOException, ServletException {
		if(!engine.can(CFMLEngine.CAN_UPDATE,password))
				throw new IOException("access denied to update CFMLEngine");
		return removeUpdate();
	 }
	 

	 /**
	* method to initalize a update of the CFML Engine.
	* checks if there is a new Version and update it whwn a new version is available
	* @param password
	* @return has updated
	* @throws IOException
	* @throws ServletException 
	*/
	 public boolean removeLatestUpdate(String password) throws IOException, ServletException {
		if(!engine.can(CFMLEngine.CAN_UPDATE,password))
				throw new IOException("access denied to update CFMLEngine");
		return removeLatestUpdate();
	 }
	 
	 
	 
	 /**
	* updates the engine when a update is available
	* @return has updated
	* @throws IOException
	* @throws ServletException
	*/
	 private boolean removeUpdate() throws IOException, ServletException {
		File patchDir=getPatchDirectory();
		File[] patches=patchDir.listFiles(new ExtensionFilter(new String[]{getCoreExtension()}));
		
		for(int i=0;i<patches.length;i++) {
			if(!patches[i].delete())patches[i].deleteOnExit();
		}
		_restart();
		return true;
	 }
	 

	 private boolean removeLatestUpdate() throws IOException, ServletException {
		File patchDir=getPatchDirectory();
		File[] patches=patchDir.listFiles(new ExtensionFilter(new String[]{"."+getCoreExtension()}));
		File patch=null;
		for(int i=0;i<patches.length;i++) {
			 if(patch==null || isNewerThan(Util.toInVersion(patches[i].getName()),Util.toInVersion(patch.getName()))) {
					patch=patches[i];
				 }
		}
	 	if(patch!=null && !patch.delete())patch.deleteOnExit();
		
		_restart();
		return true;
	 }
	 

	public String[] getInstalledPatches() throws ServletException, IOException {
		File patchDir=getPatchDirectory();
		File[] patches=patchDir.listFiles(new ExtensionFilter(new String[]{"."+getCoreExtension()}));
		
		List<String> list=new ArrayList<String>();
		String name;
		int extLen=getCoreExtension().length()+1;
		for(int i=0;i<patches.length;i++) {
			name=patches[i].getName();
			name=name.substring(0, name.length()-extLen);
			 list.add(name);
		}
		String[] arr = list.toArray(new String[list.size()]);
	 	Arrays.sort(arr);
		return arr;
	}
	 

	 /**
	* call all registred listener for update of the engine
	* @param engine
	*/
	 private void callListeners(CFMLEngine engine) {
		Iterator<EngineChangeListener> it = listeners.iterator();
		while(it.hasNext()) {
				it.next().onUpdate(engine);
		}
	 }
	 

	 private File getPatchDirectory() throws IOException {
		File pd = new File(getResourceRoot(),"patches");
		if(!pd.exists())pd.mkdirs();
		return pd;
	 }

	 /**
	* return directory to lucee resource root
	* @return lucee root directory
	* @throws IOException
	*/
	 public File getResourceRoot() throws IOException {
		if(resourceRoot==null) {
			File parent = getRuningContextRoot();
			
			resourceRoot = new File(parent,"lucee-server");

			// no lucee context
			if(!resourceRoot.exists()) {
					// check if there is a Railo context
				File railoRoot = new File(parent,"railo-server");
					if(railoRoot.exists()) {
					copyRecursiveAndRename(railoRoot,resourceRoot);
					// zip the railo-server di and delete it (optional)
					try {
						File p=railoRoot.getParentFile().getParentFile();
						if("lib".equalsIgnoreCase(p.getName()) || "libs".equalsIgnoreCase(p.getName()))
							p=p.getParentFile();
						
						ZipUtil.zip(railoRoot, new File(p,"railo-server-context-old.zip"));
						Util.delete(railoRoot);
					}
					catch(Throwable t){
						if(t instanceof ThreadDeath) throw (ThreadDeath)t;
						t.printStackTrace();
					}
				}
				else {
					resourceRoot.mkdirs();
				}
			}
		}
		return resourceRoot;
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
	 
	 
	 /**
	* @return return running context root
	* @throws IOException 
	* @throws IOException 
	*/
	 private File getRuningContextRoot() throws IOException {
		
		if(luceeServerRoot!=null) {
				return luceeServerRoot;
		}
		File dir=getClassLoaderRoot(mainClassLoader);
		dir.mkdirs();
		if(dir.exists() && dir.isDirectory()) return dir;
		
				
			
		throw new IOException("can't create/write to directory ["+dir+"], set \"init-param\" \"lucee-server-directory\" with path to writable directory");
	 }
	 /**
	* returns the path where the classloader is located
	* @param cl ClassLoader
	* @return file of the classloader root
	*/
	 public static File getClassLoaderRoot(ClassLoader cl) {
		String path="lucee/loader/engine/CFMLEngine.class";
		URL res = cl.getResource(path);
		 
		// get file and remove all after !
		String strFile=null;
		try {
			strFile = URLDecoder.decode(res.getFile().trim(),"iso-8859-1");
		} catch (UnsupportedEncodingException e) {
			
		}
		int index=strFile.indexOf('!');
		if(index!=-1)strFile=strFile.substring(0,index);
		
		// remove path at the end
		index=strFile.lastIndexOf(path);
		if(index!=-1)strFile=strFile.substring(0,index);
		
		// remove "file:" at start and lucee.jar at the end
		if(strFile.startsWith("file:"))strFile=strFile.substring(5);
		if(strFile.endsWith("lucee.jar")) strFile=strFile.substring(0,strFile.length()-9);
		
		File file=new File(strFile);
		if(file.isFile())file=file.getParentFile();
		
		return file;
	 }

	 /**
	* check left value against right value
	* @param left
	* @param right
	* @return returns if right is newer than left
	*/
	 private boolean isNewerThan(int left, int right) {
		return left>right;
	 }

	 /**
	* Load CFMl Engine Implementation (lucee.runtime.engine.CFMLEngineImpl) from a Classloader
	* @param classLoader
	* @return loaded CFML Engine
	* @throws ClassNotFoundException 
	* @throws NoSuchMethodException 
	* @throws SecurityException 
	* @throws InvocationTargetException 
	* @throws IllegalAccessException 
	* @throws IllegalArgumentException 
	*/
	 private CFMLEngine getEngine(ClassLoader classLoader) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class clazz=classLoader.loadClass("lucee.runtime.engine.CFMLEngineImpl");
		Method m = clazz.getMethod("getInstance",new Class[]{CFMLEngineFactory.class});
		return (CFMLEngine) m.invoke(null,new Object[]{this});
		
	 }

	 /**
	* log info to output
	* @param obj Object to output
	*/
	 public void tlog(Object obj) {
	 	log(new Date()+ " "+obj);
	 }
	 
	 /**
	* log info to output
	* @param obj Object to output
	*/
	 public void log(Object obj) {
	 	if(out==null){
	 		boolean isCLI=false;
	 		String str=System.getProperty("lucee.cli.call");
	 		if(!Util.isEmpty(str, true)) {
	 			str=str.trim();
	 			isCLI="true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str);
	 			
	 		}
	 		
	 		if(isCLI) {
	 			try{
	 				File dir = new File(getResourceRoot(),"logs");
	 				dir.mkdirs();
	 				File file = new File(dir,"out");
					
	 			file.createNewFile();
	 			out=new PrintWriter(file);
	 			}
	 			catch(Throwable t){
					if(t instanceof ThreadDeath) throw (ThreadDeath)t;
					t.printStackTrace();
				}
	 		}
	 		if(out==null)out=new PrintWriter(System.out);
	 	}
	 	out.write(""+obj+"\n");	
	 	out.flush();
	 }
	 
	 private class UpdateChecker extends Thread {
		private CFMLEngineFactory factory;

		private UpdateChecker(CFMLEngineFactory factory) {
				this.factory=factory;
		}
		
		public void run() {
				long time=10000;
				while(true) {
					 try {
						sleep(time);
						time=1000*60*60*24;
						factory.update();
						
					 } catch (Exception e) {
						
					 }
				}
		}
	 }

}