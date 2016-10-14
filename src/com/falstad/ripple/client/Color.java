package com.falstad.ripple.client;

public class Color
{
    public final static Color white = new Color(255, 255, 255);
    public final static Color lightGray = new Color(192, 192, 192);
    public final static Color gray = new Color(128, 128, 128);
    public final static Color GRAY = new Color(128, 128, 128);
    public final static Color dark_gray = new Color(64, 64, 64);
    public final static Color darkGray = new Color(64, 64, 64);
    public final static Color black = new Color(0, 0, 0);
    public final static Color red = new Color(255, 0, 0);
    public final static Color pink = new Color(255, 175, 175);
    public final static Color orange = new Color(255, 200, 0);
    public final static Color yellow = new Color(255, 255, 0);
    public final static Color green = new Color(0, 255, 0);
    public final static Color magenta = new Color(255, 0, 255);
    public final static Color cyan = new Color(0, 255, 255);
    public final static Color blue = new Color(0, 0, 255);
    public static final Color NONE = new Color("");
    
    private int r, g, b;
    
    // only for special cases, like no color, or maybe named colors
    private String colorText = null;

    private Color (String colorText) {
        this.colorText = colorText;
    }

    public Color (int r, int g, int b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getRed ()
    {
        return r;
    }

    public int getGreen ()
    {
        return g;
    }

    public int getBlue ()
    {
        return b;
    }

    public String getHexValue ()
    {
        if (colorText != null) {
            return colorText;
        }

        return "#"
            + pad(Integer.toHexString(r))
            + pad(Integer.toHexString(g))
            + pad(Integer.toHexString(b));
    }

    private String pad (String in)
    {
        if (in.length() == 0) {
            return "00";
        }
        if (in.length() == 1) {
            return "0" + in;
        }
        return in;
    }

    public String toString ()
    {
        if (colorText != null) {
            return colorText;
        }
        return "red=" + r + ", green=" + g + ", blue=" + b;
    }
    
    public static Color decode(String nm) throws NumberFormatException {
        Integer intval = Integer.decode(nm);
        int i = intval.intValue();
        return new Color((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
    }
    
    public static Color hex2Rgb(String colorStr) {
    	if(colorStr.length()>8){
    		return new Color(
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 ),
                Integer.valueOf( colorStr.substring( 7, 9 ), 16 ) );
    	}
    	else{
    		return new Color(
                    Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                    Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                    Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
    	}
    }
}
