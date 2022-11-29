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

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import lucee.commons.io.TemporaryStream;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;

public final class S3ResourceOutputStream extends OutputStream {
	
	private final S3SDK s3;

	private final String objectName;

	private final TemporaryStream ts;
	
	public S3ResourceOutputStream(S3SDK s3,String objectName) {
		this.s3=s3;
		this.objectName=objectName;
		
		ts = new TemporaryStream();
	}
	
	@Override
	public void close() throws IOException {
		ts.close();

		try {
			s3.put(objectName, ts);
		} catch (SocketException se) {
			String msg = StringUtil.emptyIfNull(se.getMessage());
			if (StringUtil.indexOfIgnoreCase(msg, "Socket closed")==-1) {
				throw se;
			}
		} catch (Exception e) {
			throw ExceptionUtil.toIOException(e);
		}
	}

	@Override
	public void flush() throws IOException {
		ts.flush();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		ts.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		ts.write(b);
	}

	@Override
	public void write(int b) throws IOException {
		ts.write(b);
	}
}
