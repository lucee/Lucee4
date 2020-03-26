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
package lucee.runtime.net.smtp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import lucee.commons.lang.StringUtil;

import org.apache.commons.lang.WordUtils;

public class StringDataSource implements DataSource {
	
	private String text;
	private String ct;
	private String charset;

	public StringDataSource(String text, String ct, String charset, int maxLineLength) {
		if (text.length > maxLineLength) {
			String sep = System.getProperty("line.separator");
			String[] lines=text.split(sep);
			for(int i = 0; i < lines.length; i++) {
				if (lines[i].length > maxLineLength) {
					lines[i]=WordUtils.wrap(lines[i], maxLineLength);
				}
			}
			this.text=lines.join(sep);
		} else {
			this.text=text;
		}
		this.ct=ct;
		this.charset=charset;
	}

	@Override
	public String getContentType() {
		return ct;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(StringUtil.isEmpty(charset)?text.getBytes():text.getBytes(charset));
	}

	@Override
	public String getName() {
		return "StringDataSource";
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new IOException("no access to write");
	}

}
