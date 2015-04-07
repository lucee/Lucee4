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
package lucee.runtime.cache.eh.remote.rest;

import java.util.Date;

import lucee.commons.io.cache.CacheEntry;
import lucee.runtime.type.Struct;

public class RESTCacheEntry implements CacheEntry {

	private String key;
	private Object value;

	public RESTCacheEntry(String key, Object value) {
		this.key=key;
		this.value=value;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public Object getValue() {
		return value;
	}

	public Date created() {
		// TODO Auto-generated method stub
		return null;
	}

	public Struct getCustomInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public int hitCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long idleTimeSpan() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Date lastHit() {
		// TODO Auto-generated method stub
		return null;
	}

	public Date lastModified() {
		// TODO Auto-generated method stub
		return null;
	}

	public long liveTimeSpan() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long size() {
		// TODO Auto-generated method stub
		return 0;
	}

}
