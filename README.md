Introduction :  JSON-editor-soapui-plugin source code
=====================================================
This SOAP UI plugin adds new Json editor "JSON Tree View" to response editors of SOAP UI community version 3.x from SmartBear. 
It allows users to select Json node in JSON tree and get the JSON path expression.
 
Requirements
=====================================================
 1. JDK 1.8 
 2. Maven version 3 or above 
 
Recommended modules
=====================================================
You must have SOAP UI community version 3.x installed in order to run this plugin.
SOAP UI source code :https://github.com/SmartBear/soapui (git command: git clone https://github.com/SmartBear/soapui.git  )	

How to build the source code 
------------------------------
Prerequisites : 
1. JDK 1.8 
2. Maven version 3 or above 


Steps to build
----------------
1. Checkout source code of JSON-editor-soapui-plugin from code cloud "https://github.com/att/SOAPUI-Extension-Plugins/JSON-editor-soapui-plugin source code" to your local directory. (For example C:\Code\JSON-editor-soapui-plugin source code)
2. Open cmd prompt (windows)
3. change directory to "JSON-editor-soapui-plugin source code" directory (For example cd C:\Code\JSON-editor-soapui-plugin source code)
4. run maven command (mvn clean install)
5. Collect JSON-editor-soapui-plugin jar (JSON-editor-soapui-plugin-1.0.0.jar) from JSON-editor-soapui-plugin source code\target directory

Installation
=====================================================
Prerequisites : 
1. JDK 1.8 
2. SOAP UI 5.3 Installed


How to add plugin to SOAP UI 
------------------------------
1.Drop JSON-editor-soapui-plugin-1.0.0.jar to "C:\Users\<user>\.soapuios" directory
2.Launch SOAP UI 5.3 UI

Maintainers
=====================================================
Mrunal Rege 
Email Id: mrunal.rege@att.com
