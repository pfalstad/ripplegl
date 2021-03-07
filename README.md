# ripplegl
Ripple Tank, webgl version

## Introduction

RippleGL is a ripple tank simulation that runs in the browser. It was originally written by me, Paul Falstad, as a Java Applet.  Then Iain Sharp adapted one of my other applets, a circuit simulator, to run in the browser using GWT.  So I used some of his code to build a similar port of the java ripple tank to GWT.  In the process, I also converted the simulation code to WebGL, for much better performance.

For a hosted version of the application see http://www.falstad.com/ripple/

## Building the web application

The tools you will need to build the project are:

* Eclipse, Oxygen version.
* GWT plugin for Eclipse.

Install "Eclipse for Java developers" from [here](https://www.eclipse.org/downloads/packages/). To add the GWT plugin for Eclipse follow the instructions [here](https://gwt-plugins.github.io/documentation/gwt-eclipse-plugin/Download.html).

This repository is a project folder for your Eclipse project space. Once you have a local copy you can then build and run in development mode or build for deployment. Running in super development mode is done by clicking on the "run" icon on the toolbar and choosing http://127.0.0.1:8888/Ripple.html from the "Development Mode" tab which appears. Building for deployment is done by selecting the project root node and using the GWT button on the Eclipse taskbar and choosing "GWT Compile Project...".

GWT will build its output in to the "war" directory. In the "war" directory the file "iframe.html" is loaded as an iFrame in to the spare space at the bottom of the right hand pannel. It can be used for branding etc.

## Deployment of the web application

* "GWT Compile Project..." as explained above. This will put the outputs in to the "war" directory in the Eclipse project folder. You then need to copy everything in the "war" directory, except the "WEB-INF" directory, on to your web server.

The link for the full-page version of the application is now:
`http://<your host>/<your path>/Ripple.html`

Just for reference the files should look like this

```
-+ Directory containing the front page (eg "ripple")
  +- Ripple.html - full page version of application
  ++ ripple (directory)
   +- various files built by GWT
   +- examples (directory)
   +- setuplist.txt (index in to example directory)
```
   
## Embedding

You can link to the full page version of the application using the link shown above.

If you want to embed the application in another page then use an iframe with the src being the full-page version.

You can add query parameters to link to change the applications startup behaviour. The following are supported:
```
.../Ripple.html?rol=<string> // Load the example from the URL
.../Ripple.html?startExample=<filename> // Loads the file named "filename" from the "examples" directory
.../Ripple.html?colorScheme=rrggbb,rrggbb,... // 8 hex color specifications for walls, + waves, - waves, 0 waves, +,-,0 waves in media, sources
```
## Building an Electron application

The [Electron](https://electronjs.org/) project allows web applications to be distributed as local executables for a variety of platforms. This repository contains the additional files needed to build Ripple as an Electron application.

The general approach to building an Electron application for a particular platform is documented [here](https://electronjs.org/docs/tutorial/application-distribution). The following instructions apply this approach to Ripple.

To build the Electron application:
* Compile the application using GWT, as above.
* Download and unpack a [pre-built Electron binary directory](https://github.com/electron/electron/releases) version 9.3.2 for the target platform.
* Copy the "app" directory from this repository to the location specified [here](https://electronjs.org/docs/tutorial/application-distribution) in the Electron binary directory structure.
* Copy the "war" directory, containing the compiled Ripple application, in to the "app" directory the Electron binary directory structure.
* Run the "Electron" executable file. It should automatically load Ripple.

Thanks to @Immortalin for the initial work in applying Electron to CircuitJS1, which was then applied to Ripple.

## License

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
