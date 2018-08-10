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
package lucee.runtime.orm.hibernate;


import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.err.ErrorPage;
import lucee.runtime.exp.CatchBlock;
import lucee.runtime.exp.IPageException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageExceptionBox;
import lucee.runtime.type.Struct;

import org.luceehibernate.HibernateException;

public class HibernatePageException extends HibernateException implements IPageException,PageExceptionBox  {

	private static final long serialVersionUID = -7745292875775743390L;

	private PageException pe;


	/**
	 * constructor of the class
	 * @param pe page exception to hold
	 */
	public HibernatePageException(PageException pe) {
		super(pe.getMessage());
		this.pe=pe;
	}


	@Override
	public String getDetail() {
		return pe.getDetail();
	}

	@Override
	public String getErrorCode() {
		return pe.getErrorCode();
	}

	@Override
	public String getExtendedInfo() {
		return pe.getExtendedInfo();
	}

	@Override
	public Struct getCatchBlock(PageContext pc) {
		return getCatchBlock(pc.getConfig());
	}

	public Struct getCatchBlock() {
		// TLPC
		return pe.getCatchBlock(CommonUtil.config());
	}


	public CatchBlock getCatchBlock(Config config) {
		return pe.getCatchBlock(config);
	}

	@Override
	public Struct getErrorBlock(PageContext pc,ErrorPage ep) {
		return pe.getErrorBlock(pc,ep);
	}
	@Override
	public void addContext(PageSource template, int line, int column,StackTraceElement ste) {
		pe.addContext(template,line,column,ste);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return pe.toDumpData(pageContext, maxlevel,dp);
	}

	@Override
	public PageException getPageException() {
		return pe;
	}

	@Override
	public void setDetail(String detail) {
			 pe.setDetail(detail);
	}

	@Override
	public void setErrorCode(String errorCode) {
			 pe.setErrorCode(errorCode);
	}

	@Override
	public void setExtendedInfo(String extendedInfo) {
			 pe.setExtendedInfo(extendedInfo);
	}

	@Override
	public boolean typeEqual(String type) {
		return 	pe.typeEqual(type);
	}

	@Override
	public String getTypeAsString() {
		return pe.getTypeAsString();
	}

	@Override
	public String getCustomTypeAsString() {
		return pe.getCustomTypeAsString();
	}

    @Override
    public int getTracePointer() {
        return pe.getTracePointer();
    }

    @Override
    public void setTracePointer(int tracePointer) {
        pe.setTracePointer(tracePointer);
    }

    @Override
    public Struct getAdditional() {
        return pe.getAddional();
    }
    public Struct getAddional() {
        return pe.getAddional();
    }

    public String getStackTraceAsString() {
        return pe.getStackTraceAsString();
    }
}
