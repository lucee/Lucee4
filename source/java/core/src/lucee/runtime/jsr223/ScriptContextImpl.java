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
package lucee.runtime.jsr223;

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleBindings;
import javax.servlet.ServletException;

import lucee.aprint;
import lucee.runtime.CFMLFactory;
import lucee.runtime.PageContext;
import lucee.runtime.util.PageContextUtil;

public class ScriptContextImpl implements ScriptContext {
	
	private ScriptEngineFactoryImpl factory;
	private CFMLFactory cfmlFactory;
	private PageContext pc;
	private ByteArrayOutputStream baos;
	
	private Bindings engineScope;
	private Bindings globalScope;
	//private Map<Integer,Bindings> bindings=new HashMap<Integer, Bindings>();
	
	
	public ScriptContextImpl(ScriptEngineFactoryImpl factory) {
		this.factory=factory;
		pc = createPageContext();

		engineScope=new EngineBinding(pc);
		globalScope=new GlobalBinding(pc);
	}
	
	public PageContext getPageContext() {
		return pc;
	}
	
	
	@Override
	public void setBindings(Bindings bindings, int scope) {
		throw new RuntimeException("not supported");
	}

	@Override
	public Bindings getBindings(int scope) {
		if(scope==GLOBAL_SCOPE) return engineScope;
		return globalScope;
	}

	protected static Bindings createBindings() {
		return new SimpleBindings();
	}

	@Override
	public void setAttribute(String name, Object value, int scope) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getAttribute(String name, int scope) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object removeAttribute(String name, int scope) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAttributesScope(String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Writer getWriter() {
		aprint.e("getWriter");
		return null;
	}

	@Override
	public Writer getErrorWriter() {
		aprint.e("getErrorWriter");
		return null;
	}

	@Override
	public void setWriter(Writer writer) {
		aprint.e("setWriter");
		
	}

	@Override
	public void setErrorWriter(Writer writer) {
		aprint.e("setErrorWriter");
	}

	@Override
	public Reader getReader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setReader(Reader reader) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Integer> getScopes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private PageContext createPageContext() {
		try {
			return PageContextUtil.getPageContext("localhost", "/index.cfm", "", null, null, null, null, System.out, false,Long.MAX_VALUE);
		}
		catch (ServletException e) {
			throw new RuntimeException(e);
		}
		
		/*Resource dir = SystemUtil.getTempDirectory();
		HttpServletRequest req = ThreadUtil.createHttpServletRequest(dir, "localhost", "index.cfm", "", null, null, null, null, null);
		HttpServletResponse rsp = ThreadUtil.createHttpServletResponse(System.out);
		if(cfmlFactory==null) {
			ServletConfig sc = factory.engine.getServletConfigs()[0];
			try {
				cfmlFactory=factory.engine.getCFMLFactory(sc, req);
			} catch (ServletException e) {
				throw new RuntimeException(e);
			}
		}
		PageContext pc = cfmlFactory.getLuceePageContext(
				cfmlFactory.getServlet(), 
        		req, rsp, null, false, -1, false,false);
		return pc;*/
	}
}