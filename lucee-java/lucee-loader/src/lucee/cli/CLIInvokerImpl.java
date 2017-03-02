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
package lucee.cli;

import java.io.File;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import lucee.cli.servlet.ServletConfigImpl;
import lucee.cli.servlet.ServletContextImpl;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;

public class CLIInvokerImpl implements CLIInvoker {

	private ServletConfigImpl servletConfig;
	private CFMLEngine engine;
	private long lastAccess;

	public CLIInvokerImpl(File root, String servletName) throws ServletException{

		Map<String,Object> attributes  = new HashMap<String, Object>();
		Map<String, String> initParams = new HashMap<String, String>();

		String param = System.getProperty("lucee.cli.config");

		if (param != null && !param.isEmpty()) {

			initParams.put("lucee-web-directory",    new File(param, "lucee-web").getAbsolutePath());
			initParams.put("lucee-server-directory", new File(param).getAbsolutePath());    // will create a subfolder named lucee-server
		}
		else {

			initParams.put("lucee-server-directory", new File(root, "WEB-INF").getAbsolutePath());
		}

		ServletContextImpl servletContext = new ServletContextImpl(root, attributes, initParams, 1, 0);
		servletConfig = new ServletConfigImpl(servletContext, servletName);
		engine = CFMLEngineFactory.getInstance(servletConfig);
	}
	
	@Override
	public void invoke(Map<String, String> config) throws RemoteException {

		try {

			engine.cli(config, servletConfig);
			lastAccess = System.currentTimeMillis();
		} catch (Throwable t) {
			if(t instanceof ThreadDeath) throw (ThreadDeath)t;
			throw new RemoteException("failed to call CFML Engine", t);
		}
	}

	public long lastAccess() {
		return lastAccess;
	}

}
