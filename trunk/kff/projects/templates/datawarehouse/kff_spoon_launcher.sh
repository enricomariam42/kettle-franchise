#!/bin/sh
clear

# General parameters ############################################################################################################
export GDK_NATIVE_WINDOWS=1

# Parameters for sample data warehouse project ##################################################################################
CUSTOMER_NAME=templates
PROJECT_NAME=datawarehouse
PROJECT_DIR=/kff/projects/$CUSTOMER_NAME/$PROJECT_NAME
PROJECT_KETTLE_VERSION_PRD=/kff/software/pdi/3.2.3
PROJECT_KETTLE_VERSION_UAI=/kff/software/pdi/3.2.3
PROJECT_KETTLE_VERSION_TST=/kff/software/pdi/3.2.3
PROJECT_KETTLE_VERSION_DEV=/kff/software/pdi/3.2.3
PROJECT_JAVA_VERSION_PRD=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java
PROJECT_JAVA_VERSION_UAI=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java
PROJECT_JAVA_VERSION_TST=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java
PROJECT_JAVA_VERSION_DEV=/usr/lib/jvm/java-1.6.0-openjdk/jre/bin/java

# Funky menu #####################################################################################################################
while true; do
    echo "****************************************************************";
    echo "* Launching Pentaho Data Integration                           *";
    echo "* Choose your PDI environment!                                 *";
    echo "****************************************************************" 
    echo "* $CUSTOMER_NAME - $PROJECT_NAME                               *" 
    echo "* P - PRD                                                      *" 
    echo "* U - UAI                                                      *" 
    echo "* T - TST                                                      *" 
    echo "* D - DEV                                                      *" 
    echo "****************************************************************"
    echo " "
    read -p "Enter your choice: " putd
    case $putd in
        P )  echo "Launching PDI $PROJECT_KETTLE_VERSION_PRD for $CUSTOMER_NAME - $PROJECT_NAME (PRD)" 
                cp -T -f $PROJECT_DIR/config/configuration_PRD.properties $HOME/.kettle/kettle.properties;
                cp -T -f /$PROJECT_DIR/config/shared.xml $HOME/.kettle/shared.xml;
                cp -T -f /kff/software/pdi/3.2.3/ui/images/bg_banner_PRD.png /kff/software/pdi/3.2.3/ui/images/bg_banner.png; 
                sh /kff/software/pdi/$PROJECT_KETTLE_VERSION_PRD/spoon.sh & 
                exit 0 
		;;
        U ) echo "Launching PDI 3.2.3 for customer 1 - project 1 (UAI)";
                cp -T -f /kff/projects/customer_name/project_name/config/configuration_UAI.properties /home/jaertsen/.kettle/kettle.properties;
                cp -T -f /kff/projects/customer_name/project_name/config/shared.xml /home/jaertsen/.kettle/shared.xml;
                cp -T -f /kff/software/pdi/3.2.3/ui/images/bg_banner_UAI.png /kjube/software/pdi/3.2.3/ui/images/bg_banner.png; 
                sh /kff/software/pdi/3.2.3/spoon.sh & 
                break
		;;
        T ) echo "Launching PDI 3.2.3 for $CUSTOMER_NAME - $PROJECT_NAME (TST)";
		cp -T -f $PROJECT_DIR/config/configuration_TST.properties $HOME/.kettle/kettle.properties;
                cp -T -f $PROJECT_DIR/config/shared.xml $HOME/.kettle/shared.xml;
                cp -T -f /kff/software/pdi/3.2.3/ui/images/bg_banner_TST.png /kff/software/pdi/3.2.3/ui/images/bg_banner.png; 
                sh $PROJECT_KETTLE_VERSION_TST/spoon.sh & 
                break
		;;
        D ) echo "Launching PDI 3.2.3 for $CUSTOMER_NAME - $PROJECT_NAME (DEV)";
		cp -T -f $PROJECT_DIR/config/configuration_DEV.properties $HOME/.kettle/kettle.properties;
                cp -T -f $PROJECT_DIR/config/shared.xml $HOME/.kettle/shared.xml;
                cp -T -f /kff/software/pdi/3.2.3/ui/images/bg_banner_DEV.png /kff/software/pdi/3.2.3/ui/images/bg_banner.png; 
                sh $PROJECT_KETTLE_VERSION_DEV/spoon.sh & 
                break
		;;
        * ) echo "Please input a valid environment letter combination !"
            ;;
    esac
done
