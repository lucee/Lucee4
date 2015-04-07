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
package lucee.transformer.bytecode.expression.var;

import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.BytecodeException;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.TypeScope;
import lucee.transformer.bytecode.util.Types;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public final class VariableRef extends ExpressionBase {

	
	private Variable variable;
	// Object touch (Object,String)
    /*private final static Method TOUCH =  new Method("touch",
			Types.OBJECT,
			new Type[]{Types.OBJECT,Types.STRING});*/
    // lucee.runtime.type.ref.Reference getReference (Object,String)
    /*private final static Method GET_REFERENCE =  new Method("getReference",
			Types.REFERENCE,
			new Type[]{Types.OBJECT,Types.STRING});*/

	// Object touch (Object,Key)
    private final static Method TOUCH_KEY =  new Method("touch",
			Types.OBJECT,
			new Type[]{Types.OBJECT,Types.COLLECTION_KEY});
    // lucee.runtime.type.ref.Reference getReference (Object,Key)
    private final static Method GET_REFERENCE_KEY =  new Method("getReference",
			Types.REFERENCE,
			new Type[]{Types.OBJECT,Types.COLLECTION_KEY});

	public VariableRef(Variable variable) {
		super(variable.getStart(),variable.getEnd());
		this.variable=variable;
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.expression.ExpressionBase#_writeOut(org.objectweb.asm.commons.GeneratorAdapter, int)
	 */
	public Type _writeOut(BytecodeContext bc, int mode) throws BytecodeException {
		GeneratorAdapter adapter = bc.getAdapter();
		int count=variable.countFM+variable.countDM;
		
		for(int i=0;i<=count;i++) {
    		adapter.loadArg(0);
		}
		TypeScope.invokeScope(adapter, variable.scope);
		
		boolean isLast;
		for(int i=0;i<count;i++) {
			isLast=(i+1)==count;
			Variable.registerKey(bc,((DataMember)variable.members.get(i)).getName());
			adapter.invokeVirtual(Types.PAGE_CONTEXT,isLast?GET_REFERENCE_KEY:TOUCH_KEY);
		}
		return Types.REFERENCE;
	}

	/* *
	 *
	 * @see lucee.transformer.bytecode.expression.Expression#getType()
	 * /
	public int getType() {
		return Types._OBJECT;
	}*/

}
