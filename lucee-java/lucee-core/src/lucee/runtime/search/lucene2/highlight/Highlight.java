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
package lucee.runtime.search.lucene2.highlight;

import lucee.commons.lang.ExceptionUtil;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;


public class Highlight {

	public static String createContextSummary(Object highlighter, Analyzer analyzer, String text,int maxNumFragments, int maxLength,String defaultValue) {
		if(maxNumFragments==0) return "";
		try {
			return _Highlight.createContextSummary(highlighter, analyzer, text,maxNumFragments, defaultValue);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}	
		return defaultValue;
	}

	public static Object createHighlighter(Query query,String highlightBegin,String highlightEnd) {
		try {
			return _Highlight.createHighlighter(query,highlightBegin,highlightEnd);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			t.printStackTrace();
		}
		return null;
	}

}
