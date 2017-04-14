rem Copyright (C) 2004. The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
rem Consult the LICENSE file in the root of the distribution for terms and restrictions.
rem
rem The path spec is currently hard-coded; you should change it to refer to the
rem correct location on your system (until we establish some environment variables)

cd \temp
python %PM_DIR%\process_monitor.py c:\cvsroot\midiki\src\java\org\mitre\midiki\impl\mitre\galaxy\io-podium2.config
