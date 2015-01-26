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

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.commons.color.ConstantsDouble;
import lucee.runtime.op.Caster;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Literal;
import lucee.transformer.bytecode.Position;
import lucee.transformer.bytecode.expression.ExprDouble;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Methods;
import lucee.transformer.bytecode.util.Types;

/**
 * Literal Double Value
 */
public final class LitDouble extends ExpressionBase implements Literal,ExprDouble {

	private static final Type CONSTANTS_DOUBLE = Type.getType(ConstantsDouble.class);
	public static final LitDouble ZERO=new LitDouble(0,null,null);
	public static final LitDouble ONE=new LitDouble(1,null,null);
	public static final LitDouble MINUS_ONE=new LitDouble(-1,null,null);
	
    private double d;

	public static LitDouble toExprDouble(double d) {
		return new LitDouble(d,null,null);
	}
	public static LitDouble toExprDouble(double d, Position start,Position end) {
		return new LitDouble(d,start,end);
	}
    
    /**
     * constructor of the class
     * @param d
     * @param line 
     */
	private LitDouble(double d, Position start,Position end) {
        super(start,end);
        
        this.d=d;
    }

	/**
     * @return return value as double value
     */ 
    public double getDoubleValue() {
        return d;
    }
    
    /**
     * @return return value as Double Object
     */
    public Double getDouble() {
        return new Double(d);
    }
    
    /**
     * @see lucee.transformer.bytecode.Literal#getString()
     */
    public String getString() {
        return Caster.toString(d);
    }
    
    /**
     * @return return value as a Boolean Object
     */
    public Boolean getBoolean() {
        return Caster.toBoolean(d);
    }
    
    /**
     * @return return value as a boolean value
     */
    public boolean getBooleanValue() {
        return Caster.toBooleanValue(d);
    }

    /**
     * @see lucee.transformer.bytecode.expression.Expression#_writeOut(org.objectweb.asm.commons.GeneratorAdapter, int)
     */
    public Type _writeOut(BytecodeContext bc, int mode) {
    	GeneratorAdapter adapter = bc.getAdapter();
        if(mode==MODE_REF) {
        	String str=ConstantsDouble.getFieldName(d);
        	if(str!=null) {
				bc.getAdapter().getStatic(CONSTANTS_DOUBLE, str, Types.DOUBLE);
			}
			else {
				adapter.push(d);
		        adapter.invokeStatic(Types.CASTER,Methods.METHOD_TO_DOUBLE_FROM_DOUBLE);
			}
            return Types.DOUBLE;
        }
        adapter.push(d);
        
        return Types.DOUBLE_VALUE;
    }

    /**
     * @see lucee.transformer.bytecode.Literal#getDouble(java.lang.Double)
     */
    public Double getDouble(Double defaultValue) {
        return getDouble();
    }

    /**
     * @see lucee.transformer.bytecode.Literal#getBoolean(java.lang.Boolean)
     */
    public Boolean getBoolean(Boolean defaultValue) {
        return getBoolean();
    }
}
