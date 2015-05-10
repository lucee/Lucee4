<!--- 
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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	public void function testKeyExists(){
		try{
			throw "";
		}
		catch(local.e){ local.exp=e;}
		
		assertTrue(exp.keyExists("message"));
		assertFalse(exp.keyExists("kojhkhk"));
	}
} 

/*
component [tests.testcases.tickets.LDEV0321] has no private function with name [keyExists] 
at lucee.runtime.type.util.ComponentUtil.notFunction(ComponentUtil.java:662):662 
at lucee.runtime.ComponentScopeShadow.call(ComponentScopeShadow.java:296):296 
at lucee.runtime.util.VariableUtilImpl.callFunctionWithoutNamedValues(VariableUtilImpl.java:742):742 
at lucee.runtime.PageContextImpl.getFunction(PageContextImpl.java:1586):1586 
at testcases.tickets.ldev0321_cfc$cf.udfCall(/Users/mic/Projects/Lucee/tests/testcases/tickets/LDEV0321.cfc:21):21 
at lucee.runtime.type.UDFImpl.implementation(UDFImpl.java:111):111 
at lucee.runtime.type.UDFImpl._call(UDFImpl.java:328):328 
at lucee.runtime.type.UDFImpl.call(UDFImpl.java:229):229 
at lucee.runtime.ComponentImpl._call(ComponentImpl.java:606):606 
at lucee.runtime.ComponentImpl._call(ComponentImpl.java:524):524 
at lucee.runtime.ComponentImpl.call(ComponentImpl.java:1760):1760 
at lucee.runtime.util.VariableUtilImpl.callFunctionWithoutNamedValues(VariableUtilImpl.java:742):742 
at lucee.runtime.util.VariableUtilImpl.callFunctionWithoutNamedValues(VariableUtilImpl.java:736):736 
at lucee.runtime.util.VariableUtilImpl.callFunction(VariableUtilImpl.java:729):729 
at lucee.runtime.interpreter.ref.func.UDFCall.getValue(UDFCall.java:65):65 
at lucee.runtime.interpreter.CFMLExpressionInterpreter.interpret(CFMLExpressionInterpreter.java:217):217 
at lucee.runtime.functions.dynamicEvaluation.Evaluate._call(Evaluate.java:109):109 
at lucee.runtime.functions.dynamicEvaluation.Evaluate.call(Evaluate.java:102):102 
at lucee.runtime.functions.dynamicEvaluation.Evaluate.call(Evaluate.java:43):43 
*/

</cfscript>