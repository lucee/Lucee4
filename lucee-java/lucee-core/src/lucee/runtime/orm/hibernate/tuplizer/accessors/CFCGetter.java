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
package lucee.runtime.orm.hibernate.tuplizer.accessors;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.orm.ORMUtil;
import lucee.runtime.orm.hibernate.CommonUtil;
import lucee.runtime.orm.hibernate.HibernateCaster;
import lucee.runtime.orm.hibernate.HibernateORMEngine;
import lucee.runtime.orm.hibernate.HibernatePageException;
import lucee.runtime.orm.hibernate.HibernateUtil;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;

import org.luceehibernate.HibernateException;
import org.luceehibernate.SessionFactory;
import org.luceehibernate.engine.SessionImplementor;
import org.luceehibernate.metadata.ClassMetadata;
import org.luceehibernate.property.Getter;
import org.luceehibernate.type.Type;

public class CFCGetter implements Getter {

	private Key key;

	/**
	 * Constructor of the class
	 * @param key
	 */
	public CFCGetter(String key){
		this(CommonUtil.createKey(key));
	}

	/**
	 * Constructor of the class
	 * @param engine
	 * @param key
	 */
	public CFCGetter( Collection.Key key){
		this.key=key;
	}

	@Override
	public Object get(Object trg) throws HibernateException {
		try {
			// MUST cache this, perhaps when building xml
			PageContext pc = CommonUtil.pc();
			ORMSession session = ORMUtil.getSession(pc);
			Component cfc = CommonUtil.toComponent(trg);
			String dsn = ORMUtil.getDataSourceName(pc, cfc);
			String name = HibernateCaster.getEntityName(cfc);
			SessionFactory sf=(SessionFactory) session.getRawSessionFactory(dsn);
			ClassMetadata metaData = sf.getClassMetadata(name);
			Type type = HibernateUtil.getPropertyType(metaData, key.getString());

			Object rtn = cfc.getComponentScope().get(key,null);
			return HibernateCaster.toSQL(type, rtn,null);
		}
		catch (PageException pe) {
			throw new HibernatePageException(pe);
		}
	}


	public HibernateORMEngine getHibernateORMEngine(){
		try {
			// TODO better impl
			return HibernateUtil.getORMEngine(CommonUtil.pc());
		}
		catch (PageException e) {}

		return null;
	}


	@Override
	public Object getForInsert(Object trg, Map arg1, SessionImplementor arg2)throws HibernateException {
		return get(trg);// MUST better solution? this is from MapGetter
	}

	@Override
	public Member getMember() {
		return null;
	}

	@Override
	public Method getMethod() {
		return null;
	}

	public String getMethodName() {
		return null;// MUST macht es sinn den namen zurueck zu geben?
	}

	public Class getReturnType() {
		return Object.class;// MUST more concrete?
	}

}
