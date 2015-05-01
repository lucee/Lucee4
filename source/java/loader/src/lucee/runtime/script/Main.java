package lucee.runtime.script;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import lucee.loader.engine.CFMLEngine;

public class Main {
	
	private static final String USAGE="Usage: script [-options]\n\n"+
		"Where options include:\n"+
		"-l  language\n"+
		"-e  code\n"
		;
	private static final Charset UTF8 = Charset.forName("UTF-8");


	public static void main(String args[]) throws Exception {
		
		
		String lang="CFML";
		String code=null;
		
		String arg,pw=null,key=null;
		for(int i=0;i<args.length;i++) {
			arg=args[i];
			if("-l".equals(arg)) {
				if(args.length>i+1) {
					lang=args[++i].trim();
				}
			}
			else if("-e".equals(arg)) {
				if(args.length>i+1) {
					code=args[++i].trim();
				}
			}
		}
		int dialect=CFMLEngine.DIALECT_CFML;
		if(code==null) printUsage("-e is missing",System.err);
		
		
		LuceeScriptEngineFactory factory = new LuceeScriptEngineFactory();
		System.out.println(factory.getScriptEngine().eval(code));
		
		ScriptEngine engine = new ScriptEngineManager().getEngineByName(lang);
		if(engine==null)System.out.println("could not load a engine with the name:"+lang);
		else System.out.println(engine.eval(code));
		
	}


	private static void printUsage(String msg,PrintStream ps) {
		ps.println();
		ps.println("Failed to execute!");
		ps.println("Reason: "+msg);
		ps.println();
		ps.print(USAGE);
		ps.flush();
		
		System.exit(0);
	}
}
