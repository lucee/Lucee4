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
/*
*

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package lucee.runtime.img.filter;import java.awt.image.BufferedImage;
import java.util.Random;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.ImageUtil;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.CollectionUtil;

/**
 * A filter which "dissolves" an image by thresholding the alpha channel with random numbers.
 */
public class DissolveFilter extends PointFilter  implements DynFiltering {
	
	private float density = 1;
	private float softness = 0;
	private float minDensity, maxDensity;
	private Random randomNumbers;
	
	public DissolveFilter() {
	}

	/**
	 * Set the density of the image in the range 0..1.
	 * @param density the density
     * @min-value 0
     * @max-value 1
     * @see #getDensity
	 */
	public void setDensity( float density ) {
		this.density = density;
	}
	
	/**
	 * Get the density of the image.
	 * @return the density
     * @see #setDensity
	 */
	public float getDensity() {
		return density;
	}
	
	/**
	 * Set the softness of the dissolve in the range 0..1.
	 * @param softness the softness
     * @min-value 0
     * @max-value 1
     * @see #getSoftness
	 */
	public void setSoftness( float softness ) {
		this.softness = softness;
	}
	
	/**
	 * Get the softness of the dissolve.
	 * @return the softness
     * @see #setSoftness
	 */
	public float getSoftness() {
		return softness;
	}
	
    public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
		float d = (1-density) * (1+softness);
		minDensity = d-softness;
		maxDensity = d;
		randomNumbers = new Random( 0 );
		return super.filter( src, dst );
	}
	
	public int filterRGB(int x, int y, int rgb) {
		int a = (rgb >> 24) & 0xff;
		float v = randomNumbers.nextFloat();
		float f = ImageMath.smoothStep( minDensity, maxDensity, v );
		return ((int)(a * f) << 24) | rgb & 0x00ffffff;
	}

	public String toString() {
		return "Stylize/Dissolve...";
	}
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		if((o=parameters.removeEL(KeyImpl.init("Density")))!=null)setDensity(ImageFilterUtil.toFloatValue(o,"Density"));
		if((o=parameters.removeEL(KeyImpl.init("Softness")))!=null)setSoftness(ImageFilterUtil.toFloatValue(o,"Softness"));
		if((o=parameters.removeEL(KeyImpl.init("Dimensions")))!=null){
			int[] dim=ImageFilterUtil.toDimensions(o,"Dimensions");
			setDimensions(dim[0],dim[1]);
		}

		// check for arguments not supported
		if(parameters.size()>0) {
			throw new FunctionException(ThreadLocalPageContext.get(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+CollectionUtil.getKeyList(parameters,", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Density, Softness, Dimensions]");
		}

		return filter(src, dst);
	}
}
