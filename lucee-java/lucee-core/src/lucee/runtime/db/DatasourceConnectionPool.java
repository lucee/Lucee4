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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.db.DBUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefInteger;
import lucee.commons.lang.types.RefIntegerImpl;
import lucee.commons.lang.types.RefIntegerSync;
import lucee.runtime.config.Config;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ArrayUtil;

public class DatasourceConnectionPool {

	private ConcurrentHashMap<String,DCStack> dcs=new ConcurrentHashMap<String,DCStack>();
	private Map<String,RefInteger> counter=new ConcurrentHashMap<String,RefInteger>();

	public DatasourceConnection getDatasourceConnection(DataSource datasource, String user, String pass) throws PageException {
		// pc=ThreadLocalPageContext.get(pc);
		if(StringUtil.isEmpty(user)) {
            user=datasource.getUsername();
            pass=datasource.getPassword();
        }
        if(pass==null)pass="";

		// get stack
		DCStack stack=getDCStack(datasource,user,pass);


		// max connection
		int max=datasource.getConnectionLimit();

		// get an existing connection
		DatasourceConnectionImpl rtn=null;
		do {
			// we have a bad connection
			if(rtn!=null) {
				IOUtil.closeEL(rtn.getConnection());
				rtn=null;
			}
			synchronized (stack) {
				while(max!=-1 && max<=_size(datasource)) {
					try {
						//stack.inc();
						stack.wait(10000L);

					}
					catch (InterruptedException e) {
						throw Caster.toPageException(e);
					}
				}

				while(!stack.isEmpty()) {
					DatasourceConnectionImpl dc=(DatasourceConnectionImpl) stack.get();
					if(dc!=null){
						rtn=dc;
						break;
					}
				}
			}
		}while(rtn!=null && !isValid(rtn,Boolean.TRUE));

		// create a new connection
		if(rtn==null)
			rtn=loadDatasourceConnection(datasource, user, pass);

		synchronized (stack) {
			_inc(datasource);
		}
		return rtn.using();
	}

	private DatasourceConnectionImpl loadDatasourceConnection(DataSource ds, String user, String pass) throws DatabaseException  {
        Connection conn=null;
        String connStr = ds.getDsnTranslated();
        try {
        	conn = DBUtil.getConnection(connStr, user, pass);
        	conn.setAutoCommit(true);
        }
        catch (SQLException e) {
        	throw new DatabaseException(e,null);
        }
		//print.err("create connection");
        return new DatasourceConnectionImpl(conn,ds,user,pass);
    }

	public void releaseDatasourceConnection(Config config,DatasourceConnection dc, boolean async) {
		releaseDatasourceConnection(dc,false);
		//if(async)((SpoolerEngineImpl)config.getSpoolerEngine()).add((DatasourceConnectionImpl)dc);
		//else releaseDatasourceConnection(dc);
	}
	public void releaseDatasourceConnection(Config config,DatasourceConnection dc, boolean async, boolean closeIt) {
		releaseDatasourceConnection(dc,closeIt);
		//if(async)((SpoolerEngineImpl)config.getSpoolerEngine()).add((DatasourceConnectionImpl)dc);
		//else releaseDatasourceConnection(dc);
	}

	public void releaseDatasourceConnection(DatasourceConnection dc) {
		releaseDatasourceConnection(dc, false);
	}

	public void releaseDatasourceConnection(DatasourceConnection dc, boolean closeIt) {
		if(dc==null) return;
		DCStack stack=getDCStack(dc.getDatasource(), dc.getUsername(), dc.getPassword());
		synchronized (stack) {
			if(closeIt) IOUtil.closeEL(dc.getConnection());
			else stack.add(dc);
			int max = dc.getDatasource().getConnectionLimit();

			if(max!=-1) {
				_dec(dc.getDatasource());
				stack.notify();

			}
			else _dec(dc.getDatasource());
		}
	}

	public void clear() {
		//int size=0;

		// remove all timed out conns
		try{
			Object[] arr = dcs.entrySet().toArray();
			if(ArrayUtil.isEmpty(arr)) return;
			for(int i=0;i<arr.length;i++) {
				DCStack conns=(DCStack) ((Map.Entry) arr[i]).getValue();
				if(conns!=null)conns.clear();
				//size+=conns.size();
			}
		}
		catch(Throwable t){
        	ExceptionUtil.rethrowIfNecessary(t);
        }
	}

	public void remove(DataSource datasource) {
		Object[] arr = dcs.keySet().toArray();
		String key,id=createId(datasource);
        for(int i=0;i<arr.length;i++) {
        	key=(String) arr[i];
        	if(key.startsWith(id)) {
				DCStack conns=dcs.get(key);
				conns.clear();
        	}
		}

        RefInteger ri=counter.get(id);
		if(ri!=null)ri.setValue(0);
		else counter.put(id,new RefIntegerSync(0));

	}



	public static boolean isValid(DatasourceConnection dc,Boolean autoCommit) {
		try {
			if(dc.getConnection().isClosed())return false;
		}
		catch (Throwable t) {
        	ExceptionUtil.rethrowIfNecessary(t);
        	return false;
        }

		try {
			if(dc.getDatasource().validate() && !DataSourceUtil.isValid(dc,10))return false;
		}
		catch (Throwable t) {
        	ExceptionUtil.rethrowIfNecessary(t);
        } // not all driver support this, because of that we ignore a error here, also protect from java 5


		try {
			if(autoCommit!=null) dc.getConnection().setAutoCommit(autoCommit.booleanValue());
		}
		catch (Throwable t) {
        	ExceptionUtil.rethrowIfNecessary(t);
        	return false;
        }


		return true;
	}


	private DCStack getDCStack(DataSource datasource, String user, String pass) {
		String id = createId(datasource,user,pass);
		synchronized(id) {
			DCStack stack=dcs.get(id);

			if(stack==null){
				dcs.put(id, stack=new DCStack());
			}
			return stack;
		}
	}

	public int openConnections() {
		Iterator<DCStack> it = dcs.values().iterator();
		int count=0;
		while(it.hasNext()){
			count+=it.next().openConnections();
		}
		return count;
	}

	private void _inc(DataSource datasource) {
		_getCounter(datasource).plus(1);
	}
	private void _dec(DataSource datasource) {
		_getCounter(datasource).minus(1);
	}
	private int _size(DataSource datasource) {
		return _getCounter(datasource).toInt();
	}

	private RefInteger _getCounter(DataSource datasource) {
		String did = createId(datasource);
		RefInteger ri=counter.get(did);
		if(ri==null) {
			counter.put(did,ri=new RefIntegerSync(0));
		}
		return ri;
	}

	public static String createId(DataSource datasource, String user, String pass) {
		return createId(datasource)+":"+user+":"+pass;
	}
	private static String createId(DataSource datasource) {
		if(datasource instanceof DataSourcePro) return ((DataSourceSupport)datasource).id();
		return datasource.getClazz().getName()+":"+datasource.getDsnTranslated()+":"+datasource.getClazz().getName();
	}
}
