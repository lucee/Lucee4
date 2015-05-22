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
package lucee.runtime.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.type.ForEachIteratorable;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.it.EnumAsIt;
import lucee.runtime.type.it.ForEachQueryIterator;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.type.wrap.MapAsStruct;

import org.w3c.dom.Node;

public class ForEachUtil {

	public static Iterator toIterator(Object o) throws PageException {
		
		
		if(o instanceof ForEachIteratorable) 
			return ((ForEachIteratorable)o).getIterator();
			
		else if(o instanceof Node)return XMLCaster.toXMLStruct((Node)o,false).getIterator();
        else if(o instanceof Map) {
            return MapAsStruct.toStruct((Map)o,true).getIterator();
        }
        else if(o instanceof ObjectWrap) {
            return toIterator(((ObjectWrap)o).getEmbededObject());
        }
        else if(Decision.isArray(o)) {
            return Caster.toArray(o).getIterator();
        }
        else if(o instanceof Iterator) {
            return (Iterator)o;
        }
        else if(o instanceof Enumeration) {
            return new EnumAsIt((Enumeration)o);
        }
        else if(o instanceof CharSequence) {
        	return ListUtil.listToArray(o.toString(), ',').getIterator();
        }
        throw new CasterException(o,"collection");
	}
	
	public static void reset(Iterator it) throws PageException {
		
		if(it instanceof ForEachQueryIterator) {
			((ForEachQueryIterator)it).reset();
		}
	}

}
