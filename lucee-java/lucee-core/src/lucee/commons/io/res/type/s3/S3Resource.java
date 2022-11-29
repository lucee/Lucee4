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
package lucee.commons.io.res.type.s3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.mastercontrol.resource.s3.S3ListItem;
import lucee.commons.io.StreamWithSize;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.util.ResourceSupport;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.loader.util.Util;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import org.apache.commons.lang.NotImplementedException;

public final class S3Resource extends ResourceSupport {

	private static final long serialVersionUID = 2265457088552587701L;

	private final S3ResourceProvider provider;
	private String objectName;
	private final S3SDK s3;

	private final boolean newPattern;
	private S3ListItem cachedListItem;

	S3Resource(S3SDK s3, S3ResourceProvider provider, String path, boolean newPattern) {
		this.s3=s3;
		this.provider=provider;
		this.newPattern=newPattern;
		
		if("/".equals(path) || Util.isEmpty(path,true)) {
			this.objectName="";
		} else {
			objectName=ResourceUtil.translatePath(path, true, false);
			if(objectName==null) {
				objectName="";
			}
		}
		this.objectName = objectName.toLowerCase(Locale.ENGLISH);
	}

	private S3Resource(S3SDK s3, S3ResourceProvider provider, String path, boolean newPattern, S3ListItem cachedListItem) {
		this(s3, provider, path, newPattern);
		this.cachedListItem = cachedListItem;
	}

	public  static String[] toStringArray(Array array) {
        String[] arr=new String[array.size()];
        for(int i=0;i<arr.length;i++) {
            arr[i]=Caster.toString(array.get(i+1,""),"");
        }
        return arr;
    }


	public void createDirectory(boolean createParentWhenNotExists) throws IOException {
		ResourceUtil.checkCreateDirectoryOK(this, createParentWhenNotExists);
		try {
			provider.lock(this);
			s3.put(objectName+"/", StreamWithSize.EMPTY);
		} catch (IOException ioe) {
			throw ioe;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} finally {
			provider.unlock(this);
		}

	}

	public void createFile(boolean createParentWhenNotExists) throws IOException {
		ResourceUtil.checkCreateFileOK(this, createParentWhenNotExists);
		if(isBucket()) {
			throw new IOException("can't create file ["+getPath()+"], on this level (Bucket Level) you can only create directories");
		}
		try {
			provider.lock(this);
			s3.put(objectName, StreamWithSize.EMPTY);
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		} finally {
			provider.unlock(this);
		}
	}

	public boolean exists() {
		if (cachedListItem != null) {
			return true;
		}
		return s3.exists(getInnerPath());
	}

	public InputStream getInputStream() throws IOException {
		ResourceUtil.checkGetInputStreamOK(this);
		provider.read(this);
		try {
			return Util.toBufferedInputStream(s3.getInputStream(objectName));
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	public int getMode() {
		return 777;
	}

	@Override
	public String getName() {
		if(isBucket()) {
			return "";
		}
		return objectName.substring(objectName.lastIndexOf('/')+1);
	}

	@Override
	public boolean isAbsolute() {
		return true;
	}

	@Override
	public String getPath() {
		return getPrefix().concat(getInnerPath());
	}
	
	private String getPrefix()  {
		return provider.getScheme() + "://";
	}

	@Override
	public String getParent() {
		return getPrefix().concat(getInnerParent());
	}
	
	private String getInnerPath() {
		return ResourceUtil.translatePath(objectName, true, false);
	}
	
	private String getInnerParent() {
		if(Util.isEmpty(objectName)) {
			return "/";
		}
		if(objectName.indexOf('/')==-1) {
			return "/";
		}
		String tmp=objectName.substring(0,objectName.lastIndexOf('/'));
		return ResourceUtil.translatePath(tmp, true, false);
	}

	@Override
	public Resource getParentResource() {
		// MUST make more direct
		return new S3Resource(new S3SDK(), provider, getInnerParent(), newPattern);
	}

	private boolean isBucket() {
		return Util.isEmpty(objectName);
	}

	@Override
	public String toString() {
		return getPath();
	}
	
	public OutputStream getOutputStream(boolean append) throws IOException {

		ResourceUtil.checkGetOutputStreamOK(this);
		
		try {
			byte[] barr = null;
			if(append){
				InputStream is=null;
				OutputStream os=null;
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					os=baos;
					is = getInputStream();
					Util.copy(is, baos);
					barr=baos.toByteArray();
				} catch (Exception e) {
					throw new PageRuntimeException(Caster.toPageException(e));
				} finally{
					Util.closeEL(is);
					Util.closeEL(os);
				}
			}
			S3ResourceOutputStream os = new S3ResourceOutputStream(s3,objectName);
			if(append && !(barr==null || barr.length==0)){
				Util.copy(new ByteArrayInputStream(barr),os);
			}
			return os;
		} catch(IOException e) {
			throw e;
		} catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		} finally {
			cachedListItem = null;
		}
	}

