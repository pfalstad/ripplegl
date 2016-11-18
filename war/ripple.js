
var gl;
var canvas;
var gridSizeX =1024, gridSizeY =1024, windowOffsetX =40, windowOffsetY =40;
var windowWidth, windowHeight;

    function getShader(gl, id) {
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

	//str = "#define ACOUSTIC 1\n" + str;
        gl.shaderSource(shader, str);
        gl.compileShader(shader);

        if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
            alert(gl.getShaderInfoLog(shader));
            return null;
        }

        return shader;
    }


    var shaderProgramMain, shaderProgramScreen, shaderProgramDraw;

    function initShader(fs) {
        var fragmentShader = getShader(gl, fs);
        var vertexShader = getShader(gl, "shader-vs");

        var shaderProgram = gl.createProgram();
        gl.attachShader(shaderProgram, vertexShader);
        gl.attachShader(shaderProgram, fragmentShader);
        gl.linkProgram(shaderProgram);

        if (!gl.getProgramParameter(shaderProgram, gl.LINK_STATUS)) {
            alert("Could not initialise shaders");
        }

        gl.useProgram(shaderProgram);

        shaderProgram.vertexPositionAttribute = gl.getAttribLocation(shaderProgram, "aVertexPosition");
        gl.enableVertexAttribArray(shaderProgram.vertexPositionAttribute);

        shaderProgram.textureCoordAttribute = gl.getAttribLocation(shaderProgram, "aTextureCoord");
        gl.enableVertexAttribArray(shaderProgram.textureCoordAttribute);

        shaderProgram.dampingAttribute = gl.getAttribLocation(shaderProgram, "aDamping");

        shaderProgram.pMatrixUniform = gl.getUniformLocation(shaderProgram, "uPMatrix");
        shaderProgram.mvMatrixUniform = gl.getUniformLocation(shaderProgram, "uMVMatrix");
        shaderProgram.samplerUniform = gl.getUniformLocation(shaderProgram, "uSampler");
        shaderProgram.colorUniform = gl.getUniformLocation(shaderProgram, "color");

	return shaderProgram;
    }

    function initShaders() {
	shaderProgramMain = initShader("shader-fs");
	shaderProgramMain.brightnessUniform = gl.getUniformLocation(shaderProgramMain, "brightness");
	shaderProgramMain.colorsUniform = gl.getUniformLocation(shaderProgramMain, "colors");

	shaderProgramScreen = initShader("shader-screen-fs");
	shaderProgramScreen.stepSizeXUniform = gl.getUniformLocation(shaderProgramScreen, "stepSizeX");
	shaderProgramScreen.stepSizeYUniform = gl.getUniformLocation(shaderProgramScreen, "stepSizeY");

	shaderProgramDraw = initShader("shader-draw-fs");
    }

    var moonTexture;

    function initTextures() {
    }


    var mvMatrix = mat4.create();
    var mvMatrixStack = [];
    var pMatrix = mat4.create();

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
    	gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGB, rttFramebuffer.width, rttFramebuffer.height, 0, gl.RGB, gl.HALF_FLOAT_OES, null);

    	var renderbuffer = gl.createRenderbuffer();
    	gl.bindRenderbuffer(gl.RENDERBUFFER, renderbuffer);
    	//gl.renderbufferStorage(gl.RENDERBUFFER, gl.DEPTH_COMPONENT16, rttFramebuffer.width, rttFramebuffer.height);

    	gl.framebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, gl.TEXTURE_2D, rttTexture, 0);
    	//gl.framebufferRenderbuffer(gl.FRAMEBUFFER, gl.DEPTH_ATTACHMENT, gl.RENDERBUFFER, renderbuffer);

    	gl.bindTexture(gl.TEXTURE_2D, null);
    	gl.bindRenderbuffer(gl.RENDERBUFFER, null);
    	gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    	return {framebuffer:rttFramebuffer, texture:rttTexture};
    }

    function deleteRenderTexture(rt) {
    	gl.deleteTexture(rt.texture);
    	gl.deleteFramebuffer(rt.framebuffer);
    }

    var laptopScreenVertexPositionBuffer;
    var laptopScreenVertexTextureCoordBuffer;
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

    function initBuffers() {
    	if (!laptopScreenVertexPositionBuffer)
    		laptopScreenVertexPositionBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexPositionBuffer);
    	vertices = [
    	            -1, +1,
    	            +1, +1,
    	            -1, -1,
    	            +1, -1,
    	            ];
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices), gl.STATIC_DRAW);
    	laptopScreenVertexPositionBuffer.itemSize = 2;
    	laptopScreenVertexPositionBuffer.numItems = 4;

    	if (!laptopScreenVertexTextureCoordBuffer)
    		laptopScreenVertexTextureCoordBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexTextureCoordBuffer);
    	var textureCoords = [
    	                     windowOffsetX/gridSizeX, 1-windowOffsetY/gridSizeY,
    	                     1-windowOffsetX/gridSizeX, 1-windowOffsetY/gridSizeY,
    	                     windowOffsetX/gridSizeX,   windowOffsetY/gridSizeY,
    	                     1-windowOffsetX/gridSizeX,   windowOffsetY/gridSizeY
    	                     ];
    	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(textureCoords), gl.STATIC_DRAW);
    	laptopScreenVertexTextureCoordBuffer.itemSize = 2;
    	laptopScreenVertexTextureCoordBuffer.numItems = 4;

    	if (!sourceBuffer)
    		sourceBuffer = gl.createBuffer();
    	gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
    	sourceBuffer.itemSize = 2;
    	sourceBuffer.numItems = 2;

    	simPosition = [];
    	simDamping = [];
    	simTextureCoord = [];
    	
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

    function setPosRect(x1, y1, x2, y2) {
	var points = [ x2, y1, x1, y1, x2, y2, x1, y1, x2, y2, x1, y2 ];
        var i;
        for (i = 0; i != 6; i++) {
             var xi = points[i*2];
             var yi = points[i*2+1];
	     simPosition.push(-1+2*xi/gridSizeX, -1+2*yi/gridSizeY);
	     simTextureCoord.push(xi/gridSizeX, yi/gridSizeY);
	     var damp = 1;
             if (xi == 1 || yi == 1 || xi == gridSizeX-2 || yi == gridSizeY-2)
                damp = .999-8*.01; // was 20
	     simDamping.push(damp);
        }
    }

    var laptopVertexPositionBuffer;
    var laptopVertexTextureCoordBuffer;
    var laptopVertexIndexBuffer;
    var sourceBuffer;
    var colors;

    var moonAngle = 180;
    var cubeAngle = 0;

    function drawWalls(rt) {
	var rttFramebuffer = rt.framebuffer;
        gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);

        gl.useProgram(shaderProgramDraw);
        gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

	mat4.identity(pMatrix);
        mat4.identity(mvMatrix);

        gl.uniform4f(shaderProgramDraw.colorUniform, 0.0, 0.0, 0.0, 1.0);

        var positionBuffer = gl.createBuffer();
        gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer);
        var coords = [
            0.0, 0.0,
            0.0, 0.5,
            0.5, 0.0,
            0.0, 0.0,
        ];
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        positionBuffer.itemSize = 2;
        positionBuffer.numItems = 4;

        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, positionBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexTextureCoordBuffer);
        gl.vertexAttribPointer(shaderProgramDraw.textureCoordAttribute, laptopScreenVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        setMatrixUniforms(shaderProgramDraw);
        //gl.drawArrays(gl.TRIANGLE_STRIP, 0, positionBuffer.numItems);
    }

    function simulate() {
    	var rt = renderTexture1;
    	renderTexture1 = renderTexture2;
    	renderTexture2 = rt;

    	var rttFramebuffer = renderTexture1.framebuffer;
    	var rttTexture = renderTexture1.texture;
        gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);

        gl.useProgram(shaderProgramScreen);
        var rttFramebuffer = renderTexture1.framebuffer;
        gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

        mat4.identity(pMatrix);
        mat4.identity(mvMatrix);

        //mvPushMatrix();

        gl.bindBuffer(gl.ARRAY_BUFFER, simVertexPositionBuffer);
        gl.vertexAttribPointer(shaderProgramScreen.vertexPositionAttribute, simVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, simVertexTextureCoordBuffer);
        gl.vertexAttribPointer(shaderProgramScreen.textureCoordAttribute, simVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.enableVertexAttribArray(shaderProgramScreen.dampingAttribute);
        gl.bindBuffer(gl.ARRAY_BUFFER, simVertexDampingBuffer);
        gl.vertexAttribPointer(shaderProgramScreen.dampingAttribute, simVertexDampingBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, renderTexture2.texture);
        gl.uniform1i(shaderProgramScreen.samplerUniform, 0);
        gl.uniform1f(shaderProgramScreen.stepSizeXUniform, 1/gridSizeX);
        gl.uniform1f(shaderProgramScreen.stepSizeYUniform, 1/gridSizeY);

        setMatrixUniforms(shaderProgramScreen);
        gl.drawArrays(gl.TRIANGLES, 0, simVertexPositionBuffer.numItems);
        gl.disableVertexAttribArray(shaderProgramScreen.dampingAttribute);
    }

    function drawSource(x, y, f) {
        gl.useProgram(shaderProgramDraw);
        gl.uniform4f(shaderProgramDraw.colorUniform, f, 0.0, 1.0, 1.0);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        srcCoords[0] = srcCoords[2] = -1+2*(x+.5)/gridSizeX;
        srcCoords[1] = +1-2*(y+.5)/gridSizeY;
        srcCoords[3] = srcCoords[1]+2/gridSizeY;
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(srcCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexTextureCoordBuffer);
        gl.vertexAttribPointer(shaderProgramDraw.textureCoordAttribute, laptopScreenVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        setMatrixUniforms(shaderProgramDraw);
        gl.drawArrays(gl.LINES, 0, 2);

        //mvPopMatrix();
    }

    function drawHandle(x, y) {
        gl.useProgram(shaderProgramDraw);
        gl.uniform4f(shaderProgramDraw.colorUniform, 1, 1.0, 1.0, 1.0);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var cx = -1+2*(x+.5)/windowWidth;
        var cy = +1-2*(y+.5)/windowHeight;
        var ox = .01;
        var oy = .01;
        var coords = [ cx-ox, cy-oy, cx+ox, cy-oy, cx+ox, cy+oy, cx-ox, cy+oy ];
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexTextureCoordBuffer);
        gl.vertexAttribPointer(shaderProgramDraw.textureCoordAttribute, laptopScreenVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        setMatrixUniforms(shaderProgramDraw);
//        gl.lineWidth(3);
        gl.drawArrays(gl.LINE_LOOP, 0, 4);

        //mvPopMatrix();
    }

    function drawLineSource(x, y, x2, y2, f) {
        gl.useProgram(shaderProgramDraw);
        gl.uniform4f(shaderProgramDraw.colorUniform, f, 0.0, 0.0, 1.0);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        srcCoords[0] = -1+2*(x +.5)/gridSizeX;
        srcCoords[1] = +1-2*(y +.5)/gridSizeY;
        srcCoords[2] = -1+2*(x2+.5)/gridSizeX;
        srcCoords[3] = +1-2*(y2+.5)/gridSizeY;
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(srcCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexTextureCoordBuffer);
        gl.vertexAttribPointer(shaderProgramDraw.textureCoordAttribute, laptopScreenVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        setMatrixUniforms(shaderProgramDraw);
        gl.drawArrays(gl.LINES, 0, 2);

        //mvPopMatrix();
    }

    function drawWall(x, y, x2, y2) {
		var rttFramebuffer = renderTexture1.framebuffer;
		gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
		gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
		gl.colorMask(false, false, true, false);
//		gl.clear(gl.COLOR_BUFFER_BIT);

        gl.useProgram(shaderProgramDraw);
        gl.uniform4f(shaderProgramDraw.colorUniform, 0.0, 0.0, 0.0, 1.0);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        srcCoords[0] = -1+2*(x +.5)/gridSizeX;
        srcCoords[1] = +1-2*(y +.5)/gridSizeY;
        srcCoords[2] = -1+2*(x2+.5)/gridSizeX;
        srcCoords[3] = +1-2*(y2+.5)/gridSizeY;
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(srcCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexTextureCoordBuffer);
        gl.vertexAttribPointer(shaderProgramDraw.textureCoordAttribute, laptopScreenVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        setMatrixUniforms(shaderProgramDraw);
        gl.drawArrays(gl.LINES, 0, 2);

		gl.colorMask(true, true, true, true);
		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }

    function drawMedium(x, y, x2, y2, x3, y3, x4, y4, m) {
		var rttFramebuffer = renderTexture1.framebuffer;
		gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
		gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
		gl.colorMask(false, false, true, false);
//		gl.clear(gl.COLOR_BUFFER_BIT);

        gl.useProgram(shaderProgramDraw);
        gl.uniform4f(shaderProgramDraw.colorUniform, 0.0, 0.0, m, 1.0);

        var medCoords = [
                         -1+2*(x +.5)/gridSizeX,
                         +1-2*(y +.5)/gridSizeY,
                         -1+2*(x2+.5)/gridSizeX,
                         +1-2*(y2+.5)/gridSizeY,
                         -1+2*(x3+.5)/gridSizeX,
                         +1-2*(y3+.5)/gridSizeY,
                         -1+2*(x4+.5)/gridSizeX,
                         +1-2*(y4+.5)/gridSizeY
                         ];
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(medCoords), gl.STATIC_DRAW);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexTextureCoordBuffer);
        gl.vertexAttribPointer(shaderProgramDraw.textureCoordAttribute, laptopScreenVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        setMatrixUniforms(shaderProgramDraw);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);

		gl.colorMask(true, true, true, true);
		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    }

    var laptopAngle = 0;

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
        gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexPositionBuffer);
        gl.vertexAttribPointer(shaderProgramMain.vertexPositionAttribute, laptopScreenVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexTextureCoordBuffer);
        gl.vertexAttribPointer(shaderProgramMain.textureCoordAttribute, laptopScreenVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, renderTexture1.texture);
        gl.uniform1i(shaderProgramMain.samplerUniform, 0);
        gl.uniform1f(shaderProgramMain.brightnessUniform, bright);
        gl.uniform3fv(shaderProgramMain.colorsUniform, colors);

        setMatrixUniforms(shaderProgramMain);
        gl.drawArrays(gl.TRIANGLE_STRIP, 0, laptopScreenVertexPositionBuffer.numItems);

        mvPopMatrix();
    }


    var lastTime = 0;

    function animate() {
        var timeNow = new Date().getTime();
        if (lastTime != 0) {
            var elapsed = timeNow - lastTime;

            moonAngle += 0.05 * elapsed;
            cubeAngle += 0.05 * elapsed;

            //laptopAngle -= 0.005 * elapsed;
        }
        lastTime = timeNow;
    }


    function tick() {
        requestAnimFrame(tick);
        drawScene();
    }


    document.passCanvas = function passCanvas (cv, sim) {
    	console.log("pass canvas " + cv);
    	canvas = cv;
    	gl = cv.getContext("experimental-webgl");
    	console.log("got gl context " + gl + " " + cv.width + " " + cv.height);
    	var float_texture_ext = gl.getExtension('OES_texture_float');
    	var float_texture_ext = gl.getExtension('OES_texture_half_float');

    	gridSizeX = gridSizeY = 1024;
    	windowOffsetX = windowOffsetY = 40;
    	renderTexture2 = initTextureFramebuffer();
    	renderTexture1 = initTextureFramebuffer();
    	initShaders();
    	initBuffers();
    	initTextures();
    	//loadLaptop();

    	drawWalls(renderTexture1);

    	gl.clearColor(0.0, 0.0, 1.0, 1.0);

    	sim.updateRipple = function updateRipple (bright) { drawScene(bright); }
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
    		console.log("set resolution " + gridSizeX + " " + windowOffsetX + " " + windowWidth + " " + windowHeight);
    	}
    	sim.drawSource = function (x, y, f) { drawSource(x, y, f); }
    	sim.drawLineSource = function (x, y, x2, y2, f) { drawLineSource(x, y, x2, y2, f); }
    	sim.drawHandle = function (x, y) { drawHandle(x, y); }
    	sim.drawWall = function (x, y, x2, y2) { drawWall(x, y, x2, y2); }
    	sim.drawMedium = function (x, y, x2, y2, x3, y3, x4, y4, m) { drawMedium(x, y, x2, y2, x3, y3, x4, y4, m); }
    	sim.doBlank = function () {
    		var rttFramebuffer = renderTexture1.framebuffer;
    		gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
    		gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
    		gl.colorMask(true, true, false, false);	
    		gl.clear(gl.COLOR_BUFFER_BIT);
    		gl.colorMask(true, true, true, true);
    		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
    	}
    	sim.doBlankWalls = function () {
    		var rttFramebuffer = renderTexture1.framebuffer;
    		gl.bindFramebuffer(gl.FRAMEBUFFER, rttFramebuffer);
    		gl.viewport(0, 0, rttFramebuffer.width, rttFramebuffer.height);
    		gl.colorMask(false, false, true, false);
    		gl.clear(gl.COLOR_BUFFER_BIT);
    		gl.colorMask(true, true, true, true);
    		gl.bindFramebuffer(gl.FRAMEBUFFER, null);
	}
	sim.setColors = function () {
		colors = [];
		for(var i = 0; i < arguments.length; i++) {
			var arg = arguments[i];
			colors.push(((arg>>16)&0xff)/255, ((arg>>8)&0xff)/255, (arg&0xff)/255);
		}
	}

}


