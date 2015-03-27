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

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.ScopeFactory;
import lucee.runtime.type.scope.ScopeSupport;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.BytecodeException;
import lucee.transformer.bytecode.Position;
import lucee.transformer.bytecode.expression.Expression;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.literal.LitString;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.TypeScope;
import lucee.transformer.bytecode.util.Types;

public class Assign extends ExpressionBase {

	//final static Method[] METHODS_SCOPE_SET = new Method[6];
	


//  java.lang.Object set(String,Object)
    /*private final static Method METHOD_SCOPE_SET = new Method("set",
			Types.OBJECT,
			new Type[]{Types.STRING,Types.OBJECT});*/
    
//  java.lang.Object set(String,Object)
    private final static Method METHOD_SCOPE_SET_KEY = new Method("set",
			Types.OBJECT,
			new Type[]{Types.COLLECTION_KEY,Types.OBJECT});
	
// .setArgument(obj)
    private final static Method SET_ARGUMENT = new Method("setArgument",
			Types.OBJECT,
			new Type[]{Types.OBJECT});
    
    
    // Object touch (Object,String)
    /*private final static Method TOUCH = new Method("touch",
			Types.OBJECT,
			new Type[]{Types.OBJECT,Types.STRING});*/

    // Object touch (Object,String)
    private final static Method TOUCH_KEY = new Method("touch",
			Types.OBJECT,
			new Type[]{Types.OBJECT,Types.COLLECTION_KEY});
    

    //Object set (Object,String,Object)
    private final static Method SET_KEY = new Method("set",
			Types.OBJECT,
			new Type[]{Types.OBJECT,Types.COLLECTION_KEY,Types.OBJECT});
    
    //Object set (Object,String,Object)
    /*private final static Method SET = new Method("set",
			Types.OBJECT,
			new Type[]{Types.OBJECT,Types.STRING,Types.OBJECT});*/

    // Object getFunction (Object,String,Object[])
    /*private final static Method GET_FUNCTION = new Method("getFunction",
			Types.OBJECT,
			new Type[]{Types.OBJECT,Types.STRING,Types.OBJECT_ARRAY});*/
    
    // Object getFunctionWithNamedValues (Object,String,Object[])
    /*private final static Method GET_FUNCTION_WITH_NAMED_ARGS = new Method("getFunctionWithNamedValues",
			Types.OBJECT,
			new Type[]{Types.OBJECT,Types.STRING,Types.OBJECT_ARRAY});*/
	

    // Object getFunction (Object,String,Object[])
    private final static Method GET_FUNCTION_KEY = new Method("getFunction",
			Types.OBJECT,
			new Type[]{Types.OBJECT,Types.COLLECTION_KEY,Types.OBJECT_ARRAY});
    
    // Object getFunctionWithNamedValues (Object,String,Object[])
    private final static Method GET_FUNCTION_WITH_NAMED_ARGS_KEY = new Method("getFunctionWithNamedValues",
			Types.OBJECT,
			new Type[]{Types.OBJECT,Types.COLLECTION_KEY,Types.OBJECT_ARRAY});
	
	
	private Variable variable;
	private Expression value;


	/**
	 * Constructor of the class
	 * @param variable
	 * @param value
	 */
	public Assign(Variable variable, Expression value, Position end) {
		super(variable.getStart(),end);
		this.variable=variable;
		this.value=value;
		//this.returnOldValue=returnOldValue;
	}
	

