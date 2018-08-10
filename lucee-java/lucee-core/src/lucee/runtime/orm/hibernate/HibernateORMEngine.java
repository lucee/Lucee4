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
package lucee.runtime.orm.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.loader.util.Util;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.db.DataSource;
import lucee.runtime.db.DataSourceManager;
import lucee.runtime.db.DatasourceConnection;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.listener.ApplicationContextPro;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.orm.ORMEngine;
import lucee.runtime.orm.ORMSession;
import lucee.runtime.orm.ORMUtil;
import lucee.runtime.orm.hibernate.event.AllEventListener;
import lucee.runtime.orm.hibernate.event.EventListener;
import lucee.runtime.orm.hibernate.event.InterceptorImpl;
import lucee.runtime.orm.hibernate.event.PostDeleteEventListenerImpl;
import lucee.runtime.orm.hibernate.event.PostInsertEventListenerImpl;
import lucee.runtime.orm.hibernate.event.PostLoadEventListenerImpl;
import lucee.runtime.orm.hibernate.event.PostUpdateEventListenerImpl;
import lucee.runtime.orm.hibernate.event.PreDeleteEventListenerImpl;
import lucee.runtime.orm.hibernate.event.PreInsertEventListenerImpl;
import lucee.runtime.orm.hibernate.event.PreLoadEventListenerImpl;
import lucee.runtime.orm.hibernate.event.PreUpdateEventListenerImpl;
import lucee.runtime.orm.hibernate.tuplizer.AbstractEntityTuplizerImpl;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.util.ListUtil;

