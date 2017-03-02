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
package lucee.commons.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.db.driver.ConnectionProxy;
import lucee.runtime.db.driver.state.StateFactory;

/**
 * Utility for db
 */
public final class DBUtil {

	// TODO impl. this class, not used at the moment
	/**
	 * returns label matching className
	 * @param className
	 * @return label
	 */
	public static String getLabelForDriverClass(String className) {
		if("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(className))
			return "MSSQL DataBase";
		// TODO connect WS from lucee. ch to get more
		return className;
	}

	public static void setAutoCommitEL(Connection conn, boolean b) {
		/*try {
			if(conn!=null){
				if(conn.getAutoCommit()==b) return;
			}
		} 
		catch (Throwable e) {}*/
		
		
		try {
			
			if(conn!=null)conn.setAutoCommit(b);
        } 
        catch (Throwable t) {
        	ExceptionUtil.rethrowIfNecessary(t);
        }
	}

	public static void setReadOnlyEL(Connection conn, boolean b) {
		try {
			if(conn!=null)conn.setReadOnly(b);
		} 
		catch (Throwable t) {
        	ExceptionUtil.rethrowIfNecessary(t);
        }
	}

	public static void commitEL(Connection conn) {
		try {
			if(conn!=null)conn.commit();
		} 
		catch (Throwable t) {
        	ExceptionUtil.rethrowIfNecessary(t);
        }
	}

	public static void setTransactionIsolationEL(Connection conn,int level) {
		try {
			if(conn!=null)conn.setTransactionIsolation(level);
		} 
		catch (Throwable t) {
        	ExceptionUtil.rethrowIfNecessary(t);
        }
	}

	public static void closeEL(Statement stat) {
		if(stat!=null) {
            try {
                stat.close();
            } catch (Throwable t) {
            	ExceptionUtil.rethrowIfNecessary(t);
            }
        }
	}

	public static void closeEL(ResultSet rs) {
		if(rs!=null) {
            try {
                rs.close();
            } catch (Throwable t) {
            	ExceptionUtil.rethrowIfNecessary(t);
            }
        }
	}

	public static Connection getConnection(String connStr, String user, String pass) throws SQLException {
		try {
			//return DriverManager.getConnection(dsn, user, pass);
			return new ConnectionProxy(new StateFactory(), DriverManager.getConnection(connStr, user, pass));
        } 
        catch (SQLException e) {
		
        	if(connStr.indexOf('?')!=-1) {
                connStr=connStr+"&user="+user+"&password="+pass;
    			//return DriverManager.getConnection(connStr);
                return new ConnectionProxy(new StateFactory(), DriverManager.getConnection(connStr));
            }
        	throw e;
        }
	}

}
