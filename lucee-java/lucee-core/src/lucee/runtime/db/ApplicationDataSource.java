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

import java.util.TimeZone;

import lucee.commons.lang.ClassException;
import lucee.runtime.config.ConfigWebFactory;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.type.Struct;

public class ApplicationDataSource extends DataSourceSupport {

	private String connStr;
	private boolean validate;

	private ApplicationDataSource(String name, String className, String connStr, String username, String password,
			boolean blob, boolean clob, int connectionLimit, int connectionTimeout, long metaCacheTimeout, TimeZone timezone, int allow, boolean storage, boolean readOnly, boolean validate) throws ClassException {
		this(name, toClass(className), connStr, username, password,
				blob, clob, connectionLimit, connectionTimeout, metaCacheTimeout, timezone, allow, storage, readOnly, validate);
	}

	private ApplicationDataSource(String name, Class clazz, String connStr, String username, String password,
			boolean blob, boolean clob, int connectionLimit, int connectionTimeout, long metaCacheTimeout, TimeZone timezone, int allow, boolean storage, boolean readOnly, boolean validate) {
		super(name, clazz,username,ConfigWebFactory.decrypt(password),
				blob,clob,connectionLimit, connectionTimeout, metaCacheTimeout, timezone, allow<0?ALLOW_ALL:allow, storage, readOnly);
		this.validate = validate;
		this.connStr = connStr;
	}
	

	public static DataSource getInstance(String name, String className, String connStr, String username, String password,
			boolean blob, boolean clob, int connectionLimit, int connectionTimeout, long metaCacheTimeout, TimeZone timezone, int allow, boolean storage, boolean readOnly, boolean validate) throws ClassException {
		
		return new ApplicationDataSource(name, className, connStr, username, password, blob, clob, connectionLimit, connectionTimeout, metaCacheTimeout, timezone, allow, storage, readOnly, validate);
	}

	@Override
	public String getDsnOriginal() {
		throw exp();
	}

	@Override
	public String getConnectionString() {
		throw exp();
	}

	@Override
	public String getDsnTranslated() {
		return getConnectionStringTranslated();
	}

	@Override
	public String getConnectionStringTranslated() {
		return connStr;
	}

	@Override
	public String getDatabase() {
		throw new PageRuntimeException(new ApplicationException("Datasource defined in the Application.cfc has no name."));
	}

	@Override
	public int getPort() {
		throw exp();
	}

	@Override
	public String getHost() {
		throw exp();
	}

	@Override
	public DataSource cloneReadOnly() {
		return new ApplicationDataSource(getName(), getClazz(), connStr, getUsername(), getPassword(),
				isBlob(), isClob(), getConnectionLimit(), getConnectionTimeout(), getMetaCacheTimeout(), getTimeZone(), allow, isStorage(), isReadOnly(), validate());
	}

	@Override
	public String getCustomValue(String key) {
		throw exp();
	}

	@Override
	public String[] getCustomNames() {
		throw exp();
	}

	@Override
	public Struct getCustoms() {
		throw exp();
	}

	@Override
	public boolean validate() {
		return validate;
	}


	private PageRuntimeException exp() {
		//return new MethodNotSupportedException();
		throw new PageRuntimeException(new ApplicationException("method not supported"));
	}
}
