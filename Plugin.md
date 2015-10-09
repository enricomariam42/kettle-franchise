# Introduction #
This page documents the KFF plugins for kettle.

# "Install" the jar file #
All KFF plugins are available as a [jar file](http://code.google.com/p/kettle-franchise/downloads/list) which you need to drop into the libext directory of your kettle installation in Kettle version 3.2 and in the plugins/steps folder in version 4.x.

![http://www.kjube.be/images/pic160.png](http://www.kjube.be/images/pic160.png)

# Environment variables #
In order for the plugin to work correctly in versions before PDI 4.0, you need to set two environment variables:
  * GDK\_NATIVE\_WINDOWS=1
  * KETTLE\_PLUGIN\_PACKAGES=be.kjube

In case you use the KFF start-up script you needn't worry about these variables since KFF includes it's own start-up script [kff\_spoon\_launcher.sh] (only for Linux right now) which set these variables.

# Plug-in's #
The following plug-in's have been released:
  1. [Data Grid](PluginDataGrid.md)
  1. [Environment Configurator](PluginEnvironmentConfigurator.md)