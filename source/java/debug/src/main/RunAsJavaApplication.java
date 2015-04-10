package main;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.component.LifeCycle.Listener;
import org.eclipse.jetty.webapp.WebAppContext;


/**
 * Runs Lucee as a Java Application
 */
public class RunAsJavaApplication {

    private static final int DEFAULT_PORT = 8080;


    

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
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath( strContext );
        contextHandler.setResourceBase(strWebContext);
        
/*
        // Create the SessionHandler (wrapper) to handle the sessions
           HashSessionManager manager = new HashSessionManager();
           SessionHandler sessionHandler = new SessionHandler(manager);
           sessionHandler.setHandler(new MyDumpHandler());
           webapp.setSessionHandler(sessionHandler);
           */
        
        
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

class MyDumpHandler extends AbstractHandler
{
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        PrintWriter out = response.getWriter();
        response.setContentType("text/plain");

        try
        {
            HttpSession session = request.getSession();
            if (session.isNew())
            {
                out.printf("New Session: %s%n", session.getId());
            }
            else
            {
                out.printf("Old Session: %s%n", session.getId());
            }
        }
        catch (IllegalStateException ex)
        {
            out.println("Exception!" + ex);
            ex.printStackTrace(out);
        }
        out.close();
    }
}
