[![Java CI with Maven](https://github.com/adriandeleon/aws-utils/actions/workflows/maven.yml/badge.svg)](https://github.com/adriandeleon/aws-utils/actions/workflows/maven.yml)

# AwsUtils

## A few AWS EC2 helpers for the Java AWS SDK.

Latest version: 1.0.1

This library requieres Java [JDK 21](https://jdk.java.net/21/) to compile.

### Helper methods

This library includes a few helpers/wrappers like:

- Open ports to an instance.
- Get an instance by tag name.
- List instances by tag name.
- Add/update/remove rules for a security group.

#### Methods:

- [`openEC2Ports(String,String):void`](https://github.com/adriandeleon/aws-utils/blob/518c4e009282a2f18970fec29d013ee02050fbea/src/main/java/me/adriandeleon/AwsEc2Utils.java#L52)
  Open an EC2 instance firewall (ip range, protocols) for our IP.
- [`getInstance(String):<Instance>`](https://github.com/adriandeleon/aws-utils/blob/518c4e009282a2f18970fec29d013ee02050fbea/src/main/java/me/adriandeleon/AwsEc2Utils.java#L74)
  Get an EC2 instance by instance Id.
- [`getListOfInstanceNames(String):List<String>`](https://github.com/adriandeleon/aws-utils/blob/518c4e009282a2f18970fec29d013ee02050fbea/src/main/java/me/adriandeleon/AwsEc2Utils.java#L108)
  Gets list of EC2 instance names by tag name.
- [`getSecurityGroupList(String):List<String>`](https://github.com/adriandeleon/aws-utils/blob/518c4e009282a2f18970fec29d013ee02050fbea/src/main/java/me/adriandeleon/AwsEc2Utils.java#L140)
  Gets the security group list of an EC2 instance by id.
- [`addEC2SecurityGroupRule(String,String,String):void`](https://github.com/adriandeleon/aws-utils/blob/518c4e009282a2f18970fec29d013ee02050fbea/src/main/java/me/adriandeleon/AwsEc2Utils.java#L140)
  Add a new rule to an EC2 security group.
- [`removeEC2SecurityGroupRule(String,String):void`](https://github.com/adriandeleon/aws-utils/blob/518c4e009282a2f18970fec29d013ee02050fbea/src/main/java/me/adriandeleon/AwsEc2Utils.java#L161)
  Remove a rule from an EC2 security group.
- [`getIpPermission(String,String):Optional<IpPermission>`](https://github.com/adriandeleon/aws-utils/blob/518c4e009282a2f18970fec29d013ee02050fbea/src/main/java/me/adriandeleon/AwsEc2Utils.java#L210) Gets the ip permissions of a rule in a security group by its description.
- [`updateEC2SecurityGroupRule(String,String,String):void`](https://github.com/adriandeleon/aws-utils/blob/518c4e009282a2f18970fec29d013ee02050fbea/src/main/java/me/adriandeleon/AwsEc2Utils.java#L243)  Update a rule in an EC2 security group.
- [`updateEC2SecurityGroupRule(String,String,String,String):void`](https://github.com/adriandeleon/aws-utils/blob/518c4e009282a2f18970fec29d013ee02050fbea/src/main/java/me/adriandeleon/AwsEc2Utils.java#L311)  Update a rule in an EC2 security group.
- [`updateSecurityGroupRuleWithMyIp(String,String):void`](https://github.com/adriandeleon/aws-utils/blob/518c4e009282a2f18970fec29d013ee02050fbea/src/main/java/me/adriandeleon/AwsEc2Utils.java#L358) Update a rule in a security group our ip.
- [`getIpFromAws():void`](https://github.com/adriandeleon/aws-utils/blob/518c4e009282a2f18970fec29d013ee02050fbea/src/main/java/me/adriandeleon/AwsEc2Utils.java#L383) Get the public IP for this computer (using the CheckIP AWS endpoint).

### Examples

An example:

```java
  //Open the firewall to our IP.
  AwsUtils.openEC2Ports("instance-name","rule-name");
```

### Installation.

There are two ways to include the library using maven: a local download, or add the Github Packages maven repo to your
m2 settings file.

### Local download.

clone this repo and then and run:

```shell
mvn install
```

This will install the jar in your local maven repository.

### Remote GitHub Packages maven repository.

add the following configuration to your local maven `settings.xml` file (usually located in `$HOME/.m2/settings.xml`)

```xml

<servers>
    <server>
        <id>github-public</id>
        <username>adriandeleon</username>
        <!-- Public token with `read:packages` scope -->
        <password>
            &#103;&#104;&#112;&#95;&#54;&#121;&#83;&#120;&#86;&#101;&#106;&#80;&#97;&#88;&#88;&#81;&#85;&#48;&#51;&#116;&#110;&#50;&#97;&#52;&#70;&#74;&#84;&#67;&#102;&#97;&#104;&#77;&#107;&#82;&#49;&#81;&#86;&#83;&#106;&#97;
        </password>
    </server>
</servers>
```

### Adding the jar to your `pom.xml`

Once you added the jar to your local maven repository or added the remote Github Packages maven repo, then you can add
this to your `pom.xml` file:

First, add this to your `<repository>` stanza:

```xml

<repositories>
    <!--https://github.com/orgs/community/discussions/25629#discussioncomment-3248525-->
    <!--https://github.com/orgs/community/discussions/26634-->
    <repository>
        <id>github-public</id>
        <name>adriandeleon's Github Packages maven repository</name>
        <url>https://maven.pkg.github.com/adriandeleon/*</url>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>
```

Then you can add the dependency to your `<dependencies>` stanza:

```xml

<dependency>
    <groupId>me.adriandeleon</groupId>
    <artifactId>aws-utils</artifactId>
    <version>1.0.1</version>
</dependency>
```

**Note**:
You should have a valid[ `$HOME/.aws/config`](https://docs.aws.amazon.com/sdkref/latest/guide/file-location.html) file
and the `AWS_REGION` environment variable set for this library to work.

### Running the integration tests.

If you plan on running the integration tests, you will need to set a valid config file (see above), and export the
following environment variables first:

```shell
AWSUTILS_TEST_VALID_INSTANCE_TAG_NAME
AWSUTILS_TEST_VALID_INSTANCE_ID
AWS_REGION
```

These should correspond to a valid instance tag name and a valid instanceId on the AWS account that you will run the
integration tests against.
