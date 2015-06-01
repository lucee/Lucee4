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
package lucee.runtime.tag;

import javax.servlet.jsp.tagext.BodyTag;

import lucee.runtime.exp.PageException;

public interface Http extends BodyTag {
	public static final String MULTIPART_RELATED = "multipart/related";
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";
	
	/**
     * Maximum redirect count (5)
     */
	public static final short MAX_REDIRECT=15;
	
    /**
     * Constant value for HTTP Status Code "moved Permanently 301"
     */
    public static final int STATUS_REDIRECT_MOVED_PERMANENTLY=301;
    /**
     * Constant value for HTTP Status Code "Found 302"
     */
    public static final int STATUS_REDIRECT_FOUND=302;
    /**
     * Constant value for HTTP Status Code "see other 303"
     */
    public static final int STATUS_REDIRECT_SEE_OTHER=303;
    

    public static final int STATUS_REDIRECT_TEMPORARY_REDIRECT = 307;

	
	public void setParam(HttpParamBean param);
	
	public void setFirstrowasheaders(boolean firstrowasheaders);
	
	public void setEncodeurl(boolean encoded);
	
	public void setPassword(String password);
	
	public void setProxypassword(String proxypassword);
	
	public void setDelimiter(String delimiter);
	
	public void setResolveurl(boolean resolveurl);
	
	public void setPreauth(boolean preauth);
	
	public void setTimeout(double timeout) throws PageException;
	
	public void setProxyserver(String proxyserver);
	
	public void setProxyport(double proxyport);
	
	public void setFile(String file);
	
	public void setThrowonerror(boolean throwonerror);
	
	public void setCharset(String charset);
	
	public void setColumns(String columns) throws PageException;
	
	public void setPort(double port);
	
	public void setUseragent(String useragent);
	
	public void setTextqualifier(String textqualifier);
	
	public void setProxyuser(String proxyuser);
	
	public void setUsername(String username);
	
	public void setUrl(String url);
	
	public void setRedirect(boolean redirect);
	
	public void setPath(String path);
	
	public void setName(String name);
	
	public void setAuthtype(String strAuthType) throws PageException;
	
	public void setWorkstation(String workStation);
	
	public void setDomain(String domain);
	
	public void setMethod(String method) throws PageException;
	
	public void setCompression(String strCompression) throws PageException;
	
	public void setGetasbinary(String getAsBinary);
	
	public void setMultipart(boolean multiPart);
	
	public void setMultiparttype(String multiPartType) throws PageException;
	
	public void setResult(String result);
	
	public void setAddtoken(boolean addtoken);

}