echo "Building Identity Management"
mvn clean install
if [ $? -ne 0 ]
then	
	cd ..
	echo "Failed to build Identity Management"
	notify-send "Failed to build Identity Management"
	exit 1
else
	cd tests/distribution/target/
	unzip org.wso2.carbon.identity.mgt.test.distribution-1.0.3-SNAPSHOT.zip
        cd ../../../
	notify-send "Sarting Identity Management"
        sh tests/distribution/target/wso2carbon-kernel-1.0.3-SNAPSHOT/bin/carbon.sh -debug 5005
fi
