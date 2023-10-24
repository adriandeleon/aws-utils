[![Java CI with Maven](https://github.com/adriandeleon/aws-utils/actions/workflows/maven.yml/badge.svg)](https://github.com/adriandeleon/aws-utils/actions/workflows/maven.yml)

# AwsUtils
## A few AWS EC2 helpers for the Java AWS SDK.

Latest version: 1.0.0

This library requieres Java SDK 21 to compile.


To include it in your project, first download this repo and run:
```shell
mvn install
```
Then you can add this to your `pom.xml` file:
```xml
   <dependency>
      <groupId>me.adriandeleon</groupId>
      <artifactId>aws-utils</artifactId>
      <version>1.0.0</version>
   </dependency>
```


This library includes a few helpers/wrappers for the Java AWS SDK like: 

- `openEC2Ports(String,String):void` Open an EC2 instance firewall (ip range, protocols) for our IP.
- `getInstance(String):<Instance>`  Get an EC2 instance by instance Id.
- `getListOfInstanceNames(String):List<String>`Gets list of EC2 instance names by tag name.
- `getSecurityGroupList(String):List<String>`  Gets the security group list of an EC2 instance by id.
- `addEC2SecurityGroupRule(String,String,String):void` Add a new rule to an EC2 security group.
- `removeEC2SecurityGroupRule(String,String):void`  Remove a rule from an EC2 security group.
- `getIpPermission(String,String):Optional<IpPermission>` Gets the ip permissions of a rule in a security group by its description.
- `updateEC2SecurityGroupRule(String,String,String):void`  Update a rule in an EC2 security group.
- `updateEC2SecurityGroupRule(String,String,String,String):void`  Update a rule in an EC2 security group.
- `updateSecurityGroupRuleWithMyIp(String,String):void` Update a rule in a security group our ip.
- `getIpFromAws():void` Get the public IP for this computer (using the CheckIP AWS endpoint).

### Examples

An example: 

```
  //Open the firewall to our IP.
  AwsUtils.openEC2Ports("instance-name", "rule-name");
```