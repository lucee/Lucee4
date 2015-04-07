package lucee.debug.loader.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.osgi.OSGiUtil;

/**
 * 
 */
public final class CFMLEngineFactoryDummy extends CFMLEngineFactory {

    private static CFMLEngineFactoryDummy factory;
    private CFMLEngine engine;
	
    
    
    
    private CFMLEngineFactoryDummy(ServletConfig config) {
    	super(config);
    	engine = CFMLEngineImpl.getInstance(this,null);
        
        
        InputStream is=null;
        // addd lucee.core dummy ()
        try {
        	is=getClass().getClassLoader().getResourceAsStream("resource/lib/lucee-core-dummy.jar");
        	OSGiUtil.installBundle(
					engine.getBundleContext()
					, is,true,true);
			
		} 
        catch (Throwable t) {
			t.printStackTrace();
		}
        finally {
        	IOUtil.closeEL(is);
        }
    }
    
    /**
     * returns instance of this factory (singelton-> always the same instance)
     * @param config
     * @return Singelton Instance of the Factory
     * @throws ServletException 
     */
    public static CFMLEngine getInstance(ServletConfig config) throws ServletException {
    	
    	if(factory==null) {
    		factory=new CFMLEngineFactoryDummy(config);
    		CFMLEngineFactory.registerInstance(factory.engine);
    		
    	}
    	factory.engine.addServletConfig(config);
        
        return factory.engine;
    }


    /**
     * @see lucee.loader.engine.CFMLEngineFactory#getResourceRoot()
     */
    public File getResourceRoot() throws IOException {
    	
    	String path=SystemUtil.parsePlaceHolder(config.getInitParameter("lucee-server-directory"), config.getServletContext());
    	path=StringUtil.replace(path, "webroot", "work", true);
    	//print.err(path);
        return new File(path);
    }

    /**
     * @see lucee.loader.engine.CFMLEngineFactory#restart(java.lang.String)
     */
    public boolean restart(String password) throws IOException, ServletException {
        engine.reset();
        return true;
    }

    /**
     * @see lucee.loader.engine.CFMLEngineFactory#update(java.lang.String)
     */
    public boolean update(String password) throws IOException, ServletException {
        return true;
    }
    
    

}
