/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.cli.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import lucee.cli.util.EnumerationWrapper;

public class ServletContextImpl implements ServletContext {
	private Map<String, Object> attributes;
	private Map<String, String> parameters;
	private int majorVersion;
	private int minorVersion;
	private File root;

	public ServletContextImpl(File root, Map<String, Object> attributes,
			Map<String, String> parameters, int majorVersion, int minorVersion) {
		this.root = root;
		this.attributes = attributes;
		this.parameters = parameters;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
	}

	/**
	 * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	/**
	 * @see javax.servlet.ServletContext#getAttributeNames()
	 */
	public Enumeration<String> getAttributeNames() {
		return new EnumerationWrapper<String>(attributes);
	}

	/**
	 * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
	 */
	public String getInitParameter(String key) {
		return parameters.get(key);
	}

	/**
	 * @see javax.servlet.ServletContext#getInitParameterNames()
	 */
	public Enumeration<String> getInitParameterNames() {
		return new EnumerationWrapper<String>(parameters);
	}

	/**
	 * @see javax.servlet.ServletContext#getMajorVersion()
	 */
	public int getMajorVersion() {
		return majorVersion;
	}

	/**
	 * @see javax.servlet.ServletContext#getMinorVersion()
	 */
	public int getMinorVersion() {
		return minorVersion;
	}

	/**
	 * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
	 */
	public String getMimeType(String file) {
		throw notSupported("getMimeType(String file)");
	}

	/**
	 * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
	 */
	public String getRealPath(String realpath) {
		return getRealFile(realpath).getAbsolutePath();
	}

	/**
	 * @see javax.servlet.ServletContext#getResource(java.lang.String)
	 */
	public URL getResource(String realpath) throws MalformedURLException {
		File file = getRealFile(realpath);
		return file.toURI().toURL();
	}

	/**
	 * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
	 */
	public InputStream getResourceAsStream(String realpath) {
		try {
			return new FileInputStream(getRealFile(realpath));
		} catch (IOException e) {
			return null;
		}
	}

	public File getRealFile(String realpath) {
		return new File(root, realpath);
	}

	public File getRoot() {
		return root;
	}

	public Set<String> getResourcePaths(String realpath) {
		throw notSupported("getResourcePaths(String realpath)");
	}

	public RequestDispatcher getRequestDispatcher(String path) {
		throw notSupported("getNamedDispatcher(String name)");
	}

	public ServletContext getContext(String key) {
		// TODO ?
		return this;
	}

	public RequestDispatcher getNamedDispatcher(String name) {
		throw notSupported("getNamedDispatcher(String name)");
	}

	/**
	 * @see javax.servlet.ServletContext#log(java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void log(String msg, Throwable t) {// TODO better
		if (t == null)
			System.out.println(msg);
		else
			System.out.println(msg + ":\n" + t.getMessage());

		//if(t==null)log.log(Log.LEVEL_INFO, "ServletContext", msg);
		//else log.log(Log.LEVEL_ERROR, "ServletContext", msg+":\n"+ExceptionUtil.getStacktrace(t,false));
	}

	/**
	 * @see javax.servlet.ServletContext#log(java.lang.Exception,
	 *      java.lang.String)
	 */
	public void log(Exception e, String msg) {
		log(msg, e);
	}

	/**
	 * @see javax.servlet.ServletContext#log(java.lang.String)
	 */
	public void log(String msg) {
		log(msg, null);
	}

	/**
	 * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String key) {
		attributes.remove(key);
	}

	/**
	 * @see javax.servlet.ServletContext#setAttribute(java.lang.String,
	 *      java.lang.Object)
	 */
	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}

	public String getServletContextName() {
		// can return null
		return null;
	}

	public String getServerInfo() {
		// deprecated
		throw notSupported("getServlet()");
	}

	public Servlet getServlet(String arg0) throws ServletException {
		// deprecated
		throw notSupported("getServlet()");
	}

	public Enumeration<String> getServletNames() {
		// deprecated
		throw notSupported("getServlet()");
	}

	public Enumeration<Servlet> getServlets() {
		// deprecated
		throw notSupported("getServlet()");
	}

	private RuntimeException notSupported(String method) {
		throw new RuntimeException(new ServletException("method " + method
				+ " not supported"));
	}

	@Override
	public Dynamic addFilter(String arg0, String arg1) {
		throw notSupported("");
	}

	@Override
	public Dynamic addFilter(String arg0, Filter arg1) {
		throw notSupported("");
	}

	@Override
	public Dynamic addFilter(String arg0, Class<? extends Filter> arg1) {
		throw notSupported("");
	}

	@Override
	public void addListener(String arg0) {
		throw notSupported("");
	}

	@Override
	public <T extends EventListener> void addListener(T arg0) {
		throw notSupported("");
	}

	@Override
	public void addListener(Class<? extends EventListener> arg0) {
		throw notSupported("");
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
			String arg1) {
		throw notSupported("");
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
			Servlet arg1) {
		throw notSupported("");
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
			Class<? extends Servlet> arg1) {
		throw notSupported("addServlet");
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> arg0)
			throws ServletException {
		throw notSupported("createFilter");
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> arg0)
			throws ServletException {
		throw notSupported("createListener");
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> arg0)
			throws ServletException {
		throw notSupported("createServlet");
	}

	@Override
	public void declareRoles(String... arg0) {
		throw notSupported("declareRoles(String ...)");
		
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.getClass().getClassLoader();
	}

	@Override
	public String getContextPath() {
		return root.getAbsolutePath();
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		throw notSupported("getDefaultSessionTrackingModes()");
	}

	@Override
	public int getEffectiveMajorVersion() {
		return getMajorVersion();
	}

	@Override
	public int getEffectiveMinorVersion() {
		return getMinorVersion();
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		throw notSupported("getEffectiveSessionTrackingModes()");
	}

	@Override
	public FilterRegistration getFilterRegistration(String arg0) {
		throw notSupported("getFilterRegistration(String)");
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		throw notSupported("getFilterRegistrations()");
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		throw notSupported("getJspConfigDescriptor()");
	}

	@Override
	public ServletRegistration getServletRegistration(String arg0) {
		throw notSupported("getServletRegistration(String)");
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		throw notSupported("getServletRegistrations()");
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		throw notSupported("getSessionCookieConfig()");
	}

	@Override
	public String getVirtualServerName() {
		throw notSupported("getVirtualServerName()");
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		if(!parameters.containsKey(name)) {
			this.parameters.put(name, value);
			return true;
		}
		return false;
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> arg0) {
		throw notSupported("setSessionTrackingModes(Set<SessionTrackingMode>) ");
		
	}

}