	@Override
	public Resource getRealResource(String relpath) {
		relpath=ResourceUtil.merge(getInnerPath(), relpath);
		if(relpath.startsWith("../")) {
			return null;
		}
		return new S3Resource(new S3SDK(), provider, relpath, newPattern);
	}

	@Override
	public ResourceProvider getResourceProvider() {
		return provider;
	}

	@Override
	public boolean isDirectory() {
		if (cachedListItem != null) {
			return cachedListItem.getKey().endsWith("/");
		}
		return s3.directoryExists(getInnerPath());
	}

	@Override
	public boolean isFile() {
		if (cachedListItem != null) {
			return !isDirectory();
		}
		return s3.fileExists(getInnerPath());
	}

	public boolean isReadable() {
		return exists();
	}

	public boolean isWriteable() {
		return exists();
	}

	@Override
	public long lastModified() {
		if (cachedListItem != null) {
			return cachedListItem.getLastModified();
		}
		return getS3Info(() -> s3.getLastModified(getInnerPath())).orElse(0L);
	}

	@Override
	public long length() {
		if (cachedListItem != null) {
			return cachedListItem.getSize();
		}
		return getS3Info(() -> s3.getSize(getInnerPath())).orElse(0L);
	}

	private static <T> Optional<T> getS3Info(Supplier<T> processor) {
		try {
			return Optional.of(processor.get());
		} catch (AmazonS3Exception e) {
			if (e.getStatusCode() == 404) {
				return Optional.empty();
			}
			throw e;
		}
	}

	public Resource[] listResources() {
		try {
			List<S3ListItem> contents = s3.listContents(isBucket()?null:(objectName+"/"));
			ArrayList<S3Resource> tmp = new ArrayList<>(contents.size());
			String key;

			boolean isBucket=isBucket();

			int size = contents.size();
			for(int i=0; i<size; i++) {
				key=ResourceUtil.translatePath(contents.get(i).getKey(), true, false);
				if(!isBucket && !key.startsWith(objectName+"/")) {
					continue;
				}
				if(Util.isEmpty(key)) {
					continue;
				}

				tmp.add(new S3Resource(new S3SDK(), provider, key, newPattern, contents.get(i)));
			}

			return tmp.toArray(new S3Resource[0]);
		} catch(Exception t) {
			t.printStackTrace();
			return null;
		}
	}

	@Override
	public void remove(boolean force) throws IOException {
		boolean isd=isDirectory();
		if(isd) {
			Resource[] children = listResources();
			if(children != null && children.length>0) {
				if(force) {
					for(int i=0;i<children.length;i++) {
						children[i].remove(force);
					}
				} else {
					throw new IOException("can not remove directory ["+this+"], directory is not empty");
				}
			}
		}
		// delete res itself
		provider.lock(this);
		try {
			s3.delete(isd? (objectName+"/") :objectName);
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		} finally {
			cachedListItem=null;
			provider.unlock(this);
		}
	}

	public boolean setLastModified(long time) {
		// Can't do that on S3 so ignoring
		return false;
	}

	public void setMode(int mode) throws IOException {
		// Can't do that on S3 so ignoring
	}

	public boolean setReadable(boolean readable) {
		// Can't do that on S3 so ignoring
		return false;
	}

	public boolean setWritable(boolean writable) {
		// Can't do that on S3 so ignoring
		return false;
	}

	public AccessControlPolicy getAccessControlPolicy() {
		throw new NotImplementedException();
	}
	
	public void setAccessControlPolicy(AccessControlPolicy acp) {
		// Not managing ACLs here
	}

	public void setACL(int acl) {
		// Not managing ACLs here
	}
}



