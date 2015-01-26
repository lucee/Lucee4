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
/**
 * Implements the CFML Function arraysort
 */
package lucee.runtime.functions.arrays;

import java.util.Comparator;

import lucee.runtime.PageContext;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.functions.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Closure;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.ArrayUtil;

public final class ArraySort extends BIF {

	private static final long serialVersionUID = -747941236369495141L;

	public static boolean call(PageContext pc , Array array, Object sortTypeOrClosure) throws PageException {
		return call(pc , array, sortTypeOrClosure, "asc",false);
	}
	public static boolean call(PageContext pc , Array array, Object sortTypeOrClosure, String sortorder) throws PageException {
		return call(pc , array, sortTypeOrClosure, sortorder,false);
	}
	
	public static boolean call(PageContext pc , Array array, Object sortTypeOrClosure, String sortorder, boolean localeSensitive) throws PageException {
		if(array.getDimension()>1)
			throw new ExpressionException("only 1 dimensional arrays can be sorted");

		if(sortTypeOrClosure instanceof UDF){
			UDFComparator comp=new UDFComparator(pc, (UDF)sortTypeOrClosure);
			array.sort(comp);
		}
		else {
			array.sort(ArrayUtil.toComparator(pc,Caster.toString(sortTypeOrClosure), sortorder,localeSensitive));
		}
		return true;
	}
	
	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==2)return call(pc,Caster.toArray(args[0]),args[1]);
		if(args.length==3)return call(pc,Caster.toArray(args[0]),args[1],Caster.toString(args[2]));
		return call(pc,Caster.toArray(args[0]),args[1],Caster.toString(args[2]),Caster.toBooleanValue(args[3]));
	}
}

class UDFComparator implements Comparator<Object> {

	private UDF udf;
	private Object[] args=new Object[2];
	private PageContext pc;
	
	public UDFComparator(PageContext pc,UDF udf){
		this.pc=pc;
		this.udf=udf;
	}

	@Override
	public int compare(Object oLeft, Object oRight) {
		try {
			args[0]=oLeft;
			args[1]=oRight;
			Object res = udf.call(pc, args, false);
			Integer i = Caster.toInteger(res,null);
			if(i==null) throw new FunctionException(pc,"ArraySort",2,"function","return value of the "+(udf instanceof Closure?"closure":"function ["+udf.getFunctionName()+"]")+" cannot be casted to a integer.",CasterException.createMessage(res, "integer"));
        	return i.intValue();
		} 
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

}