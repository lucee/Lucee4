package main;


import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.webapp.WebAppContext;


/**
 * Runs Lucee as a Java Application
 */
public class RunAsJavaApplication {

    private static final int DEFAULT_PORT = 8080;

    public static void addContextX(Server server, String strContext, String host, String path, 
    		String strWebContext, String strServerContext) throws IOException {

    	if (strWebContext == null) strWebContext = "./web";
    	if (strServerContext == null) strServerContext = "./server";
        
    	strWebContext=new File(strWebContext).getCanonicalPath();
    	strServerContext=new File(strServerContext).getCanonicalPath();
    	
    	WebAppContext webapp = new WebAppContext();
        webapp.setContextPath( strContext);
        server.setHandler(webapp);
        
        ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);

        ContextHandler context = new ContextHandler();
        context.setContextPath(strContext);
        server.setHandler(context);
        
        servletHandler.setHandler(context);
        // Map a servlet onto the container
        ServletHolder servlet = servletHandler.addServletWithMapping(lucee.debug.loader.servlet.CFMLServlet.class, 
        		"*.cfc/*,*.cfm/*,*.cfml/*,*.cfc,*.cfm,*.cfml");
        servlet.setInitOrder(0);
        servlet.setInitParameter("lucee-server-directory", strServerContext);
        servlet = servletHandler.addServletWithMapping(lucee.debug.loader.servlet.LuceeServlet.class,
        		"*.lucee/*,*.luc/*,*.lucee,*.luc");
        servlet.setInitOrder(0);
        servlet.setInitParameter("lucee-server-directory", strServerContext);
        
        
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{ "index.cfm","index.lucee" });
        strWebContext += path;
        resourceHandler.setResourceBase(strWebContext);
        
        
        
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resourceHandler,servletHandler,new DefaultHandler()});
        server.setHandler(handlers);
    }

    

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        try {
            _main(args);
        } catch (MultiException e) {
            List<Throwable> list = e.getThrowables();
            int len = list.size();
            for (int i = 0; i < len; i++) {
                list.get(i).printStackTrace();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static void _main(String[] args)
            throws Exception {
        // Create the server
        int port = DEFAULT_PORT; 
        
     // Create the server
        Server server = new Server(port);
 
        // Create a port listener
        
        
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
        
	    String host = null;

        // Create a context
        File webxml = new File(strServerContext + "/web.xml");
        if (webxml.exists()) {
        	System.err.println("not supported atm");
            //addWebXmlContext(server, "/", host, "/", strWebContext, strServerContext);
        } else {
            addContext(server, "/", host, "/", strWebContext, strServerContext);
            addContext(server, "/sub/", host, "/sub/", strWebContext, strServerContext);
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
        server.join();

    }
    
    
    public static void addContext(Server server, String strContext, String host, String path, String strWebContext, String strServerContext) throws Exception {

    	if (strWebContext == null) strWebContext = "./web";
    	if (strServerContext == null) strServerContext = "./server";
    	strWebContext=new File(strWebContext).getCanonicalPath();
    	strServerContext=new File(strServerContext).getCanonicalPath();
    
    	
        // Create a context 
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath( strContext );
        contextHandler.setResourceBase(strWebContext);
        
        
        
        // Create a servlet container
        ServletHandler servlets = new ServletHandler();
        
        
        // Maps the main servlet onto the container.
        ServletHolder servlet = servlets.addServletWithMapping(lucee.debug.loader.servlet.CFMLServlet.class, 
        		"*.cfc/*,*.cfm/*,*.cfml/*,*.cfc,*.cfm,*.cfml");
        servlet.setInitOrder(0);
        servlet.setInitParameter("lucee-server-directory", strServerContext);
        
        servlet = servlets.addServletWithMapping(lucee.debug.loader.servlet.LuceeServlet.class,
        		"*.lucee/*,*.luc/*,*.lucee,*.luc");
        servlet.setInitOrder(1);
        
        servlet = servlets.addServletWithMapping(lucee.debug.loader.servlet.RESTServlet.class,
        		"/rest/*");
        servlet.setInitOrder(2);
        
        
        servlet.setInitParameter("lucee-server-directory", strServerContext);
        
        
        contextHandler.setHandler(servlets);
        server.setHandler(contextHandler);
        
        
        
        
        // Start the http server
        server.start();
        server.join();
	}
}
