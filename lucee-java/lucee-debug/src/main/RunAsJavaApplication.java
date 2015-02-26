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
        
        HttpContext context = new HttpContext();
        context.setContextPath(strContext);
        context.addWelcomeFile("index.cfm");

	    if ( host != null && !host.isEmpty() )
	        context.addVirtualHost(host);

        server.addContext(context);

        // Create a servlet container
        ServletHandler servlets = new ServletHandler();
        context.addHandler(servlets);

        // Map a servlet onto the container
        ServletHolder cfml = servlets.addServlet("CFMLServlet", "*.cfc/*,*.cfm/*,*.cfml/*,*.cfc,*.cfm,*.cfml", "lucee.debug.loader.servlet.CFMLServlet");
        cfml.setInitOrder(0);

        cfml.setInitParameter("lucee-server-directory", strServerContext);
        
        // Lucee REST Servlet
        ServletHolder rest = servlets.addServlet("RESTServlet", "/rest/*", "lucee.debug.loader.servlet.RESTServlet");
        rest.setInitOrder(0);

        strWebContext += path;
        context.setResourceBase(strWebContext);
        context.addHandler(new ResourceHandler());
    }

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
        
        server.start();

	    if ( host != null && !host.isEmpty() )
		    DesktopUtil.launchBrowser( host, port, false );
    }
}
