/**
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
package lucee.loader.engine;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

import lucee.runtime.CFMLFactory;
import lucee.runtime.PageContext;
import lucee.runtime.video.VideoUtil;
import lucee.runtime.util.Cast;
import lucee.runtime.util.Creation;
import lucee.runtime.util.Decision;
import lucee.runtime.util.Excepton;
import lucee.runtime.util.HTTPUtil;
import lucee.runtime.util.Operation;
import lucee.runtime.util.ResourceUtil;
import lucee.runtime.util.ZipUtil;

/**
 * wrapper for a CFMlEngine
 */
public class CFMLEngineWrapper implements CFMLEngine, EngineChangeListener {
    
    private CFMLEngine engine;

    /**
     * constructor of the class
     * @param engine
     */
    public CFMLEngineWrapper(CFMLEngine engine) {
        this.engine=engine;
    }
    
    /**
     * @see lucee.loader.engine.CFMLEngine#addServletConfig(javax.servlet.ServletConfig)
     */
    public void addServletConfig(ServletConfig config) throws ServletException {
        engine.addServletConfig(config);
    }

    /**
     * @see lucee.loader.engine.CFMLEngine#serviceCFML(javax.servlet.http.HttpServlet, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void serviceCFML(HttpServlet servlet, HttpServletRequest req,
            HttpServletResponse rsp) throws ServletException, IOException {
        engine.serviceCFML(servlet,req,rsp);
    }

    /**
     * @see lucee.loader.engine.CFMLEngine#serviceAMF(javax.servlet.http.HttpServlet, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void serviceAMF(HttpServlet servlet, HttpServletRequest req,
            HttpServletResponse rsp) throws ServletException, IOException {
        engine.serviceAMF(servlet,req,rsp);
    }
    
    /**
     * @see lucee.loader.engine.CFMLEngine#serviceFile(javax.servlet.http.HttpServlet, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void serviceFile(HttpServlet servlet, HttpServletRequest req,
            HttpServletResponse rsp) throws ServletException, IOException {
        engine.serviceFile(servlet,req,rsp);
    }
    

    public void serviceRest(HttpServlet servlet, HttpServletRequest req,
            HttpServletResponse rsp) throws ServletException, IOException {
        engine.serviceRest(servlet,req,rsp);
    }

    /**
     * @see lucee.loader.engine.CFMLEngine#getVersion()
     */
    public String getVersion() {
        return engine.getVersion();
    }

    /**
     * @see lucee.loader.engine.CFMLEngine#getUpdateType()
     */
    public String getUpdateType() {
        return engine.getUpdateType();
    }

    /**
     * @see lucee.loader.engine.CFMLEngine#getUpdateLocation()
     */
    public URL getUpdateLocation() {
        return engine.getUpdateLocation();
    }

    /**
     * @see lucee.loader.engine.CFMLEngine#can(int, java.lang.String)
     */
    public boolean can(int type, String password) {
        return engine.can(type,password);
    }

    /**
     * @see lucee.loader.engine.CFMLEngine#getCFMLEngineFactory()
     */
    public CFMLEngineFactory getCFMLEngineFactory() {
        return engine.getCFMLEngineFactory();
    }

    /**
     * @see lucee.loader.engine.CFMLEngine#reset()
     */
    public void reset() {
        engine.reset();
    }

    /**
     * @see lucee.loader.engine.CFMLEngine#reset(String)
     */
    public void reset(String configId) {
        engine.reset(configId);
    }

    /**
     * @see lucee.loader.engine.EngineChangeListener#onUpdate(lucee.loader.engine.CFMLEngine)
     */
    public void onUpdate(CFMLEngine newEngine) {
        this.engine=newEngine;
    }

    /**
     * @see lucee.loader.engine.CFMLEngine#getCastUtil()
     */
    public Cast getCastUtil() {
        return engine.getCastUtil();
    }

    /**
     * @see lucee.loader.engine.CFMLEngine#getOperatonUtil()
     */
    public Operation getOperatonUtil() {
        return engine.getOperatonUtil();
    }

    /**
     * @see lucee.loader.engine.CFMLEngine#getDecisionUtil()
     */
    public Decision getDecisionUtil() {
        return engine.getDecisionUtil();
    }

    /**
     * @see lucee.loader.engine.CFMLEngine#getExceptionUtil()
     */
    public Excepton getExceptionUtil() {
        return engine.getExceptionUtil();
    }

    /**
     * @see lucee.loader.engine.CFMLEngine#getCreationUtil()
     */
    public Creation getCreationUtil() {
        return engine.getCreationUtil();
    }

	/**
	 *
	 * @see lucee.loader.engine.CFMLEngine#getCFMLFactory(javax.servlet.ServletContext, javax.servlet.ServletConfig, javax.servlet.http.HttpServletRequest)
	 */
	public CFMLFactory getCFMLFactory(ServletContext srvContext, ServletConfig srvConfig, HttpServletRequest req) throws ServletException {
		return engine.getCFMLFactory(srvContext, srvConfig, req);
	}

	/**
	 * @see lucee.loader.engine.CFMLEngine#getBlazeDSUtil()
	 */
	public Object getBlazeDSUtil() {
		return engine.getBlazeDSUtil();
	}

	/**
	 * @see lucee.loader.engine.CFMLEngine#getFDController()
	 */
	public Object getFDController() {
		return engine.getFDController();
	}

	/**
	 * @see lucee.loader.engine.CFMLEngine#getHTTPUtil()
	 */
	public HTTPUtil getHTTPUtil() {
		return engine.getHTTPUtil();
	}

	/**
	 * @see lucee.loader.engine.CFMLEngine#getResourceUtil()
	 */
	public ResourceUtil getResourceUtil() {
		return engine.getResourceUtil();
	}

	/**
	 * @see lucee.loader.engine.CFMLEngine#getThreadPageContext()
	 */
	public PageContext getThreadPageContext() {
		return engine.getThreadPageContext();
	}

	/**
	 * @see lucee.loader.engine.CFMLEngine#getVideoUtil()
	 */
	public VideoUtil getVideoUtil() {
		return engine.getVideoUtil();
	}

	/**
	 * @see lucee.loader.engine.CFMLEngine#getZipUtil()
	 */
	public ZipUtil getZipUtil() {
		return engine.getZipUtil();
	}

	/**
	 * @see lucee.loader.engine.CFMLEngine#getState()
	 */
	public String getState() {
		return engine.getState();
	}
	
	/**
	 * this interface is new to this class and not offically part of Lucee 3.x, do not use outside the loader
	 * @param other
	 * @param checkReferenceEqualityOnly
	 * @return
	 */
	public boolean equalTo(CFMLEngine other, boolean checkReferenceEqualityOnly) {
		while(other instanceof CFMLEngineWrapper)
			other=((CFMLEngineWrapper)other).engine;
		if(checkReferenceEqualityOnly) return engine==other;
		return engine.equals(other);
	}

	@Override
	public void cli(Map<String, String> config, ServletConfig servletConfig) throws IOException, JspException, ServletException {
		engine.cli(config, servletConfig);
	}

	@Override
	public void registerThreadPageContext(PageContext pc) {
		engine.registerThreadPageContext(pc);
	}
}
