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
package lucee.runtime.db;

import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.collections.map.ReferenceMap;

import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageRuntimeException;

public abstract class DataSourceSupport implements DataSourcePro, Cloneable {

    private final Class clazz;
	private final boolean blob;
    private final boolean clob;
    private final int connectionLimit;
    private final int connectionTimeout;
	private final long metaCacheTimeout;
	private final TimeZone timezone;
    private final String name;
	private final boolean storage;
    protected final int allow;
    private final boolean readOnly;
	private final String username;
	private final String password;
    
	
	private Map<String,ProcMetaCollection> procedureColumnCache;


	public DataSourceSupport(String name, Class clazz,String username, String password, boolean blob,boolean clob,int connectionLimit, int connectionTimeout, long metaCacheTimeout, TimeZone timezone, int allow, boolean storage, boolean readOnly){
		this.name=name;
        this.clazz=clazz;
		this.blob=blob;
		this.clob=clob;
		this.connectionLimit=connectionLimit;
		this.connectionTimeout=connectionTimeout;
		this.metaCacheTimeout=metaCacheTimeout;
        this.timezone=timezone;
        this.allow=allow;
        this.storage=storage;
        this.readOnly=readOnly;
        this.username=username;
        this.password=password;
	}
	
	@Override
	public Object clone() {
		return cloneReadOnly();
	}
	

	public Map<String,ProcMetaCollection> getProcedureColumnCache() {
		if(procedureColumnCache==null)
			procedureColumnCache=new ReferenceMap();
		return procedureColumnCache;
	}
	


	@Override
    public final boolean isBlob() {
        return blob;
    }

    @Override
    public final boolean isClob() {
        return clob;
    }

    @Override
    public final int getConnectionLimit() {
        return connectionLimit;
    }

    @Override
    public final int getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public final long getMetaCacheTimeout() {
		return metaCacheTimeout;
	} 
	
	@Override
	public final TimeZone getTimeZone() {
		return timezone;
	} 
    
	@Override
	public final Class getClazz() {
        return clazz;
    }

	@Override
	public final void setClazz(Class clazz) {
		throw new PageRuntimeException(new ApplicationException("this method is no longer supported")); 
        //this.clazz = clazz;
    }

	@Override
	public final String getName() {
        return name;
    }

	@Override
	public final boolean isStorage() {
		return storage;
	}
	
	@Override
	public final boolean hasAllow(int allow) {
        return (this.allow&allow)>0;
    }

	@Override
	public final boolean hasSQLRestriction() {
        return this.allow!=DataSource.ALLOW_ALL;
    }
    
	@Override
	public final boolean isReadOnly() {
        return readOnly;
    }
    
	@Override
	public String getPassword() {
        return password;
    }
	
	@Override
	public String getUsername() {
        return username;
    }

	@Override
	public boolean equals(Object obj) {
		if(this==obj)return true;
		if(!(obj instanceof DataSourcePro)) return false;
		DataSourcePro ds = (DataSourcePro)obj;
		return id().equals(ds.id());
	} 
	

	@Override
	public int hashCode() {
		return id().hashCode();
	}
	

	public String id() {
		
		return new StringBuilder(getConnectionStringTranslated())
		.append(':')
		.append(getConnectionLimit())
		.append(':')
		.append(getConnectionTimeout())
		.append(':')
		.append(getMetaCacheTimeout())
		.append(':')
		.append(getName().toLowerCase())
		.append(':')
		.append(getUsername())
		.append(':')
		.append(getPassword())
		.append(':')
		.append(getClazz().getName())
		.append(':')
		.append((getTimeZone()==null?"null":getTimeZone().getID()))
		.append(':')
		.append(isBlob())
		.append(':')
		.append(isClob())
		.append(':')
		.append(isReadOnly())
		.append(':')
		.append(isStorage()).toString();
		
	} 

    @Override
    public String toString() {
		return id();
	}
    
    public static Class toClass(String className) throws ClassException {
    	try {
			return Class.forName(className);
		} 
		catch (ClassNotFoundException e) {
			Config config = ThreadLocalPageContext.getConfig();
			if(config!=null) return ClassUtil.loadClass(config.getClassLoader(),className);
			return ClassUtil.loadClass(className);
		}
	}

}
