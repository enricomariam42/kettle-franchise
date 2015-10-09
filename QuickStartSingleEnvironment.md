# Introduction #
This how-to guide show how to set up a single (development) environment using KFF.

# Installation #
## Step 1 - Download and install KFF ##
Download KFF from the downloads section (, for the moment get the kff directory from svn) and place it in a directory /kff on the root of (one of) your disk(s). The final directory structure should look like [this](StandardsDirectory.md).

In case you want to use another installation directory, that is no problem. You will have to adjust the some variables in your launch scripts: kff\_spoon\_launcher.sh and kff\_kitchen\_launcher.sh for that later on during the installation scenario.

## Step 2 - Download PDI (3.2.3, 3.2.4 or 3.2.5) and install ##
Unzip PDI into the /kff/software/pdi/3.2.3/ (for version 3.2.3).

The KFF reference build for 3.2.3 can be found over [here](http://kettle3.s3.amazonaws.com/pdi-ce-3.2.3-r13847.zip).
A recent build of 3.2.5 (Sept 3rd, 2010) is placed [here](http://kettle3.s3.amazonaws.com/pdi-ce-3.2.5-r13847.zip).


## Step 3 - [Download the KFF plugins](http://code.google.com/p/kettle-franchise/downloads/detail?name=kff-plugins.jar&can=2&q=) ##
... and drop it into the libext directory of your PDI installation.

## Step 4 - Copy the provided template to create your own datawarehouse project ##

E.g. copy /kff/projects/templates/datawarehouse to /kff/projects/pepsi/finance-datamart/ if you are going to build the finance data mart for your precious customer Pepsi.

## Step 5 - Modify your launch scripts ##
### kff\_spoon\_launcher.sh ###
Set parameters according to your liking:

```
# Parameters for sample data warehouse project ##################################
CUSTOMER_NAME=templates
PROJECT_NAME=datawarehouse
PROJECT_DIR=/kff/projects/$CUSTOMER_NAME/$PROJECT_NAME
PROJECT_KETTLE_VERSION_PRD=/kff/software/pdi/3.2.3
PROJECT_KETTLE_VERSION_UAI=/kff/software/pdi/3.2.3
PROJECT_KETTLE_VERSION_TST=/kff/software/pdi/3.2.3
PROJECT_KETTLE_VERSION_DEV=/kff/software/pdi/4.0.0
PROJECT_JAVA_VERSION_PRD=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java
PROJECT_JAVA_VERSION_UAI=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java
PROJECT_JAVA_VERSION_TST=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java
PROJECT_JAVA_VERSION_DEV=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java
```

### kff\_kitchen\_launcher.sh ###
Set parameters according to your liking:

```
# Batch process (kettle job) to launch ##########################
KFF_CUSTOMER=templates ;
KFF_APPLICATION=datawarehouse ;
KFF_BATCH=batch_datawarehouse.kjb
```

```
# Lifecycle management ##########################################
case $(hostname) in
"Jaybox")
KFF_BASE_DIR=/kff ;
KFF_LIFECYCLE=DEV ; 
KFF_PDI_VERSION=/kff/software/pdi/3.2.3 ; 
KFF_JVM_VERSION=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java ;;
```


... busy writing here ...

# Evaluation #
Results should be the same as under the quick installation.