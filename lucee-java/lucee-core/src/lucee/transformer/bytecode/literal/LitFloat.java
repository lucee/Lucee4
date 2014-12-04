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
import lucee.transformer.bytecode.expression.ExprFloat;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Methods;
import lucee.transformer.bytecode.util.Types;

/**
 * Literal Double Value
 */
public final class LitFloat extends ExpressionBase implements Literal,ExprFloat {
    
    private float f;

	public static ExprFloat toExprFloat(float f, Position start,Position end) {
		return new LitFloat(f,start,end);
	}
    
    /**
     * constructor of the class
     * @param d
     * @param line 
     */
	public LitFloat(float f, Position start,Position end) {
        super(start,end);
        this.f=f;
    }

	/**
     * @return return value as double value
     */ 
    public float getFloatValue() {
        return f;
    }
    
    public Float getFloat() {
        return new Float(f);
    }
    
    /**
     * @see lucee.transformer.bytecode.Literal#getString()
     */
    public String getString() {
        return Caster.toString(f);
    }
    
    /**
     * @return return value as a Boolean Object
     */
    public Boolean getBoolean() {
        return Caster.toBoolean(f);
    }
    
    /**
     * @return return value as a boolean value
     */
    public boolean getBooleanValue() {
        return Caster.toBooleanValue(f);
    }

    /**
     * @see lucee.transformer.bytecode.expression.Expression#_writeOut(org.objectweb.asm.commons.GeneratorAdapter, int)
     */
    public Type _writeOut(BytecodeContext bc, int mode) {
    	GeneratorAdapter adapter = bc.getAdapter();
        adapter.push(f);
        if(mode==MODE_REF) {
            adapter.invokeStatic(Types.CASTER,Methods.METHOD_TO_FLOAT_FROM_FLOAT);
            return Types.FLOAT;
        }
        return Types.FLOAT_VALUE;
    }

    /**
     * @see lucee.transformer.bytecode.Literal#getDouble(java.lang.Double)
     */
    public Double getDouble(Double defaultValue) {
        return new Double(getFloatValue());
    }

    /**
     * @see lucee.transformer.bytecode.Literal#getBoolean(java.lang.Boolean)
     */
    public Boolean getBoolean(Boolean defaultValue) {
        return getBoolean();
    }
}
