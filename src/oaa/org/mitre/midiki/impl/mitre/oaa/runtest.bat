set MIDIKI_HOME=c:\cvsroot\midiki\src\java
set MIDIKI_OAA_HOME=c:\cvsroot\midiki\src\oaa
set OAA_HOME=c:\oaa2\runtime\oaalib\oaa2.jar
java -cp %MIDIKI_HOME%;%MIDIKI_OAA_HOME%;%OAA_HOME% org.mitre.dm.Executive %MIDIKI_OAA_HOME%\org\mitre\midiki\impl\mitre\oaa\midiki.properties
