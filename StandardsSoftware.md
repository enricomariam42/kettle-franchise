# Introduction #
In order to use KFF, you require the following softwares and plugins:
  * java run time environment
  * kettle (aka Pentaho data integration)

# Java run time environment #
We recommend that you place a stable version of JRE in your FF directory structure. It is usefull to deploy the same JRE to all the machines you are deploying your projects on.

Please install JRE in the directory:
```
/kff/software/jre/<version>/
```
as described in the [standard directory guideliness](StandardsDirectory.md).

# Kettle - PDI #
You can get the latest stable version(s) of kettle on [sourceforge](http://sourceforge.net/projects/pentaho/), while nightly builds of kettle are available [here](http://ci.pentaho.com/) for the more courageous amongst you. KFF works with kettle 3.2.3 and above.

Please unzip PDI in the directory:
```
/kff/software/pdi/<version>/
```
as described in the [standard directory guideliness](StandardsDirectory.md).