	public Type _writeOut(BytecodeContext bc, int mode) throws BytecodeException {
    	GeneratorAdapter adapter = bc.getAdapter();
		int count=variable.countFM+variable.countDM;
        // count 0
        if(count==0){
        	if(variable.ignoredFirstMember() && variable.scope==ScopeSupport.SCOPE_VAR){
    			//print.dumpStack();
        		return Types.VOID;
    		}
        	return _writeOutEmpty(bc);
        }
        
        boolean doOnlyScope=variable.scope==Scope.SCOPE_LOCAL;
    	
    	Type rtn=Types.OBJECT;
    	//boolean last;
    	for(int i=doOnlyScope?0:1;i<count;i++) {
			adapter.loadArg(0);
    	}
		rtn=_writeOutFirst(bc, (variable.members.get(0)),mode,count==1,doOnlyScope);
    	
		// pc.get(
		for(int i=doOnlyScope?0:1;i<count;i++) {
			Member member=(variable.members.get(i));
			boolean last=(i+1)==count;
			
			
			// Data Member
			if(member instanceof DataMember)	{
				Variable.registerKey(bc, ((DataMember)member).getName());
				
    			if(last)value.writeOut(bc, MODE_REF);
    			adapter.invokeVirtual(Types.PAGE_CONTEXT,last?SET_KEY:TOUCH_KEY);
    			rtn=Types.OBJECT;
			}
			
			// UDF
			else if(member instanceof UDF) {
				if(last)throw new BytecodeException("can't asign value to a user defined function",getStart());
				UDF udf=(UDF) member;
				Variable.registerKey(bc, udf.getName());
				//udf.getName().writeOut(bc, MODE_REF);
				ExpressionUtil.writeOutExpressionArray(bc, Types.OBJECT, udf.getArguments());
				
				adapter.invokeVirtual(Types.PAGE_CONTEXT,udf.hasNamedArgs()?GET_FUNCTION_WITH_NAMED_ARGS_KEY:GET_FUNCTION_KEY);
				rtn=Types.OBJECT;
			}
    	}
    	return rtn;
	}

	private Type _writeOutFirst(BytecodeContext bc, Member member, int mode, boolean last, boolean doOnlyScope) throws BytecodeException {
		
		if(member instanceof DataMember) {
			return _writeOutOneDataMember(bc,(DataMember)member,last,doOnlyScope);
			//return Variable._writeOutFirstDataMember(adapter,(DataMember)member,variable.scope, last);
		}
    	else if(member instanceof UDF) {
    		if(last)throw new BytecodeException("can't assign value to a user defined function",getStart());
    		return Variable._writeOutFirstUDF(bc,(UDF)member,variable.scope,doOnlyScope);
    	}
    	else {
    		if(last)throw new BytecodeException("can't assign value to a built in function",getStart());
    		return Variable._writeOutFirstBIF(bc,(BIF)member,mode,last,getStart());
    	}
	}



	private Type _writeOutOneDataMember(BytecodeContext bc, DataMember member,boolean last, boolean doOnlyScope) throws BytecodeException {
    	GeneratorAdapter adapter = bc.getAdapter();
		
    	if(doOnlyScope){
    		adapter.loadArg(0);
    		if(variable.scope==Scope.SCOPE_LOCAL){
    			return TypeScope.invokeScope(adapter, TypeScope.METHOD_LOCAL_TOUCH,Types.PAGE_CONTEXT);
    		}
    		return TypeScope.invokeScope(adapter, variable.scope);
    	}
    	
    	// pc.get
		adapter.loadArg(0);
		if(last) {
			TypeScope.invokeScope(adapter, variable.scope);
			Variable.registerKey(bc, member.getName());
			value.writeOut(bc, MODE_REF);
			adapter.invokeInterface(TypeScope.SCOPES[variable.scope],METHOD_SCOPE_SET_KEY);
		}
		else {
			adapter.loadArg(0);
			TypeScope.invokeScope(adapter, variable.scope);
			Variable.registerKey(bc, member.getName());
			adapter.invokeVirtual(Types.PAGE_CONTEXT,TOUCH_KEY);
		}
		return Types.OBJECT;
		
		
	}

	private Type _writeOutEmpty(BytecodeContext bc) throws BytecodeException {
		GeneratorAdapter adapter = bc.getAdapter();

		if(variable.scope==Scope.SCOPE_ARGUMENTS) {
			adapter.loadArg(0);
			TypeScope.invokeScope(adapter, Scope.SCOPE_ARGUMENTS);
			value.writeOut(bc, MODE_REF);
			adapter.invokeInterface(TypeScope.SCOPE_ARGUMENT,SET_ARGUMENT);
		}
		else {
			adapter.loadArg(0);
			TypeScope.invokeScope(adapter, Scope.SCOPE_UNDEFINED);
			Variable.registerKey(bc,LitString.toExprString(ScopeFactory.toStringScope(variable.scope,"undefined")));
			value.writeOut(bc, MODE_REF);
			adapter.invokeInterface(TypeScope.SCOPES[Scope.SCOPE_UNDEFINED],METHOD_SCOPE_SET_KEY);
		}
		
		
		return Types.OBJECT;
	}

	/**
	 * @return the value
	 */
	public Expression getValue() {
		return value;
	}


	/**
	 * @return the variable
	 */
	public Variable getVariable() {
		return variable;
	}
}
