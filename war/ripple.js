
var gl;
var canvas;
var gridSizeX =1024, gridSizeY =1024, windowOffsetX =40, windowOffsetY =40;
var windowWidth, windowHeight, viewAngle, viewHeight;
var sim;
var damping;
var transform = [1, 0, 0, 1, 0, 0];

    function getShader(gl, id, prefix) {
        var shaderScript = document.getElementById(id);
        if (!shaderScript) {
            return null;
        }

        var str = "";
        var k = shaderScript.firstChild;
        while (k) {
            if (k.nodeType == 3) {
                str += k.textContent;
            }
            k = k.nextSibling;
        }

        var shader;
        if (shaderScript.type == "x-shader/x-fragment") {
            shader = gl.createShader(gl.FRAGMENT_SHADER);
        } else if (shaderScript.type == "x-shader/x-vertex") {
            shader = gl.createShader(gl.VERTEX_SHADER);
        } else {
            return null;
        }

        if (prefix)
        	str = prefix + str;
        gl.shaderSource(shader, str);
        gl.compileShader(shader);

        if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
            alert(gl.getShaderInfoLog(shader));
            return null;
        }

        return shader;
    }


    var shaderProgramMain, shaderProgramFixed, shaderProgramAcoustic, shaderProgramDraw, shaderProgramMode, shaderProgramPoke;

    function initShader(fs, vs, prefix) {
        var fragmentShader = getShader(gl, fs, prefix);
//        var vs = (fs == "shader-draw-fs" || fs == "shader-mode-fs") ? "shader-draw-vs" : "shader-vs";
        var vertexShader = getShader(gl, vs, prefix);

        var shaderProgram = gl.createProgram();
        gl.attachShader(shaderProgram, vertexShader);
        gl.attachShader(shaderProgram, fragmentShader);
        gl.linkProgram(shaderProgram);

        if (!gl.getProgramParameter(shaderProgram, gl.LINK_STATUS)) {
        	debugger;
            alert("Could not initialise shaders");
        }

        gl.useProgram(shaderProgram);

        shaderProgram.vertexPositionAttribute = gl.getAttribLocation(shaderProgram, "aVertexPosition");
        shaderProgram.textureCoordAttribute = gl.getAttribLocation(shaderProgram, "aTextureCoord");
        shaderProgram.dampingAttribute = gl.getAttribLocation(shaderProgram, "aDamping");
        shaderProgram.colorAttribute = gl.getAttribLocation(shaderProgram, "aColor");

        shaderProgram.pMatrixUniform = gl.getUniformLocation(shaderProgram, "uPMatrix");
        shaderProgram.mvMatrixUniform = gl.getUniformLocation(shaderProgram, "uMVMatrix");
        shaderProgram.samplerUniform = gl.getUniformLocation(shaderProgram, "uSampler");

        return shaderProgram;
    }

    function initShaders() {
    	shaderProgramMain = initShader("shader-display-fs", "shader-vs", null);
    	shaderProgramMain.brightnessUniform = gl.getUniformLocation(shaderProgramMain, "brightness");
    	shaderProgramMain.colorsUniform = gl.getUniformLocation(shaderProgramMain, "colors");

    	shaderProgram3D = initShader("shader-draw-fs", "shader-3d-vs", null);
    	shaderProgram3D.brightnessUniform = gl.getUniformLocation(shaderProgram3D, "brightness");
    	shaderProgram3D.colorsUniform = gl.getUniformLocation(shaderProgram3D, "colors");
    	shaderProgram3D.xOffsetUniform = gl.getUniformLocation(shaderProgram3D, "xOffset");

    	shaderProgramFixed = initShader("shader-simulate-fs", "shader-vs", null);
    	shaderProgramFixed.stepSizeXUniform = gl.getUniformLocation(shaderProgramFixed, "stepSizeX");
    	shaderProgramFixed.stepSizeYUniform = gl.getUniformLocation(shaderProgramFixed, "stepSizeY");

    	shaderProgramAcoustic = initShader("shader-simulate-fs", "shader-vs", "#define ACOUSTIC 1\n");
    	shaderProgramAcoustic.stepSizeXUniform = gl.getUniformLocation(shaderProgramAcoustic, "stepSizeX");
    	shaderProgramAcoustic.stepSizeYUniform = gl.getUniformLocation(shaderProgramAcoustic, "stepSizeY");

    	shaderProgramDraw = initShader("shader-draw-fs", "shader-draw-vs");
    	shaderProgramDrawLine = initShader("shader-draw-line-fs", "shader-draw-vs");
    	shaderProgramMode = initShader("shader-mode-fs", "shader-draw-vs");
    	shaderProgramPoke = initShader("shader-poke-fs", "shader-vs");

        shaderProgramPoke.pokePositionUniform = gl.getUniformLocation(shaderProgramPoke, "pokePosition");
        shaderProgramPoke.pokeValueUniform = gl.getUniformLocation(shaderProgramPoke, "pokeValue");
    }

    var moonTexture;

    function initTextures() {
    }


    var mvMatrix = mat4.create();
    var mvMatrixStack = [];
    var pMatrix = mat4.create();
    var matrix3d = mat4.create();
    var zoom3d = 1;
    
    function mvPushMatrix() {
        var copy = mat4.create();
        mat4.set(mvMatrix, copy);
        mvMatrixStack.push(copy);
    }

    function mvPopMatrix() {
        if (mvMatrixStack.length == 0) {
            throw "Invalid popMatrix!";
        }
        mvMatrix = mvMatrixStack.pop();
    }

    function setMatrixUniforms(shaderProgram) {
        gl.uniformMatrix4fv(shaderProgram.pMatrixUniform, false, pMatrix);
        gl.uniformMatrix4fv(shaderProgram.mvMatrixUniform, false, mvMatrix);

        var normalMatrix = mat3.create();
        mat4.toInverseMat3(mvMatrix, normalMatrix);
        mat3.transpose(normalMatrix);
        gl.uniformMatrix3fv(shaderProgram.nMatrixUniform, false, normalMatrix);
    }

    function degToRad(degrees) {
        return degrees * Math.PI / 180;
    }


    var renderTexture1, renderTexture2;
    var fbType;

    function initTextureFramebuffer() {
    	var rttFramebuffer = gl.createFramebuffer();
    	gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
    	rttFramebuffer.width = gridSizeX;
    	rttFramebuffer.height = gridSizeY;

    	var rttTexture = gl.createTexture();
    	gl.bindTexture(gl.TEXTURE_2D, rttTexture);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
    	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
    	//gl.generateMipmap(gl.TEXTURE_2D);

    	//gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, rttFramebuffer.width, rttFramebuffer.height, 0, gl.RGBA, gl.UNSIGNED_BYTE, null);
    	gl.HALF_FLOAT_OES = 0x8D61;
    	
    	if (fbType == 0) {
    		// this works on android
    		gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, rttFramebuffer.width, rttFramebuffer.height, 0, gl.RGBA, gl.FLOAT, null);
    	} else {
    		// for ios
    		gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGB, rttFramebuffer.width, rttFramebuffer.height, 0, gl.RGB, gl.HALF_FLOAT_OES, null);
    	}

    	var renderbuffer = gl.createRenderbuffer();
    	gl.bindRenderbuffer(gl.RENDERBUFFER, renderbuffer);
