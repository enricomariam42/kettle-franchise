# Introduction #
The environment configurator allows you read a 'kettle.properties' in any location you want and override the value set in $HOME/.kettle/kettle.properties.

# Details #
The environment configurator step looks like this:

![http://www.kjube.be/images/pic164.png](http://www.kjube.be/images/pic164.png)

Basically there 3 ways of setting the path to .properties file you want to use.

## Don't fill in any of the fields ##

In this mode the environment configurator is configured out of the box for KFF. It will assume that you pass the named parameters KFF\_CUSTOMER, KFF\_APPLICATION, KFF\_LIFECYCLE to kitchen on the command line.

With those parameters the configurator will look for the .properties files on the path:

```
/kff/projects/$KFF_CUSTOMER/$KFF_APPLICATION/config/configuration_$KFF_LIFECYCLE.properties
```

Example: config files for the datawarehouse template that ships with KFF

![http://www.kjube.be/images/pic166.png](http://www.kjube.be/images/pic166.png)

## Decide yourself which will be the variable names ##

![http://www.kjube.be/images/pic167.png](http://www.kjube.be/images/pic167.png)

In this example the path to your custom .properties file will be:

```
/kff/projects/$MY_CUSTOMER/$MY_APPLICATION/config/configuration_$MY_LIFECYCLE.properties
```

## Set the path/filename manually ##

![http://www.kjube.be/images/pic168.png](http://www.kjube.be/images/pic168.png)

This gives you full control over the path and file name you want to use.

# Usage #

Typically you would include the KFF environment configurator as the first step in your top level job (as we've done in the KFF batch\_launcher.kjb) so you are sure all variables are set before you start processing.

![http://www.kjube.be/images/pic165.png](http://www.kjube.be/images/pic165.png)

But we are sure you'll all find your own usage for this step.