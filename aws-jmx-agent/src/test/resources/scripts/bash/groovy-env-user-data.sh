#!/bin/bash
export GROOVY_VERSION=2.4.10
yum update -y
#yum install java-1.8.0-openjdk-1.8.0.91
mkdir /home/ec2-user/groovy
cd /home/ec2-user/groovy
#=================================================================================================
# Download and install groovy
#=================================================================================================
#wget https://dl.bintray.com/groovy/maven/apache-groovy-binary-${GROOVY_VERSION}.zip
aws s3 cp s3://my-software-installs/groovy/apache-groovy-binary-${GROOVY_VERSION}.zip .
unzip apache-groovy-binary-${GROOVY_VERSION}.zip
rm apache-groovy-binary-${GROOVY_VERSION}.zip
cd /home/ec2-user/
chown -R ec2-user /home/ec2-user/groovy
chgrp -R ec2-user /home/ec2-user/groovy
#=================================================================================================
# Download and install aws support jar
#=================================================================================================
mkdir -p /home/ec2-user/.groovy/lib
cd /home/ec2-user/.groovy/lib
aws s3 cp s3://my-software-installs/aws/helios-aws-support.jar .
chown ec2-user helios-aws-support.jar
chgrp ec2-user helios-aws-support.jar
#=================================================================================================
# Configure environment and mark ready
#=================================================================================================
echo "export GROOVY_HOME=~/groovy/groovy-${GROOVY_VERSION}" >> /home/ec2-user/.bashrc
echo "export PATH=\$GROOVY_HOME/bin:\$PATH" >> /home/ec2-user/.bashrc
NOW=`TZ=America/New_York date`
echo $NOW > /home/ec2-user/ready
chown ec2-user /home/ec2-user/ready
chgrp ec2-user /home/ec2-user/ready

