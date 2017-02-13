# Carbon Identity Management
---

|  Branch | Build Status |
| :------------ |:-------------
| master      | [![Build Status](https://wso2.org/jenkins/buildStatus/icon?job=carbon-identity-mgt)](https://wso2.org/jenkins/job/carbon-identity-mgt) |


---
Carbon Identity Management project is one stop shop for all your identity needs.
## Features:
* Read/Write Identity Store.
* Read/Write Credential Store.
* Privileged Realm Service.
* Built in identity/credential store connectors.

## Getting Started

This component is currently under development and can try-out as follows.

## Download

Use Maven snippet:
````xml
<dependency>
    <groupId>org.wso2.carbon.identity.mgt</groupId>
    <artifactId>org.wso2.carbon.identity.mgt</artifactId>
    <version>${carbon.identity.mgt.version}</version>
</dependency>
````

### Snapshot Releases

Use following Maven repository for snapshot versions of Carbon Identity Management.

````xml
<repository>
    <id>wso2.snapshots</id>
    <name>WSO2 Snapshot Repository</name>
    <url>http://maven.wso2.org/nexus/content/repositories/snapshots/</url>
    <snapshots>
        <enabled>true</enabled>
        <updatePolicy>daily</updatePolicy>
    </snapshots>
    <releases>
        <enabled>false</enabled>
    </releases>
</repository>
````

### Released Versions

Use following Maven repository for released stable versions of Carbon Identity Management.

````xml
<repository>
    <id>wso2.releases</id>
    <name>WSO2 Releases Repository</name>
    <url>http://maven.wso2.org/nexus/content/repositories/releases/</url>
    <releases>
        <enabled>true</enabled>
        <updatePolicy>daily</updatePolicy>
        <checksumPolicy>ignore</checksumPolicy>
    </releases>
</repository>
````
## Building From Source

Clone this repository first (`https://github.com/wso2/carbon-identity-mgt.git`) and use Maven install to build
`mvn clean install`.

## Contributing to Carbon Identity Management Project

Pull requests are highly encouraged and we recommend you to create a [JIRA](https://wso2.org/jira/projects/IDENTITY/issues/IDENTITY) to discuss the issue or feature that you
 are contributing to.

## License

Carbon Identity Management is available under the Apache 2 License.

## Copyright

Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
