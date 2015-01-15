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
package lucee.runtime.listener;

import lucee.print;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.exp.PageException;

public final class MixedAppListener extends ModernAppListener {

	@Override
	public void onRequest(PageContext pc, PageSource requestedPage, RequestListener rl) throws PageException {
		RefBoolean isCFC=new RefBooleanImpl(false);
		PageSource appPS=//pc.isCFCRequest()?null:
			AppListenerUtil.getApplicationPageSource(pc, requestedPage, mode,AppListenerUtil.TYPE_ALL, isCFC);
		
		if(isCFC.toBooleanValue())_onRequest(pc, requestedPage,appPS,rl);
		else ClassicAppListener._onRequest(pc, requestedPage,appPS,rl);
	}
	
	@Override
	public final String getType() {
		return "mixed";
	}
}
