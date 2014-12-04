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
/**
 * Implements the CFML Function iscustomfunction
 */
package lucee.runtime.functions.decision;

import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Decision;
import lucee.runtime.type.ObjectWrap;

public final class IsCustomFunction implements Function {

	private static final long serialVersionUID = 1578909692090122692L;

	public static boolean call(PageContext pc , Object object) {
		if(object instanceof ObjectWrap) {
        	return call(pc,((ObjectWrap)object).getEmbededObject(null));
        }
		return Decision.isUserDefinedFunction(object) && !Decision.isClosure(object);
	}
}