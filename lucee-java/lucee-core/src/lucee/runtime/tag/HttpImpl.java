package lucee.runtime.tag;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.URLEncoder;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient4.HTTPResponse4Impl;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.http.ReqRspUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;

public class HttpImpl implements Http,BodyTag {
	
	private Http instance;

	public HttpImpl(){
		try{
			instance = new Http41(); // try to use the implemenation based on the newer apache library
		}
		catch(Throwable t){
			ExceptionUtil.rethrowIfNecessary(t);
			instance = new Http4(); // if it fails we have a fallback to the old implementation
		}
	}

	@Override
	public void setParam(HttpParamBean param) {
		instance.setParam(param);
	}

	@Override
	public void setFirstrowasheaders(boolean firstrowasheaders) {
		instance.setFirstrowasheaders(firstrowasheaders);
	}

	@Override
	public void setEncodeurl(boolean encoded) {
		instance.setEncodeurl(encoded);
	}

	@Override
	public void setPassword(String password) {
		instance.setPassword(password);
	}

	@Override
	public void setProxypassword(String proxypassword) {
		instance.setProxypassword(proxypassword);
	}

	@Override
	public void setDelimiter(String delimiter) {
		instance.setDelimiter(delimiter);
	}

	@Override
	public void setResolveurl(boolean resolveurl) {
		instance.setResolveurl(resolveurl);
	}

	@Override
	public void setPreauth(boolean preauth) {
		instance.setPreauth(preauth);
	}

	@Override
	public void setTimeout(Object timeout) throws PageException {
		instance.setTimeout(timeout);
	}

	@Override
	public void setProxyserver(String proxyserver) {
		instance.setProxyserver(proxyserver);
	}

	@Override
	public void setProxyport(double proxyport) {
		instance.setProxyport(proxyport);
	}

	@Override
	public void setFile(String file) {
		instance.setFile(file);
	}

	@Override
	public void setThrowonerror(boolean throwonerror) {
		instance.setThrowonerror(throwonerror);
	}

	@Override
	public void setCharset(String charset) {
		instance.setCharset(charset);
	}

	@Override
	public void setColumns(String columns) throws PageException {
		instance.setColumns(columns);
	}

	@Override
	public void setPort(double port) {
		instance.setPort(port);
	}

	@Override
	public void setUseragent(String useragent) {
		instance.setUseragent(useragent);
	}

	@Override
	public void setTextqualifier(String textqualifier) {
		instance.setTextqualifier(textqualifier);
	}

	@Override
	public void setProxyuser(String proxyuser) {
		instance.setProxyuser(proxyuser);
	}

	@Override
	public void setUsername(String username) {
		instance.setUsername(username);
	}

	@Override
	public void setUrl(String url) {
		instance.setUrl(url);
	}

	@Override
	public void setRedirect(boolean redirect) {
		instance.setRedirect(redirect);
	}

	@Override
	public void setPath(String path) {
		instance.setPath(path);
	}

	@Override
	public void setName(String name) {
		instance.setName(name);
	}

	@Override
	public void setAuthtype(String strAuthType) throws PageException {
		instance.setAuthtype(strAuthType);
	}

	@Override
	public void setWorkstation(String workStation) {
		instance.setWorkstation(workStation);
	}

	@Override
	public void setDomain(String domain) {
		instance.setDomain(domain);
	}

	@Override
	public void setMethod(String method) throws PageException {
		instance.setMethod(method);
	}

	@Override
	public void setCompression(String strCompression) throws PageException {
		instance.setCompression(strCompression);
	}

	@Override
	public void setGetasbinary(String getAsBinary) {
		instance.setGetasbinary(getAsBinary);
	}

	@Override
	public void setMultipart(boolean multiPart) {
		instance.setMultipart(multiPart);
	}

	@Override
	public void setMultiparttype(String multiPartType) throws PageException {
		instance.setMultiparttype(multiPartType);
	}

	@Override
	public void setResult(String result) {
		instance.setResult(result);
	}

	@Override
	public void setAddtoken(boolean addtoken) {
		instance.setAddtoken(addtoken);
	}


	@Override
	public int doAfterBody() throws JspException {
		return instance.doAfterBody();
	}

	@Override
	public int doEndTag() throws JspException {
		return instance.doEndTag();
	}

	@Override
	public int doStartTag() throws JspException {
		return instance.doStartTag();
	}

	@Override
	public Tag getParent() {
		return instance.getParent();
	}

	@Override
	public void release() {
		instance.release();
	}

	@Override
	public void setPageContext(PageContext pc) {
		instance.setPageContext(pc);
	}

	@Override
	public void setParent(Tag arg0) {
		instance.setParent(arg0);
	}

	@Override
	public void doInitBody() throws JspException {
		instance.doInitBody();
	}

	@Override
	public void setBodyContent(BodyContent arg0) {
		instance.setBodyContent(arg0);
	}

