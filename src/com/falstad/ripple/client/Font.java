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
