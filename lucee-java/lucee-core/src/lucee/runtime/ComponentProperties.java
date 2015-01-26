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
package lucee.runtime;

import java.io.Serializable;
import java.util.Map;

import lucee.runtime.component.Property;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;


public class ComponentProperties implements Serializable {
	
	private static final Collection.Key WSDL_FILE = KeyImpl.intern("wsdlfile");
	final String dspName;
	final String extend;
	final String hint;
	final Boolean output;
	final String callPath;
	final boolean relPath;
	final boolean _synchronized;
	Class javaAccessClass;
	Map<String,Property> properties;
	Struct meta;
	String implement;
	boolean persistent;
	boolean accessors;

	public ComponentProperties(String dspName,String extend,String implement,String hint, Boolean output, String callPath, 
			boolean relPath,boolean _synchronized,Class javaAccessClass,boolean persistent,boolean accessors,Struct meta) {
		this.dspName=dspName;
		this.extend=extend;
		this.implement=implement;
		this.hint=hint;
		this.output=output;
		this.callPath=callPath;
		this.relPath=relPath;
		this._synchronized=_synchronized;
		this.javaAccessClass=javaAccessClass;
		this.meta=meta;
		this.persistent=persistent;
		this.accessors=accessors;
	}

	public ComponentProperties duplicate() {
		ComponentProperties cp= new ComponentProperties(dspName,extend,implement,hint,output,callPath,relPath,_synchronized,javaAccessClass,persistent,accessors,meta);
		cp.properties=properties;
		return cp;
	}

    
	/**
	 * returns null if there is no wsdlFile defined
	 * @return the wsdlFile
	 * @throws ExpressionException 
	 */
	public String getWsdlFile() {
		if(meta==null) return null;
		return (String) meta.get(WSDL_FILE,null);
	}


}
