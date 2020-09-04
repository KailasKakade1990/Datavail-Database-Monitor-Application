#!/usr/bin/expect -f

#echo Enter Host:
#read host
#echo enter Port:
#read port
#echo Enter user name:
#read username
#echo Enter password:
#read password

#echo $username

set username [lindex $argv 0];
set password [lindex $argv 1];
set host [lindex $argv 2];
set port [lindex $argv 3];
set path [lindex $argv 4];

#spawn scp -P 2222 -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ./target/Agent-0.0.1-SNAPSHOT.jar deltaagent.service ./setup.sh ./service/agentboot.sh ./config/datavail-config.xml ./Datavail.Delta.Agent.ErrorLogPlugin.jar ./Datavail.Delta.Agent.ConnectionThresholdPlugin.jar ./Datavail.Delta.Agent.CheckInPlugin.jar ./Datavail.Delta.Agent.GaleraClusterMonitorPlugin.jar ./Datavail.Delta.Agent.GaleraSynchStatusPlugin.jar ./Datavail.Delta.Agent.NDBClusterStatusPlugin.jar ./Datavail.Delta.Agent.SlaveIsUpPlugin.jar ./Datavail.Delta.Agent.SlaveLagPlugin.jar ./Datavail.Delta.Agent.TableLocksPlugin.jar ./log4j2.xml ./DeltaAgent.xml ./config.xml ./AgentUpdater-0.0.1-SNAPSHOT.jar ./configuration.xml ./dependency-jars/commons-io-1.3.2.jar ./dependency-jars/json-20160810.jar eric@localhost:/home/eric
#expect "password:"
#send "Maxi@1029\r"
#expect "*\r"
#interact

#spawn ssh -p 2222 eric@localhost "chmod a+x setup.sh; echo Maxi@1029 | sudo -S ./setup.sh"
#expect "password:"
#send "Maxi@1029\r"
#expect "*\r"
#interact

spawn scp -P $port -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no ./target/Agent-0.0.1-SNAPSHOT.jar deltaagent.service ./setup.sh ./service/agentboot.sh ./config/datavail-config.xml ./Datavail.Delta.Agent.ErrorLogPlugin.jar ./Datavail.Delta.Agent.ConnectionThresholdPlugin.jar ./Datavail.Delta.Agent.CheckInPlugin.jar ./Datavail.Delta.Agent.GaleraClusterMonitorPlugin.jar ./Datavail.Delta.Agent.GaleraSynchStatusPlugin.jar ./Datavail.Delta.Agent.NDBClusterStatusPlugin.jar ./Datavail.Delta.Agent.SlaveIsUpPlugin.jar ./Datavail.Delta.Agent.SlaveLagPlugin.jar ./Datavail.Delta.Agent.TableLocksPlugin.jar ./log4j2.xml ./DeltaAgent.xml ./config.xml ./AgentUpdater-0.0.1-SNAPSHOT.jar ./deltaagentupdater.service $username@$host:$path
expect "password:"
send "$password\r"
expect "*\r"
interact

spawn ssh -p $port $username@$host "chmod a+x setup.sh; echo $password | sudo -S ./setup.sh"
expect "password:"
send "$password\r"
expect "*\r"
interact