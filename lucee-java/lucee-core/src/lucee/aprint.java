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
package lucee;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.QueryImpl;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 *  
 */
public class aprint {

	

	public static void date(String value) {
		long millis=System.currentTimeMillis();
    	o(
    			new Date(millis)
    			+"-"
    			+(millis-(millis/1000*1000))
    			+" "+value);
	}

	public static void ds(boolean useOutStream) {
		new Exception("Stack trace").printStackTrace(useOutStream?System.out:System.err);
	}
	
	public static void ds(Object label,boolean useOutStream) {
		_oe(useOutStream?System.out:System.err, label);
		ds(useOutStream);
	}
	
	public static void ds() {ds(false);}
	public static void ds(Object label) {ds(label,false);}
	public static void dumpStack() {ds(false);}
	public static void dumpStack(boolean useOutStream) {ds(useOutStream);}
	public static void dumpStack(String label) {ds(label,false);}
	public static void dumpStack(String label,boolean useOutStream) {ds(label,useOutStream);}
    
    public static void err(boolean o) { 
		System.err.println(o);
	}
    public static void err(double d) {
		System.err.println(d);
	}
    public static void err(long d) {
		System.err.println(d);
	}
    public static void err(float d) {
		System.err.println(d);
	}
    public static void err(int d) {
		System.err.println(d);
	}
    public static void err(short d) {
		System.err.println(d);
	}

    public static void out(Object o1,Object o2,Object o3) {
		System.out.print(o1);
		System.out.print(o2);
		System.out.println(o3);
	}
	
	public static void out(Object o1,Object o2) {
		System.out.print(o1);
		System.out.println(o2);
	}

	public static void out(Object o,long l) {
		System.out.print(o);
		System.out.println(l);
	}
	
	public static void out(Object o,double d) {
		System.out.print(o);
		System.out.println(d);
	}
    
    
    public static void out(byte[] arr, int offset, int len) {
        System.out.print("byte[]{");
        for(int i=offset;i<len+offset;i++) {
            if(i>0)System.out.print(',');
            System.out.print(arr[i]);
        }
        System.out.println("}");
    }
    
    

    

	
	public static void out(double o) {
		System.out.println(o);
	}
	
	public static void out(float o) {
		System.out.println(o);
	}
	public static void out(long o) {
		System.out.println(o);
	}
	public static void out(int o) {
		System.out.println(o);
	}
	public static void out(char o) {
		System.out.println(o);
	}
	public static void out(boolean o) {
		System.out.println(o);
	}
	public static void out() {
		System.out.println();
	}

	public static void printST(Throwable t) {
		if(t instanceof InvocationTargetException){
			t=((InvocationTargetException)t).getTargetException();
		}
		err(t.getClass().getName());
		t.printStackTrace();
		
	}
	
	public static void printST(Throwable t,PrintStream ps) {
		if(t instanceof InvocationTargetException){
			t=((InvocationTargetException)t).getTargetException();
		}
		err(t.getClass().getName());
		t.printStackTrace(ps);
		
	}
	
	

	public static void out(Object o) {
		_oe(System.out, o);
	}
	public static void err(Object o) {
		_oe(System.err, o);
	}

	public static void writeTemp(String name,Object o, boolean addStackTrace) {
		//write(SystemUtil.getTempDirectory().getRealResource(name+".log"), o);
		write(SystemUtil.getHomeDirectory().getRealResource(name+".log"), o,addStackTrace);
	}
	public static void writeHome(String name,Object o, boolean addStackTrace) {
		write(SystemUtil.getHomeDirectory().getRealResource(name+".log"), o,addStackTrace);
	}
	public static void writeCustom(String path,Object o, boolean addStackTrace) {
		write(ResourcesImpl.getFileResourceProvider().getResource(path), o,addStackTrace);
	}

