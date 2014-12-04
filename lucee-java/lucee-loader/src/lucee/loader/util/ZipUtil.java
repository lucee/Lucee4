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
package lucee.loader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
	
	public static void zip(File src, File trgZipFile) throws IOException	{
		if(trgZipFile.isDirectory()) throw new IllegalArgumentException("argument trgZipFile is the name of a existing directory");
		
		
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(trgZipFile));
		try{
			if(src.isFile())
				addEntries(zos,src.getParentFile(),src);
			else if(src.isDirectory())
				addEntries(zos,src,src.listFiles());
		}
		finally {
			Util.closeEL(zos);
		}
	}

	private static void addEntries(ZipOutputStream zos, File root, File... files) throws IOException {
		if(files!=null)for (File file : files) {
			
			// directory
			if(file.isDirectory()) {
				addEntries(zos, root, file.listFiles());
				continue;
			}
			if(!file.isFile()) continue;
				
			// file
			InputStream is=null;
			ZipEntry ze = generateZipEntry(root,file);
			
			try {
				zos.putNextEntry(ze);
				Util.copy(is=new FileInputStream(file),zos,false,false);
			}
			finally {
				Util.closeEL(is);
				zos.closeEntry();
			}
		}
	}

	private static ZipEntry generateZipEntry(File root, File file) {
		String strRoot=root.getAbsolutePath();
		String strFile=file.getAbsolutePath();
		return new ZipEntry(strFile.substring(strRoot.length() + 1, strFile.length()));
		
	}
}	 