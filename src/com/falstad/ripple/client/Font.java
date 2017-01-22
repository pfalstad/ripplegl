/*
    Copyright (C) 2017 by Paul Falstad

    This file is part of RippleGL.

    RippleGL is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 2 of the License, or
    (at your option) any later version.

    RippleGL is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RippleGL.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.falstad.ripple.client;



class Font {
	static final int BOLD=1;
	
	String fontname;
	int size;
	
	public Font(String name, int style, int size){
		String styleStr="normal ";
		if (name=="SansSerif")
			name="sans-serif";
		if ((style & BOLD) !=0)
			styleStr="bold ";
		fontname=styleStr+size+"px "+name;
		this.size=size;
	}
}
