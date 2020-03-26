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
package lucee.runtime.schedule;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.ContentType;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.security.Credentials;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.other.CreateUUID;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.util.URLResolver;


class ExecutionThread extends Thread {

	private Config config;
	//private Log log;
	private ScheduleTask task;
	private String charset;
	private enum STATE {
		RUNNING,
		NOTRUNNING
	}
	private static Map<String, STATE> taskState = new ConcurrentHashMap<String, STATE>();

	public ExecutionThread(Config config, ScheduleTask task, String charset) {
		this.config=config;
		this.task=task;
		this.charset=charset;
	}

	public void run() {
		execute(config, task, charset);
	}
	
	private static String _taskKey( ScheduleTask task ) {
		return  ((ScheduleTaskImpl)task).md5();
	}
	
	private static void _initializeTaskState( ScheduleTask task ) {
		String key = _taskKey( task );
        if ( !taskState.containsKey( key ) ) {
        	taskState.put(key, STATE.NOTRUNNING);
        }
	}
	
	private static boolean _isRunning( ScheduleTask task) {
		String key = _taskKey( task );
		return taskState.get( key ) == STATE.RUNNING;
	}
	
	private static void _markAsRunning( ScheduleTask task ) {
		String key = _taskKey( task );
		taskState.put(key, STATE.RUNNING);
	}

	private static void _markAsNotRunning( ScheduleTask task ) {
		String key = _taskKey( task );
		taskState.put(key, STATE.NOTRUNNING);
	}
	
	public static void execute(Config config, ScheduleTask task, String charset) {
		Log log = getLog(config);
		boolean hasError=false;
        String logName="schedule task:"+task.getTask();
        
        _initializeTaskState( task );

        if ( _isRunning( task) ) {
        	log.warn(logName, "Scheduled task skipped because it was already running: " + task.getUrl());
        	return;
        }
        _markAsRunning( task );
        
        try {
	       // init
	        //HttpClient client = new HttpClient();
	        //client.setStrictMode(false);
	        //HttpState state = client.getState();
	        
	        String url;
	        if(task.getUrl().getQuery()==null)
	        	url=task.getUrl().toExternalForm()+"?RequestTimeout="+(task.getTimeout()/1000);
	        else if(StringUtil.isEmpty(task.getUrl().getQuery()))
	        	url=task.getUrl().toExternalForm()+"RequestTimeout="+(task.getTimeout()/1000);
	        else {
	        	if(StringUtil.indexOfIgnoreCase(task.getUrl().getQuery()+"", "RequestTimeout")!=-1)
	        		url=task.getUrl().toExternalForm();
	        	else
	        		url=task.getUrl().toExternalForm()+"&RequestTimeout="+(task.getTimeout()/1000);
	        }
	        
	        //HttpMethod method = new GetMethod(url);
	        //HostConfiguration hostConfiguration = client.getHostConfiguration();
	        
	        Header[] headers=new Header[]{
	        	HTTPEngine.header("User-Agent","CFSCHEDULE")
	        };
			//method.setRequestHeader("User-Agent","CFSCHEDULE");
	        
	       // Userame / Password
	        Credentials credentials = task.getCredentials();
	        String user=null,pass=null;
	        if(credentials!=null) {
	        	user=credentials.getUsername();
	        	pass=credentials.getPassword();
	            //get.addRequestHeader("Authorization","Basic admin:spwwn1p");
	        }
	        
	        // Proxy
	        ProxyData proxy=task.getProxyData();
	        if(!ProxyDataImpl.isValid(proxy) && config.isProxyEnableFor(task.getUrl().getHost())) {
	        	proxy=config.getProxyData();
	        }
	        
	        HTTPResponse rsp=null;
	       
	        // execute
	        try {
	        	rsp = HTTPEngine.get(new URL(url), user, pass, task.getTimeout(),HTTPEngine.MAX_REDIRECT, charset, null, proxy, headers);
	        } catch (Exception e) {
	        	
	            LogUtil.log(log,Log.LEVEL_ERROR,logName,e);
	            hasError=true;
	        }
	        
	        // write file
	        Resource file = task.getResource();
	        if(!hasError && file!=null && task.isPublish()) {
	        	String n=file.getName();
	        	if(n.indexOf("{id}")!=-1){
	        		n=StringUtil.replace(n, "{id}",CreateUUID.invoke(), false);	
	        		file=file.getParentResource().getRealResource(n);
	        	}
	        	
		        if(isText(rsp) && task.isResolveURL()) {
		        	
	        	    String str;
	                try {
	                    InputStream stream = rsp.getContentAsStream();
	                    str = stream==null?"":IOUtil.toString(stream,(Charset)null);
	                    if(str==null)str="";
	                } 
	                catch (IOException e) {
	                	str=e.getMessage();
	                }
	        	    
	        	    try {
	                    str=new URLResolver().transform(str,task.getUrl(),false);
	                } catch (PageException e) {
	                    LogUtil.log(log,Log.LEVEL_ERROR,logName,e);
	                    hasError=true;
	                }
	        	    try {
	                    IOUtil.write(file,str,charset,false);
	                } 
	                catch (IOException e) {
	                    LogUtil.log(log,Log.LEVEL_ERROR,logName,e);
	                    hasError=true;
	                }
		        }
		        else {
		        	//print.out("1111111111111111111111111111111");
		            try {
	                    IOUtil.copy(
	                            rsp.getContentAsStream(),
	                            file,
	                            true
	                    );
	                    //new File(file.getAbsolutePath()).write(method.getResponseBodyAsStream());
	                } 
	                catch (IOException e) {
	                    LogUtil.log(log,Log.LEVEL_ERROR,logName,e);
	                    hasError=true;
	                }
		        }
	        }
	        if(!hasError)log.log(Log.LEVEL_INFO,logName,"executed");
        } finally {
            _markAsNotRunning( task );
        }
	}
	
    private static Log getLog(Config config) {
		return ((ConfigImpl)config).getLog("scheduler");
	}

	private static boolean isText(HTTPResponse rsp) {
    	ContentType ct = rsp.getContentType();
        if(ct==null)return true;
        String mimetype = ct.getMimeType();
        return
        	mimetype == null ||  mimetype.startsWith("text") || mimetype.startsWith("application/octet-stream");
        
    }
	
}