	static String headerValue(String value) {
		if(value==null) return null;
		value=value.trim();
		value=value.replace('\n', ' ');
		value=value.replace('\r', ' ');
		return value;
	}
	
	static boolean hasHeaderIgnoreCase(HttpRequestBase req,String name) {
		org.apache.http.Header[] headers = req.getAllHeaders();
		if(headers==null) return false;
		for(int i=0;i<headers.length;i++){
			if(name.equalsIgnoreCase(headers[i].getName())) return true;
		}
		return false;
	}

    static String urlenc(String str, String charset) throws UnsupportedEncodingException {
    	if(!ReqRspUtil.needEncoding(str,false)) return str;
    	return URLEncoder.encode(str,charset);
    }
	
	static Charset getCharset(String strCharset) {
		if(!StringUtil.isEmpty(strCharset,true)) 
			return CharsetUtil.toCharset(strCharset);
		return CharsetUtil.getWebCharset();
	}
	
	static String getContentType(HttpParamBean param) {
		String mimeType=param.getMimeType();
		if(StringUtil.isEmpty(mimeType,true)) {
			mimeType=ResourceUtil.getMimeType(param.getFile(), ResourceUtil.MIMETYPE_CHECK_EXTENSION+ResourceUtil.MIMETYPE_CHECK_HEADER, null);
		}
		return mimeType;
	}
    
	static boolean isGzipEncoded(String contentEncoding) {
		return !StringUtil.isEmpty(contentEncoding) && StringUtil.indexOfIgnoreCase(contentEncoding, "gzip")!=-1;
	}

	static boolean isStatusOK(int statusCode) {
		return statusCode>=200 && statusCode<=299;
	}

	public static Object getOutput(InputStream is, String contentType, String contentEncoding, boolean closeIS) {
		if(StringUtil.isEmpty(contentType))contentType="text/html";
		
		// Gzip
		if(HttpImpl.isGzipEncoded(contentEncoding)){
			try {
				is=new GZIPInputStream(is);
			} 
			catch (IOException e) {}
		}
		
		try {
			// text
			if(HTTPUtil.isTextMimeType(contentType)) {
				String[] tmp = HTTPUtil.splitMimeTypeAndCharset(contentType,null);
				Charset cs=HttpImpl.getCharset(tmp[1]);
				
				try {
					return IOUtil.toString(is, cs);
				} catch (IOException e) {}
			}
			// Binary
			else {
				try {
					return IOUtil.toBytes(is);
				} 
				catch (IOException e) {}
			}
		}
		finally{
			if(closeIS)IOUtil.closeEL(is);
		}

		return "";
	}
	
	static URL locationURL(HttpUriRequest req, HttpResponse rsp) {
		URL url=null;
		try {
			url = req.getURI().toURL();
		} catch (MalformedURLException e1) {
			return null;
		}
		
		Header h = HTTPResponse4Impl.getLastHeaderIgnoreCase(rsp, "location");
		if(h!=null) {
			String str = h.getValue();
			try {
				return new URL(str);
			} catch (MalformedURLException e) {
				try {
					return new URL(url.getProtocol(), url.getHost(), url.getPort(), mergePath(url.getFile(), str));
					
				} catch (MalformedURLException e1) {
					return null;
				}
			}
		}
		return null;
	}

    /**
     * merge to pathes to one
     * @param current
     * @param relPath
     * @return
     * @throws MalformedURLException
     */
    static String mergePath(String current, String relPath) throws MalformedURLException {
        
        // get current directory
        String currDir;
        if(current==null || current.indexOf('/')==-1)currDir="/";
        else if(current.endsWith("/"))currDir=current;
        else currDir=current.substring(0,current.lastIndexOf('/')+1);
        
        // merge together
        String path;
        if(relPath.startsWith("./"))path=currDir+relPath.substring(2);
        else if(relPath.startsWith("/"))path=relPath;
        else if(!relPath.startsWith("../"))path=currDir+relPath;
        else {
            while(relPath.startsWith("../") || currDir.length()==0) {
                relPath=relPath.substring(3);
                currDir=currDir.substring(0,currDir.length()-1);
                int index = currDir.lastIndexOf('/');
                if(index==-1)throw new MalformedURLException("invalid relpath definition for URL");
                currDir=currDir.substring(0,index+1);
            }
            path=currDir+relPath;
        }
        
        return path;
    }
    	
	/**
     * checks if status code is a redirect
     * @param status
     * @return is redirect
     */
    
	static boolean isRedirect(int status) {
    	return 
        	status==STATUS_REDIRECT_FOUND || 
        	status==STATUS_REDIRECT_MOVED_PERMANENTLY ||
        	status==STATUS_REDIRECT_SEE_OTHER ||
        	status==STATUS_REDIRECT_TEMPORARY_REDIRECT;
    }
}
