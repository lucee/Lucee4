/**
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
 **/
package lucee.runtime.search.lucene2.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;


/**
 * <p>Analyzer for Italian language</p>
 */
public final class ItalianAnalyzer extends Analyzer {

	private final static char A_GRAPH=(char)224;
	private final static char E_GRAPH=(char)232;
	private final static char I_GRAPH=(char)236;
	private final static char O_GRAPH=(char)242;
	private final static char U_GRAPH=(char)249;
	

	private final static char E_EGU=(char)233;
	
	
	private static SnowballAnalyzer analyzer;

	private final static String[] STOP_WORDS = { "a", "abbia",
                    "abbiamo", "abbiano", "abbiate", "ad", "agl", "agli", "ai",
                    "al", "all", "alla", "alle", "allo", "anche", "avemmo",
                    "avendo", "avesse", "avessero", "avessi", "avessimo",
                    "aveste", "avesti", "avete", "aveva", "avevamo", "avevano",
                    "avevate", "avevi", "avevo", "avr"+A_GRAPH, "avrai", "avranno",
                    "avrebbe", "avrebbero", "avrei", "avremmo", "avremo",
                    "avreste", "avresti", "avrete", "avr"+O_GRAPH, "avuta", "avute",
                    "avuti", "avuto", "c", "che", "chi", "ci", "coi", "come",
                    "con", "contro", "cui", "da", "dagl", "dagli", "dai",
                    "dal", "dall", "dalle", "dallo", "degl", "degli", "dei",
                    "del", "dell", "della", "delle", "dello", "di", "dov",
                    "dove", "e", ""+E_GRAPH, "ebbe", "ebbero", "ebbi", "ed", "erano",
                    "eravamo", "eravate", "eri", "ero", "essendo", "fa", "f"+A_GRAPH,
                    "facciamo", "facciano", "faccio", "facemmo", "facendo",
                    "facesse", "facessero", "facessi", "facessimo", "faceste",
                    "facesti", "faceva", "facevamo", "facevano", "facevate",
                    "facevi", "facevo", "fai", "fanno", "far"+A_GRAPH, "farai",
                    "faranno", "farebbe", "farebbero", "farei", "faremmo",
                    "faremo", "fareste", "faresti", "farete", "far"+O_GRAPH, "fece",
                    "fecero", "fossero", "fossimo", "foste", "fosti", "fu",
                    "fui", "fummo", "furono", "gli", "ha", "hai", "hanno",
                    "ho", "i", "il", "in", "io", "l", "la", "l"+A_GRAPH, "le", "lei",
                    "li", "l"+I_GRAPH, "lo", "loro", "lui", "ma", "mi", "mia", "mie",
                    "miei", "mio", "ne", "negl", "negli", "nei", "nel", "nell",
                    "nella", "nelle", "nello", "noi", "non", "nostra",
                    "nostre", "nostri", "nostro", "o", "per", "perch"+E_EGU, "pi"+U_GRAPH,
                    "quale", "quanta", "quante", "quanti", "quanto", "quella",
                    "quelle", "quelli", "quello", "questa", "queste", "questi",
                    "questo", "sar"+A_GRAPH, "sarai", "saranno", "sarebbe",
                    "sarebbero", "sarei", "saremmo", "saremo", "sareste",
                    "saresti", "sarete", "sar"+O_GRAPH, "se", "sei", "si", "s"+I_GRAPH,
                    "sia", "siamo", "siano", "siate", "siete", "sono", "sta",
                    "stai", "stando", "stanno", "star"+A_GRAPH, "starai", "staranno",
                    "starebbe", "starebbero", "starei", "staremmo", "staremo",
                    "stareste", "staresti", "starete", "star"+O_GRAPH, "stava",
                    "stavamo", "stavano", "stavate", "stavi", "stavo",
                    "stemmo", "stesse", "stessero", "stessi", "stessimo",
                    "steste", "stesti", "stette", "stettero", "stetti", "stia",
                    "stiamo", "stiano", "stiate", "sto", "su", "sua", "sue",
                    "sugl", "sugli", "sui", "sul", "sull", "sulla", "sulle",
                    "sullo", "suo", "suoi", "ti", "tra", "tu", "tua", "tue",
                    "tuo", "tuoi", "tutti", "tutto", "un", "una", "uno", "vi",
                    "voi", "vostra", "vostre", "vostri", "vostro" };


	/**
	 * Creates new instance of SpanishAnalyzer
	 */
	public ItalianAnalyzer() {
		analyzer = new SnowballAnalyzer("Italian", STOP_WORDS);
	}

	public ItalianAnalyzer(String stopWords[]) {
		analyzer = new SnowballAnalyzer("Italian", stopWords);
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		return analyzer.tokenStream(fieldName, reader);
	}
}