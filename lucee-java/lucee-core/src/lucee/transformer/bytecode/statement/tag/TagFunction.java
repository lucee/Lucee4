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
package lucee.transformer.bytecode.statement.tag;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lucee.commons.lang.StringUtil;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.type.util.ComponentUtil;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BodyBase;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.BytecodeException;
import lucee.transformer.bytecode.Literal;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.Position;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.expression.ExprString;
import lucee.transformer.bytecode.expression.Expression;
import lucee.transformer.bytecode.literal.LitBoolean;
import lucee.transformer.bytecode.literal.LitString;
import lucee.transformer.bytecode.statement.FlowControlFinal;
import lucee.transformer.bytecode.statement.IFunction;
import lucee.transformer.bytecode.statement.PrintOut;
import lucee.transformer.bytecode.statement.udf.Function;
import lucee.transformer.bytecode.statement.udf.FunctionImpl;
 
public final class TagFunction extends TagBase implements IFunction {

	private static final ExprString ANY = LitString.toExprString("any");

	private static final Expression PUBLIC = LitString.toExprString("public");

	private static final Expression EMPTY = LitString.toExprString("");
	
	public TagFunction(Position start,Position end) {
		super(start,end);
		
	}
	
	/**
	 * @see lucee.transformer.bytecode.statement.IFunction#writeOut(lucee.transformer.bytecode.BytecodeContext, int)
	 */
	public void writeOut(BytecodeContext bc, int type) throws BytecodeException {
    	//ExpressionUtil.visitLine(bc, getStartLine());
    	_writeOut(bc,type);
    	//ExpressionUtil.visitLine(bc, getEndLine());
	}
	
	/**
	 * @see lucee.transformer.bytecode.statement.tag.TagBase#_writeOut(lucee.transformer.bytecode.BytecodeContext)
	 */
	public void _writeOut(BytecodeContext bc) throws BytecodeException {
		_writeOut(bc,Function.PAGE_TYPE_REGULAR);
	}

	public void _writeOut(BytecodeContext bc, int type) throws BytecodeException {
		
		Body functionBody = new BodyBase();
		Function func = createFunction(bc.getPage(),functionBody,bc.getOutput());
		func.setParent(getParent());

		List<Statement> statements = getBody().getStatements();
		Statement stat;
		Tag tag;
		
		// suppress WS between cffunction and the last cfargument
		Tag last=null;
		if(bc.getSupressWSbeforeArg()){
			// check if there is a cfargument at all
			Iterator<Statement> it = statements.iterator();
			while (it.hasNext()) {
				stat = it.next();
				if (stat instanceof Tag) {
					tag = (Tag) stat;
					if (tag.getTagLibTag().getTagClassName().equals("lucee.runtime.tag.Argument")) {
						last=tag;
					}
				}
			}
			
			// check if there are only literal WS printouts
			if(last!=null) {
				it = statements.iterator();
				while (it.hasNext()) {
					stat = it.next();
					if(stat==last) break;
					
					if(stat instanceof PrintOut){
						PrintOut po=(PrintOut) stat;
						Expression expr = po.getExpr();
						if(!(expr instanceof LitString) || !StringUtil.isWhiteSpace(((LitString)expr).getString())) {
							last=null;
							break;
						}
					}
				}
			}
		}
		
		
		
		Iterator<Statement> it = statements.iterator();
		boolean beforeLastArgument=last!=null;
		while (it.hasNext()) {
			stat = it.next();
			if(beforeLastArgument) {
				if(stat==last) {
					beforeLastArgument=false;
				}
				else if(stat instanceof PrintOut){
					PrintOut po=(PrintOut) stat;
					Expression expr = po.getExpr();
					if(expr instanceof LitString) {
						LitString ls=(LitString) expr;
						if(StringUtil.isWhiteSpace(ls.getString())) continue;
					}
				}
				
			}
			if (stat instanceof Tag) {
				tag = (Tag) stat;
				if (tag.getTagLibTag().getTagClassName().equals(
						"lucee.runtime.tag.Argument")) {
					addArgument(func, tag);
					continue;
				}
			}
			functionBody.addStatement(stat);
		}
		func._writeOut(bc,type);

	}

