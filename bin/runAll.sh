#!/bin/bash
#
#  Run each of the profiles ...
for configName in 'local-inmemory' 'local-filesystem'
do
  echo "***********************************************************"
  echo "*         Running '$configName' configuration "
  echo "***********************************************************"
  mvn clean install -P$configName
done