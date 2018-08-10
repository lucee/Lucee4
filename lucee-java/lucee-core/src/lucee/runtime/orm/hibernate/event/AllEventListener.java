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
import lucee.runtime.orm.hibernate.CommonUtil;

import org.luceehibernate.event.PostDeleteEvent;
import org.luceehibernate.event.PostDeleteEventListener;
import org.luceehibernate.event.PostInsertEvent;
import org.luceehibernate.event.PostInsertEventListener;
import org.luceehibernate.event.PostLoadEvent;
import org.luceehibernate.event.PostLoadEventListener;
import org.luceehibernate.event.PostUpdateEvent;
import org.luceehibernate.event.PostUpdateEventListener;
import org.luceehibernate.event.PreDeleteEvent;
import org.luceehibernate.event.PreDeleteEventListener;
import org.luceehibernate.event.PreInsertEvent;
import org.luceehibernate.event.PreInsertEventListener;
import org.luceehibernate.event.PreLoadEvent;
import org.luceehibernate.event.PreLoadEventListener;
import org.luceehibernate.event.PreUpdateEvent;
import org.luceehibernate.event.PreUpdateEventListener;

public class AllEventListener extends EventListener implements PreDeleteEventListener, PreInsertEventListener, PreLoadEventListener, PreUpdateEventListener,
PostDeleteEventListener, PostInsertEventListener, PostLoadEventListener, PostUpdateEventListener {

	private static final long serialVersionUID = 8969282190912098982L;



	public AllEventListener(Component component) {
	    super(component, null, true);
	}


	public void onPostInsert(PostInsertEvent event) {
		invoke(CommonUtil.POST_INSERT, event.getEntity());
    }

    public void onPostUpdate(PostUpdateEvent event) {
    	invoke(CommonUtil.POST_UPDATE, event.getEntity());
    }

    public boolean onPreDelete(PreDeleteEvent event) {
    	invoke(CommonUtil.PRE_DELETE, event.getEntity());
		return false;
    }

    public void onPostDelete(PostDeleteEvent event) {
    	invoke(CommonUtil.POST_DELETE, event.getEntity());
    }

    public void onPreLoad(PreLoadEvent event) {
    	invoke(CommonUtil.PRE_LOAD, event.getEntity());
    }

    public void onPostLoad(PostLoadEvent event) {
    	invoke(CommonUtil.POST_LOAD, event.getEntity());
    }

	public boolean onPreUpdate(PreUpdateEvent event) {
		return preUpdate(event);
	}



	public boolean onPreInsert(PreInsertEvent event) {
		invoke(CommonUtil.PRE_INSERT, event.getEntity());
		return false;
	}
}
