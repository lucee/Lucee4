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
package lucee.commons.io.res.type.s3;

import java.io.IOException;
import java.util.Map;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceLock;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourceProviderPro;
import lucee.commons.io.res.Resources;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefInteger;
import lucee.commons.lang.types.RefIntegerImpl;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.net.s3.Properties;
import lucee.runtime.net.s3.PropertiesImpl;

public final class S3ResourceProvider implements ResourceProviderPro {
	
	
	private int socketTimeout=-1;
	private int lockTimeout=20000;
	private int cache=20000;
	private ResourceLock lock;
	private String scheme="s3";
	private Map arguments;

	

	
	/**
	 * initalize ram resource
	 * @param scheme
	 * @param arguments
	 * @return RamResource
	 */
	public ResourceProvider init(String scheme,Map arguments) {
		if(!StringUtil.isEmpty(scheme))this.scheme=scheme;
		
		if(arguments!=null) {
			this.arguments=arguments;
			// socket-timeout
			String strTimeout = (String) arguments.get("socket-timeout");
			if(strTimeout!=null) {
				socketTimeout=toIntValue(strTimeout,socketTimeout);
			}
			// lock-timeout
			strTimeout=(String) arguments.get("lock-timeout");
			if(strTimeout!=null) {
				lockTimeout=toIntValue(strTimeout,lockTimeout);
			}
			// cache
			String strCache=(String) arguments.get("cache");
			if(strCache!=null) {
				cache=toIntValue(strCache,cache);
			}
		}
		
		return this;
	}

	private int toIntValue(String str, int defaultValue) {
		try{
			return Integer.parseInt(str);
		}
		catch(Throwable t){
			return defaultValue;
		}
	}


	@Override
	public String getScheme() {
		return scheme;
	}
	
	public Resource getResource(String path) {
		path=lucee.commons.io.res.util.ResourceUtil.removeScheme(scheme, path);
		S3SDK s3 = new S3SDK();

		path=loadWithNewPattern(path);
		
		return new S3Resource(s3,this,path,true);
	}

	
	public static String loadWithNewPattern(String path) {
		PageContext pc = ThreadLocalPageContext.get();
		Properties prop=null; 
		if(pc!=null){
			prop=pc.getApplicationContext().getS3();
		}

		if(prop==null) {
			prop=new PropertiesImpl();
		}
		
		int atIndex=path.indexOf('@');
		int slashIndex=path.indexOf('/');
		if(slashIndex==-1){
			path+="/";
		}

		path=prettifyPath(path.substring(atIndex+1));
		
		return path;
	}

	private static String prettifyPath(String path) {
		path=path.replace('\\','/');
		return StringUtil.replace(path, "//", "/", false);
	}

	@Override
	public boolean isAttributesSupported() {
		return false;
	}

	@Override
	public boolean isCaseSensitive() {
		return true;
	}

	@Override
	public boolean isModeSupported() {
		return false;
	}

	@Override
	public void lock(Resource res) throws IOException {
		lock.lock(res);
	}

	@Override
	public void read(Resource res) throws IOException {
		lock.read(res);
	}

	public void setResources(Resources res) {
		lock=res.createResourceLock(lockTimeout,true);
	}

	@Override
	public void unlock(Resource res) {
		lock.unlock(res);
	}

	/**
	 * @return the socketTimeout
	 */
	public int getSocketTimeout() {
		return socketTimeout;
	}

	/**
	 * @return the lockTimeout
	 */
	public int getLockTimeout() {
		return lockTimeout;
	}

	/**
	 * @return the cache
	 */
	public int getCache() {
		return cache;
	}

	@Override
	public Map getArguments() {
		return arguments;
	}

	@Override
	public char getSeparator() {
		return '/';
	}
	

}
