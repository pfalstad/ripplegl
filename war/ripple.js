
document.woof = function woof() { 
	console.log("woof was called");
}

var gl;
var canvas;

console.log("gello");




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
	shaderProgramScreen = initShader("shader-screen-fs");
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
        rttFramebuffer.width = 1024;
        rttFramebuffer.height = 1024;

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


    var laptopScreenVertexPositionBuffer;
    var laptopScreenVertexTextureCoordBuffer;
    var simVertexPositionBuffer;
    var simVertexTextureCoordBuffer;
    var simVertexBuffer;

    var simPosition = [];
    var simTextureCoord = [];
    var simDamping = [];

        var gridSizeX = 1024;
        var gridSizeY = 1024;
        var dampAreaWidth = 100;

    function initBuffers() {
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

        laptopScreenVertexTextureCoordBuffer = gl.createBuffer();
        gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexTextureCoordBuffer);
        var textureCoords = [
	      dampAreaWidth/gridSizeX, 1-dampAreaWidth/gridSizeY,
	    1-dampAreaWidth/gridSizeX, 1-dampAreaWidth/gridSizeY,
	      dampAreaWidth/gridSizeX,   dampAreaWidth/gridSizeY,
	    1-dampAreaWidth/gridSizeX,   dampAreaWidth/gridSizeY
        ];
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(textureCoords), gl.STATIC_DRAW);
        laptopScreenVertexTextureCoordBuffer.itemSize = 2;
        laptopScreenVertexTextureCoordBuffer.numItems = 4;

        sourceBuffer = gl.createBuffer();
        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        var coords = [
            -.26, 0, -.25, 0
        ];
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(coords), gl.STATIC_DRAW);
        sourceBuffer.itemSize = 2;
        sourceBuffer.numItems = 2;
	

	  setPosRect(dampAreaWidth, dampAreaWidth, gridSizeX-dampAreaWidth, gridSizeY-dampAreaWidth);
                
                // sides
                setPosRect(1, dampAreaWidth, dampAreaWidth, gridSizeY-dampAreaWidth);
                setPosRect(gridSizeX-dampAreaWidth, dampAreaWidth, gridSizeX-2, gridSizeY-dampAreaWidth);
                setPosRect(dampAreaWidth, 1, gridSizeX-dampAreaWidth, dampAreaWidth);
                setPosRect(dampAreaWidth, gridSizeY-dampAreaWidth, gridSizeX-dampAreaWidth, gridSizeY-2);
                
                // corners
                setPosRect(1, 1, dampAreaWidth, dampAreaWidth);
                setPosRect(gridSizeX-dampAreaWidth, 1, gridSizeX-2, dampAreaWidth);
                setPosRect(1, gridSizeY-dampAreaWidth, dampAreaWidth, gridSizeY-2);
                setPosRect(gridSizeX-dampAreaWidth, gridSizeY-dampAreaWidth, gridSizeX-2, gridSizeY-2);
                

	simVertexPositionBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexPositionBuffer);
	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simPosition), gl.STATIC_DRAW);
	simVertexPositionBuffer.itemSize = 2;
	simVertexPositionBuffer.numItems = simPosition.length/2;

	simVertexTextureCoordBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, simVertexTextureCoordBuffer);
	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(simTextureCoord), gl.STATIC_DRAW);
	simVertexTextureCoordBuffer.itemSize = 2;
	simVertexTextureCoordBuffer.numItems = simPosition.length/2;

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

        gl.uniform4f(shaderProgramDraw.colorUniform, 0.0, 0.0, 1.0, 1.0);

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

        mvPushMatrix();

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

        setMatrixUniforms(shaderProgramScreen);
        gl.drawArrays(gl.TRIANGLES, 0, simVertexPositionBuffer.numItems);
        gl.disableVertexAttribArray(shaderProgramScreen.dampingAttribute);

	// do sources

        gl.useProgram(shaderProgramDraw);
        gl.uniform4f(shaderProgramDraw.colorUniform, Math.sin(cubeAngle*2), 0.0, 0.0, 1.0);

        gl.bindBuffer(gl.ARRAY_BUFFER, sourceBuffer);
        gl.vertexAttribPointer(shaderProgramDraw.vertexPositionAttribute, sourceBuffer.itemSize, gl.FLOAT, false, 0, 0);

        gl.bindBuffer(gl.ARRAY_BUFFER, laptopScreenVertexTextureCoordBuffer);
        gl.vertexAttribPointer(shaderProgramDraw.textureCoordAttribute, laptopScreenVertexTextureCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);

        setMatrixUniforms(shaderProgramDraw);
        gl.drawArrays(gl.LINES, 0, 2);

        mvPopMatrix();

        //gl.bindTexture(gl.TEXTURE_2D, rttTexture);
        //gl.generateMipmap(gl.TEXTURE_2D);
        //gl.bindTexture(gl.TEXTURE_2D, null);
    }


    var laptopAngle = 0;

    function drawScene() {
       var iter;
       for (iter = 0; iter != 5; iter++) {
          simulate();
          cubeAngle += 0.05;
       }

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


document.passCanvas = function passCanvas (cv) {
	console.log("pass canvas " + cv);
	canvas = cv;
        gl = cv.getContext("experimental-webgl");
	console.log("got gl context " + gl + " " + cv.width + " " + cv.height);
var float_texture_ext = gl.getExtension('OES_texture_float');
var float_texture_ext = gl.getExtension('OES_texture_half_float');

        renderTexture2 = initTextureFramebuffer();
        renderTexture1 = initTextureFramebuffer();
        initShaders();
        initBuffers();
        initTextures();
        //loadLaptop();

	drawWalls(renderTexture1);

        gl.clearColor(0.0, 0.0, 0.0, 1.0);
}

document.updateRipple = function updateRipple () {
	drawScene();
}

