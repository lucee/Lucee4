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
package lucee.runtime.instrumentation;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SystemOut;

import org.objectweb.asm.ClassReader;

@SuppressWarnings("UseOfSunClasses")
public class InstrumentationFactory {
	
	private static Instrumentation inst;
	private static boolean doInit=true;
	
	public static synchronized Instrumentation getInstance() {
		getInstance("lucee","lucee.runtime.instrumentation.Agent");
		if(inst==null)getInstance("railo","railo.runtime.instrumentation.Agent");
		
		return inst;
	}
	
	private static synchronized Instrumentation getInstance(String name,String className) {
		if(doInit) {
			doInit=false;
			
			Class agent = ClassUtil.loadClass(className,null);
			if(agent==null) {
				SystemOut.printDate("missing class "+className);
				return null;
			}
			
			// if Agent was loaded at startup there is already a Instrumentation
			inst=getInstrumentation(agent);
			
			// try to load Agent
			if(inst==null) {
				SystemOut.printDate("class "+className+".getInstrumentation() is not returning a Instrumentation");
				try {
					String id=getPid();
					String path=getResourcFromLib(name,className).getAbsolutePath();
					
					Class vmClass = ClassUtil.loadClass("com.sun.tools.attach.VirtualMachine");
					Object vmObj=attach(vmClass,id);
					loadAgent(vmClass,vmObj,path);
					detach(vmClass,vmObj);
				} 
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					//t.printStackTrace();
					return null;
				}
				inst=getInstrumentation(agent);
			}
			
			if(inst!=null)SystemOut.printDate("java.lang.instrument.Instrumentation is used to reload class files");
				
		}
		return inst;
	}

	private static Instrumentation getInstrumentation(Class agent) {
		try {
			Method getInstrumentation = agent.getMethod("getInstrumentation", new Class[0]);
			return (Instrumentation) getInstrumentation.invoke(null, new Object[0]);
		} 
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			t.printStackTrace();
			return null;
		}
	}

	private static Object attach(Class vmClass, String id) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Method attach = vmClass.getMethod("attach", new Class[]{String.class});
		return attach.invoke(null, new Object[]{id});
	}
	
	private static void loadAgent(Class vmClass, Object vmObj, String path) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Method loadAgent = vmClass.getMethod("loadAgent", new Class[]{String.class});
		loadAgent.invoke(vmObj, new Object[]{path});
	}
	
	private static void detach(Class vmClass, Object vmObj) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Method detach = vmClass.getMethod("detach", new Class[]{});
		detach.invoke(vmObj, new Object[]{});
	}
	
	private static Resource getResourcFromLib(String name,String className) {
		Resource[] pathes = SystemUtil.getClassPathes();
		Resource res = null;
		String fileName=null;
		if(pathes!=null)for(int i=0;i<pathes.length;i++){
			fileName=pathes[i].getName();
			if(fileName.equalsIgnoreCase(name+"-instrumentation.jar") || fileName.equalsIgnoreCase(name+"-inst.jar")) {
				res=pathes[i];
				break;
			}
		}
		
		if(res==null) {
			Class agent = ClassUtil.loadClass(className,null);
			if(agent!=null)res=getResourcFromLib(agent);
			else res=getResourcFromLib(ClassReader.class);
			
		}
		return res;
	}

	private static Resource getResourcFromLib(Class clazz) {
		String path=clazz.getClassLoader().getResource(".").getFile();
		Resource dir = ResourcesImpl.getFileResourceProvider().getResource(path);
		Resource res = dir.getRealResource("lucee-instrumentation.jar");
		if(!res.exists())res=dir.getRealResource("lucee-inst.jar");
		if(!res.exists())res=null;
		return res;
	}
	private static String getPid() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		RuntimeMXBean mxbean = ManagementFactory.getRuntimeMXBean();
	    Field jvmField = mxbean.getClass().getDeclaredField("jvm");

	    jvmField.setAccessible(true);
	    sun.management.VMManagement management = (sun.management.VMManagement) jvmField.get(mxbean);
	    Method method = management.getClass().getDeclaredMethod("getProcessId");
	    method.setAccessible(true);
	    Integer processId = (Integer) method.invoke(management);

	    return processId.toString();
	}
}