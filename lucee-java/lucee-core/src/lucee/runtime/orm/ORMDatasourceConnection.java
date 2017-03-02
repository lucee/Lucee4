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
package lucee.runtime.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.db.DatasourceConnectionImpl;
import lucee.runtime.db.SQL;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;

public class ORMDatasourceConnection implements DatasourceConnection {

	private DataSource datasource;
	private ORMConnection connection;
	private Boolean supportsGetGeneratedKeys;

	public ORMDatasourceConnection(PageContext pc, ORMSession session, DataSource ds) throws PageException {
		datasource=ds;
		// this should never happen
		if(datasource==null) {
			try {
				datasource=ORMUtil.getDefaultDataSource(pc);
			}
			catch (PageException pe) {
				throw new PageRuntimeException(pe);
			}
		}
		connection=new ORMConnection(pc,session,datasource,false);
	}

	public Connection getConnection() {
		connection.begin();
		return connection;
	}
	
	public boolean isAutoCommit() throws SQLException {
		return connection.getAutoCommit();
	}
	
	public void setAutoCommit(boolean setting) throws SQLException {
		connection.setAutoCommit(setting);
	}

	@Override
	public DataSource getDatasource() {
		return datasource;
	}

	@Override
	public String getPassword() {
		return datasource.getPassword();
	}

	@Override
	public String getUsername() {
		return datasource.getUsername();
	}

	@Override
	public boolean isTimeout() {
		return false;
	}
	


	@Override
	public boolean equals(Object obj) {
		if(this==obj) return true;
		if(!(obj instanceof ORMDatasourceConnection)) return false;
		return DatasourceConnectionImpl.equals(this, (DatasourceConnection) obj);
	}

	public boolean supportsGetGeneratedKeys() {
		if(supportsGetGeneratedKeys==null){
			try {
				supportsGetGeneratedKeys=Caster.toBoolean(getConnection().getMetaData().supportsGetGeneratedKeys());
			} catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				return false;
			}
		}
		return supportsGetGeneratedKeys.booleanValue();
	}

	public PreparedStatement getPreparedStatement(SQL sql, boolean createGeneratedKeys, boolean allowCaching) throws SQLException {
		if(createGeneratedKeys)	return getConnection().prepareStatement(sql.getSQLString(),Statement.RETURN_GENERATED_KEYS);
		return getConnection().prepareStatement(sql.getSQLString());
	}

	@Override
	public PreparedStatement getPreparedStatement(SQL sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return getConnection().prepareStatement(sql.getSQLString(),resultSetType,resultSetConcurrency);
	}

	@Override
	public void close() throws SQLException {
		getConnection().close();
	}

}
