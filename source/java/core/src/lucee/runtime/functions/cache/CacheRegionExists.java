/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
 */
package lucee.runtime.functions.cache;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebAdmin;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.Password;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

/**
 * implements BIF CacheRegionExists.  This function only exists for compatibility with other CFML Engines and should be avoided where possible.
 * The preferred method to manipulate Cache connections is via the Administrator interface or in Application.
 */
public class CacheRegionExists implements Function {

    public static boolean call( PageContext pc, String cacheName, String strWebAdminPassword ) throws PageException {

        Password webAdminPassword = Util.getPassword( pc, strWebAdminPassword ,false);

        try {
            ConfigWebAdmin adminConfig = ConfigWebAdmin.newInstance( (ConfigWebImpl)pc.getConfig(), webAdminPassword );
            return adminConfig.cacheConnectionExists( cacheName );
        }
        catch ( Exception e ) {
            throw Caster.toPageException(e);
        }
    }


    public static boolean call( PageContext pc, String cacheName ) throws PageException {

        return call( pc, cacheName, null );
    }

}