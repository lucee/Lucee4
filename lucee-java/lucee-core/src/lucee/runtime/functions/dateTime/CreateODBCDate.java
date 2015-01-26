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
 * Implements the CFML Function createodbcdate
 */
package lucee.runtime.functions.dateTime;

import java.util.Calendar;
import java.util.TimeZone;

import lucee.commons.date.TimeZoneUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.dt.DateImpl;
import lucee.runtime.type.dt.DateTime;

public final class CreateODBCDate implements Function {
	public static DateTime call(PageContext pc , DateTime datetime) throws ExpressionException {
		return call(pc, datetime,null);
	}
	public static DateTime call(PageContext pc , DateTime datetime, String strTimezone) throws ExpressionException {
		TimeZone tz;
		if(StringUtil.isEmpty(strTimezone)) tz=((PageContextImpl)pc).getTimeZone();
		else tz=TimeZoneUtil.toTimeZone(strTimezone);
		Calendar c = Calendar.getInstance(tz);
		c.setTime(datetime);
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return new DateImpl(c.getTime());
	}
}