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
package lucee.runtime.op;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lucee.commons.date.DateTimeUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.Pair;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.RemoteClient;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.SQL;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.system.ContractPath;
import lucee.runtime.net.http.HttpServletRequestDummy;
import lucee.runtime.net.http.HttpServletResponseDummy;
import lucee.runtime.spooler.ExecutionPlan;
import lucee.runtime.spooler.SpoolerTask;
import lucee.runtime.spooler.remote.RemoteClientTask;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.Date;
import lucee.runtime.type.dt.DateImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.dt.Time;
import lucee.runtime.type.dt.TimeImpl;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.scope.ClusterEntry;
import lucee.runtime.type.scope.ClusterEntryImpl;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.util.Creation;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * implemention of the ctration object
 */
public final class CreationImpl implements Creation,Serializable {

    private static CreationImpl singelton;

    private CreationImpl(CFMLEngine engine) {
    	// !!! do not store engine Object, the engine is not serializable
	}

	/**
     * @return singleton instance
     */
    public static Creation getInstance(CFMLEngine engine) { 
        if(singelton==null)singelton=new CreationImpl(engine);
        return singelton;
    }

    @Override
    public Array createArray() {
        return new ArrayImpl();
    }

	@Override
	public Array createArray(String list, String delimiter,boolean removeEmptyItem, boolean trim) {
		if(removeEmptyItem)return ListUtil.listToArrayRemoveEmpty(list, delimiter);
		if(trim)return ListUtil.listToArrayTrim(list, delimiter);
		return ListUtil.listToArray(list, delimiter);
	}
	
    @Override
    public Array createArray(int dimension) throws PageException {
        return new ArrayImpl(dimension);
    }

    @Override
    public Struct createStruct() {
        return new StructImpl();
    }

    @Override
    public Struct createStruct(int type) {
        return new StructImpl(type);
    }

    @Override
    public Query createQuery(String[] columns, int rows, String name) {
        return new QueryImpl(columns,rows,name);
    }

    @Override
    public Query createQuery(Collection.Key[] columns, int rows, String name) throws DatabaseException {
        return new QueryImpl(columns,rows,name);
    }
    
    @Override
    public Query createQuery(DatasourceConnection dc, SQL sql, int maxrow, String name) throws PageException {
		return new QueryImpl(ThreadLocalPageContext.get(),dc,sql,maxrow,-1,-1,name);
	}
    
    public Query createQuery(DatasourceConnection dc, SQL sql, int maxrow, int fetchsize, int timeout, String name) throws PageException {
		return new QueryImpl(ThreadLocalPageContext.get(),dc,sql,maxrow,fetchsize,timeout,name);
	}
    
    @Override
    public DateTime createDateTime(long time) {
        return new DateTimeImpl(time,false);
    }

    @Override
    public TimeSpan createTimeSpan(int day,int hour,int minute,int second) {
        return new TimeSpanImpl(day,hour,minute,second);
    }

    @Override
    public Date createDate(long time) {
        return new DateImpl(time);
    }

    @Override
    public Time createTime(long time) {
        return new TimeImpl(time,false);
    }

    @Override
    public DateTime createDateTime(int year, int month, int day, int hour, int minute, int second, int millis) throws ExpressionException {
        return DateTimeUtil.getInstance().toDateTime(ThreadLocalPageContext.getTimeZone(),year,month,day,hour,minute,second,millis);
    }

    @Override
    public Date createDate(int year, int month, int day) throws ExpressionException {
        return new DateImpl(DateTimeUtil.getInstance().toDateTime(null,year,month,day, 0, 0, 0,0));
    }

    @Override
    public Time createTime(int hour, int minute, int second, int millis) {
        return new TimeImpl(
        		DateTimeUtil.getInstance().toTime(null,1899,12,30,hour,minute,second,millis,0),false);
    }

    @Override
    public Document createDocument() throws PageException {
        try {
            return XMLUtil.newDocument();
        } catch (Exception e) {
            throw Caster.toPageException(e);
        }
    }

