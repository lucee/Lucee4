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
import java.io.InputStream;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageSource;
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
	        	InputStream is=null;
	        	try{
	        		barr=IOUtil.toBytes(is=ace.getInputStream());
	        		
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
	        		
	        	}
	        	finally {
	        		IOUtil.closeEL(is);
	        	}
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

    /* *
     * @return Returns the cfmlTransformer.
     * /
    public CFMLTransformer getCfmlTransformer() {
        return cfmlTransformer;
    }*/
}