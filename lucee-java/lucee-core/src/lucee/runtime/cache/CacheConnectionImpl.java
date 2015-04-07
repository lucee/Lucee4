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
package lucee.runtime.cache;

import java.io.IOException;

import lucee.commons.io.cache.Cache;
import lucee.commons.io.cache.exp.CacheException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.net.JarLoader;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.tag.Admin;
import lucee.runtime.type.Struct;


public class CacheConnectionImpl implements CacheConnection  {



		private String name;
		private Class clazz;
		private Struct custom;
		private Cache cache;
		private boolean readOnly;
		private boolean storage;

		public CacheConnectionImpl(Config config,String name, Class clazz, Struct custom, boolean readOnly, boolean storage) throws CacheException {
			this.name=name;
			this.clazz=clazz;
			if(!Reflector.isInstaneOf(clazz, Cache.class))
				throw new CacheException("class ["+clazz.getName()+"] does not implement interface ["+Cache.class.getName()+"]");
			this.custom=custom;
			this.readOnly=readOnly;
			this.storage=storage;
		}

		@Override
		public Cache getInstance(Config config) throws IOException  {
			if(cache==null){
				try{
				cache=(Cache) ClassUtil.loadInstance(clazz);
				}
				catch(NoClassDefFoundError e){
					if(!(config instanceof ConfigWeb)) throw e;
					if(JarLoader.changed((ConfigWeb)config, Admin.CACHE_JARS))
						throw new IOException(
							"cannot initialize Cache ["+clazz.getName()+"], make sure you have added all the required jar files. "+
							"GO to the Lucee Server Administrator and on the page Services/Update, click on \"Update JARs\".");
					throw new IOException(
								"cannot initialize Cache ["+clazz.getName()+"], make sure you have added all the required jar files. "+
								"if you have updated the JARs in the Lucee Administrator, please restart your Servlet Engine.");
				}
				cache.init(config,getName(), getCustom());
			}
			return cache;
		}


		@Override
		public String getName() {
			return name;
		}

		@Override
		public Class getClazz() {
			return clazz;
		}

		@Override
		public Struct getCustom() {
			return custom;
		}

		
		public String toString(){
			return "name:"+this.name+";class:"+this.clazz.getName()+";custom:"+custom+";";
		}


		@Override
		public CacheConnection duplicate(Config config) throws IOException {
			return new CacheConnectionImpl(config,name,clazz,custom,readOnly,storage);
		}


			@Override
			public boolean isReadOnly() {
				return readOnly;
			}
			public boolean isStorage() {
				return storage;
			}
	}
