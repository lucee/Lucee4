/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
 */
package lucee.runtime.script;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.servlet.ServletException;

import lucee.cli.servlet.ServletConfigImpl;
import lucee.cli.servlet.ServletContextImpl;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;


public abstract class BaseScriptEngineFactory implements ScriptEngineFactory {

	private final ScriptEngineFactory factory;

	public BaseScriptEngineFactory(boolean tag, int dialect) throws ServletException {
		System.setProperty("lucee.cli.call", "true");

		// returns null when not used within Lucee
		CFMLEngine engine=null;
		try {
		engine = CFMLEngineFactory.getInstance();
		}
		catch(RuntimeException re){}
		
		// create Engine
		if(engine==null) {
			String servletName="";
			Map<String, Object> attributes = new HashMap<String, Object>();
			Map<String, String> initParams = new HashMap<String, String>();
			File root = new File("."); // working directory that the java command was called from
			
			
			ServletContextImpl servletContext = new ServletContextImpl(root,attributes, initParams, 1, 0);
			ServletConfigImpl servletConfig = new ServletConfigImpl(servletContext, servletName);
			engine = CFMLEngineFactory.getInstance(servletConfig);
		}

		factory=tag?
				CFMLEngineFactory.getInstance().getTagEngineFactory(dialect)
				:CFMLEngineFactory.getInstance().getScriptEngineFactory(dialect);
	}
	
	@Override
	public String getEngineName() {
		return factory.getEngineName();
	}

	@Override
	public String getEngineVersion() {
		return factory.getEngineVersion();
	}

	@Override
	public List<String> getExtensions() {
		return factory.getExtensions();
	}

	@Override
	public List<String> getMimeTypes() {
		return factory.getMimeTypes();
	}

	@Override
	public List<String> getNames() {
		return factory.getNames();
	}

	@Override
	public String getLanguageName() {
		return factory.getLanguageName();
	}

	@Override
	public String getLanguageVersion() {
		return factory.getLanguageVersion();
	}

	@Override
	public Object getParameter(String key) {
		return factory.getParameter(key);
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		return factory.getMethodCallSyntax(obj, m, args);
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		return factory.getOutputStatement(toDisplay);
	}

	@Override
	public String getProgram(String... statements) {
		return factory.getProgram(statements);
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return factory.getScriptEngine();
	}
}