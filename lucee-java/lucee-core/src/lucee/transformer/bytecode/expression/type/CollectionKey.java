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
package lucee.transformer.bytecode.expression.type;

import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.BytecodeException;
import lucee.transformer.bytecode.Position;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.expression.var.Variable;
import lucee.transformer.bytecode.literal.LitString;
import lucee.transformer.bytecode.util.Types;

import org.objectweb.asm.Type;

public class CollectionKey extends ExpressionBase {

	private String value;

	public CollectionKey(String value) {
		super(null,null);
		this.value=value;
	}

	public CollectionKey(String value,Position start,Position end) {
		super(start,end);
		this.value=value;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws BytecodeException {
		Variable.registerKey(bc, LitString.toExprString(value));
		return Types.COLLECTION_KEY;
	}

}
