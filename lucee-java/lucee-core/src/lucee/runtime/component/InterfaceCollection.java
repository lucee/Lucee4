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
package lucee.runtime.component;

import java.util.HashMap;
import java.util.Map;

import lucee.runtime.InterfaceImpl;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.util.ComponentUtil;

public class InterfaceCollection {

	private InterfaceImpl[] interfaces;
	private Map udfs= new HashMap();
	private long lastUpdate=0;


	public InterfaceCollection(PageContextImpl pc, PageSource child,String implement) throws PageException {
		interfaces = InterfaceImpl.loadImplements(pc,child, implement,udfs);
	}

	/**
	 * @return the interfaces
	 */
	public InterfaceImpl[] getInterfaces() {
		return interfaces;
	}

	/**
	 * @return the udfs
	 */
	public Map getUdfs() {
		return udfs;
	}
	
	public long lastUpdate() {
		if(lastUpdate==0){
			long temp;
			for(int i=0;i<interfaces.length;i++){
				temp=ComponentUtil.getCompileTime(null,interfaces[i].getPageSource(),0);
				if(temp>lastUpdate)
					lastUpdate=temp;
			}
		}
		return lastUpdate;
	}

}