	public static void write(Resource res,Object o, boolean addStackTrace) {
		OutputStream os=null;
		PrintStream ps=null;
		try{
			ResourceUtil.touch(res);
			os = res.getOutputStream(true);
			ps = new PrintStream(os);
			_oe(ps, o);
			if(addStackTrace) new Exception("Stack trace").printStackTrace(ps);
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
		finally{
			IOUtil.closeEL(ps);
			IOUtil.closeEL(os);
		}
	}
	public static void _oe(Object o, boolean d) {
		_oe(System.out, o);
	}
	public static void o(Object o) {
		_oe(System.out, o);
	}
	public static void e(Object o) {
		_oe(System.err, o);
	}
	public static void oe(Object o, boolean valid) {
		_oe(valid?System.out:System.err, o);
	}
	
	public static void dateO(String value) {
		_date(System.out, value);
	}
	
	public static void dateE(String value) {
		_date(System.err, value);
	}

	private static void _date(PrintStream ps,String value) {
		long millis = System.currentTimeMillis();
		_oe(ps,
		new Date(millis)
		+"-"
		+(millis-(millis/1000*1000))
		+" "+value);
	}
	
	
	
	private static void _oe(PrintStream ps,Object o) {
		if(o instanceof Enumeration) _oe(ps,(Enumeration)o);
    	else if(o instanceof Object[]) _oe(ps,(Object[])o);
    	else if(o instanceof boolean[]) _oe(ps,(boolean[])o);
    	else if(o instanceof byte[]) _oe(ps,(byte[])o);
    	else if(o instanceof int[]) _oe(ps,(int[])o);
    	else if(o instanceof float[]) _oe(ps,(float[])o);
    	else if(o instanceof long[]) _oe(ps,(long[])o);
    	else if(o instanceof double[]) _oe(ps,(double[])o);
    	else if(o instanceof char[]) _oe(ps,(char[])o);
    	else if(o instanceof short[]) _oe(ps,(short[])o);
    	else if(o instanceof Set) _oe(ps,(Set)o);
    	else if(o instanceof List) _oe(ps,(List)o);
    	else if(o instanceof Map) _oe(ps,(Map)o);
    	else if(o instanceof Collection) _oe(ps,(Collection)o);
    	else if(o instanceof Iterator) _oe(ps,(Iterator)o);
    	else if(o instanceof NamedNodeMap) _oe(ps,(NamedNodeMap)o);
    	else if(o instanceof ResultSet) _oe(ps,(ResultSet)o);
    	else if(o instanceof Node) _oe(ps,(Node)o);
    	else if(o instanceof Throwable) _oe(ps,(Throwable)o);
    	else if(o instanceof Cookie) {
    		Cookie c=(Cookie) o;
    		ps.println("Cookie(name:"+c.getName()+";domain:"+c.getDomain()+";maxage:"+c.getMaxAge()+";path:"+c.getPath()+";value:"+c.getValue()+";version:"+c.getVersion()+";secure:"+c.getSecure()+")");
    	}
    	else if(o instanceof InputSource) {
    		InputSource is=(InputSource) o;
    		Reader r = is.getCharacterStream();
    		try {
				ps.println(IOUtil.toString(is.getCharacterStream()));
			} catch (IOException e) {}
			finally {
				IOUtil.closeEL(r);
			}
    	}
    	
    	else ps.println(o);
    }
	
	
	
	
	private static void _oe(PrintStream ps,Object[] arr) {
    	if(arr==null){
    		ps.println("null");
    		return;
    	}
        ps.print(arr.getClass().getComponentType().getName()+"[]{");
        for(int i=0;i<arr.length;i++) {
            if(i>0) {
                ps.print("\t,");
            }
            _oe(ps,arr[i]);
        }
        ps.println("}");
    }
    
    private static void _oe(PrintStream ps,int[] arr) {
        ps.print("int[]{");
        for(int i=0;i<arr.length;i++) {
            if(i>0)ps.print(',');
            ps.print(arr[i]);
        }
        ps.println("}");
    }
    
    private static void _oe(PrintStream ps,byte[] arr) {
        ps.print("byte[]{");
        for(int i=0;i<arr.length;i++) {
            if(i>0)ps.print(',');
            ps.print(arr[i]);
        }
        ps.println("}");
    }
    
    private static void _oe(PrintStream ps,boolean[] arr) {
        ps.print("boolean[]{");
        for(int i=0;i<arr.length;i++) {
            if(i>0)ps.print(',');
            ps.print(arr[i]);
        }
        ps.println("}");
    }
    
    private static void _oe(PrintStream ps,char[] arr) {
        ps.print("char[]{");
        for(int i=0;i<arr.length;i++) {
            if(i>0)ps.print(',');
            ps.print(arr[i]);
        }
        ps.println("}");
    }
    
    private static void _oe(PrintStream ps,short[] arr) {
        ps.print("short[]{");
        for(int i=0;i<arr.length;i++) {
            if(i>0)ps.print(',');
            ps.print(arr[i]);
        }
        ps.println("}");
    }
    
    private static void _oe(PrintStream ps,float[] arr) {
        ps.print("float[]{");
        for(int i=0;i<arr.length;i++) {
            if(i>0)ps.print(',');
            ps.print(arr[i]);
        }
        ps.println("}");
    }
    
    private static void _oe(PrintStream ps,long[] arr) {
        ps.print("long[]{");
        for(int i=0;i<arr.length;i++) {
            if(i>0)ps.print(',');
            ps.print(arr[i]);
        }
        ps.println("}");
    }
    
    private static void _oe(PrintStream ps,double[] arr) {
        ps.print("double[]{");
        for(int i=0;i<arr.length;i++) {
            if(i>0)ps.print(',');
            ps.print(arr[i]);
        }
        ps.println("}");
    }
    

	private static void _oe(PrintStream ps,Node n) {
		ps.print(Caster.toString(n,null));
	}
	
	
	private static void _oe(PrintStream ps,Throwable t) {
    	t.printStackTrace(ps);
    }
    

    private static void _oe(PrintStream ps,Enumeration en) {
        
    	_oe(ps,en.getClass().getName()+" [");
        while(en.hasMoreElements()) {
        	ps.print(en.nextElement());
            ps.println(",");
        }
        _oe(ps,"]");
    }
    
    private static void _oe(PrintStream ps,List list) {
        ListIterator it = list.listIterator();
        _oe(ps,list.getClass().getName()+" {");
        while(it.hasNext()) {
            int index = it.nextIndex();
            it.next();
            ps.print(index);
            ps.print(":");
            ps.print(list.get(index));
            ps.println(";");
        }
        _oe(ps,"}");
    }
    
    private static void _oe(PrintStream ps,Collection coll) {
    	Iterator it = coll.iterator();
        _oe(ps,coll.getClass().getName()+" {");
        while(it.hasNext()) {
        	_oe(ps, it.next());
        }
        _oe(ps,"}");
    }
    
    private static void _oe(PrintStream ps,Iterator it) {
        
        _oe(ps,it.getClass().getName()+" {");
        while(it.hasNext()) {
            ps.print(it.next());
            ps.println(";");
        }
        _oe(ps,"}");
    }
    
    
    private static void _oe(PrintStream ps,Set set) {
    	Iterator it = set.iterator();
    	ps.println(set.getClass().getName()+" {");
    	boolean first=true;
        while(it.hasNext()) {
        	if(!first){
        		ps.println();
        		ps.print(",");
        	}
            _oe(ps,it.next());
            first=false;
        }
        _oe(ps,"}");
    }
    
    private static void _oe(PrintStream ps,ResultSet res) {
    	try {
			_oe(ps, new QueryImpl(res,"query",null).toString());
		} catch (PageException e) {
			_oe(ps, res.toString());
		}
    }

    private static void _oe(PrintStream ps,Map map) {
    	if(map==null) {
    		ps.println("null");
    		return;
    	}
        Iterator it = map.keySet().iterator();
        
        if(map.size()<2) {
        	ps.print(map.getClass().getName()+" {");
            while(it.hasNext()) {
                Object key = it.next();

                _oe(ps,key);
                ps.print(":");
                _oe(ps,map.get(key));
            }
            ps.println("}");
        } 
        else {
	        ps.println(map.getClass().getName()+" {");
	        while(it.hasNext()) {
	            Object key = it.next();
	            ps.print("	");
	            _oe(ps,key);
	            ps.print(":");
	            _oe(ps,map.get(key));
	            ps.println(";");
	        }
	        ps.println("}");
        }
    }

    private static void _oe(PrintStream ps,NamedNodeMap map) {
    	if(map==null) {
    		ps.println("null");
    		return;
    	}
        int len = map.getLength();
        ps.print(map.getClass().getName()+" {");
        Attr attr;
        for(int i=0;i<len;i++) {
        	attr=(Attr)map.item(i);

        	ps.print(attr.getName());
        	ps.print(":");
        	ps.print(attr.getValue());
            ps.println(";");
        }
        ps.println("}");
    }
    
    

}