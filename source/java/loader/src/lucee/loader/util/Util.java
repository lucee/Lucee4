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
package lucee.loader.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipFile;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;

/**
 * Util class for different little jobs
 */
public class Util {

	private static File tempFile;
	private static File homeFile;

	private final static SimpleDateFormat HTTP_TIME_STRING_FORMAT;
	static {
		HTTP_TIME_STRING_FORMAT = new SimpleDateFormat(
				"EE, dd MMM yyyy HH:mm:ss zz", Locale.ENGLISH);
		HTTP_TIME_STRING_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance.getIOUtil().copy(...)
	 *             copy a inputstream to a outputstream
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public final static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[0xffff];
        int len;
        while((len = in.read(buffer)) !=-1)
          out.write(buffer, 0, len);
        
        closeEL(in);
        closeEL(out);
	}
    
	@Deprecated
    public final static void copy(InputStream in, OutputStream out, boolean closeIS, boolean closeOS) throws IOException {
        byte[] buffer = new byte[0xffff];
        int len;
        while((len = in.read(buffer)) !=-1)
          out.write(buffer, 0, len);
        
        if(closeIS)closeEL(in);
        if(closeOS)closeEL(out);
    }

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance.getIOUtil().toString
	 *             (InputStream is, Charset cs)
	 *             read String data from a InputStream and returns it as String
	 *             Object
	 * @param is InputStream to read data from.
	 * @return readed data from InputStream
	 * @throws IOException
	 */
	public static String toString(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is)); 
        StringBuffer content=new StringBuffer();
        
        String line=br.readLine();
        if(line!=null) {
            content.append(line);
            while((line=br.readLine())!=null)   {
                content.append("\n"+line);
            }
        }
        br.close();
        return content.toString();
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance.getCastUtil().toBooleanValue
	 *             (...)
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static boolean toBooleanValue(String str) throws IOException {
		str=str.trim().toLowerCase();

		if("true".equals(str)) return true;
		if("false".equals(str)) return false;
		if("yes".equals(str)) return true;
		if("no".equals(str)) return false;
		throw new IOException("can't cast string to a boolean value");
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance.getIOUtil().closeSilent
	 *             (InputStream is,OutputStream os)
	 *             close inputstream without a Exception
	 * @param is
	 * @param os
	 */
	public static void closeEL(InputStream is, OutputStream os) {
		closeEL(is);
		closeEL(os);
	}

	/**
	 * @deprecated no replacement
	 * @param zf
	 */
	public static void closeEL(ZipFile zf) {
		try {
			if(zf!=null)zf.close();
		} 
		catch (Throwable e) {}
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance.getIOUtil().closeSilent
	 *             (InputStream is)
	 *             close inputstream without a Exception
	 * @param is
	 */
	public static void closeEL(InputStream is) {
		try {
			if(is!=null)is.close();
		} 
		catch (Throwable e) {}
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance.getIOUtil().closeSilent(Reader
	 *             r)
	 *             close reader without a Exception
	 * @param is
	 */
	public static void closeEL(Reader r) {
		try {
			if(r!=null)r.close();
		} 
		catch (Throwable e) {}
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance.getIOUtil().closeSilent(Writer
	 *             w)
	 *             close reader without a Exception
	 * @param is
	 */
	public static void closeEL(Writer w) {
		try {
			if(w!=null)w.close();
		} 
		catch (Throwable e) {}
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance.getIOUtil().closeSilent
	 *             (InputStream is,OutputStream os)
	 *             close outputstream without a Exception
	 * @param os
	 */
	public static void closeEL(OutputStream os) {
		try {
			if(os!=null)os.close();
		} 
		catch (Throwable e) {}
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance.getIOUtil().toString(...)
	 * @param is inputStream to get content From
	 * @param charset
	 * @return returns content from a file inputed by input stream
	 * @throws IOException
	 * @throws PageException
	 */
	public static String getContentAsString(InputStream is, String charset)
			throws IOException, PageException {
        BufferedReader br = (charset==null)?
                new BufferedReader(new InputStreamReader(is)):
                new BufferedReader(new InputStreamReader(is,charset)); 
        StringBuffer content=new StringBuffer();
        
        String line=br.readLine();
        if(line!=null) {
            content.append(line);
            while((line=br.readLine())!=null)   {
                content.append("\n"+line);
            }
        }
        br.close();
        return content.toString();
	}

	/**
	 * check if string is empty (null or "")
	 * 
	 * @param str
	 * @return is empty or not
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	/**
	 * check if string is empty (null or "")
	 * 
	 * @param str
	 * @return is empty or not
	 */
	public static boolean isEmpty(String str, boolean trim) {
		if (!trim)
			return isEmpty(str);
		return str == null || str.trim().length() == 0;
	}

	/**
	 * @deprecated no replacement
	 * @param str
	 * @return
	 */
	public static int length(String str) {
		if (str == null)
			return 0;
		return str.length();
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance().getStringUtil().replace(...)
	 * @param str String to work with
	 * @param sub1 value to replace
	 * @param sub2 replacement
	 * @param onlyFirst replace only first or all
	 * @return new String
	 */
	public static String replace(String str, String sub1, String sub2,
			boolean onlyFirst) {
        if(sub1.equals(sub2)) return str;
        
        if(!onlyFirst && sub1.length()==1 && sub2.length()==1)return str.replace(sub1.charAt(0),sub2.charAt(0));
        
        
        StringBuffer sb=new StringBuffer();
        int start=0;
        int pos;
        int sub1Length=sub1.length();
        
        while((pos=str.indexOf(sub1,start))!=-1){
            sb.append(str.substring(start,pos));
            sb.append(sub2);
            start=pos+sub1Length;
            if(onlyFirst)break;
        }
        sb.append(str.substring(start));
        
        return sb.toString();
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance().getResourceUtil().
	 *             parsePlaceHolder(...)
	 *             replace path placeholder with the real path, placeholders are
	 *             [{temp-directory},{system-directory},{home-directory}]
	 * @param path
	 * @return updated path
	 */
	public static String parsePlaceHolder(String path) {
		return CFMLEngineFactory.getInstance().getResourceUtil()
				.parsePlaceHolder(path);
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance().getResourceUtil().
	 *             getTempDirectory()
	 *             returns the Temp Directory of the System
	 * @return temp directory
	 */
	public static File getTempDirectory() {
        if(tempFile!=null) return tempFile;
        
        String tmpStr = System.getProperty("java.io.tmpdir");
        if(tmpStr!=null) {
            tempFile=new File(tmpStr);
            if(tempFile.exists()) {
                tempFile=getCanonicalFileEL(tempFile);
                return tempFile;
            }
        }
        try {
            File tmp = File.createTempFile("a","a");
            tempFile=tmp.getParentFile();
            tempFile=getCanonicalFileEL(tempFile);
            tmp.delete();
        }
        catch(IOException ioe) {}
        
        return tempFile;
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance().getResourceUtil().
	 *             getHomeDirectory()
	 *             returns the Home Directory of the System
	 * @return home directory
	 */
	public static File getHomeDirectory() {
		return (File) CFMLEngineFactory.getInstance().getResourceUtil()
				.getHomeDirectory();
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance().getResourceUtil().
	 *             getSystemDirectory()
	 * @return return System directory
	 */
	public static File getSystemDirectory() {
		return (File) CFMLEngineFactory.getInstance().getResourceUtil()
				.getSystemDirectory();
	}

	/**
	 * @deprecated no replacement
	 *             Returns the canonical form of this abstract pathname.
	 * @param file file to get canoncial form from it
	 * 
	 * @return The canonical pathname string denoting the same file or
	 *         directory as this abstract pathname
	 * 
	 * @throws SecurityException
	 *             If a required system property value cannot be accessed.
	 */
	public static File getCanonicalFileEL(File file) {
		try {
			return file.getCanonicalFile();
		} catch (IOException e) {
			return file;
		}
	}

	/**
	 * @deprecated deprecated with no replacement
	 * @param date
	 * @return
	 */
	public static String toHTTPTimeString(Date date) {
		return replace(HTTP_TIME_STRING_FORMAT.format(date), "+00:00", "", true);
	}

	/**
	 * @deprecated deprecated with no replacement
	 * @return
	 */
	public static String toHTTPTimeString() {
		return replace(HTTP_TIME_STRING_FORMAT.format(new Date()), "+00:00",
				"", true);
	}

	/**
	 * @deprecated deprecated with no replacement
	 */
	public static boolean hasUpperCase(String str) {
		if (isEmpty(str))
			return false;
		return !str.equals(str.toLowerCase());
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance().getIOUtil().toBufferedInputStream
	 *             (...)
	 * @param os
	 * @return
	 */
	public static BufferedInputStream toBufferedInputStream(InputStream is) {
		if(is instanceof BufferedInputStream) return (BufferedInputStream) is;
		return new BufferedInputStream(is);
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance().getIOUtil().toBufferedOutputStream
	 *             (...)
	 * @param os
	 * @return
	 */
	public static BufferedOutputStream toBufferedOutputStream(OutputStream os) {
		if(os instanceof BufferedOutputStream) return (BufferedOutputStream) os;
		return new BufferedOutputStream(os);
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance.getIOUtil().copy(...)
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void copy(Resource in, Resource out) throws IOException {
		InputStream is=null;
		OutputStream os=null;
		try {
			is=toBufferedInputStream(in.getInputStream());
			os=toBufferedOutputStream(out.getOutputStream());
		}
		catch(IOException ioe) {
			closeEL(os);
			closeEL(is);
			throw ioe;
		}
		copy(is,os);
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance().getStringUtil().toVariableName
	 *             (...)
	 * @param str
	 * @param addIdentityNumber
	 * @return
	 */
	public static String toVariableName(String str, boolean addIdentityNumber) {
		return CFMLEngineFactory.getInstance().getStringUtil()
				.toVariableName(str, addIdentityNumber, false);
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance().getStringUtil().first(...);
	 * @param str
	 * @param delimiter
	 * @return
	 */
	public static String first(String str, String delimiter) {
		return CFMLEngineFactory.getInstance().getStringUtil()
				.first(str, delimiter, true);
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance().getStringUtil().last(...);
	 * @param str
	 * @param delimiter
	 * @return
	 */
	public static String last(String str, String delimiter) {
		return CFMLEngineFactory.getInstance().getStringUtil()
				.last(str, delimiter, true);
	}

	/**
	 * @deprecated use instead
	 *             CFMLEngineFactory.getInstance().getStringUtil().removeQuotes
	 *             (...);
	 * @param str
	 * @param trim
	 * @return
	 */
	public static String removeQuotes(String str, boolean trim) {
		return CFMLEngineFactory.getInstance().getStringUtil()
				.removeQuotes(str, trim);
	}

	public static void delete(File f) {
		if(f.isDirectory()) {
			for(File c:f.listFiles()){
				delete(c);
			}
		}
		f.delete();
	}
}