#!/bin/sh

# Batch process (kettle job) to launch ##########################################################
KFF_CUSTOMER=templates ;
KFF_APPLICATION=datawarehouse ;
KFF_BATCH=batch_datawarehouse.kjb

# Quickly switch logging level  #################################################################
#KFF_LOG_LEVEL=Error
#KFF_LOG_LEVEL=Basic
KFF_LOG_LEVEL=Detailed

# Lifecycle management ##########################################################################
case $(hostname) in
"Hannelore")
KFF_BASE_DIR=/kff ;
KFF_LIFECYCLE=DEV ; 
KFF_PDI_VERSION=/kff/software/pdi/3.2.3 ; 
KFF_JVM_VERSION=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java ;;
"test.server.be")
echo 'This must be TST' ;
KFF_BASE_DIR=/kff ;
KFF_LIFECYCLE=TST ; 
KFF_PDI_VERSION=/kff/software/pdi/3.2.3 ; 
KFF_JVM_VERSION=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java ;;
"uai.server.be")
echo 'This must be UAI' ;
KFF_BASE_DIR=/kff ;
KFF_LIFECYCLE=UAI ; 
KFF_PDI_VERSION=/kff/software/pdi/3.2.3 ; 
KFF_JVM_VERSION=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java ;;
"prd.server.be")
echo 'This must be PRD'; 
KFF_BASE_DIR=/kff ;
KFF_LIFECYCLE=PRD ; 
KFF_PDI_VERSION=/kff/software/pdi/3.2.3 ; 
KFF_JVM_VERSION=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java ;;
*)
echo "$(hostname) is not a recognized system. This must be a mistake";
exit;;
esac

# Date time Global Variables ###############################################################
DATE=$(date +%Y%m%d)
TIME=$(date +%H%M%S)

########## Launch batch ##############
cd "$KFF_PDI_VERSION"
sh kitchen.sh -file="$KFF_BASE_DIR"/projects/"$KFF_CUSTOMER"/"$KFF_APPLICATION"/code/batch_launcher.kjb -level=$KFF_LOG_LEVEL -param:KFF_CUSTOMER="$KFF_CUSTOMER" -param:KFF_APPLICATION="$KFF_APPLICATION" -param:KFF_LIFECYCLE="$KFF_LIFECYCLE" -param:KFF_BATCH="$KFF_BATCH" -logfile="$KFF_BASE_DIR"/projects/"$KFF_CUSTOMER"/"$KFF_APPLICATION"/log/"$KFF_BATCH".$DATE.$TIME.log 
