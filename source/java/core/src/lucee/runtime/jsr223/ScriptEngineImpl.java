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

import java.io.IOException;
import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import lucee.commons.io.IOUtil;
import lucee.runtime.PageContext;
import lucee.runtime.compiler.Renderer;
import lucee.runtime.compiler.Renderer.Result;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;


public class ScriptEngineImpl implements ScriptEngine {

	private ScriptEngineFactoryImpl factory;
	private ScriptContextImpl context;
	//private Map<Integer,Bindings> bindings=new HashMap<Integer, Bindings>();
	
	public ScriptEngineImpl(ScriptEngineFactoryImpl factory) {
		this.factory=factory;
	}

	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
		ScriptContextImpl sc = context!=null?(ScriptContextImpl)context: getContext();
		PageContext oldPC = ThreadLocalPageContext.get();
		PageContext pc;
		try{
			ThreadLocalPageContext.register(pc=sc.getPageContext());
			Result res = factory.tag?Renderer.tag(pc, script,factory.dialect):Renderer.script(pc, script,factory.dialect);
			return res.getValue();
		}
		catch (PageException pe) {
			throw toScriptException(pe);
		}
		finally{
			ThreadLocalPageContext.release();
			if(oldPC!=null)ThreadLocalPageContext.register(oldPC);
		}
	}

	/*private PageContext registerPageContext(ScriptContext context) {
		PageContext pc = ((ScriptContextImpl)context).getPageContext();
		ThreadLocalPageContext.register(pc);
		return pc;
	}*/

	@Override
	public ScriptContextImpl getContext() {
		if(context==null) context = new ScriptContextImpl(factory);
		return context;
	}
	@Override
	public void setContext(ScriptContext context) {
		this.context=(ScriptContextImpl) context;
	}
	
	@Override
	public Bindings createBindings() {
		return ScriptContextImpl.createBindings();
		//return new SimpleBindings();
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return factory;
	}

	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		
		try {
			return eval(IOUtil.toString(reader),context);
		} catch (IOException ioe) {
			throw toScriptException(ioe);
		}
	}

	@Override
	public Object eval(String script) throws ScriptException {
		return eval(script,getContext());
	}

	@Override
	public Object eval(Reader reader) throws ScriptException {
		return eval(reader,getContext());
	}

	@Override
	public Object eval(String script, Bindings n) throws ScriptException { // TODO
		return eval(script,getContext());
	}

	@Override
	public Object eval(Reader reader, Bindings n) throws ScriptException {// TODO
		return eval(reader,getContext());
	}

	@Override
	public void put(String key, Object value) {
		ScriptContextImpl sc = getContext();
		Bindings b = sc.getBindings(ScriptContext.ENGINE_SCOPE);
		PageContext oldPC = ThreadLocalPageContext.get();
		if (b != null) {
			try{
				ThreadLocalPageContext.register(sc.getPageContext());
				b.put(key, value);
			}
			finally{
				ThreadLocalPageContext.release();
				if(oldPC!=null)ThreadLocalPageContext.register(oldPC);
			}
		}
	}

	@Override
	public Object get(String key) {
		ScriptContextImpl sc = getContext();
		Bindings b = sc.getBindings(ScriptContext.ENGINE_SCOPE);
		PageContext oldPC = ThreadLocalPageContext.get();
		if (b != null) {
			try{
				ThreadLocalPageContext.register(sc.getPageContext());
				return b.get(key);
			}
			finally{
				ThreadLocalPageContext.release();
				if(oldPC!=null)ThreadLocalPageContext.register(oldPC);
			}
		}
		return null;
	}

	@Override
	public Bindings getBindings(int scope) {
		return getContext().getBindings(scope);
	}

	@Override
	public void setBindings(Bindings bindings, int scope) {
		getContext().setBindings(bindings, scope);
	}
	

	
	private ScriptException toScriptException(Exception e) {
		ScriptException se = new ScriptException(e);
		se.setStackTrace(e.getStackTrace());
		return se;
	}
}