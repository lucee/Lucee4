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
package lucee.runtime.orm.hibernate.event;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.component.Property;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.orm.hibernate.CommonUtil;
import lucee.runtime.orm.hibernate.HibernateUtil;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;

import org.luceehibernate.event.PreUpdateEvent;

public abstract class EventListener {

	private static final long serialVersionUID = -4842481789634140033L;




    protected Component component;

    private boolean allEvents;
	private Key eventType;

	public EventListener(Component component, Key eventType, boolean allEvents) {
	       this.component=component;
	       this.allEvents=allEvents;
	       this.eventType=eventType;
    }

	protected boolean preUpdate(PreUpdateEvent event) {
		Struct oldData=CommonUtil.createStruct();
		Property[] properties = HibernateUtil.getProperties(component,HibernateUtil.FIELDTYPE_COLUMN,null);
		Object[] data = event.getOldState();

		if(data!=null && properties!=null && data.length==properties.length) {
			for(int i=0;i<data.length;i++){
				oldData.setEL(CommonUtil.createKey(properties[i].getName()), data[i]);
			}
		}
		invoke(CommonUtil.PRE_UPDATE, event.getEntity(),oldData);
		return false;
	}


    public Component getCFC() {
		return component;
	}


    protected void invoke(Collection.Key name, Object obj) {
    	invoke(name, obj,null);
    }
    protected void invoke(Collection.Key name, Object obj, Struct data) {
    	if(eventType!=null && !eventType.equals(name)) return;
    	//print.e(name);
    	Component caller = CommonUtil.toComponent(obj,null);
    	Component c=allEvents?component:caller;
    	if(c==null) return;

    	if(!allEvents &&!caller.getPageSource().equals(component.getPageSource())) return;
		invoke(name, c, data, allEvents?obj:null);

	}

    public static void invoke(Key name, Component cfc, Struct data, Object arg) {
    	if(cfc==null) return;

		try {
			PageContext pc = ThreadLocalPageContext.get();
			Object[] args;
			if(data==null) {
				args=arg!=null?new Object[]{arg}:new Object[]{};
			}
			else {
				args=arg!=null?new Object[]{arg,data}:new Object[]{data};
			}
			cfc.call(pc, name, args);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	public static boolean hasEventType(Component cfc, Collection.Key eventType) {
		return cfc.get(eventType,null) instanceof UDF;
	}
}