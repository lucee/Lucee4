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
package lucee.runtime.functions.dateTime;

import lucee.commons.date.DateTimeUtil;
import lucee.commons.date.TimeZoneUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.dt.DateTime;

/**
 * Implements the CFML Function year
 */
public final class Year implements Function {

	public static double call(PageContext pc , DateTime date) {
		return DateTimeUtil.getInstance().getYear(pc.getTimeZone(),date);
	}
	
	public static double call(PageContext pc , DateTime date, String strTimezone) throws ExpressionException {
		return DateTimeUtil.getInstance().getYear(strTimezone==null?pc.getTimeZone():TimeZoneUtil.toTimeZone(strTimezone),date);
	}
}