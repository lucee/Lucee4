/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 * Copyright (c) 2016, Lucee Assosication Switzerland
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
package lucee.runtime.type.scope.storage;

import java.io.IOException;

import lucee.commons.io.cache.Cache;
import lucee.commons.io.log.Log;
import lucee.runtime.PageContext;
import lucee.runtime.cache.CacheConnection;
import lucee.runtime.cache.CacheUtil;
import lucee.runtime.config.Config;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.cache.Util;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.scope.ScopeContext;
import lucee.runtime.type.util.StructUtil;

/**
 * client scope that store it's data in a datasource
 */
public abstract class StorageScopeCache extends StorageScopeImpl {

	private static final long serialVersionUID = 6234854552927320080L;
	public static final long SAVE_EXPIRES_OFFSET = 60*60*1000;

	private final String cacheName;
	private final String appName;
	private final String cfid;

	private long lastModified;

	/**
	 * Constructor of the class
	 * @param pc
	 * @param name
	 * @param sct
	 * @param b 
	 */
	protected StorageScopeCache(PageContext pc,String cacheName, String appName,String strType,int type,Struct sct, long lastModified) { 
		// !!! do not store the pagecontext or config object, this object is Serializable !!!
		super(
				sct,
				doNowIfNull(pc.getConfig(),Caster.toDate(sct.get(TIMECREATED,null),false,pc.getTimeZone(),null)),
				doNowIfNull(pc.getConfig(),Caster.toDate(sct.get(LASTVISIT,null),false,pc.getTimeZone(),null)),
				-1, 
				type==SCOPE_CLIENT?Caster.toIntValue(sct.get(HITCOUNT,"1"),1):1
				,strType,type);
		
		//this.isNew=isNew;
		this.appName=appName;
		this.cacheName=cacheName;
		this.cfid=pc.getCFID();
		this.lastModified=lastModified;
	}
	
	public long lastModified() {
		return lastModified;
	}

	/**
	 * Constructor of the class, clone existing
	 * @param other
	 */
	protected StorageScopeCache(StorageScopeCache other,boolean deepCopy) {
		super(other,deepCopy);
		
		this.appName=other.appName;
		this.cacheName=other.cacheName;
		this.cfid=other.cfid;
		this.lastModified=other.lastModified;
	}
	
	private static DateTime doNowIfNull(Config config,DateTime dt) {
		if(dt==null)return new DateTimeImpl(config);
		return dt;
	}
	
	@Override
	public void touchAfterRequest(PageContext pc) {
		setTimeSpan(pc);
		super.touchAfterRequest(pc);
		//if(super.hasContent()) 
			store(pc.getConfig());
	}

	@Override
	public String getStorageType() {
		return "Cache";
	}

	@Override
	public void touchBeforeRequest(PageContext pc) {
		setTimeSpan(pc);
		super.touchBeforeRequest(pc);
	}
	
	protected static StorageValue _loadData(PageContext pc, String cacheName, String appName, String strType, Log log) throws PageException	{
		Cache cache = getCache(pc.getConfig(),cacheName);
		String key=getKey(pc.getCFID(),appName,strType);
		
		Object val = cache.getValue(key,null);
		
		if(val instanceof StorageValue) {
			ScopeContext.info(log,"load existing data from  cache ["+cacheName+"] to create "+strType+" scope for "+pc.getApplicationContext().getName()+"/"+pc.getCFID());
			return (StorageValue)val;
		}
		ScopeContext.info(log,"create new "+strType+" scope for "+pc.getApplicationContext().getName()+"/"+pc.getCFID()+" in cache ["+cacheName+"]");
		return null;
	}

	public synchronized void store(Config config) {
		try {
			Cache cache = getCache(config, cacheName);
			String key=getKey(cfid, appName, getTypeAsString());
			
			/* / merge existing data if necessary ; MARK disabled merge
			Object existing = cache.getValue(key,null);
			// cached data changed in meantime
			if(existing instanceof StorageValue  && ((StorageValue)existing).lastModified()>lastModified()) {
				Struct trg=((StorageValue)existing).getValue();
				StructUtil.copy(sct, trg, true);
				sct=trg;
			}*/

			cache.put(key, new StorageValue(sct),null,new Long(getTimeSpan()));
		} 
		catch (Exception pe) {pe.printStackTrace();}
	}
	
	
	public synchronized void unstore(Config config) {
		try {
			Cache cache = getCache(config, cacheName);
			String key=getKey(cfid, appName, getTypeAsString());
			cache.remove(key);
		} 
		catch (Exception pe) {}
	}
	

	private static Cache getCache(Config config, String cacheName) throws PageException {
		try {
			CacheConnection cc = Util.getCacheConnection(config,cacheName);
			if(!cc.isStorage()) 
				throw new ApplicationException("storage usage for this cache is disabled, you can enable this in the lucee administrator.");
			return CacheUtil.getInstance(cc,config); //cc.getInstance(config); 

		} catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}
	

	public static String getKey(String cfid, String appName, String type) {
		return new StringBuilder("lucee-storage:").append(type).append(":").append(cfid).append(":").append(appName).toString().toUpperCase();
	}
}
