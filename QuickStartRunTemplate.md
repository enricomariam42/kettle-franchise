# Introduction #
This quick start guide shows you how to get started with KFF, installing, configuring and launching the provided template.  This quick start section just applies minimal modifications to get started. It will run the template project just as is. It is intended for evaluation purposes. For a more custom configuration, check out the next section (Quick start).

# Installing #
## Step 1 - Download and install KFF ##
Download KFF from the downloads section (, for the moment get the kff directory from svn) and place it in a directory /kff on the root of (one of) your disk(s). The final directory structure should look like [this](StandardsDirectory.md).

Remark: You can also install KFF in other locations, see the section [Setting up a single (development) environment ](QuickStartSingleEnvironment.md).

## Step 2 - Download PDI (3.2.3, 3.2.4 or 3.2.5) and install ##
Unzip PDI into the /kff/software/pdi/3.2.3/ (for version 3.2.3).

The KFF reference build for 3.2.3 can be found over [here](http://kettle3.s3.amazonaws.com/pdi-ce-3.2.3-r13847.zip).
A recent build of 3.2.5 (Sept 3rd, 2010) is placed [here](http://kettle3.s3.amazonaws.com/pdi-ce-3.2.5-r13847.zip).


## Step 3 - [Download the KFF plugins](http://code.google.com/p/kettle-franchise/downloads/detail?name=kff-plugins.jar&can=2&q=) ##
... and drop it into the libext directory of your PDI installation.

## Step 4 - Configure your local MySQL database ##
Create a database dwh\_DEV on your MySQL machine with username dwh\_DEV and pwd kff.

## Step 5 - Modify the custom KFF launch scripts ##
### Modify kff\_spoon\_launcher.sh ###
You find the script under /kff/projects/templates/datawarehouse/

Set PROJECT\_KETTLE\_VERSION\_DEV to the right path of your PDI installation.

```
PROJECT_KETTLE_VERSION_DEV=/kff/software/pdi/3.2.3
```

Idem dito set PROJECT\_JAVA\_VERSION\_DEV to point to your java installation.

```
PROJECT_JAVA_VERSION_DEV=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java
```

### Modify kff\_kitchen\_launcher.sh ###
Similar, set KFF\_PDI\_VERSION and KFF\_JVM\_VERSION to point to PDI and Java. Additionally fill in the hostname (replace Jaybox) of your local machine for the start up of PDI.

Sample
```
case $(hostname) in
"Jaybox")
KFF_BASE_DIR=/kff ;
KFF_LIFECYCLE=DEV ; 
KFF_PDI_VERSION=/kff/software/pdi/3.2.3 ; 
KFF_JVM_VERSION=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java ;;
```

## Step 6 - Run ##
Run kff\_kitchen\_launcher.sh

# Results #

## Logging tables existence ##
Logging tables are created under the database schema dwh\_DEV.

![http://www.kjube.be/images/pic177.png](http://www.kjube.be/images/pic177.png)

## Log files ##
Your project log directory will contain two log files with the timestamp of your execution.

![http://www.kjube.be/images/pic178.png](http://www.kjube.be/images/pic178.png)

The spreadsheet contains an overview of all the jobs/transformations that were executed.

![http://www.kjube.be/images/pic179.png](http://www.kjube.be/images/pic179.png)