	private void addArgument(Function func, Tag tag) {
		Attribute attr;
		// name
		Expression name = tag.removeAttribute("name").getValue();
		
		// type
		attr = tag.removeAttribute("type");
		Expression type = (attr == null) ? ANY : attr.getValue();

		// required
		attr = tag.removeAttribute("required");
		Expression required = (attr == null) ? LitBoolean.FALSE : attr
				.getValue();

		// default
		attr = tag.removeAttribute("default");
		Expression defaultValue = (attr == null) ? null : attr.getValue();
		
		// passby
		attr = tag.removeAttribute("passby");
		LitBoolean passByReference = LitBoolean.TRUE;
		if(attr!=null) {
			// i can cast irt to LitString because he evulator check this before
			 String str = ((LitString)attr.getValue()).getString();
			 if(str.trim().equalsIgnoreCase("value"))
				 passByReference=LitBoolean.FALSE;
		}
		
		
		// displayname
		attr = tag.removeAttribute("displayname");
		Expression displayName = (attr == null) ? EMPTY : attr.getValue();

		// hint
		attr = tag.removeAttribute("hint");
		if (attr == null)
			attr = tag.removeAttribute("description");
		
		Expression hint;
		if(attr == null)hint=EMPTY;
		else hint=attr.getValue();
		
		func.addArgument(name, type, required, defaultValue, passByReference,displayName, hint,tag.getAttributes());

	}

	private Function createFunction(Page page, Body body,boolean defaultOutput) throws BytecodeException {
		Attribute attr;
		
		// name
		Expression name = removeAttribute("name").getValue();
		/*if(name instanceof LitString) {
			((LitString)name).upperCase();
		}*/
		// return
		attr = removeAttribute("returntype");
		// if(attr==null) attr = getAttribute("return");
		// if(attr==null) attr = getAttribute("type");
		Expression returnType = (attr == null) ? ANY : attr.getValue();

		// output
		attr = removeAttribute("output");
		Expression output = (attr == null) ? (defaultOutput ? LitBoolean.TRUE : LitBoolean.FALSE) : attr.getValue();
		
		// bufferOutput
		attr = removeAttribute("bufferoutput");
		Expression bufferOutput = (attr == null) ? null : attr.getValue();

		// modifier
		boolean _abstract=false,_final=false;
		attr = removeAttribute("modifier");
		if(attr!=null) {
			Expression val = attr.getValue();
			if(val instanceof Literal) {
				Literal l=(Literal) val;
				String str = StringUtil.emptyIfNull(l.getString()).trim();
				if("abstract".equalsIgnoreCase(str))_abstract=true;
				else if("final".equalsIgnoreCase(str))_final=true;
			}
		}

		// access
		attr = removeAttribute("access");
		Expression access = (attr == null) ? PUBLIC : attr.getValue();

		// dspLabel
		attr = removeAttribute("displayname");
		Expression displayname = (attr == null) ? EMPTY : attr.getValue();

		// hint
		attr = removeAttribute("hint");
		Expression hint = (attr == null) ? EMPTY : attr.getValue();

		// description
		attr = removeAttribute("description");
		Expression description = (attr == null) ? EMPTY : attr.getValue();

		// returnformat
		attr = removeAttribute("returnformat");
		Expression returnFormat = (attr == null) ? null : attr.getValue();

		// secureJson
		attr = removeAttribute("securejson");
		Expression secureJson = (attr == null) ? null : attr.getValue();

		// verifyClient
		attr = removeAttribute("verifyclient");
		Expression verifyClient = (attr == null) ? null : attr.getValue();

		// localMode
		attr = removeAttribute("localmode");
		Expression localMode = (attr == null) ? null : attr.getValue();
		
		
		
		// cachedWithin
		Literal cachedWithin=null;
		attr = removeAttribute("cachedwithin");
		if(attr!=null) {
			Expression val = attr.getValue();
			if(val instanceof Literal)
				cachedWithin=((Literal)val);
		}
		String strAccess = ((LitString)access).getString();
		int acc = ComponentUtil.toIntAccess(strAccess,-1);
		if(acc==-1)
			throw new BytecodeException("invalid access type ["+strAccess+"], access types are remote, public, package, private",getStart());
        
		Function func = new FunctionImpl(page,name, returnType,returnFormat, output, bufferOutput, acc, displayname,description,
				hint,secureJson,verifyClient,localMode,cachedWithin,_abstract,_final, body, getStart(),getEnd());
		 
		
		
		
//		 %**%
		Map attrs = getAttributes();
		Iterator it = attrs.entrySet().iterator();
		HashMap<String,Attribute> metadatas=new HashMap<String,Attribute>();
		while(it.hasNext()){
			attr=(Attribute) ((Map.Entry)it.next()).getValue();
			metadatas.put(attr.getName(),attr);
		}
		func.setMetaData(metadatas);
		return func;
	}
	
	@Override
	public FlowControlFinal getFlowControlFinal() {
		return null;
	}

}
