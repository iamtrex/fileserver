### File Server

Client and Server to serve a website that creates a user-based file server.

React/Redux frontend, to serve a simple webpage, and a Java JAX-RS based 
REST interface to serve the content and run on the local machine. 

Current testing uses apache tomcat to deploy on local computer and service. 

Essentially this will be a privatized Google Drive / Dropbox replacement 
that can run on a local server and act as a backup of important content. 

The details are TBD, but looking for the following functionality:
* Browse files
* Download files
* Upload files
* Preview files / thumbnail / in-browser viewing experience. 
* User-specific access rights
* File sharing
* More to come as I think of them :) 


Setup Instructions (~ 5 minutes)
* Check ServerConfigurations.properties and change the values to something appropriate!
* Get instance of Tomcat http://tomcat.apache.org/ (version 8+ required)
* Deploy exploded war file to <tomcat-dir>/webapps/<app-name>/
    * I personally used IntelliJ and just set it to create exploded war with all the dependencies under the server directory.
* Copy web.xml and rewrite.config into <tomcat-dir>/webapps/<app-name>/WEB-INF
* Copy context.xml into a new folder named <tomcat-dir>/webapps/<app-name>/META-INF
* Run npm install in /client
* In package.json, configure the output directory for the COPY command to <tomcat-dir>/webapps/<app-name> 
* Run npm run build-and-copy -> This will build the client-side code into a 'static' folder under the tomcat app
* Now you can start Tomcat -> <tomcat-dir>/bin, run catalina start. Make sure JRE_HOME is set.
 
By default, the server will be hosted on port 8080.
