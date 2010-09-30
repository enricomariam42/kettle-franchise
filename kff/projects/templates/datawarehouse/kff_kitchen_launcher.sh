#!/bin/sh

# Date time Global Variables ####################################################
DATE=$(date +%Y%m%d)
TIME=$(date +%H%M%S)

# Back-up existing config files #################################################
cp $HOME/.kettle/kettle.properties $HOME/.kettle/kettle.properties.$DATE.$TIME 
cp $HOME/.kettle/shared.xml $HOME/.kettle/shared.xml.$DATE.$TIME 

# Batch process (kettle job) to launch ##########################################
KFF_CUSTOMER=templates ;
KFF_APPLICATION=datawarehouse ;
KFF_BATCH=batch_datawarehouse.kjb
PROJECT_DIR=/kff/projects/$KFF_CUSTOMER/$KFF_APPLICATION

# Quickly switch logging level  #################################################
#KFF_LOG_LEVEL=None
#KFF_LOG_LEVEL=Error
#KFF_LOG_LEVEL=Minimal
#KFF_LOG_LEVEL=Basic
KFF_LOG_LEVEL=Detailed
#KFF_LOG_LEVEL=Debug
#KFF_LOG_LEVEL=Rowlevel

# Lifecycle management ##########################################################
case $(hostname) in
"Jaybox"|"hannelore")
KFF_BASE_DIR=/kff ;
KFF_LIFECYCLE=DEV ; 
KFF_PDI_VERSION=/kff/software/pdi/4.0.0 ; 
KFF_JVM_VERSION=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java ;
cp -T -f $PROJECT_DIR/config/shared_DEV.xml  $HOME/.kettle/shared.xml ;;
"test.server.be")
echo 'This must be TST' ;
KFF_BASE_DIR=/kff ;
KFF_LIFECYCLE=TST ; 
KFF_PDI_VERSION=/kff/software/pdi/3.2.3 ; 
KFF_JVM_VERSION=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java ;
cp -T -f $PROJECT_DIR/config/shared_TST.xml $HOME/.kettle/shared.xml ;;
"uai.server.be")
echo 'This must be UAI' ;
KFF_BASE_DIR=/kff ;
KFF_LIFECYCLE=UAI ; 
KFF_PDI_VERSION=/kff/software/pdi/3.2.3 ; 
KFF_JVM_VERSION=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java ;
cp -T -f $PROJECT_DIR/config/shared_UAI.xml $HOME/.kettle/shared.xml ;; 
"prd.server.be")
echo 'This must be PRD'; 
KFF_BASE_DIR=/kff ;
KFF_LIFECYCLE=PRD ; 
KFF_PDI_VERSION=/kff/software/pdi/3.2.3 ; 
KFF_JVM_VERSION=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java ;
cp -T -f $PROJECT_DIR/config/shared_PRD.xml $HOME/.kettle/shared.xml ;;
*)
echo "$(hostname) is not a recognized system. This must be a mistake";
exit;;
esac

# Launch batch ################################################################
cd "$KFF_PDI_VERSION"
sh kitchen.sh -file="$KFF_BASE_DIR"/projects/"$KFF_CUSTOMER"/"$KFF_APPLICATION"/code/batch_launcher.kjb -level=$KFF_LOG_LEVEL -param:KFF_CUSTOMER="$KFF_CUSTOMER" -param:KFF_APPLICATION="$KFF_APPLICATION" -param:KFF_LIFECYCLE="$KFF_LIFECYCLE" -param:KFF_BATCH="$KFF_BATCH" -logfile="$KFF_BASE_DIR"/projects/"$KFF_CUSTOMER"/"$KFF_APPLICATION"/log/"$KFF_BATCH".$DATE.$TIME.log 