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
package lucee.transformer.bytecode.literal;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.runtime.op.Caster;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Literal;
import lucee.transformer.bytecode.Position;
import lucee.transformer.bytecode.expression.Expression;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Methods;
import lucee.transformer.bytecode.util.Types;

/**
 * Literal Double Value
 */
public final class LitLong extends ExpressionBase implements Literal {
    
    private long l;

	public static Expression toExpr(long l, Position start,Position end) {
		return new LitLong(l,start,end);
	}
    
    /**
     * constructor of the class
     * @param d
     * @param line 
     */
	public LitLong(long l, Position start,Position end) {
        super(start,end);        
        this.l=l;
    }

	/**
     * @return return value as int
     */ 
    public long getLongValue() {
        return l;
    }
    
    /**
     * @return return value as Double Object
     */
    public Long getLong() {
        return new Long(l);
    }
    
    /**
     * @see lucee.transformer.bytecode.Literal#getString()
     */
    public String getString() {
        return Caster.toString(l);
    }
    
    /**
     * @return return value as a Boolean Object
     */
    public Boolean getBoolean() {
        return Caster.toBoolean(l);
    }
    
    /**
     * @return return value as a boolean value
     */
    public boolean getBooleanValue() {
        return Caster.toBooleanValue(l);
    }

    /**
     * @see lucee.transformer.bytecode.expression.Expression#_writeOut(org.objectweb.asm.commons.GeneratorAdapter, int)
     */
    public Type _writeOut(BytecodeContext bc, int mode) {
    	GeneratorAdapter adapter = bc.getAdapter();
        adapter.push(l);
        if(mode==MODE_REF) {
            adapter.invokeStatic(Types.CASTER,Methods.METHOD_TO_LONG_FROM_LONG_VALUE);
            return Types.LONG;
        }
        return Types.LONG_VALUE;
    }

    /**
     * @see lucee.transformer.bytecode.Literal#getDouble(java.lang.Double)
     */
    public Double getDouble(Double defaultValue) {
        return getDouble();
    }

    private Double getDouble() {
		return new Double(l);
	}

	/**
     * @see lucee.transformer.bytecode.Literal#getBoolean(java.lang.Boolean)
     */
    public Boolean getBoolean(Boolean defaultValue) {
        return getBoolean();
    }
}
