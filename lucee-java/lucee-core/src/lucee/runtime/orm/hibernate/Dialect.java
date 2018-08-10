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
package lucee.runtime.orm.hibernate;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.loader.util.Util;
import lucee.runtime.db.DataSource;
import lucee.runtime.type.Struct;

public class Dialect {
	private static final char CHAR_EGU = (char)233;
	private static Struct dialects=CommonUtil.createStruct();
	private static Struct dialects2=CommonUtil.createStruct();

	static {
		// if this list change, also update list in web-cfmtaglibrary_1 for "application-ormsettings-dialect"
		dialects.setEL("Cache71", org.luceehibernate.dialect.Cache71Dialect.class.getName());
		dialects.setEL("Cach"+CHAR_EGU+" 2007.1", org.luceehibernate.dialect.Cache71Dialect.class.getName());
		dialects.setEL("Cache 2007.1", org.luceehibernate.dialect.Cache71Dialect.class.getName());

		dialects.setEL("DataDirectOracle9", org.luceehibernate.dialect.DataDirectOracle9Dialect.class.getName());


		dialects.setEL("DB2390", org.luceehibernate.dialect.DB2390Dialect.class.getName());
	    dialects.setEL("DB2/390", org.luceehibernate.dialect.DB2390Dialect.class.getName());
	    dialects.setEL("DB2OS390", org.luceehibernate.dialect.DB2390Dialect.class.getName());
		dialects.setEL("DB2400", org.luceehibernate.dialect.DB2400Dialect.class.getName());
		dialects.setEL("DB2/400", org.luceehibernate.dialect.DB2400Dialect.class.getName());
		dialects.setEL("DB2AS400", org.luceehibernate.dialect.DB2400Dialect.class.getName());
		dialects.setEL("DB2", org.luceehibernate.dialect.DB2Dialect.class.getName());
		dialects.setEL("com.ddtek.jdbc.db2.DB2Driver", org.luceehibernate.dialect.DB2Dialect.class.getName());

		dialects.setEL("Derby", org.luceehibernate.dialect.DerbyDialect.class.getName());
		dialects.setEL("Firebird", org.luceehibernate.dialect.FirebirdDialect.class.getName());
		dialects.setEL("org.firebirdsql.jdbc.FBDriver", org.luceehibernate.dialect.FirebirdDialect.class.getName());
		dialects.setEL("FrontBase", org.luceehibernate.dialect.FrontBaseDialect.class.getName());

		dialects.setEL("H2", org.luceehibernate.dialect.H2Dialect.class.getName());
		dialects.setEL("org.h2.Driver", org.luceehibernate.dialect.H2Dialect.class.getName());
		dialects.setEL("H2DB", org.luceehibernate.dialect.H2Dialect.class.getName());
		dialects.setEL("HSQL", org.luceehibernate.dialect.HSQLDialect.class.getName());
		dialects.setEL("HSQLDB", org.luceehibernate.dialect.HSQLDialect.class.getName());
		dialects.setEL("org.hsqldb.jdbcDriver", org.luceehibernate.dialect.HSQLDialect.class.getName());

		dialects.setEL("Informix", org.luceehibernate.dialect.InformixDialect.class.getName());
		dialects.setEL("Ingres", org.luceehibernate.dialect.IngresDialect.class.getName());
		dialects.setEL("Interbase", org.luceehibernate.dialect.InterbaseDialect.class.getName());
		dialects.setEL("JDataStore", org.luceehibernate.dialect.JDataStoreDialect.class.getName());
		dialects.setEL("Mckoi", org.luceehibernate.dialect.MckoiDialect.class.getName());
		dialects.setEL("MckoiSQL", org.luceehibernate.dialect.MckoiDialect.class.getName());
		dialects.setEL("Mimer", org.luceehibernate.dialect.MimerSQLDialect.class.getName());
		dialects.setEL("MimerSQL", org.luceehibernate.dialect.MimerSQLDialect.class.getName());

		dialects.setEL("MySQL5", org.luceehibernate.dialect.MySQL5Dialect.class.getName());
		dialects.setEL("MySQL5InnoDB", org.luceehibernate.dialect.MySQL5InnoDBDialect.class.getName());
		dialects.setEL("MySQL5/InnoDB", org.luceehibernate.dialect.MySQL5InnoDBDialect.class.getName());
		dialects.setEL("MySQL", org.luceehibernate.dialect.MySQLDialect.class.getName());
		dialects.setEL("org.gjt.mm.mysql.Driver", org.luceehibernate.dialect.MySQLDialect.class.getName());
		dialects.setEL("MySQLInnoDB", org.luceehibernate.dialect.MySQLInnoDBDialect.class.getName());
		dialects.setEL("MySQL/InnoDB", org.luceehibernate.dialect.MySQLInnoDBDialect.class.getName());
		dialects.setEL("MySQLwithInnoDB", org.luceehibernate.dialect.MySQLInnoDBDialect.class.getName());
		dialects.setEL("MySQLMyISAM", org.luceehibernate.dialect.MySQLMyISAMDialect.class.getName());
		dialects.setEL("MySQL/MyISAM", org.luceehibernate.dialect.MySQLMyISAMDialect.class.getName());
		dialects.setEL("MySQLwithMyISAM", org.luceehibernate.dialect.MySQLMyISAMDialect.class.getName());

		dialects.setEL("Oracle10g", org.luceehibernate.dialect.Oracle10gDialect.class.getName());
		dialects.setEL("Oracle8i", org.luceehibernate.dialect.Oracle8iDialect.class.getName());
		dialects.setEL("Oracle9", org.luceehibernate.dialect.Oracle9Dialect.class.getName());
		dialects.setEL("Oracle9i", org.luceehibernate.dialect.Oracle9iDialect.class.getName());
		dialects.setEL("Oracle", org.luceehibernate.dialect.OracleDialect.class.getName());
		dialects.setEL("oracle.jdbc.driver.OracleDriver", org.luceehibernate.dialect.OracleDialect.class.getName());
		dialects.setEL("Pointbase", org.luceehibernate.dialect.PointbaseDialect.class.getName());
		dialects.setEL("PostgresPlus", org.luceehibernate.dialect.PostgresPlusDialect.class.getName());
		dialects.setEL("PostgreSQL", org.luceehibernate.dialect.PostgreSQLDialect.class.getName());
		dialects.setEL("org.postgresql.Driver", org.luceehibernate.dialect.PostgreSQLDialect.class.getName());
		dialects.setEL("Progress", org.luceehibernate.dialect.ProgressDialect.class.getName());

		dialects.setEL("SAPDB", org.luceehibernate.dialect.SAPDBDialect.class.getName());

		dialects.setEL("SQLServer", org.luceehibernate.dialect.SQLServerDialect.class.getName());
		dialects.setEL("MSSQL", org.luceehibernate.dialect.SQLServerDialect.class.getName());
		dialects.setEL("MicrosoftSQLServer", org.luceehibernate.dialect.SQLServerDialect.class.getName());
		dialects.setEL("com.microsoft.jdbc.sqlserver.SQLServerDriver", org.luceehibernate.dialect.SQLServerDialect.class.getName());

		dialects.setEL("Sybase11", org.luceehibernate.dialect.Sybase11Dialect.class.getName());
		dialects.setEL("SybaseAnywhere", org.luceehibernate.dialect.SybaseAnywhereDialect.class.getName());
		dialects.setEL("SybaseASE15", org.luceehibernate.dialect.SybaseASE15Dialect.class.getName());
		dialects.setEL("Sybase", org.luceehibernate.dialect.SybaseDialect.class.getName());

		Iterator it = dialects.entrySet().iterator();
		String value;
		Map.Entry entry;
		while(it.hasNext()){
			entry=(Entry) it.next();
			value=(String) entry.getValue();

			dialects2.setEL(CommonUtil.createKey(value), value);
			dialects2.setEL(CommonUtil.createKey(CommonUtil.last(value, '.')), value);
		}

    }

	/**
	 * return a SQL dialect that match the given Name
	 * @param name
	 * @return
	 */
	public static String getDialect(DataSource ds){
		String name=ds.getClazz().getName();
		if("net.sourceforge.jtds.jdbc.Driver".equalsIgnoreCase(name)){
			String dsn=ds.getDsnTranslated();
			if(dsn.toLowerCase().indexOf("sybase")!=-1)
				return getDialect("Sybase");
			return getDialect("SQLServer");
		}
		return getDialect(name);
	}

	public static String getDialect(String name){
		if(Util.isEmpty(name))return null;
		String dialect= (String) dialects.get(CommonUtil.createKey(name), null);
		if(dialect==null)dialect= (String) dialects2.get(CommonUtil.createKey(name), null);
		return dialect;
	}

	public static Iterator<String> getDialectNames(){
		return dialects.keysAsStringIterator();
	}
}
