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
package lucee.runtime.gateway.proxy;

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.gateway.Gateway;
import lucee.runtime.gateway.GatewayEngine;
import lucee.runtime.gateway.GatewayEngineImpl;
import lucee.runtime.gateway.GatewayEnginePro;
import lucee.runtime.gateway.GatewayPro;

// FUTURE remove this class
public class GatewayProFactory {
	
	public static Gateway toGateway(GatewayPro gateway){
		return ((GatewayProxy)gateway).getGateway();
	}
	
	public static GatewayPro toGatewayPro(Object obj) throws ApplicationException{
		if(obj instanceof GatewayPro) 
			return (GatewayPro) obj;
		if(obj instanceof Gateway) 
			return new GatewayProxy((Gateway)obj);
		throw new ApplicationException("the class ["+obj.getClass().getName()+"] does not implement the interface ["+Gateway.class.getName()+"], make sure you have not multiple implementation of that interface in your classpath");
		
	}

	public static GatewayEngineImpl toGatewayEngineImpl(GatewayEnginePro engine) {
		if(engine instanceof GatewayEngineImpl)return (GatewayEngineImpl) engine;
		return ((GatewayEngineProxy) engine).getEngine();
	}


	public static GatewayEngine toGatewayEngine(GatewayEnginePro engine) {
		return new GatewayEngineProxy((GatewayEngineImpl) engine);
	}
}
