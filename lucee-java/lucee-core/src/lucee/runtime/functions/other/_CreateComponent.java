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

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.orm.EntityNew;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.FunctionValue;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.KeyConstants;

public class _CreateComponent {
	
	private static final Object[] EMPTY = new Object[0]; 

	public static Object call(PageContext pc , Object[] objArr) throws PageException {
		String path = Caster.toString(objArr[objArr.length-1]);
		Component cfc = CreateObject.doComponent(pc, path);
		
		// no init method
		if(!(cfc.get(KeyConstants._init,null) instanceof UDF)){
			
			if(objArr.length>1) {
				Object arg1 = objArr[0];
				if(arg1 instanceof FunctionValue) {
					Struct args=Caster.toFunctionValues(objArr,0,objArr.length-1);
					EntityNew.setPropeties(pc, cfc, args,true);
				}
				else if(Decision.isStruct(arg1)){
					Struct args=Caster.toStruct(arg1);
					EntityNew.setPropeties(pc, cfc, args,true);
				}
			}
			
			return cfc;
		}
		
		Object rtn;
		// no arguments
		if(objArr.length==1) {
			rtn = cfc.call(pc, KeyConstants._init, EMPTY);
		}	
		// named arguments
		else if(objArr[0] instanceof FunctionValue) {
			Struct args=Caster.toFunctionValues(objArr,0,objArr.length-1);
			rtn = cfc.callWithNamedValues(pc, KeyConstants._init, args);
		}
		// no name arguments
		else {
			Object[] args = new Object[objArr.length-1];
			for(int i=0;i<objArr.length-1;i++) {
				args[i]=objArr[i];
				if(args[i] instanceof FunctionValue) 
					throw new ExpressionException("invalid argument defintion,when using named parameters to a function, every parameter must have a name.");
			}
			rtn = cfc.call(pc, KeyConstants._init, args);
		}
		if(rtn==null)return cfc;
		return rtn;
	}

}
