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
package lucee.transformer.bytecode.op;

import java.util.Iterator;
import java.util.List;

import lucee.runtime.interpreter.VariableInterpreter;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.BytecodeException;
import lucee.transformer.bytecode.Position;
import lucee.transformer.bytecode.cast.CastDouble;
import lucee.transformer.bytecode.cast.CastString;
import lucee.transformer.bytecode.expression.ExprDouble;
import lucee.transformer.bytecode.expression.Expression;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.expression.var.DataMember;
import lucee.transformer.bytecode.expression.var.Member;
import lucee.transformer.bytecode.expression.var.Variable;
import lucee.transformer.bytecode.literal.LitString;
import lucee.transformer.bytecode.util.Methods;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.ArrayVisitor;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public class OPUnary extends ExpressionBase implements ExprDouble {

	public static final short POST = 1;
	public static final short PRE = 2;
	
	public static final int CONCAT = 1001314342;
	public static final int PLUS = OpDouble.PLUS;
	public static final int MINUS = OpDouble.MINUS;
	public static final int DIVIDE = OpDouble.DIVIDE;
	public static final int MULTIPLY = OpDouble.MULTIPLY;
	


	final static Method UNARY_POST_PLUS_1= new Method("unaryPoPl",
			Types.DOUBLE_VALUE, new Type[]{Types.PAGE_CONTEXT,Types.COLLECTION_KEY,Types.DOUBLE_VALUE});
	final static Method UNARY_POST_PLUS_N= new Method("unaryPoPl",
			Types.DOUBLE_VALUE, new Type[]{Types.PAGE_CONTEXT,Types.COLLECTION_KEY_ARRAY,Types.DOUBLE_VALUE});
	
	final static Method UNARY_POST_MINUS_N= new Method("unaryPoMi",
			Types.DOUBLE_VALUE, new Type[]{Types.PAGE_CONTEXT,Types.COLLECTION_KEY_ARRAY,Types.DOUBLE_VALUE});
	final static Method UNARY_POST_MINUS_1= new Method("unaryPoMi",
			Types.DOUBLE_VALUE, new Type[]{Types.PAGE_CONTEXT,Types.COLLECTION_KEY,Types.DOUBLE_VALUE});
	
	
	final static Method UNARY_PRE_PLUS_N= new Method("unaryPrPl",
			Types.DOUBLE_VALUE, new Type[]{Types.PAGE_CONTEXT,Types.COLLECTION_KEY_ARRAY,Types.DOUBLE_VALUE});
	final static Method UNARY_PRE_PLUS_1= new Method("unaryPrPl",
			Types.DOUBLE_VALUE, new Type[]{Types.PAGE_CONTEXT,Types.COLLECTION_KEY,Types.DOUBLE_VALUE});
	

	final static Method UNARY_PRE_MINUS_N= new Method("unaryPrMi",
			Types.DOUBLE_VALUE, new Type[]{Types.PAGE_CONTEXT,Types.COLLECTION_KEY_ARRAY,Types.DOUBLE_VALUE});
	final static Method UNARY_PRE_MINUS_1= new Method("unaryPrMi",
			Types.DOUBLE_VALUE, new Type[]{Types.PAGE_CONTEXT,Types.COLLECTION_KEY,Types.DOUBLE_VALUE});

	final static Method UNARY_PRE_MULTIPLY_N= new Method("unaryPrMu",
			Types.DOUBLE_VALUE, new Type[]{Types.PAGE_CONTEXT,Types.COLLECTION_KEY_ARRAY,Types.DOUBLE_VALUE});
	final static Method UNARY_PRE_MULTIPLY_1= new Method("unaryPrMu",
			Types.DOUBLE_VALUE, new Type[]{Types.PAGE_CONTEXT,Types.COLLECTION_KEY,Types.DOUBLE_VALUE});

	final static Method UNARY_PRE_DIVIDE_N= new Method("unaryPrDi",
			Types.DOUBLE_VALUE, new Type[]{Types.PAGE_CONTEXT,Types.COLLECTION_KEY_ARRAY,Types.DOUBLE_VALUE});
	final static Method UNARY_PRE_DIVIDE_1= new Method("unaryPrDi",
			Types.DOUBLE_VALUE, new Type[]{Types.PAGE_CONTEXT,Types.COLLECTION_KEY,Types.DOUBLE_VALUE});
	
	final static Method UNARY_PRE_CONCAT_N= new Method("unaryPreConcat",
			Types.STRING, new Type[]{Types.PAGE_CONTEXT,Types.COLLECTION_KEY_ARRAY,Types.STRING});
	final static Method UNARY_PRE_CONCAT_1= new Method("unaryPreConcat",
			Types.STRING, new Type[]{Types.PAGE_CONTEXT,Types.COLLECTION_KEY,Types.STRING});
	
	
	
	
	
	final static Method UNARY_POST_PLUS2= new Method("unaryPoPl",
			Types.DOUBLE_VALUE, new Type[]{Types.COLLECTION,Types.COLLECTION_KEY,Types.DOUBLE_VALUE});
	
	final static Method UNARY_POST_MINUS2= new Method("unaryPoMi",
			Types.DOUBLE_VALUE, new Type[]{Types.COLLECTION,Types.COLLECTION_KEY,Types.DOUBLE_VALUE});
	
	
	final static Method UNARY_PRE_PLUS2= new Method("unaryPrPl",
			Types.DOUBLE_VALUE, new Type[]{Types.COLLECTION,Types.COLLECTION_KEY,Types.DOUBLE_VALUE});
	

	final static Method UNARY_PRE_MINUS2= new Method("unaryPrMi",
			Types.DOUBLE_VALUE, new Type[]{Types.COLLECTION,Types.COLLECTION_KEY,Types.DOUBLE_VALUE});

	final static Method UNARY_PRE_MULTIPLY2= new Method("unaryPrMu",
			Types.DOUBLE_VALUE, new Type[]{Types.COLLECTION,Types.COLLECTION_KEY,Types.DOUBLE_VALUE});

	final static Method UNARY_PRE_DIVIDE2= new Method("unaryPrDi",
			Types.DOUBLE_VALUE, new Type[]{Types.COLLECTION,Types.COLLECTION_KEY,Types.DOUBLE_VALUE});
	
	final static Method UNARY_PRE_CONCAT2= new Method("unaryPreConcat",
			Types.STRING, new Type[]{Types.COLLECTION,Types.COLLECTION_KEY,Types.STRING});
	
	
	
	
	private final Variable var;
	private Expression value;
	private final short type;
	private final int operation;

	public OPUnary(Variable var, Expression value, short type, int operation, Position start, Position end) { 
		super(start, end);
		this.var=var;
		this.value=value;
		this.type=type;
		this.operation=operation;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws BytecodeException {
		GeneratorAdapter adapter = bc.getAdapter();
		// convert value
		if(operation==CONCAT) value=CastString.toExprString(value);
		else value=CastDouble.toExprDouble(value);
		
		List<Member> members = var.getMembers();
		int size=members.size();
		
		
		String scope=VariableInterpreter.scopeInt2String(var.getScope());
		
		/*
		 *  (susi.sorglos++ or variables.susi++)
		 */
		if((scope==null && size>1) || (scope!=null && size>0)) {
			Member last = var.removeMember(members.size()-1);
			if(!(last instanceof DataMember)) 
				throw new BytecodeException("you cannot use a unary operator with a function "+last.getClass().getName(), getStart());
			
			
			// write the variable
			var.setAsCollection(Boolean.TRUE);
			var._writeOut(bc, mode);
			
			
			// write out last Key
			Variable.registerKey(bc,((DataMember) last).getName());
			
			// write out value
			value.writeOut(bc, MODE_VALUE);

			
			if(type==POST) {
				if(operation!=OpDouble.PLUS && operation!=OpDouble.MINUS ) 
					throw new BytecodeException("Post only possible with plus or minus "+operation, value.getStart());
				
				if(operation==PLUS) adapter.invokeStatic(Types.OPERATOR, UNARY_POST_PLUS2);
				else if(operation==MINUS) adapter.invokeStatic(Types.OPERATOR, UNARY_POST_MINUS2);
			}
			else if(type==PRE) {
				
				if(operation==PLUS) adapter.invokeStatic(Types.OPERATOR, UNARY_PRE_PLUS2);
				else if(operation==MINUS) adapter.invokeStatic(Types.OPERATOR, UNARY_PRE_MINUS2);
				else if(operation==DIVIDE) adapter.invokeStatic(Types.OPERATOR, UNARY_PRE_DIVIDE2);
				else if(operation==MULTIPLY) adapter.invokeStatic(Types.OPERATOR, UNARY_PRE_MULTIPLY2);
				else if(operation==CONCAT) adapter.invokeStatic(Types.OPERATOR, UNARY_PRE_CONCAT2);
			}
			
			if(operation==CONCAT) return Types.STRING;
			
			// convert from Double to double (if necessary)
			if(mode==MODE_REF) {
	            adapter.invokeStatic(Types.CASTER,Methods.METHOD_TO_DOUBLE_FROM_DOUBLE);
	            return Types.DOUBLE;
	        }
	        return Types.DOUBLE_VALUE;
		}
		
		

		/*
		 *  undefined scope only with one key (susi++;)
		 */
		
		// PageContext instance
		adapter.loadArg(0);
		
		
		// Collection key Array
		int arrSize=scope!=null?members.size()+1:members.size();
		boolean useArray = arrSize>1 || scope!=null;
		if(useArray) {
			ArrayVisitor av=new ArrayVisitor();
			int index=0;
			av.visitBegin(adapter, Types.COLLECTION_KEY, arrSize);
				Iterator<Member> it = members.iterator();
				Member m;DataMember dm;
				
				if(scope!=null) {
					av.visitBeginItem(adapter, index++);
					Variable.registerKey(bc,LitString.toExprString(scope));
					av.visitEndItem(adapter);
				}
				
				while(it.hasNext()){
					av.visitBeginItem(adapter, index++);
					m = it.next();
					if(!(m instanceof DataMember)) throw new BytecodeException("you cannot use a unary operator with a function "+m.getClass().getName(), getStart());
					Variable.registerKey(bc,((DataMember) m).getName());
					av.visitEndItem(adapter);
				}
			av.visitEnd();
		}
		else {
			Member m = members.iterator().next();
			if(!(m instanceof DataMember)) throw new BytecodeException("you cannot use a unary operator with a function "+m.getClass().getName(), getStart());
			Variable.registerKey(bc,((DataMember) m).getName());	
		}
		
		if(type==POST) {
			if(operation!=OpDouble.PLUS && operation!=OpDouble.MINUS ) throw new BytecodeException("Post only possible with plus or minus "+operation, value.getStart());
			
			value.writeOut(bc, MODE_VALUE);
			if(operation==PLUS) adapter.invokeStatic(Types.OPERATOR, useArray?UNARY_POST_PLUS_N:UNARY_POST_PLUS_1);
			else if(operation==MINUS) adapter.invokeStatic(Types.OPERATOR, useArray?UNARY_POST_MINUS_N:UNARY_POST_MINUS_1);
		}
		else if(type==PRE) {
			value.writeOut(bc, MODE_VALUE);

			if(operation==PLUS) adapter.invokeStatic(Types.OPERATOR, useArray?UNARY_PRE_PLUS_N:UNARY_PRE_PLUS_1);
			else if(operation==MINUS) adapter.invokeStatic(Types.OPERATOR, useArray?UNARY_PRE_MINUS_N:UNARY_PRE_MINUS_1);
			else if(operation==DIVIDE) adapter.invokeStatic(Types.OPERATOR, useArray?UNARY_PRE_DIVIDE_N:UNARY_PRE_DIVIDE_1);
			else if(operation==MULTIPLY) adapter.invokeStatic(Types.OPERATOR, useArray?UNARY_PRE_MULTIPLY_N:UNARY_PRE_MULTIPLY_1);
			else if(operation==CONCAT) adapter.invokeStatic(Types.OPERATOR, useArray?UNARY_PRE_CONCAT_N:UNARY_PRE_CONCAT_1);
		}
		
		if(operation==CONCAT) return Types.STRING;
		
		// convert from double to Double (if necessary)
		if(mode==MODE_REF) {
            adapter.invokeStatic(Types.CASTER,Methods.METHOD_TO_DOUBLE_FROM_DOUBLE);
            return Types.DOUBLE;
        }
        return Types.DOUBLE_VALUE;
	}
}
