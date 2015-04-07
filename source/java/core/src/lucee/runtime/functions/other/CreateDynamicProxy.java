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
package lucee.runtime.functions.other;

import java.io.IOException;

import lucee.commons.lang.ClassUtil;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.bytecode.util.JavaProxyFactory;

public class CreateDynamicProxy implements Function {
	
	private static final long serialVersionUID = -1787490871697335220L;

	public static Object call(PageContext pc , Object oCFC,Object oInterfaces) throws PageException {
		try {
			return _call(pc, oCFC, oInterfaces);
		} catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}
	
	public static Object _call(PageContext pc , Object oCFC,Object oInterfaces) throws PageException, IOException {
		
		// Component
		Component cfc;
		if(oCFC instanceof Component)
			cfc= (Component)oCFC;
		else
			cfc=pc.loadComponent(Caster.toString(oCFC));
		
		// interfaces
		String[] strInterfaces;
		if(Decision.isArray(oInterfaces)) {
			strInterfaces=ListUtil.toStringArray(Caster.toArray(oInterfaces));
		}
		else {
			String list = Caster.toString(oInterfaces);
			strInterfaces=ListUtil.listToStringArray(list, ',');
		}
		strInterfaces=ListUtil.trimItems(strInterfaces);
		
		
		ClassLoader cl = ((PageContextImpl)pc).getClassLoader();
		Class[] interfaces=new Class[strInterfaces.length];
		for(int i=0;i<strInterfaces.length;i++){
			interfaces[i]=ClassUtil.loadClass(cl, strInterfaces[i]);
			if(!interfaces[i].isInterface()) throw new FunctionException(pc, "CreateDynamicProxy", 2, "interfaces", "definition ["+strInterfaces[i]+"] is a class and not a interface");
		}
		
		return JavaProxyFactory.createProxy(pc,cfc, null,interfaces);
	}
	    
}