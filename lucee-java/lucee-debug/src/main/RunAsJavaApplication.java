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
package main;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.WebApplicationContext;
import org.mortbay.util.MultiException;

import lucee.print;

import java.io.File;
import java.util.List;

import javax.smartcardio.ATR;


/**
 * Runs Lucee as a Java Application
 */
public class RunAsJavaApplication {

    private static final int DEFAULT_PORT = 8080;

    public static void addContext(HttpServer server, String strContext, String host, String path, String strWebContext, String strServerContext) {

    	if (strWebContext == null) strWebContext = "./web";
    	if (strServerContext == null) strServerContext = "./server";
        //String webContextDir = strWebContext + "/WEB-INF/lucee";
        //String serverContextDir = strWebContext + "/WEB-INF/lucee";
        //if (adminContextDir == null) adminContextDir = appDir + "/WEB-INF/lib/lucee-server";
        //System.out.println("appdir:" + appDir);
        //System.out.println("webcontext:" + webContextDir);
        //System.out.println("servercontext:" + adminContextDir);

        HttpContext context = new HttpContext();
        //context.setClassLoader(new ContextClassloader());
        context.setContextPath(strContext);
        context.addWelcomeFile("index.cfm");

	    if ( host != null && !host.isEmpty() )
	        context.addVirtualHost(host);

        //context.setClassPath(lib);
        server.addContext(context);

        // Create a servlet container
        ServletHandler servlets = new ServletHandler();
        context.addHandler(servlets);

        // Map a servlet onto the container
        ServletHolder servlet = servlets.addServlet("CFMLServlet", "*.cfc/*,*.cfm/*,*.cfml/*,*.cfc,*.cfm,*.cfml", "lucee.debug.loader.servlet.CFMLServlet");
        servlet.setInitOrder(0);

        servlet.setInitParameter("lucee-server-directory", strServerContext);
        //servlet.setInitParameter("lucee-web-directory", strWebContext);

        // Uncomment line below to debug Lucee REST Servlet
        //servlet = servlets.addServlet("RESTServlet", "/rest/*", "lucee.debug.loader.servlet.RESTServlet");

        //servlet = servlets.addServlet("FileServlet","/","servlet.FileServlet");

        /* Uncomment to add remote flash support; toggle block comment by adding/removing a '/' at the beginning of this line
        servlet = servlets.addServlet("openamf","/flashservices/gateway/*,/openamf/gateway/*","servlet.AMFServlet");
        servlet = servlets.addServlet("openamf","/openamf/gateway/*","lucee.loader.servlet.AMFServlet");
        servlet = servlets.addServlet("MessageBrokerServlet","/flashservices/gateway/*,/messagebroker/*,/flex2gateway/*","flex.messaging.MessageBrokerServlet");
        servlet.setInitParameter("services.configuration.file", "/WEB-INF/flex/services-config.xml");
        //*/

        strWebContext += path;
        context.setResourceBase(strWebContext);
        context.addHandler(new ResourceHandler());
    }

    /*public static void addWebXmlContext(HttpServer server, String strContext, String host, String path, String strWebContext, String strServerContext) {

        if (appDir == null) appDir = "./web";
        if (webContextDir == null) webContextDir = appDir + "/WEB-INF/lucee";
        if (adminContextDir == null) adminContextDir = appDir + "/WEB-INF/lib/lucee-server";
        System.out.println("appdir:" + appDir);
        System.out.println("webcontext:" + webContextDir);
        System.out.println("servercontext:" + adminContextDir);
        WebApplicationContext context = new WebApplicationContext(appDir);
        context.setContextPath(strContext);

	    if ( host != null && !host.isEmpty() )
	        context.addVirtualHost(host);

        server.addContext(context);
        appDir += path;
        context.addHandler(new ResourceHandler());
    }*/

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        try {
            _main(args);
        } catch (MultiException e) {
            List list = e.getExceptions();
            int len = list.size();
            for (int i = 0; i < len; i++) {
                ((Throwable) list.get(i)).printStackTrace();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static void _main(String[] args)
            throws Exception {
        // Create the server
        HttpServer server = new HttpServer();
        int port = DEFAULT_PORT; 
        String strWebContext = "./web";
        String strServerContext="./server";
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        if (args.length > 1) {
        	strWebContext = args[1];
        }
        if (args.length > 2) {
        	strServerContext = args[2];
        }
        

        // Create a port listener
        SocketListener listener = new SocketListener();
        
        listener.setPort(port);
        server.addListener(listener);

	    String host = null;

        // Create a context
        File webxml = new File(strServerContext + "/web.xml");
        if (webxml.exists()) {
        	System.err.println("not supported atm");
            //addWebXmlContext(server, "/", host, "/", strWebContext, strServerContext);
        } else {
            addContext(server, "/", host, "/", strWebContext, strServerContext);
        }

        //addContext(server,"/susi/","localhost","/jm/",null,null);
        //addContext(server,"/sub1/","localhost","/subweb1/",null,null);
        //addContext(server,"/sub2/","localhost","/subweb2/",null,null);
        //addContext(server,"/","192.168.2.104","/",null,null);

        //addContext(server,"/","context.example.local","/",null);
        //addContext(server,"/","7of9","/",null);

        //for(int i=1;i<10;i++)
        //    addContext(server,"/","context"+i+".example.local","/context"+i+"/",null,null);


        server.start();

	    if ( host != null && !host.isEmpty() )
		    DesktopUtil.launchBrowser( host, port, false );
    }
}
