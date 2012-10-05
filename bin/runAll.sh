#!/bin/bash
#
#  Run each of the profiles ...
for configName in 'local-inmemory' 'local-filesystem'
do
  echo "***********************************************************"
  echo "*         Running '$configName' configuration "
  echo "***********************************************************"
  mvn clean install -P$configName
  mkdir -p results/$configName
  rm -rf results/$configName
  cp -r ./perf-tests-report/target/test-classes/d3/* results/$configName/
done