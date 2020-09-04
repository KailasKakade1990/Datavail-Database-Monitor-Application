
# Detla Agent
## Install

1. Update the config.xml
Make sure the following xml file has all the correct entries.
<properties>
<entry key="sleepinterval">2</entry>
<entry key="serverurl">http://192.168.22.213/v41/</entry>
<entry key="localconfigfile">DeltaAgent.xml</entry>
<entry key="remoteconfigfile">temp/delta.xml</entry>
<entry key="pluginfilepath">lib/</entry>
<entry key="linuxpassword">12345678</entry>
<entry key="clientid">clientidplaceholder</entry>
<entry key="springbootconfigpath">temp/springboot.xml</entry>

<entry key="linuxhost">192.168.1.144</entry>
<entry key="linuxuser">mate18</entry>
<entry key="linuxpass">12345678</entry>
<entry key="linuxmysqlhost">jdbc:mysql://localhost:3306/testing_agent</entry>
<entry key="linuxmysqluser">root</entry>
<entry key="linuxmysqlpass">Maxi@1029</entry>
<entry key="command">cat /var/log/mysql/error.log</entry>
<entry key="deltaagent">DeltaAgent.xml</entry>
<entry key="deltaagentlogfile">AgentLog.txt</entry>
<entry key="clienturl">http://192.168.22.213/v41/Server/PostData</entry>
<entry key="clienturl1">http://192.168.22.213/v41/Server/CheckIn</entry>
</properties>

### Remote Install
Run ./install.sh with the following arguments : username password host port path
    a. username -> must be a user with sudo capabilities
    b. password -> password of user
    c. host -> the host you want to install the delta agent on
    d. port -> the scp / ssh port to use
    e. path -> path on the remote machine where you want to run the installation from.
    Note: All of the arguments are required.
    example: ./install.sh someuser somepassword 192.168.1.28 22 /home/someuser

### Local Install
1. scp all the the tar.gz to the linux host you want to run it on.
2. ssh to the linux host.
3. unzip the tar.gz file
4. chmod u+x ./setup.sh
5. run ./setup.sh

That is it, you are now up and running with the deltaagent and deltaagentupdater services.

To check the status simple run:
 service deltaagent status
 service deltaagentupdater status

To stop the service simply run:
 service deltaagent stop
 service deltaagentupdater stop

To start the service simply run:
 service deltaagent start
 service deltaagentupdater start