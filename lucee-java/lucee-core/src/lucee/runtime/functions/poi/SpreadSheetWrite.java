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
package lucee.runtime.functions.poi;

import lucee.commons.io.res.Resource;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.poi.Excel;

public class SpreadSheetWrite {
	public static String call(PageContext pc,Excel excel, String path) throws PageException  {
		return call(pc, excel, path, null, false);
	}
	public static String call(PageContext pc,Excel excel, String path,Object passwordOrOverwrite) throws PageException  {
		if(Decision.isBoolean(passwordOrOverwrite))
			return call(pc, excel, path, null, Caster.toBooleanValue(passwordOrOverwrite));
		return call(pc, excel, path, passwordOrOverwrite, false);	
	}
	
	public static String call(PageContext pc,Excel excel, String path,Object oPassword,boolean overwrite) throws PageException  {
		Resource res=Caster.toResource(pc,path, false);
		String password=oPassword==null?null:Caster.toString(oPassword);
		if(!overwrite && res.isFile())
			throw new ApplicationException("Resource ["+res+"] already exist");
		
		
		excel.write(res,password);
		return null;
	}
}
