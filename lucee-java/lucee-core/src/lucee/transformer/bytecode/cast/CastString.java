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
package lucee.transformer.bytecode.cast;

import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.BytecodeException;
import lucee.transformer.bytecode.Literal;
import lucee.transformer.bytecode.expression.ExprBoolean;
import lucee.transformer.bytecode.expression.ExprDouble;
import lucee.transformer.bytecode.expression.ExprString;
import lucee.transformer.bytecode.expression.Expression;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.literal.LitString;
import lucee.transformer.bytecode.util.Methods;
import lucee.transformer.bytecode.util.Types;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * Cast to a String
 */
public final class CastString extends ExpressionBase implements ExprString,Cast {
    
    private Expression expr;

    /**
     * constructor of the class
     * @param expr
     */
    private CastString(Expression expr) {
        super(expr.getStart(),expr.getEnd());
        this.expr=expr;
    }
    
    /**
     * Create a String expression from a Expression
     * @param expr
     * @param pos 
     * @return String expression
     */
    public static ExprString toExprString(Expression expr) {
        if(expr instanceof ExprString) return (ExprString) expr;
        if(expr instanceof Literal) return LitString.toExprString(((Literal)expr).getString(),expr.getStart(),expr.getEnd());
        return new CastString(expr);
    }

    /**
     * @see lucee.transformer.bytecode.expression.Expression#_writeOut(org.objectweb.asm.commons.GeneratorAdapter, int)
     */
    public Type _writeOut(BytecodeContext bc, int mode) throws BytecodeException {

    	GeneratorAdapter adapter = bc.getAdapter();
    	if(expr instanceof ExprBoolean) {
            expr.writeOut(bc,MODE_VALUE);
            adapter.invokeStatic(Types.CASTER,Methods.METHOD_TO_STRING_FROM_BOOLEAN);
        }
        else if(expr instanceof ExprDouble) {
            expr.writeOut(bc,MODE_VALUE);
            adapter.invokeStatic(Types.CASTER,Methods.METHOD_TO_STRING_FROM_DOUBLE);
        }
        else {
            Type rtn = expr.writeOut(bc,MODE_REF);
            if(rtn.equals(Types.STRING)) return Types.STRING;
            adapter.invokeStatic(Types.CASTER,Methods.METHOD_TO_STRING);
        }

        return Types.STRING;
    }

	@Override
	public Expression getExpr() {
		return expr;
	}

}
