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
package lucee.runtime.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.compress.ZipUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.util.FileWrapper;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient3.HeaderImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.extension.RHExtensionProvider;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

public class DeployHandler {

	private static final ResourceFilter ALL_EXT = new ExtensionResourceFilter(new String[]{".lex",".lar"});
	//private static final ResourceFilter ARCHIVE_EXT = new ExtensionResourceFilter(new String[]{".ra",".ras"});

	/**
	 * deploys all files found 
	 * @param config
	 */
	public static void deploy(Config config){
		if(!contextIsValid(config)) return;

		synchronized (config) {
			Resource dir = getDeployDirectory(config);
			if(!dir.exists()) dir.mkdirs();
			
			Resource[] children = dir.listResources(ALL_EXT);
			Resource child;
			String ext;
			for(int i=0;i<children.length;i++){
				child=children[i];
				try {
					// Lucee archives
					ext=ResourceUtil.getExtension(child, null);
					if("lar".equalsIgnoreCase(ext)) {
						deployArchive(config,child);
					}
					
					// Lucee Extensions
					else if("lex".equalsIgnoreCase(ext))
						XMLConfigAdmin.updateRHExtension((ConfigImpl) config, child,true);
				}
				catch (Throwable t) {
					Log log = config.getLog("deploy");
					log.error("Extension", t);
				}
			}
		}
	}

	private static boolean contextIsValid(Config config) {
		// this test is not very good but it works
		ConfigWeb[] webs;
		if(config instanceof ConfigWeb)
			webs =new ConfigWeb[]{((ConfigWeb)config)};
		else 
			webs=((ConfigServer)config).getConfigWebs();
		
		for(int i=0;i<webs.length;i++){
			try{
				ReqRspUtil.getRootPath(webs[i].getServletContext());
			}
			catch(Throwable t){
				return false;
			}
		}
		return true;
	}

	public static Resource getDeployDirectory(Config config) {
		return config.getConfigDir().getRealResource("deploy");
	}
	
	/**
	 * deploys a physical resource to the system
	 * @param config
	 * @param archive
	 * @throws PageException
	 */
	public static void deployArchive(Config config,Resource archive) throws PageException {
		Log logger = ((ConfigImpl)config).getLog("deploy");
		String type=null,virtual=null,name=null;
		boolean readOnly,topLevel,hidden,physicalFirst;
		short inspect;
		InputStream is = null;
		ZipFile file=null;
		try {
			file=new ZipFile(FileWrapper.toFile(archive));
			ZipEntry entry = file.getEntry("META-INF/MANIFEST.MF");
			
			// no manifest
			if(entry==null) {
				moveToFailedFolder(getDeployDirectory(config),archive);
				throw new ApplicationException("cannot deploy "+Constants.NAME+" Archive ["+archive+"], file is to old, the file does not have a MANIFEST.");
			}
		
			is = file.getInputStream(entry);
			Manifest manifest = new Manifest(is);
			Attributes attr = manifest.getMainAttributes();
			
			//id = unwrap(attr.getValue("mapping-id"));
			type = unwrap(attr.getValue("mapping-type"));
			virtual = unwrap(attr.getValue("mapping-virtual-path"));
			name = ListUtil.trim(virtual, "/");
			readOnly = Caster.toBooleanValue(unwrap(attr.getValue("mapping-readonly")),false);
			topLevel = Caster.toBooleanValue(unwrap(attr.getValue("mapping-top-level")),false);
			inspect = ConfigWebUtil.inspectTemplate(unwrap(attr.getValue("mapping-inspect")), Config.INSPECT_UNDEFINED);
			if(inspect==Config.INSPECT_UNDEFINED) {
				Boolean trusted = Caster.toBoolean(unwrap(attr.getValue("mapping-trusted")),null);
				if(trusted!=null) {
					if(trusted.booleanValue()) inspect=Config.INSPECT_NEVER;
					else inspect=Config.INSPECT_ALWAYS;
				}	
			}
			hidden = Caster.toBooleanValue(unwrap(attr.getValue("mapping-hidden")),false);
			physicalFirst = Caster.toBooleanValue(unwrap(attr.getValue("mapping-physical-first")),false);
		} 
		catch (Throwable t) {
			moveToFailedFolder(getDeployDirectory(config),archive);
			throw Caster.toPageException(t);
		}
		
		finally{
			IOUtil.closeEL(is);
			ZipUtil.close(file);
		}
		try {
		Resource trgDir = config.getConfigDir().getRealResource("archives").getRealResource(type).getRealResource(name);
		Resource trgFile = trgDir.getRealResource(archive.getName());
		trgDir.mkdirs();
		
		// delete existing files
		
		
			ResourceUtil.deleteContent(trgDir, null);
			ResourceUtil.moveTo(archive, trgFile,true);
			
			logger.log(Log.LEVEL_INFO,"archive","add "+type+" mapping ["+virtual+"] with archive ["+trgFile.getAbsolutePath()+"]");
			if("regular".equalsIgnoreCase(type))
				XMLConfigAdmin.updateMapping((ConfigImpl)config,virtual, null, trgFile.getAbsolutePath(), "archive", inspect, topLevel);
			else if("cfc".equalsIgnoreCase(type))
				XMLConfigAdmin.updateComponentMapping((ConfigImpl)config,virtual, null, trgFile.getAbsolutePath(), "archive", inspect);
			else if("ct".equalsIgnoreCase(type))
				XMLConfigAdmin.updateCustomTagMapping((ConfigImpl)config,virtual, null, trgFile.getAbsolutePath(), "archive", inspect);
			
			
		}
		catch (Throwable t) {
			moveToFailedFolder(getDeployDirectory(config),archive);
			throw Caster.toPageException(t);
		}
	}

