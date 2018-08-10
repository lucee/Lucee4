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
package lucee.runtime.orm.hibernate.tuplizer.proxy;

import java.io.Serializable;

import lucee.runtime.Component;
import lucee.runtime.exp.PageException;
import lucee.runtime.orm.hibernate.CommonUtil;
import lucee.runtime.orm.hibernate.HibernatePageException;

import org.luceehibernate.engine.SessionImplementor;
import org.luceehibernate.proxy.AbstractLazyInitializer;

/**
 * Lazy initializer for "dynamic-map" entity representations.
 * SLOW
 */
public class CFCLazyInitializer extends AbstractLazyInitializer implements Serializable {


	CFCLazyInitializer(String entityName, Serializable id, SessionImplementor session) {
		super(entityName, id, session);

	}

	public Component getCFC() {
		try {
			return CommonUtil.toComponent(getImplementation());
		} catch (PageException pe) {
			throw new HibernatePageException(pe);
		}
	}

	public Class getPersistentClass() {
		throw new UnsupportedOperationException("dynamic-map entity representation");
	}



}




