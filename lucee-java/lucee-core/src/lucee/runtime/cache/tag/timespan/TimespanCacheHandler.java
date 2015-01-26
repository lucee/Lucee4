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
package lucee.runtime.cache.tag.timespan;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import lucee.runtime.PageContext;
import lucee.runtime.cache.ram.RamCache;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cache.tag.CacheHandlerFactory;
import lucee.runtime.cache.tag.CacheHandlerFilter;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.cache.tag.query.QueryCacheItem;
import lucee.runtime.cache.util.CacheKeyFilterAll;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.cache.Util;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.dt.TimeSpan;
import lucee.commons.io.cache.Cache;
import lucee.commons.io.cache.CacheEntry;

public class TimespanCacheHandler implements CacheHandler {

	private int defaultCacheType;
	private Cache defaultCache;
	
	public TimespanCacheHandler(int defaultCacheType, Cache defaultCache){
		this.defaultCacheType=defaultCacheType;
		this.defaultCache=defaultCache;
	}

	@Override
	public CacheItem get(PageContext pc, String id) {
		return CacheHandlerFactory.toCacheItem(getCache(pc).getValue(id,null),null);
	}
	
	@Override
	public boolean remove(PageContext pc, String id) {
		try {
			return getCache(pc).remove(id);
		}
		catch (IOException e) {}
		return false;
	}
	

	@Override
	public void set(PageContext pc, String id, Object cachedWithin, CacheItem value) throws PageException {
		long timeSpan;
		if(Decision.isDate(cachedWithin, false) && !(cachedWithin instanceof TimeSpan))
			timeSpan=Caster.toDate(cachedWithin, null).getTime()-System.currentTimeMillis();
		else
			timeSpan = Caster.toTimespan(cachedWithin).getMillis();
		
		// ignore timespan smaller or equal to 0
		if(timeSpan<=0) return;
		
		getCache(pc).put(id, value, Long.valueOf(timeSpan), Long.valueOf(timeSpan));
	}
	
	@Override
	public void clean(PageContext pc) {
		try{
		Cache c = getCache(pc);
		List<CacheEntry> entries = c.entries();
		if(entries.size()<100) return;
		
		Iterator<CacheEntry> it = entries.iterator();
		while(it.hasNext()){
			it.next(); // touch them to makes sure the cache remove them, not really good, cache must do this by itself
		}
		}
		catch(IOException ioe){}
	}
	

	@Override
	public void clear(PageContext pc) {
		try {
			getCache(pc).remove(CacheKeyFilterAll.getInstance());
		}
		catch (IOException e) {}
	}
	

	@Override
	public void clear(PageContext pc, CacheHandlerFilter filter) {
		clear(pc, getCache(pc), filter);
	}
	
	public static void clear(PageContext pc, Cache cache, CacheHandlerFilter filter) {
		try{
			Iterator<CacheEntry> it = cache.entries().iterator();
			CacheEntry ce;
			Object obj;
			while(it.hasNext()){
				ce = it.next();
				if(filter==null) {
					cache.remove(ce.getKey());
					continue;
				}
				
				obj=ce.getValue();
				if(obj instanceof QueryCacheItem)
					obj=((QueryCacheItem)obj).getQuery();
				if(filter.accept(obj)) 
					cache.remove(ce.getKey());
			}
		}
		catch (IOException e) {}
	}

	@Override
	public int size(PageContext pc) {
		try {
			return getCache(pc).keys().size();
		}
		catch (IOException e) {
			return 0;
		}
	}
	

	private Cache getCache(PageContext pc) {
		Cache c = Util.getDefault(pc,defaultCacheType,null);
		if(c==null) {
			if(defaultCache==null)defaultCache=new RamCache().init(0, 0, RamCache.DEFAULT_CONTROL_INTERVAL);
			return defaultCache;
		}
		return c;
	}

	@Override
	public String label() throws PageException {
		return "timespan";
	}

}
