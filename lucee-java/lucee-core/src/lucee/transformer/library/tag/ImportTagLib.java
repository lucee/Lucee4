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

package lucee.transformer.library.tag;

import java.util.Map;

import lucee.transformer.cfml.ExprTransformer;

/**
 * 
 */
public final class ImportTagLib extends TagLib {
    
    private String taglib;
    private String prefix;
    
    public ImportTagLib(String taglib,String prefix) {
    	super(false);
        this.taglib=taglib;
        this.prefix=prefix;
    }
    

    /**
     * @see lucee.transformer.library.tag.TagLib#getAppendixTag(java.lang.String)
     */
    public TagLibTag getAppendixTag(String name) {
        return super.getAppendixTag(name);
    }
    /**
     * @see lucee.transformer.library.tag.TagLib#getELClass()
     */
    public String getELClass() {
        return super.getELClass();
    }
    /**
     * @see lucee.transformer.library.tag.TagLib#getExprTransfomer()
     */
    public ExprTransformer getExprTransfomer() throws TagLibException {
        return super.getExprTransfomer();
    }
    /**
     * @see lucee.transformer.library.tag.TagLib#getNameSpace()
     */
    public String getNameSpace() {
        return super.getNameSpace();
    }
    /**
     * @see lucee.transformer.library.tag.TagLib#getNameSpaceAndSeparator()
     */
    public String getNameSpaceAndSeparator() {
        return super.getNameSpaceAndSeparator();
    }
    /**
     * @see lucee.transformer.library.tag.TagLib#getNameSpaceSeparator()
     */
    public String getNameSpaceSeparator() {
        return super.getNameSpaceSeparator();
    }
    /**
     * @see lucee.transformer.library.tag.TagLib#getTag(java.lang.String)
     */
    public TagLibTag getTag(String name) {
        return super.getTag(name);
    }
    /**
     * @see lucee.transformer.library.tag.TagLib#getTags()
     */
    public Map getTags() {
        return super.getTags();
    }
    /**
     * @see lucee.transformer.library.tag.TagLib#setELClass(java.lang.String)
     */
    protected void setELClass(String eLClass) {
        super.setELClass(eLClass);
    }
    /**
     * @see lucee.transformer.library.tag.TagLib#setNameSpace(java.lang.String)
     */
    public void setNameSpace(String nameSpace) {
        super.setNameSpace(nameSpace);
    }
    /**
     * @see lucee.transformer.library.tag.TagLib#setNameSpaceSeperator(java.lang.String)
     */
    public void setNameSpaceSeperator(String nameSpaceSeperator) {
        super.setNameSpaceSeperator(nameSpaceSeperator);
    }
    /**
     * @see lucee.transformer.library.tag.TagLib#setTag(lucee.transformer.library.tag.TagLibTag)
     */
    public void setTag(TagLibTag tag) {
        super.setTag(tag);
    }
}