    @Override
    public Document createDocument(Resource res, boolean isHTML) throws PageException {
        InputStream is=null;
    	try {
            return XMLUtil.parse(new InputSource(is=res.getInputStream()),null,isHTML);
        } catch (Exception e) {
            throw Caster.toPageException(e);
        }
        finally {
        	IOUtil.closeEL(is);
        }
    }

    @Override
    public Document createDocument(String xml, boolean isHTML) throws PageException {
        try {
            return XMLUtil.parse(XMLUtil.toInputSource(null, xml),null,isHTML);
        } catch (Exception e) {
            throw Caster.toPageException(e);
        }
    }

    @Override
    public Document createDocument(InputStream is, boolean isHTML) throws PageException {
        try {
            return XMLUtil.parse(new InputSource(is),null,isHTML);
        } catch (Exception e) {
            throw Caster.toPageException(e);
        }
    }

	@Override
	public Key createKey(String key) {
		return KeyImpl.init(key);
	}

	public SpoolerTask createRemoteClientTask(ExecutionPlan[] plans,RemoteClient remoteClient,Struct attrColl,String callerId, String type) {
		return new RemoteClientTask(plans,remoteClient,attrColl,callerId, type);
	}

	public ClusterEntry createClusterEntry(Key key,Serializable value, int offset) {
		return new ClusterEntryImpl(key,value,offset);
	}

	public Resource createResource(String path, boolean existing) throws PageException {
		if(existing)return ResourceUtil.toResourceExisting(ThreadLocalPageContext.get(), path);
		return ResourceUtil.toResourceNotExisting(ThreadLocalPageContext.get(), path);
	}

	public HttpServletRequest createHttpServletRequest(File contextRoot,String serverName, String scriptName,String queryString, 
			Cookie[] cookies, Map<String,Object> headers, Map<String, String> parameters, Map<String,Object> attributes, HttpSession session) {

		// header
		Pair<String,Object>[] _headers=new Pair[headers.size()];
		{
			int index=0;
			Iterator<Entry<String, Object>> it = headers.entrySet().iterator();
			Entry<String, Object> entry;
			while(it.hasNext()){
				entry = it.next();
				_headers[index++]=new Pair<String,Object>(entry.getKey(), entry.getValue());
			}
		}
		// parameters
		Pair<String,Object>[] _parameters=new Pair[headers.size()];
		{
			int index=0;
			Iterator<Entry<String, String>> it = parameters.entrySet().iterator();
			Entry<String, String> entry;
			while(it.hasNext()){
				entry = it.next();
				_parameters[index++]=new Pair<String,Object>(entry.getKey(), entry.getValue());
			}
		}
		
		return new HttpServletRequestDummy(ResourceUtil.toResource(contextRoot), serverName, scriptName, queryString, cookies,
				_headers, _parameters, Caster.toStruct(attributes,null), session);
	}

	public HttpServletResponse createHttpServletResponse(OutputStream io) {
		return new HttpServletResponseDummy(io);
	}

	@Override
	public PageContext createPageContext(HttpServletRequest req, HttpServletResponse rsp, OutputStream out) {
		Config config = ThreadLocalPageContext.getConfig();
		return (PageContext) ((CFMLFactoryImpl)config.getFactory()).getPageContext(config.getFactory().getServlet(), req, rsp, null, false, -1, false);
	}

	@Override
	public Component createComponentFromName(PageContext pc, String fullName) throws PageException {
		return pc.loadComponent(fullName);
	}

	@Override
	public Component createComponentFromPath(PageContext pc, String path) throws PageException {	
		path=path.trim();
		String pathContracted=ContractPath.call(pc, path);
    	
		if(pathContracted.toLowerCase().endsWith(".cfc"))
			pathContracted=pathContracted.substring(0,pathContracted.length()-4);
		
    	pathContracted=pathContracted
			.replace(File.pathSeparatorChar, '.')
			.replace('/', '.')
			.replace('\\', '.');
    	
    	while(pathContracted.toLowerCase().startsWith("."))
			pathContracted=pathContracted.substring(1);
    	
		return createComponentFromName(pc, pathContracted);
	}


}
