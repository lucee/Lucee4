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
package lucee.transformer.bytecode.literal;

import lucee.runtime.op.Caster;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Literal;
import lucee.transformer.bytecode.Position;
import lucee.transformer.bytecode.expression.ExprInt;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Methods;
import lucee.transformer.bytecode.util.Types;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * Literal Double Value
 */
public final class LitInteger extends ExpressionBase implements Literal,ExprInt {
    
    private int i;

	public static ExprInt toExpr(int i, Position start,Position end) {
		return new LitInteger(i,start,end);
	}
	public static ExprInt toExpr(int i) {
		return new LitInteger(i,null,null);
	}
    
    /**
     * constructor of the class
     * @param d
     * @param line 
     */
	public LitInteger(int i, Position start,Position end) {
        super(start,end);        
        this.i=i;
    }

	/**
     * @return return value as int
     */ 
    public int geIntValue() {
        return i;
    }
    
    /**
     * @return return value as Double Object
     */
    public Integer getInteger() {
        return new Integer(i);
    }
    
    /**
     * @see lucee.transformer.bytecode.Literal#getString()
     */
    public String getString() {
        return Caster.toString(i);
    }
    
    /**
     * @return return value as a Boolean Object
     */
    public Boolean getBoolean() {
        return Caster.toBoolean(i);
    }
    
    /**
     * @return return value as a boolean value
     */
    public boolean getBooleanValue() {
        return Caster.toBooleanValue(i);
    }

    /**
     * @see lucee.transformer.bytecode.expression.Expression#_writeOut(org.objectweb.asm.commons.GeneratorAdapter, int)
     */
    public Type _writeOut(BytecodeContext bc, int mode) {
    	GeneratorAdapter adapter = bc.getAdapter();
        adapter.push(i);
        if(mode==MODE_REF) {
            adapter.invokeStatic(Types.CASTER,Methods.METHOD_TO_INTEGER_FROM_INT);
            return Types.INTEGER;
        }
        return Types.INT_VALUE;
    }

    /**
     * @see lucee.transformer.bytecode.Literal#getDouble(java.lang.Double)
     */
    public Double getDouble(Double defaultValue) {
        return getDouble();
    }

    private Double getDouble() {
		return new Double(i);
	}

	/**
     * @see lucee.transformer.bytecode.Literal#getBoolean(java.lang.Boolean)
     */
    public Boolean getBoolean(Boolean defaultValue) {
        return getBoolean();
    }
}
