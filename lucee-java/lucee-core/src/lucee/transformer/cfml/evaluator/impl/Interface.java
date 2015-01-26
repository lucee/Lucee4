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
package lucee.transformer.cfml.evaluator.impl;

import java.util.Iterator;
import java.util.List;

import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.cast.CastString;
import lucee.transformer.bytecode.expression.ExprString;
import lucee.transformer.bytecode.literal.LitString;
import lucee.transformer.bytecode.statement.PrintOut;
import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.statement.tag.TagFunction;
import lucee.transformer.bytecode.statement.tag.TagImport;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.library.tag.TagLibTag;

public class Interface extends Component {
	/**
	 * @see lucee.transformer.cfml.evaluator.EvaluatorSupport#evaluate(org.w3c.dom.Element, lucee.transformer.library.tag.TagLibTag)
	 */
	public void evaluate(Tag tag,TagLibTag libTag) throws EvaluatorException { 
		super.evaluate(tag,libTag);
		Body body = tag.getBody();
		List statments = body.getStatements();
		Statement stat;
		Iterator it = statments.iterator();
		Tag t;
		while(it.hasNext()) {
			stat=(Statement) it.next();
			
			if(stat instanceof PrintOut) {
				//body.remove(stat);
			}
			else if(stat instanceof Tag) {
				t=(Tag) stat;
				if(stat instanceof TagImport) {
					// ignore
				}
				else if(stat instanceof TagFunction) {
					
					Function.throwIfNotEmpty(t);
					Attribute attr = t.getAttribute("access");
					
					if(attr!=null) {
						ExprString expr = CastString.toExprString(attr.getValue());
						
						if(!(expr instanceof LitString))
							throw new EvaluatorException(
						"the attribute access of the Tag function inside an interface must contain a constant value");
						String access = ((LitString)expr).getString().trim();
						if(!"public".equalsIgnoreCase(access))
							throw new EvaluatorException(
						"the attribute access of the tag function inside an interface definition can only have the value [public] not ["+access+"]");
					}
					else t.addAttribute(new Attribute(false,"access",LitString.toExprString("public"),"string"));
					
				}
				else throw new EvaluatorException("tag "+libTag.getFullName()+" can only contain function definitions.");
			}
		}
		
		
	}

}
