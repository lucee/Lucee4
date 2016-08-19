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

import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.BytecodeException;
import lucee.transformer.bytecode.Literal;
import lucee.transformer.bytecode.cast.CastString;
import lucee.transformer.bytecode.expression.ExprString;
import lucee.transformer.bytecode.expression.Expression;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.literal.LitString;
import lucee.transformer.bytecode.util.Types;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

public final class OpString extends ExpressionBase implements ExprString {
    
    private ExprString right;
    private ExprString left;

    // String concat (String)
    private final static Method METHOD_CONCAT = new Method("concat",
			Types.STRING,
			new Type[]{Types.STRING});
    
    private OpString(Expression left, Expression right) {
        super(left.getStart(),right.getEnd());
        this.left=CastString.toExprString(left);
        this.right=CastString.toExprString(right);   
    }
    
    /**
     * Create a String expression from a Expression
     * @param left 
     * @param right 
     * 
     * @return String expression
     */
    public static ExprString toExprString(Expression left, Expression right) {
        return toExprString(left, right, true);
    }
    
    public static ExprString toExprString(Expression left, Expression right, boolean concatStatic) {
        if(concatStatic && left instanceof Literal && right instanceof Literal) {
            String l = ((Literal)left).getString();
        	String r = ((Literal)right).getString();
        	if((l.length()+r.length())<=LitString.MAX_SIZE)return LitString.toExprString(l.concat(r),left.getStart(),right.getEnd());
        }
        return new OpString(left,right);
    }
    
    
    /**
     *
     * @see lucee.transformer.bytecode.expression.ExpressionBase#_writeOut(org.objectweb.asm.commons.GeneratorAdapter, int)
     */
    public Type _writeOut(BytecodeContext bc, int mode) throws BytecodeException {
        left.writeOut(bc,MODE_REF);
        right.writeOut(bc,MODE_REF);
        bc.getAdapter().invokeVirtual(Types.STRING,METHOD_CONCAT);
        return Types.STRING;
    }

    /* *
     * @see lucee.transformer.bytecode.expression.Expression#getType()
     * /
    public int getType() {
        return Types._STRING;
    }*/

}
