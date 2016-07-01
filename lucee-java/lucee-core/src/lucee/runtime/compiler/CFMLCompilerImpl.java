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
package lucee.runtime.compiler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

import lucee.commons.digest.RSA;
import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.bytecode.BytecodeException;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.Position;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.ClassRenamer;
import lucee.transformer.cfml.tag.CFMLTransformer;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.util.AlreadyClassException;



/**
 * CFML Compiler compiles CFML source templates
 */
public final class CFMLCompilerImpl implements CFMLCompiler {
	

	private CFMLTransformer cfmlTransformer;
	private ConcurrentLinkedQueue<WatchEntry> watched=new ConcurrentLinkedQueue<WatchEntry>(); 
	
	
	/**
	 * Constructor of the compiler
	 * @param config
	 */
	public CFMLCompilerImpl() {
		cfmlTransformer=new CFMLTransformer();
	}
	
	@Override
	public byte[] compile(ConfigImpl config,PageSource source, TagLib[] tld, FunctionLib[] fld, 
        Resource classRootDir, String className) throws TemplateException, IOException {
		//synchronized(source){
			Resource classFile=classRootDir.getRealResource(className+".class");
			Resource classFileDirectory=classFile.getParentResource();
	        byte[] barr = null;
			Page page = null;
			
			if(!classFileDirectory.exists()) classFileDirectory.mkdirs(); 
			
	        try {
	        	page = cfmlTransformer.transform(config,source,tld,fld);
	        	page.setSplitIfNecessary(false);
	        	try {
	        		barr = page.execute(source,classFile);
	        	}
	        	catch(RuntimeException re) {
	        		String msg=StringUtil.emptyIfNull(re.getMessage());
	        		if(StringUtil.indexOfIgnoreCase(msg, "Method code too large!")!=-1) {
	        			page = cfmlTransformer.transform(config,source,tld,fld); // MUST a new transform is necessary because the page object cannot be reused, rewrite the page that reusing it is possible
	    	        	page.setSplitIfNecessary(true);
	        			barr = page.execute(source,classFile);
	        		}
	        		else throw re;
	        	}
		        catch(ClassFormatError cfe) {
		        	String msg=StringUtil.emptyIfNull(cfe.getMessage());
		        	if(StringUtil.indexOfIgnoreCase(msg, "Invalid method Code length")!=-1) {
		        		page = cfmlTransformer.transform(config,source,tld,fld); // MUST see above
			        	page.setSplitIfNecessary(true);
		        		barr = page.execute(source,classFile);
		        	}
		        	else throw cfe;
		        }
	        	
	        	
	        	
	        	
	        	
	        	
	        	
	        	
	        	
	        	
				IOUtil.copy(new ByteArrayInputStream(barr), classFile,true);
		        return barr;
			} 
	        catch (AlreadyClassException ace) {
        		
        		barr = ace.getEncrypted()?readEncrypted(ace):readPlain(ace);
        		
        		String srcName = ASMUtil.getClassName(barr);
        		// source is cfm and target cfc
        		if(srcName.endsWith("_cfm$cf") && className.endsWith("_cfc$cf"))
        				throw new TemplateException("source file ["+source.getDisplayPath()+"] contains the bytecode for a regular cfm template not for a component");
        		// source is cfc and target cfm
        		if(srcName.endsWith("_cfc$cf") && className.endsWith("_cfm$cf"))
        				throw new TemplateException("source file ["+source.getDisplayPath()+"] contains a component not a regular cfm template");
        		
        		// rename class name when needed
        		if(!srcName.equals(className))barr=ClassRenamer.rename(barr, className);
        		
        		
        		barr=Page.setSourceLastModified(barr,source.getPhyscalFile().lastModified());
        		IOUtil.copy(new ByteArrayInputStream(barr), classFile,true);
        	
	        	return barr;
	        }
	        catch (BytecodeException bce) {
	        	Position pos = bce.getPosition();
	        	int line=pos==null?-1:pos.line;
	        	int col=pos==null?-1:pos.column;
	        	bce.addContext(source, line, col,null);
	        	throw bce;
	        	//throw new TemplateException(source,e.getLine(),e.getColumn(),e.getMessage());
			}
	        /*finally {
	        	
	        }*/
		//}
	}

	private byte[] readPlain(AlreadyClassException ace) throws IOException {
		return IOUtil.toBytes(ace.getInputStream(),true);
	}

	private byte[] readEncrypted(AlreadyClassException ace) throws IOException {
		
		String str = System.getenv("PUBLIC_KEY");
		if(StringUtil.isEmpty(str,true)) str=System.getProperty("PUBLIC_KEY");
		if(StringUtil.isEmpty(str,true)) throw new RuntimeException("to decrypt encrypted bytecode, you need to set PUBLIC_KEY as system property or or enviroment variable");
		
		System.out.println("PK:"+str);
		
		byte[] bytes = IOUtil.toBytes(ace.getInputStream(),true);
		try {	
			PublicKey publicKey = RSA.toPublicKey(str);
			// first 2 bytes are just a mask to detect encrypted code, so we need to set offset 2
			bytes=RSA.decrypt(bytes, publicKey,2);
		}
		catch (IOException ioe) {
			throw ioe;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return bytes;
	}

	public void watch(PageSource ps, long now) {
		watched.offer(new WatchEntry(ps,now,ps.getPhyscalFile().length(),ps.getPhyscalFile().lastModified()));
	}
	
	public void checkWatched() {
		WatchEntry we;
		long now=System.currentTimeMillis();
		Stack<WatchEntry> tmp =new Stack<WatchEntry>();
		while((we=watched.poll())!=null) {
			// to young 
			if(we.now+1000>now) {
				tmp.add(we);
				continue;
			}
			if(we.length!=we.ps.getPhyscalFile().length() 
					&& we.ps.getPhyscalFile().length()>0) { // TODO this is set to avoid that removed files are removed from pool, remove this line if a UDF still wprks fine when the page is gone
				((PageSourceImpl)we.ps).flush();
			}
		}
		
		// add again entries that was to young for next round
		Iterator<WatchEntry> it = tmp.iterator();
		while(it.hasNext()) {
			watched.add(we=it.next());
		}
	}


	private class WatchEntry {

		private final PageSource ps;
		private final long now;
		private final long length;
		//private final long lastModified;

		public WatchEntry(PageSource ps, long now, long length, long lastModified) {
			this.ps=ps;
			this.now=now;
			this.length=length;
			//this.lastModified=lastModified;
		}

	}
}