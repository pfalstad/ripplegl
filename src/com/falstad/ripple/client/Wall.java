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

public class Wall extends DragObject {

	Wall() {
		handles.add(new DragHandle(this));
		handles.add(new DragHandle(this));
		setTransform();
	}
	
	Wall(int x1, int y1, int x2, int y2) {
		handles.add(new DragHandle(this, x1, y1));
		handles.add(new DragHandle(this, x2, y2));
		setTransform();
	}

	Wall(StringTokenizer st) {
		super(st);
		handles.add(new DragHandle(this, st));
		handles.add(new DragHandle(this, st));
		setTransform();
	}
	
	void prepare() {
		RippleSim.drawWall(handles.get(0).x, handles.get(0).y, handles.get(1).x, handles.get(1).y);
	}
	
	@Override void drawSelection() {
		prepare();
	}
	
	int getDumpType() { return 'w'; }

}
