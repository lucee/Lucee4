/**
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
package lucee.transformer.cfml.evaluator.func.impl;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.bytecode.expression.Expression;
import lucee.transformer.bytecode.expression.var.Argument;
import lucee.transformer.bytecode.expression.var.BIF;
import lucee.transformer.bytecode.expression.var.NullExpression;
import lucee.transformer.bytecode.expression.var.Variable;
import lucee.transformer.cfml.evaluator.FunctionEvaluator;
import lucee.transformer.library.function.FunctionLibFunction;

public class IsNull implements FunctionEvaluator{

	public void evaluate(BIF bif, FunctionLibFunction flf) throws TemplateException {
		Argument arg = bif.getArguments()[0];
		Expression value = arg.getValue();
		
		
		
		if(value instanceof Variable){
			((Variable)value).setDefaultValue(NullExpression.NULL_EXPRESSION);
			
			
			/*try{
				ExprString exprStr=VariableString.translateVariableToExprString(value,false);
				arg.setValue(exprStr,String.class.getName());
			}
			catch(Throwable t){
				
			}*/
		}
	}

}