//    	gl.renderbufferStorage(gl.RENDERBUFFER, gl.DEPTH_COMPONENT16, rttFramebuffer.width, rttFramebuffer.height);

    	gl.framebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, gl.TEXTURE_2D, rttTexture, 0);
//    	gl.framebufferRenderbuffer(gl.FRAMEBUFFER, gl.DEPTH_ATTACHMENT, gl.RENDERBUFFER, renderbuffer);

        var status = gl.checkFramebufferStatus(gl.FRAMEBUFFER);
        if (status !== gl.FRAMEBUFFER_COMPLETE) {
          return null;
        }

	while(gl.getError() != gl.NO_ERROR) { }
	var pixels = new Float32Array(4);
	gl.readPixels(0, 0, 1, 1, gl.RGBA, gl.FLOAT, pixels);
	if (gl.getError() != gl.NO_ERROR)
	    console.log("readPixels failed");
	else
	    sim.readPixelsWorks = true;

    	gl.bindTexture(gl.TEXTURE_2D, null);
    	gl.bindRenderbuffer(gl.RENDERBUFFER, null);
    	gl.bindFramebuffer(gl.FRAMEBUFFER, null);

    	return {framebuffer:rttFramebuffer, texture:rttTexture};
    }

    function deleteRenderTexture(rt) {
    	gl.deleteTexture(rt.texture);
    	gl.deleteFramebuffer(rt.framebuffer);
    }

    var vertexPositionBuffer;
    var vertexTextureCoordBuffer;
    var screen3DTextureBuffer;
    var simVertexPositionBuffer;
    var simVertexTextureCoordBuffer;
    var simVertexBuffer;
    var simVertexDampingBuffer;
    
    var simPosition = [];
    var simTextureCoord = [];
    var simDamping = [];
    var srcCoords = [
                     -.26, 0, -.25, 0
                     ];
    var gridSize3D = 256;
    var gridRange;

    function initBuffers() {
    	if (!vertexPositionBuffer)
    		vertexPositionBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, vertexPositionBuffer);
    	vertices = [
    	            -1, +1,
    	            +1, +1,
    	            -1, -1,
    	            +1, -1,
    	            ];
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices), gl.STATIC_DRAW);
    	vertexPositionBuffer.itemSize = 2;
    	vertexPositionBuffer.numItems = 4;

    	if (!vertexTextureCoordBuffer)
    		vertexTextureCoordBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, vertexTextureCoordBuffer);
    	var textureCoords = [
    	                     windowOffsetX/gridSizeX, 1-windowOffsetY/gridSizeY,
    	                     1-windowOffsetX/gridSizeX, 1-windowOffsetY/gridSizeY,
    	                     windowOffsetX/gridSizeX,   windowOffsetY/gridSizeY,
    	                     1-windowOffsetX/gridSizeX,   windowOffsetY/gridSizeY
    	                     ];
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(textureCoords), gl.STATIC_DRAW);
    	vertexTextureCoordBuffer.itemSize = 2;
    	vertexTextureCoordBuffer.numItems = 4;

    	if (!sourceBuffer)
    		sourceBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
    	sourceBuffer.itemSize = 2;
    	sourceBuffer.numItems = 2;

    	if (!colorBuffer)
    		colorBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, colorBuffer);
    	colorBuffer.itemSize = 4;
    	colorBuffer.numItems = 2;

    	if (!screen3DTextureBuffer)
    		screen3DTextureBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, screen3DTextureBuffer);
    	screen3DTextureBuffer.itemSize = 2;
    	var texture3D = [];
    	gridRange = textureCoords[2]-textureCoords[0];
    	for (i = 0; i <= gridSize3D; i++) {
    		texture3D.push(textureCoords[0],
    					   textureCoords[0]+gridRange*i/gridSize3D,
    					   textureCoords[0]+gridRange/gridSize3D,
    					   textureCoords[0]+gridRange*i/gridSize3D);
    	}
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(texture3D), gl.STATIC_DRAW);
    	screen3DTextureBuffer.numItems = texture3D.length / 2;
    	
    	simPosition = [];
    	simDamping = [];
    	simTextureCoord = [];
    	
    	// visible area
    	setPosRect(windowOffsetX, windowOffsetY, gridSizeX-windowOffsetX, gridSizeY-windowOffsetY);

    	// sides
    	setPosRect(1, windowOffsetY, windowOffsetX, gridSizeY-windowOffsetY);
    	setPosRect(gridSizeX-windowOffsetX, windowOffsetY, gridSizeX-2, gridSizeY-windowOffsetY);
    	setPosRect(windowOffsetX, 1, gridSizeX-windowOffsetX, windowOffsetY);
    	setPosRect(windowOffsetX, gridSizeY-windowOffsetY, gridSizeX-windowOffsetX, gridSizeY-2);

    	// corners
    	setPosRect(1, 1, windowOffsetX, windowOffsetY);
    	setPosRect(gridSizeX-windowOffsetX, 1, gridSizeX-2, windowOffsetY);
    	setPosRect(1, gridSizeY-windowOffsetY, windowOffsetX, gridSizeY-2);
    	setPosRect(gridSizeX-windowOffsetX, gridSizeY-windowOffsetY, gridSizeX-2, gridSizeY-2);


    	if (!simVertexPositionBuffer)
    		simVertexPositionBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexPositionBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simPosition), gl.STATIC_DRAW);
    	simVertexPositionBuffer.itemSize = 2;
    	simVertexPositionBuffer.numItems = simPosition.length/2;

    	if (!simVertexTextureCoordBuffer)
    		simVertexTextureCoordBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexTextureCoordBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simTextureCoord), gl.STATIC_DRAW);
    	simVertexTextureCoordBuffer.itemSize = 2;
    	simVertexTextureCoordBuffer.numItems = simPosition.length/2;

    	if (!simVertexDampingBuffer)
    		simVertexDampingBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexDampingBuffer);
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simDamping), gl.STATIC_DRAW);
    	simVertexDampingBuffer.itemSize = 1;
    	simVertexDampingBuffer.numItems = simDamping.length;
    }

    // create coordinates for a rectangular portion of the grid, making sure to set the damping attribute
    // appropriately (1 for visible area, slightly less for offscreen area used to avoid reflections at edges)
    function setPosRect(x1, y1, x2, y2) {
    	var points = [ x2, y1, x1, y1, x2, y2, x1, y1, x2, y2, x1, y2 ];
    	var i;
    	for (i = 0; i != 6; i++) {
    		var xi = points[i*2];
    		var yi = points[i*2+1];
    		simPosition.push(-1+2*xi/gridSizeX, -1+2*yi/gridSizeY);
    		simTextureCoord.push(xi/gridSizeX, yi/gridSizeY);
    		var damp = damping;
    		if (xi == 1 || yi == 1 || xi == gridSizeX-2 || yi == gridSizeY-2)
    			damp *= .999-8*.01; // was 20
    		simDamping.push(damp);
    	}
    }

    var sourceBuffer;
    var colorBuffer;
    var colors;

    function simulate() {
    	var rt = renderTexture1;
    	renderTexture1 = renderTexture2;
    	renderTexture2 = rt;

    	var rttFramebuffer = renderTexture1.framebuffer;
    	var rttTexture = renderTexture1.texture;
        gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);

        var prog = sim.acoustic ? shaderProgramAcoustic : shaderProgramFixed;
        gl.useProgram(prog);
        var rttFramebuffer = renderTexture1.framebuffer;
        gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
    	gl.clearColor(0.0, 0.0, 0.0, 1.0);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);

        //mvPushMatrix();

        gl.bindBuffer(gl.ARRAY_BUFFER, simVertexPositionBuffer);
        gl.vertexAttribPointer(prog.vertexPositionAttribute, simVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, simVertexTextureCoordBuffer);
        gl.vertexAttribPointer(prog.textureCoordAttribute, simVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(prog.dampingAttribute);
        gl.enableVertexAttribArray(prog.vertexPositionAttribute);
        gl.enableVertexAttribArray(prog.textureCoordAttribute);
        
        gl.bindBuffer(gl.ARRAY_BUFFER, simVertexDampingBuffer);
        gl.vertexAttribPointer(prog.dampingAttribute, simVertexDampingBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, renderTexture2.texture);
        gl.uniform1i(prog.samplerUniform, 0);
        gl.uniform1f(prog.stepSizeXUniform, 1/gridSizeX);
        gl.uniform1f(prog.stepSizeYUniform, 1/gridSizeY);

        setMatrixUniforms(prog);
        gl.drawArrays(gl.TRIANGLES, 0, simVertexPositionBuffer.numItems);
        gl.disableVertexAttribArray(prog.dampingAttribute);
        gl.disableVertexAttribArray(prog.vertexPositionAttribute);
        gl.disableVertexAttribArray(prog.textureCoordAttribute);
    }

    // poke wave (draw a little cone near mouse position).
    // We want to blend this with current value of function but blending with float textures is not well supported.
    // So we use a dedicated shader.  We could just process a small area around the mouse position but that is too
    // much hassle so we just do the entire screen for now.  Since we use this to draw moving sources, it would
    // be good to optimize this in the future.
    function drawPoke(x, y, v) {
    	var rt = renderTexture1;
    	renderTexture1 = renderTexture2;
    	renderTexture2 = rt;

    	var rttFramebuffer = renderTexture1.framebuffer;
    	var rttTexture = renderTexture1.texture;
        gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);

        var prog = shaderProgramPoke;
        gl.useProgram(prog);
        var rttFramebuffer = renderTexture1.framebuffer;
        gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);

        gl.bindBuffer(gl.ARRAY_BUFFER, simVertexPositionBuffer);
        gl.vertexAttribPointer(prog.vertexPositionAttribute, simVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, simVertexTextureCoordBuffer);
        gl.vertexAttribPointer(prog.textureCoordAttribute, simVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(prog.vertexPositionAttribute);
        gl.enableVertexAttribArray(prog.textureCoordAttribute);
        
        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, renderTexture2.texture);
        gl.uniform1i(prog.samplerUniform, 0);

        setMatrixUniforms(prog);

        // get the matrix (but don't load it into the shader) so we can transform the poke coordinates
        loadMatrix(pMatrix);
        gl.uniform2f(prog.pokePositionUniform, pMatrix[0]*x+pMatrix[12], pMatrix[5]*y+pMatrix[13]);
        gl.uniform1f(prog.pokeValueUniform, v);

        gl.drawArrays(gl.TRIANGLES, 0, simVertexPositionBuffer.numItems);
        gl.disableVertexAttribArray(prog.vertexPositionAttribute);
        gl.disableVertexAttribArray(prog.textureCoordAttribute);
    }

    function drawSource(x, y, f) {
        gl.useProgram(shaderProgramDraw);
        gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, f, 0.0, 1.0, 1.0);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        srcCoords[0] = srcCoords[2] = x;
        srcCoords[1] = y;
        srcCoords[3] = srcCoords[1]+1;
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(srcCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

//        gl.bindBuffer(gl.ARRAY_BUFFER, vertexTextureCoordBuffer);
//        gl.vertexAttribPointer(shaderProgramDraw.textureCoordAttribute, vertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
        gl.drawArrays(gl.LINES, 0, 2);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

        //mvPopMatrix();
    }

    function drawHandle(x, y) {
        gl.useProgram(shaderProgramDraw);
        if (sim.drawingSelection >= 0) {
        	drawSelectedHandle(x, y);
        	return;
        }
        if (sim.drawingSelection < 0)
        	gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, 1, 1.0, 1.0, 1.0);
        else 
        	gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, sim.drawingSelection,
        			sim.drawingSelection, 0, 1.0);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var cx = -1+2*(x+.5)/windowWidth;
        var cy = +1-2*(y+.5)/windowHeight;
        var ox = .01;
        var oy = .01;
        var coords = [ cx-ox, cy-oy, cx+ox, cy-oy, cx+ox, cy+oy, cx-ox, cy+oy ];
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        mat4.identity(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
//        gl.lineWidth(sim.drawingSelection < 0 ? 1 : 2);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        gl.drawArrays(gl.LINE_LOOP, 0, 4);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
    }

    function drawSelectedHandle(x, y) {
	gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, sim.drawingSelection, sim.drawingSelection, 0, 0.5);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var cx = -1+2*(x+.5)/windowWidth;
        var cy = +1-2*(y+.5)/windowHeight;
        var ox = .012;
        var oy = .012;
        var coords = [ cx-ox, cy-oy, cx+ox, cy-oy, cx-ox, cy+oy, cx+ox, cy+oy ];
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        mat4.identity(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
//        gl.lineWidth(sim.drawingSelection < 0 ? 1 : 2);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        gl.enable(gl.BLEND);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
        gl.disable(gl.BLEND);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
    }

    function drawFocus(x, y) {
        gl.useProgram(shaderProgramDraw);
        gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, 1, 1.0, 1.0, 1.0);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var cx = x; // -1+2*(x+.5)/windowWidth;
        var cy = y; // +1-2*(y+.5)/windowHeight;
        var ox = 3;
        var oy = 3;
        var coords = [ cx-ox, cy, cx+ox, cy, cx, cy+oy, cx, cy-oy ];
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        setMatrixUniforms(shaderProgramDraw);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        gl.drawArrays(gl.LINES, 0, 4);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

        //mvPopMatrix();
    }

    function drawLineSource(x, y, x2, y2, f, gauss) {
        gl.useProgram(shaderProgramDrawLine);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        srcCoords[0] = x;
        srcCoords[1] = y;
        srcCoords[2] = x2;
        srcCoords[3] = y2;
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(srcCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDrawLine.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        var colors = [ f,0,1, 0, f,0,1,0 ];
        if (gauss)
        	colors = [ f,0,1,-3, f,0,1,3 ];
        gl.bindBuffer(gl.ARRAY_BUFFER, colorBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(colors), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDrawLine.colorAttribute, colorBuffer.itemSize, gl.FLOAT, false, 0, 0);

        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDrawLine);
        gl.enableVertexAttribArray(shaderProgramDrawLine.colorAttribute)
        gl.enableVertexAttribArray(shaderProgramDrawLine.vertexPositionAttribute);
        gl.drawArrays(gl.LINES, 0, 2);
        gl.disableVertexAttribArray(shaderProgramDrawLine.colorAttribute);
        gl.disableVertexAttribArray(shaderProgramDrawLine.vertexPositionAttribute);
    }

    function drawPhasedArray(x, y, x2, y2, f1, f2) {
        var rttFramebuffer = renderTexture1.framebuffer;
        gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
        gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
        gl.colorMask(true, true, false, false);
        gl.useProgram(shaderProgramMode);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        srcCoords[0] = x;
        srcCoords[1] = y;
        srcCoords[2] = x2;
        srcCoords[3] = y2;
        var colors = [f1, Math.PI/2, 0, 0, f2, Math.PI/2, 0, 0];
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(srcCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramMode.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, colorBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(colors), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramMode.colorAttribute, colorBuffer.itemSize, gl.FLOAT, false, 0, 0);

        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramMode);
        gl.enableVertexAttribArray(shaderProgramMode.vertexPositionAttribute);
        gl.enableVertexAttribArray(shaderProgramMode.colorAttribute);
        gl.drawArrays(gl.LINES, 0, 2);
        gl.disableVertexAttribArray(shaderProgramMode.vertexPositionAttribute);
        gl.disableVertexAttribArray(shaderProgramMode.colorAttribute);

        gl.colorMask(true, true, true, true);
        gl.bindFramebuffer(gl.FRAMEBUFFER, null);

        //mvPopMatrix();
    }

    function loadMatrix(mtx) {
    	mat4.identity(mtx);
    	if (sim.drawingSelection > 0) {
    		// drawing on screen
        	mtx[0] = +2/windowWidth;
        	mtx[5] = -2/windowHeight;
        	mtx[12] = -1 + .5*mtx[0];
        	mtx[13] = +1 + .5*mtx[5];
    	} else {
    		// drawing walls into render texture
        	mtx[0] = +2/gridSizeX;
        	mtx[5] = -2/gridSizeY;
        	mtx[12] = -1 + (.5+windowOffsetX)*mtx[0];
        	mtx[13] = +1 + (.5+windowOffsetY)*mtx[5];
    	}
    	mat4.multiply(mtx, [transform[0], transform[3], 0, 0,
    	                    transform[1], transform[4], 0, 0,
    	                    0,0,1,0,
    	                    transform[2], transform[5], 0, 1], mtx);
    }

    function setupForDrawing(v) {
        if (sim.drawingSelection > 0) {
       		gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, sim.drawingColor[0]*sim.drawingSelection, sim.drawingColor[1]*sim.drawingSelection,
				sim.drawingColor[2]*sim.drawingSelection, sim.drawingColor[3]);
        } else {
    		var rttFramebuffer = renderTexture1.framebuffer;
    		gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
    		gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
            gl.useProgram(shaderProgramDraw);
            
            // blue channel used for walls and media
    		gl.colorMask(false, false, true, false);
    		gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, 0.0, 0.0, v, 1.0);
    	}
    }
    
    // gl.lineWidth does not work on Chrome, so we need this workaround to draw lines as
    // triangle strips instead
    function thickLinePoints(arr, thick) {
    	var i;
    	var result = [];
    	var ax = 0, ay = 0;
    	for (i = 0; i < arr.length-2; i += 2) {
    		var dx = arr[i+2] - arr[i];
    		var dy = arr[i+3] - arr[i+1];
    		var dl = Math.hypot(dx, dy);
    		if (dl > 0) {
    			var mult = thick/dl;
    			ax =  mult*dy;
    			ay = -mult*dx;
    		}	
    		result.push(arr[i]+ax, arr[i+1]+ay, arr[i]-ax, arr[i+1]-ay);
    	}
    	result.push(arr[i]+ax, arr[i+1]+ay, arr[i]-ax, arr[i+1]-ay);
    	return result;
    }
    
    function drawWall(x, y, x2, y2, v) {
    	setupForDrawing(v);
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        // draw line back on itself, or else one endpoint won't be drawn
        srcCoords = thickLinePoints([x, y, x2, y2, x, y], 1.5);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(srcCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
//        gl.lineWidth(3);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
//        gl.drawArrays(gl.LINE_STRIP, 0, 3);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 6);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
//        gl.lineWidth(1);

		gl.colorMask(true, true, true, true);
		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }

    function drawEllipse(cx, cy, xr, yr) {
    	setupForDrawing(0);
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var coords = [];
        var i;
        for (i = -xr; i <= xr; i++) {
        	coords.push(cx-i, cy-yr*Math.sqrt(1-i*i/(xr*xr)));
        }
        for (i = xr-1; i >= -xr; i--) {
        	coords.push(cx-i, cy+yr*Math.sqrt(1-i*i/(xr*xr)));
        }
        coords.push(coords[0], coords[1]);
//        console.log("coords for ellipse: " + coords);
        coords = thickLinePoints(coords, 1.5);
//        gl.lineWidth(4);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, coords.length/2);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
//        gl.lineWidth(1);

		gl.colorMask(true, true, true, true);
		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }

    function drawParabola(x1, y1, w, h) {
    	setupForDrawing(0);
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var coords = [];
        var i;
        var w2 = w/2;
        var a = h/(w2*w2);
        for (i = 0; i <= w; i++) {
        	var x0 = i-w2;
        	coords.push(x1+i, y1+h-a*x0*x0);
        }
        coords = thickLinePoints(coords, 1.5);
//        gl.lineWidth(4);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, coords.length/2);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
//        gl.lineWidth(1);

		gl.colorMask(true, true, true, true);
		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }

    function drawLens(x1, y1, w, h, m) {
    	setupForDrawing(m);
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var i;
        var w2 = w/2;
        var coords = [x1+w2, y1+h];
        var ym = h/(Math.sqrt(2)-1);
        for (i = 0; i <= w; i++) {
        	var x = (i-w2)/w2;
        	var y = ym*(Math.sqrt(1+x*x)-1);
        	coords.push(x1+i, y1+y);
        }
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        
        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
        gl.drawArrays(gl.TRIANGLE_FAN, 0, coords.length/2);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

		gl.colorMask(true, true, true, true);
		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }
    
    function drawSolidEllipse(cx, cy, xr, yr, med) {
    	setupForDrawing(med);
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var coords = [cx, cy];
        var i;
        for (i = -xr; i <= xr; i++) {
        	coords.push(cx-i, cy-yr*Math.sqrt(1-i*i/(xr*xr)));
        }
        for (i = xr-1; i >= -xr; i--) {
        	coords.push(cx-i, cy+yr*Math.sqrt(1-i*i/(xr*xr)));
        }
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
        gl.drawArrays(gl.TRIANGLE_FAN, 0, coords.length/2);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

		gl.colorMask(true, true, true, true);
		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }

    function drawMedium(x, y, x2, y2, x3, y3, x4, y4, m1, m2) {
		var rttFramebuffer = renderTexture1.framebuffer;
		gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
		gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
		gl.colorMask(false, false, true, false);
//		gl.clear(gl.COLOR_BUFFER_BIT);
        gl.useProgram(shaderProgramDraw);

        var medCoords = [x, y, x2, y2, x3, y3, x4, y4];
        var colors = [ 0,0,m1,1, 0,0,m1,1, 0,0,m2,1, 0,0,m2,1 ];
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(medCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, colorBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(colors), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.colorAttribute, colorBuffer.itemSize, gl.FLOAT, false, 0, 0);
        
        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        gl.enableVertexAttribArray(shaderProgramDraw.colorAttribute);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        gl.disableVertexAttribArray(shaderProgramDraw.colorAttribute);

		gl.colorMask(true, true, true, true);
		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }

    function drawModes(x, y, x2, y2, a, b, c, d) {
		var rttFramebuffer = renderTexture1.framebuffer;
		gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
		gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
		gl.colorMask(true, true, false, false);
//		gl.clear(gl.COLOR_BUFFER_BIT);
        gl.useProgram(shaderProgramMode);
        var z = 0;
        var z2 = 0;
        if (sim.acoustic) {
        	z = Math.PI/2;
        	a += z;
        	b += z;
        	if (c || d) {
        		z2 = z;
        		c += z;
        		d += z;
        	}
    	}

        var medCoords = [x, y, x, y2, x2, y, x2, y2];
        var colors = [ z,z,z2,z2, z,b,z2,d, a,z,c,z2, a,b,c,d ];
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(medCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramMode.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, colorBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(colors), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramMode.colorAttribute, colorBuffer.itemSize, gl.FLOAT, false, 0, 0);
        
        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramMode);
        gl.enableVertexAttribArray(shaderProgramMode.vertexPositionAttribute);
        gl.enableVertexAttribArray(shaderProgramMode.colorAttribute);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
        gl.disableVertexAttribArray(shaderProgramMode.vertexPositionAttribute);
        gl.disableVertexAttribArray(shaderProgramMode.colorAttribute);

		gl.colorMask(true, true, true, true);
		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }

    
    function drawTriangle(x, y, x2, y2, x3, y3, m) {
		var rttFramebuffer = renderTexture1.framebuffer;
		gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
		gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
		gl.colorMask(false, false, true, false);
//		gl.clear(gl.COLOR_BUFFER_BIT);

        gl.useProgram(shaderProgramDraw);
//        console("draw triangle " + m);
        gl.vertexAttrib4f(shaderProgramDraw.colorAttribute, 0.0, 0.0, m, 1.0);

        var medCoords = [x, y, x2, y2, x3, y3];
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(medCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        loadMatrix(pMatrix);
        setMatrixUniforms(shaderProgramDraw);
        gl.enableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 3);
        gl.disableVertexAttribArray(shaderProgramDraw.vertexPositionAttribute);

		gl.colorMask(true, true, true, true);
		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }

    function getProbeValue(x, y) {
    	var rttFramebuffer = renderTexture1.framebuffer;
		gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
		gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
		var pixels = new Float32Array(4);
		gl.readPixels(windowOffsetX+x, gridSizeY-windowOffsetY-y-1, 1, 1, gl.RGBA, gl.FLOAT, pixels);
		return pixels[0];
		//console.log("got pixel data " + pixels);
	}

    function drawScene(bright) {
        gl.useProgram(shaderProgramMain);
        gl.bindFramebuffer(gl.FRAMEBUFFER, null);

        gl.viewportWidth = canvas.width;
        gl.viewportHeight = canvas.height;
        gl.viewport(0, 0, gl.viewportWidth, gl.viewportHeight);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);
        mvPushMatrix();

        // draw result
        gl.bindBuffer(gl.ARRAY_BUFFER, vertexPositionBuffer);
        gl.vertexAttribPointer(shaderProgramMain.vertexPositionAttribute, vertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);
        
        gl.bindBuffer(gl.ARRAY_BUFFER, vertexTextureCoordBuffer);
        gl.vertexAttribPointer(shaderProgramMain.textureCoordAttribute, vertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, renderTexture1.texture);
        gl.uniform1i(shaderProgramMain.samplerUniform, 0);
        gl.uniform1f(shaderProgramMain.brightnessUniform, bright);
        gl.uniform3fv(shaderProgramMain.colorsUniform, colors);

        setMatrixUniforms(shaderProgramMain);
        gl.enableVertexAttribArray(shaderProgramMain.vertexPositionAttribute);
        gl.enableVertexAttribArray(shaderProgramMain.textureCoordAttribute);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, vertexPositionBuffer.numItems);
        gl.disableVertexAttribArray(shaderProgramMain.vertexPositionAttribute);
        gl.disableVertexAttribArray(shaderProgramMain.textureCoordAttribute);

        mvPopMatrix();
    }

    function drawScene3D(bright) {
        gl.useProgram(shaderProgram3D);
        gl.bindFramebuffer(gl.FRAMEBUFFER, null);

        gl.viewportWidth = canvas.width;
        gl.viewportHeight = canvas.height;
        gl.viewport(0, 0, gl.viewportWidth, gl.viewportHeight);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);
        mvPushMatrix();

        mat4.perspective(45, gl.viewportWidth / gl.viewportHeight, 0.1, 100.0, pMatrix);
        mat4.translate(mvMatrix, [0, 0, -3.2]);
        mat4.multiply(mvMatrix, matrix3d, mvMatrix);
        mat4.scale(mvMatrix, [zoom3d, zoom3d, zoom3d]);
        
	// draw result
        gl.bindBuffer(gl.ARRAY_BUFFER, screen3DTextureBuffer);
        gl.vertexAttribPointer(shaderProgram3D.textureCoordAttribute, screen3DTextureBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, renderTexture1.texture);
        gl.uniform1i(shaderProgram3D.samplerUniform, 0);
        gl.uniform1f(shaderProgram3D.brightnessUniform, bright*.1);
        gl.uniform3fv(shaderProgram3D.colorsUniform, colors);

        setMatrixUniforms(shaderProgram3D);
        gl.enableVertexAttribArray(shaderProgram3D.textureCoordAttribute);
        gl.enable(gl.DEPTH_TEST);
        var i;
        for (i = 0; i != gridSize3D; i++) {
            gl.uniform1f(shaderProgram3D.xOffsetUniform, gridRange*i/gridSize3D);
        	gl.drawArrays(gl.TRIANGLE_STRIP, 0, screen3DTextureBuffer.numItems);
        }
        gl.disable(gl.DEPTH_TEST);
        gl.disableVertexAttribArray(shaderProgram3D.textureCoordAttribute);

        mvPopMatrix();
    }

    var lastTime = 0;

    document.passCanvas = function passCanvas (cv, sim_) {
    	canvas = cv;
    	sim = sim_;
    	gl = cv.getContext("experimental-webgl");
    	console.log("got gl context " + gl + " " + cv.width + " " + cv.height);
    	var float_texture_ext = gl.getExtension('OES_texture_float');
    	var float_texture_ext = gl.getExtension('OES_texture_half_float');

    	gridSizeX = gridSizeY = 1024;
    	windowOffsetX = windowOffsetY = 40;
    	fbType = 0;
    	renderTexture2 = initTextureFramebuffer();
    	if (!renderTexture2) {
    		// float didn't work, try half float
    		fbType = 1;
        	renderTexture2 = initTextureFramebuffer();
        	if (!renderTexture2) {
        		alert("Couldn't create frame buffer, try javascript version");
        		return;
        	}
    	}
    	renderTexture1 = initTextureFramebuffer();
    	initShaders();
    	initBuffers();
    	initTextures();
        mat4.identity(matrix3d);
		mat4.rotateX(matrix3d, -Math.PI/3);

//    	drawWalls(renderTexture1);

    	gl.clearColor(0.0, 0.0, 1.0, 1.0);

	sim.readPixelsWorks = false;
    	sim.acoustic = false;
    	sim.updateRipple = function updateRipple (bright) { drawScene(bright); }
    	sim.updateRipple3D = function updateRipple3D (bright) { drawScene3D(bright); }
    	sim.simulate = function () { simulate(); }
    	sim.setResolution = function (x, y, wx, wy) {
    		gridSizeX = x;
    		gridSizeY = y;
    		windowOffsetX = wx;
    		windowOffsetY = wy;
    		windowWidth  = gridSizeX-windowOffsetX*2;
    		windowHeight = gridSizeY-windowOffsetY*2;
    		deleteRenderTexture(renderTexture1);
    		deleteRenderTexture(renderTexture2);
    		renderTexture2 = initTextureFramebuffer();
    		renderTexture1 = initTextureFramebuffer();
    		initBuffers();
    	}
    	sim.drawSource = function (x, y, f) { drawSource(x, y, f); }
    	sim.drawLineSource = function (x, y, x2, y2, f, g) { drawLineSource(x, y, x2, y2, f, g); }
    	sim.drawPhasedArray = function (x, y, x2, y2, f1, f2) { drawPhasedArray(x, y, x2, y2, f1, f2); }
    	sim.drawHandle = function (x, y) { drawHandle(x, y); }
    	sim.drawFocus = function (x, y) { drawFocus(x, y); }
    	sim.drawPoke = function (x, y, v) { drawPoke(x, y, v); }
    	sim.drawWall = function (x, y, x2, y2) { drawWall(x, y, x2, y2, 0); }
    	sim.getProbeValue = function (x, y) { return getProbeValue(x, y); }
    	sim.clearWall = function (x, y, x2, y2) { drawWall(x, y, x2, y2, 1); }
    	sim.drawParabola = function (x, y, w, h) { drawParabola(x, y, w, h); }
    	sim.drawLens = function (x, y, w, h, m) { drawLens(x, y, w, h, m); }
    	sim.drawEllipse = function (x, y, x2, y2, m) { drawEllipse(x, y, x2, y2); }
    	sim.drawSolidEllipse = function (x, y, x2, y2, m) { drawSolidEllipse(x, y, x2, y2, m); }
    	sim.drawMedium = function (x, y, x2, y2, x3, y3, x4, y4, m, m2) { drawMedium(x, y, x2, y2, x3, y3, x4, y4, m, m2); }
    	sim.drawTriangle = function (x, y, x2, y2, x3, y3, m) { drawTriangle(x, y, x2, y2, x3, y3, m); }
    	sim.drawModes = function (x, y, x2, y2, a, b, c, d) { drawModes(x, y, x2, y2, a, b, c, d); }
    	sim.setTransform = function (a, b, c, d, e, f) {
    		transform[0] = a; transform[1] = b;
    		transform[2] = c; transform[3] = d;
    		transform[4] = e; transform[5] = f;
    	}
    	sim.doBlank = function () {
    		var rttFramebuffer = renderTexture1.framebuffer;
    		gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
    		gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
    		gl.colorMask(true, true, false, false);	
        	gl.clearColor(0.0, 0.0, 1.0, 1.0);
    		gl.clear(gl.COLOR_BUFFER_BIT);
    		gl.colorMask(true, true, true, true);
    		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    	}
    	sim.doBlankWalls = function () {
    		var rttFramebuffer = renderTexture1.framebuffer;
    		gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
    		gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
    		gl.colorMask(false, false, true, false);
        	gl.clearColor(0.0, 0.0, 1.0, 1.0);
    		gl.clear(gl.COLOR_BUFFER_BIT);
    		gl.colorMask(true, true, true, true);
    		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
	}
    	sim.set3dViewAngle = function (x, y) {
    	    var mtemp = mat4.create();
    	    mat4.identity(mtemp);
    		mat4.rotateY(mtemp, x/100);
    		mat4.rotateX(mtemp, y/100);
    		mat4.multiply(mtemp, matrix3d, matrix3d);
    	}
    	sim.set3dViewZoom = function (z) {
    		zoom3d = z;
    	}
	sim.setColors = function () {
		colors = [];
		for(var i = 0; i < arguments.length; i++) {
			var arg = arguments[i];
			colors.push(((arg>>16)&0xff)/255, ((arg>>8)&0xff)/255, (arg&0xff)/255);
		}
    }
    sim.setDamping = function (d) {
        damping = d;
        initBuffers();
    }
	sim.drawingSelection = -1;
    mat4.identity(pMatrix);
    mat4.identity(mvMatrix);

}