import org.luceehibernate.EntityMode;
import org.luceehibernate.SessionFactory;
import org.luceehibernate.cfg.Configuration;
import org.luceehibernate.event.EventListeners;
import org.luceehibernate.event.PostDeleteEventListener;
import org.luceehibernate.event.PostInsertEventListener;
import org.luceehibernate.event.PostLoadEventListener;
import org.luceehibernate.event.PostUpdateEventListener;
import org.luceehibernate.event.PreDeleteEventListener;
import org.luceehibernate.event.PreLoadEventListener;
import org.luceehibernate.tuple.entity.EntityTuplizerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HibernateORMEngine implements ORMEngine {

	private static final int INIT_NOTHING=1;
	private static final int INIT_CFCS=2;
	private static final int INIT_ALL=2;

	private Map<String,SessionFactoryData> factories=new ConcurrentHashMap<String, SessionFactoryData>();

	public HibernateORMEngine() {}

	@Override
	public void init(PageContext pc) throws PageException{
		SessionFactoryData data = getSessionFactoryData(pc, INIT_CFCS);
		data.init();// init all factories
	}

	@Override
	public ORMSession createSession(PageContext pc) throws PageException {
		try{
			SessionFactoryData data = getSessionFactoryData(pc, INIT_NOTHING);
			return new HibernateORMSession(pc,data);
		}
		catch(PageException pe){
			throw pe;
		}
	}


	/*QueryPlanCache getQueryPlanCache(PageContext pc) throws PageException {
		return getSessionFactoryData(pc,INIT_NOTHING).getQueryPlanCache();
	}*/

	/*public SessionFactory getSessionFactory(PageContext pc) throws PageException{
		return getSessionFactory(pc,INIT_NOTHING);
	}*/

	public boolean reload(PageContext pc, boolean force) throws PageException {
		if(force) {
			getSessionFactoryData(pc, INIT_ALL);
		}
		else {
			if(factories.containsKey(hash(pc)))return false;
		}
		getSessionFactoryData(pc, INIT_CFCS);
		return true;
	}

	private SessionFactoryData getSessionFactoryData(PageContext pc,int initType) throws PageException {
		ApplicationContextPro appContext = (ApplicationContextPro) pc.getApplicationContext();
		if(!appContext.isORMEnabled())
			throw ExceptionUtil.createException((ORMSession)null,null,"ORM is not enabled","");


		// datasource
		ORMConfiguration ormConf=appContext.getORMConfiguration();
		String key = hash(ormConf);
		SessionFactoryData data = factories.get(key);
		if(initType==INIT_ALL && data!=null) {
			data.reset();
			data=null;
		}
		if(data==null) {
			data=new SessionFactoryData(this,ormConf);
			factories.put(key, data);
		}


		// config
		try{
			//arr=null;
			if(initType!=INIT_NOTHING){
				synchronized (data) {

					if(ormConf.autogenmap()){
						data.tmpList=HibernateSessionFactory.loadComponents(pc, this, ormConf);

						data.clearCFCs();
					}
					else
						throw ExceptionUtil.createException(data,null,"orm setting autogenmap=false is not supported yet",null);

					// load entities
					if(data.tmpList!=null && data.tmpList.size()>0) {
						data.getNamingStrategy();// called here to make sure, it is called in the right context the first one

						// creates CFCInfo objects
						{
							Iterator<Component> it = data.tmpList.iterator();
							while(it.hasNext()){
								createMapping(pc,it.next(),ormConf,data);
							}
						}

						if(data.tmpList.size()!=data.sizeCFCs()){
							Component cfc;
							String name,lcName;
							Map<String,String> names=new HashMap<String,String>();
							Iterator<Component> it = data.tmpList.iterator();
							while(it.hasNext()){
								cfc=it.next();
								name=HibernateCaster.getEntityName(cfc);
								lcName=name.toLowerCase();
								if(names.containsKey(lcName))
									throw ExceptionUtil.createException(data,null,"Entity Name ["+name+"] is ambigous, ["+names.get(lcName)+"] and ["+cfc.getPageSource().getDisplayPath()+"] use the same entity name.","");
								names.put(lcName,cfc.getPageSource().getDisplayPath());
							}
						}
					}
				}
			}
		}
		finally {
			data.tmpList=null;
		}

		// already initialized for this application context

		//MUST
		//cacheconfig
		//cacheprovider
		//...

		Log log = ((ConfigImpl)pc.getConfig()).getLog("orm");

		Iterator<Entry<Key, String>> it = HibernateSessionFactory.createMappings(ormConf,data).entrySet().iterator();
		Entry<Key, String> e;
		while(it.hasNext()) {
			e = it.next();
			if(data.getConfiguration(e.getKey())!=null) continue;

			DatasourceConnection dc = CommonUtil.getDatasourceConnection(pc,data.getDataSource(e.getKey()));
			try{
				data.setConfiguration(log,e.getValue(),dc);
			}
			catch (Exception ex) {
				throw CommonUtil.toPageException(ex);
			}
			finally {
				CommonUtil.releaseDatasourceConnection(pc, dc);
			}
			addEventListeners(pc, data,e.getKey());

			EntityTuplizerFactory tuplizerFactory = data.getConfiguration(e.getKey()).getEntityTuplizerFactory();
			tuplizerFactory.registerDefaultTuplizerClass(EntityMode.MAP, AbstractEntityTuplizerImpl.class);
			tuplizerFactory.registerDefaultTuplizerClass(EntityMode.POJO, AbstractEntityTuplizerImpl.class);

			data.buildSessionFactory(e.getKey());
		}

		return data;
	}

	private static void addEventListeners(PageContext pc, SessionFactoryData data, Key key) throws PageException {
		if(!data.getORMConfiguration().eventHandling()) return;
		String eventHandler = data.getORMConfiguration().eventHandler();
		AllEventListener listener=null;
		if(!Util.isEmpty(eventHandler,true)){
			//try {
				Component c = pc.loadComponent(eventHandler.trim());

				listener = new AllEventListener(c);
		        //config.setInterceptor(listener);
			//}catch (PageException e) {e.printStackTrace();}
		}
		Configuration conf = data.getConfiguration(key);
		conf.setInterceptor(new InterceptorImpl(listener));
        EventListeners listeners = conf.getEventListeners();
        Map<String, CFCInfo> cfcs = data.getCFCs(key);
        // post delete
		List<EventListener>
		list=merge(listener,cfcs,CommonUtil.POST_DELETE);
		listeners.setPostDeleteEventListeners(list.toArray(new PostDeleteEventListener[list.size()]));

        // post insert
		list=merge(listener,cfcs,CommonUtil.POST_INSERT);
		listeners.setPostInsertEventListeners(list.toArray(new PostInsertEventListener[list.size()]));

		// post update
		list=merge(listener,cfcs,CommonUtil.POST_UPDATE);
		listeners.setPostUpdateEventListeners(list.toArray(new PostUpdateEventListener[list.size()]));

		// post load
		list=merge(listener,cfcs,CommonUtil.POST_LOAD);
		listeners.setPostLoadEventListeners(list.toArray(new PostLoadEventListener[list.size()]));

		// pre delete
		list=merge(listener,cfcs,CommonUtil.PRE_DELETE);
		listeners.setPreDeleteEventListeners(list.toArray(new PreDeleteEventListener[list.size()]));

		// pre insert
		//list=merge(listener,cfcs,CommonUtil.PRE_INSERT);
		//listeners.setPreInsertEventListeners(list.toArray(new PreInsertEventListener[list.size()]));

		// pre load
		list=merge(listener,cfcs,CommonUtil.PRE_LOAD);
		listeners.setPreLoadEventListeners(list.toArray(new PreLoadEventListener[list.size()]));

		// pre update
		//list=merge(listener,cfcs,CommonUtil.PRE_UPDATE);
		//listeners.setPreUpdateEventListeners(list.toArray(new PreUpdateEventListener[list.size()]));
	}

	private static List<EventListener> merge(EventListener listener, Map<String, CFCInfo> cfcs, Collection.Key eventType) {
		List<EventListener> list=new ArrayList<EventListener>();


		Iterator<Entry<String, CFCInfo>> it = cfcs.entrySet().iterator();
		Entry<String, CFCInfo> entry;
		Component cfc;
		while(it.hasNext()){
			entry = it.next();
			cfc = entry.getValue().getCFC();
			if(EventListener.hasEventType(cfc,eventType)) {
				if(CommonUtil.POST_DELETE.equals(eventType))
					list.add(new PostDeleteEventListenerImpl(cfc));
				if(CommonUtil.POST_INSERT.equals(eventType))
					list.add(new PostInsertEventListenerImpl(cfc));
				if(CommonUtil.POST_LOAD.equals(eventType))
					list.add(new PostLoadEventListenerImpl(cfc));
				if(CommonUtil.POST_UPDATE.equals(eventType))
					list.add(new PostUpdateEventListenerImpl(cfc));

				if(CommonUtil.PRE_DELETE.equals(eventType))
					list.add(new PreDeleteEventListenerImpl(cfc));
				if(CommonUtil.PRE_INSERT.equals(eventType))
					list.add(new PreInsertEventListenerImpl(cfc));
				if(CommonUtil.PRE_LOAD.equals(eventType))
					list.add(new PreLoadEventListenerImpl(cfc));
				if(CommonUtil.PRE_UPDATE.equals(eventType))
					list.add(new PreUpdateEventListenerImpl(cfc));
			}
		}

		// general listener
		if(listener!=null && EventListener.hasEventType(listener.getCFC(),eventType))
			list.add(listener);

		return list;
	}

	private static Object hash(PageContext pc) {
		ApplicationContextPro appContext=(ApplicationContextPro) pc.getApplicationContext();
		return hash(appContext.getORMConfiguration());
	}

	private static String hash(ORMConfiguration ormConf) {
		return ormConf.hash();
	}

	public void createMapping(PageContext pc,Component cfc, ORMConfiguration ormConf,SessionFactoryData data) throws PageException {
		String entityName=HibernateCaster.getEntityName(cfc);
		CFCInfo info=data.getCFC(entityName,null);
		String xml;
		long cfcCompTime = HibernateUtil.getCompileTime(pc,cfc.getPageSource());
		if(info==null || (ORMUtil.equals(info.getCFC(),cfc) ))	{//&& info.getModified()!=cfcCompTime
			DataSource ds = ORMUtil.getDataSource(pc,cfc);
			StringBuilder sb=new StringBuilder();

			long xmlLastMod = loadMapping(sb,ormConf, cfc);
			Element root;
			// create mapping
			if(true || xmlLastMod< cfcCompTime) {//MUSTMUST
				data.reset();
				Document doc=null;
				try {
					doc=CommonUtil.newDocument();
				}catch(Throwable t){t.printStackTrace();}

				root=doc.createElement("hibernate-mapping");
				doc.appendChild(root);
				pc.addPageSource(cfc.getPageSource(), true);
				DataSourceManager manager = pc.getDataSourceManager();
				DatasourceConnection dc = manager.getConnection(pc, ds, null, null);
				try{
					HBMCreator.createXMLMapping(pc,dc,cfc,root,data);
				}
				finally{
					pc.removeLastPageSource(true);
					manager.releaseConnection(pc, dc);
				}
				xml=XMLCaster.toString(root.getChildNodes(),true,true);
				saveMapping(ormConf,cfc,root);
			}
			// load
			else {
				xml=sb.toString();
				root=CommonUtil.toXML(xml).getOwnerDocument().getDocumentElement();
				/*print.o("1+++++++++++++++++++++++++++++++++++++++++");
				print.o(xml);
				print.o("2+++++++++++++++++++++++++++++++++++++++++");
				print.o(root);
				print.o("3+++++++++++++++++++++++++++++++++++++++++");*/

			}
			data.addCFC(entityName,new CFCInfo(HibernateUtil.getCompileTime(pc,cfc.getPageSource()),xml,cfc,ds));
		}

	}

	private static void saveMapping(ORMConfiguration ormConf, Component cfc, Element hm) {
		if(ormConf.saveMapping()){
			Resource res=cfc.getPageSource().getResource();
			if(res!=null){
				res=res.getParentResource().getRealResource(res.getName()+".hbm.xml");
				try{
				CommonUtil.write(res,
						XMLCaster.toString(hm,false,true,
								HibernateSessionFactory.HIBERNATE_3_PUBLIC_ID,
								HibernateSessionFactory.HIBERNATE_3_SYSTEM_ID,
								HibernateSessionFactory.HIBERNATE_3_CHARSET.name()), HibernateSessionFactory.HIBERNATE_3_CHARSET, false);
				}
				catch(Exception e){}
			}
		}
	}

	private static long loadMapping(StringBuilder sb,ORMConfiguration ormConf, Component cfc) {

		Resource res=cfc.getPageSource().getResource();
		if(res!=null){
			res=res.getParentResource().getRealResource(res.getName()+".hbm.xml");
			try{
				sb.append(CommonUtil.toString(res, CommonUtil.UTF8));
				return res.lastModified();
			}
			catch(Exception e){}
		}
		return 0;
	}

	@Override
	public int getMode() {
		//MUST impl
		return MODE_LAZY;
	}

	@Override
	public String getLabel() {
		return "Hibernate";
	}





	@Override
	public ORMConfiguration getConfiguration(PageContext pc) {
		ApplicationContext ac = pc.getApplicationContext();
		if(!ac.isORMEnabled())
			return null;
		return  ac.getORMConfiguration();
	}

	/**
	 * @param pc
	 * @param session
	 * @param entityName name of the entity to get
	 * @param unique create a unique version that can be manipulated
	 * @param init call the nit method of the cfc or not
	 * @return
	 * @throws PageException
	 */
	public Component create(PageContext pc, HibernateORMSession session,String entityName, boolean unique) throws PageException {
		SessionFactoryData data = session.getSessionFactoryData();
		// get existing entity
		Component cfc = _create(pc,entityName,unique,data);
		if(cfc!=null)return cfc;

		SessionFactoryData oldData = getSessionFactoryData(pc, INIT_NOTHING);
		Map<Key, SessionFactory> oldFactories = oldData.getFactories();
		SessionFactoryData newData = getSessionFactoryData(pc, INIT_CFCS);
		Map<Key, SessionFactory> newFactories = newData.getFactories();

		Iterator<Entry<Key, SessionFactory>> it = oldFactories.entrySet().iterator();
		Entry<Key, SessionFactory> e;
		SessionFactory newSF;
		while(it.hasNext()){
			e = it.next();
			newSF = newFactories.get(e.getKey());
			if(e.getValue()!=newSF){
				session.resetSession(pc,newSF,e.getKey(),oldData);
				cfc = _create(pc,entityName,unique,data);
				if(cfc!=null)return cfc;
			}
		}



		ORMConfiguration ormConf = pc.getApplicationContext().getORMConfiguration();
		Resource[] locations = ormConf.getCfcLocations();

		throw ExceptionUtil.createException(data,null,
				"No entity (persitent component) with name ["+entityName+"] found, available entities are ["+ListUtil.listToList(data.getEntityNames(), ", ")+"] ",
				"component are searched in the following directories ["+toString(locations)+"]");

	}


	private String toString(Resource[] locations) {
		if(locations==null) return "";
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<locations.length;i++){
			if(i>0) sb.append(", ");
			sb.append(locations[i].getAbsolutePath());
		}
		return sb.toString();
	}

	private static Component _create(PageContext pc, String entityName, boolean unique, SessionFactoryData data) throws PageException {
		CFCInfo info = data.getCFC(entityName, null);
		if(info!=null) {
			Component cfc = info.getCFC();
			if(unique){
				cfc=(Component)cfc.duplicate(false);
				if(cfc.contains(pc,CommonUtil.INIT))cfc.call(pc, "init",new Object[]{});
			}
			return cfc;
		}
		return null;
	}
}
class CFCInfo {
	private String xml;
	private long modified;
	private Component cfc;
	private DataSource ds;

	public CFCInfo(long modified, String xml, Component cfc, DataSource ds) {
		this.modified=modified;
		this.xml=xml;
		this.cfc=cfc;
		this.ds=ds;
	}
	/**
	 * @return the cfc
	 */
	public Component getCFC() {
		return cfc;
	}
	/**
	 * @return the xml
	 */
	public String getXML() {
		return xml;
	}
	/**
	 * @return the modified
	 */
	public long getModified() {
		return modified;
	}

	public DataSource getDataSource() {
		return ds;
	}

}

