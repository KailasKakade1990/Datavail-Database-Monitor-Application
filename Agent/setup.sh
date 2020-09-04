#!/usr/bin/env bash

UUID=$(cat /proc/sys/kernel/random/uuid)
varcurrentpath=$(pwd)
varpath=/var/datavail
mkdir $varpath
cd $varpath
mkdir ./config
mkdir ./lib
cp $varcurrentpath/datavail-config.xml ./config
cp $varcurrentpath/config.xml ./
cp $varcurrentpath/DeltaAgent.xml ./
cp $varcurrentpath/deltaagent.service /etc/systemd/system/
cp $varcurrentpath/Agent-0.0.1-SNAPSHOT.jar $varpath/agent.jar
cp $varcurrentpath/deltaagentupdater.service /etc/systemd/system/
cp $varcurrentpath/AgentUpdater-0.0.1-SNAPSHOT.jar $varpath/agentupdater.jar
cp $varcurrentpath/Datavail.*.jar $varpath/lib/
cp $varcurrentpath/*.xml $varpath/
ln -s $varpath/agent.jar /etc/init.d/deltaagent
ln -s $varpath/agentupdater.jar /etc/init.d/deltaagentupdater
chmod a+x /etc/init.d/deltaagent
chmod a+x $varpath/agent.jar
chwon oracle /etc/init.d/deltaagent
chwon oracle /etc/init.d/deltaagentupdaer
chowm oracle -R .

sed -i -e 's/clientidplaceholder/'$UUID'/g' $varpath/config.xml

systemctl daemon-reload

service deltaagent start
service deltaagentupdater start
#echo To start the service run 'service deltaagent start, to stop call service deltaagent stop.'
#echo To start the service run 'service deltaagentupdater start, to stop call service deltaagentupdater stop.'