	public static void moveToFailedFolder(Resource deployDirectory,Resource res) {
		Resource dir = deployDirectory.getRealResource("failed-to-deploy");
		Resource dst = dir.getRealResource(res.getName());
		dir.mkdirs();
		
		try {
			if(dst.exists()) dst.remove(true);
			ResourceUtil.moveTo(res, dst,true);
		}
		catch (Throwable t) {}
		
		// TODO Auto-generated method stub
		
	}

	private static String unwrap(String value) {
		if(value==null) return "";
		String res = unwrap(value,'"');
		if(res!=null) return res; // was double quote
		
		return unwrap(value,'\''); // try single quote unwrap, when there is no double quote.
	}
	
	private static String unwrap(String value, char del) {
		value=value.trim();
		if(StringUtil.startsWith(value, del) && StringUtil.endsWith(value, del)) {
			return value.substring(1, value.length()-1);
		}
		return value;
	}

	/**
	 * install a extension based on the given id and version
	 * @param config
	 * @param id the id of the extension
	 * @param version pass null if you don't need a specific version
	 * @throws IOException 
	 * @throws PageException 
	 */
	public static void deployExtension(Config config, String id, Log log) {
		ConfigImpl ci=(ConfigImpl) config;
		
		// is the extension already installed
		try {
			if(XMLConfigAdmin.hasRHExtensions(ci, id)!=null) return;
		} 
		catch (Throwable t) {}
		
		log.info("extension", "installing the extension "+id);
		
		//if we have that extension locally, we take it from there
		// MUST
		
		// if not we try to download it
		String apiKey = config.getIdentification().getApiKey();
		RHExtensionProvider[] providers = ci.getRHExtensionProviders();
		URL url;
		for(int i=0;i<providers.length;i++){
			try{
				url=providers[i].getURL();
				url=new URL(url,"/rest/extension/provider/full/"+id+(apiKey==null?"":"ioid="+apiKey));
				HTTPResponse rsp = HTTPEngine.get(url, null, null, -1, 0, "UTF-8", "", null, new Header[]{new HeaderImpl("accept","application/cfml")});
				if(rsp.getStatusCode()!=200)
					throw new IOException("failed to load extension with id "+id);
				
				// copy it locally
				Resource res = SystemUtil.getTempFile("lex", true);
				IOUtil.copy(rsp.getContentAsStream(), res, true);
				
				// now forward it to the regular process
				XMLConfigAdmin.updateRHExtension((ConfigImpl) config, res,true);
			}
			catch(Throwable t){
				log.error("extension", t);
			}
			
		}
		
		
		//var uri=provider&"/rest/extension/provider/"&type&"/"&id;
		
	}
	
}