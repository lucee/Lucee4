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
package lucee.loader.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lucee.loader.engine.CFMLEngineFactory;

/**
 */
public class CFMLServlet extends AbsServlet {

	private static final long serialVersionUID = -1878214660283329587L;

	/**
	 * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig sg) throws ServletException {
		super.init(sg);
		//CFMLEngineFactory.log(Log.LEVEL_INFO, "init servlet");
		try {
			engine = CFMLEngineFactory.getInstance(sg, this);
		} catch (ServletException se) {
			se.printStackTrace();// TEMP remove stacktrace
			throw se;
		} catch (Throwable t) {
			t.printStackTrace();// TEMP remove stacktrace
		}
	}

	/**
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void service(HttpServletRequest req, HttpServletResponse rsp)
			throws ServletException, IOException {
		//CFMLEngineFactory.log(Log.LEVEL_INFO, "service CFML");
		try {
			engine.serviceCFML(this, req, rsp);
		} catch (ServletException se) {
			se.printStackTrace();// TEMP remove stacktrace
			throw se;
		} catch (Throwable t) {
			t.printStackTrace();// TEMP remove stacktrace
		}

	}
}