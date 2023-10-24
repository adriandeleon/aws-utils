package me.adriandeleon;

import kong.unirest.Unirest;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AwsEc2Utils: A utility class for managing AWS resources. (We should make a library out of this code.)
 */
@Log4j2
@UtilityClass
public final class AwsEc2Utils {
    private static final Region awsRegion = Region.US_EAST_1;
    private static final String MESSAGE_GROUP_ID_CANNOT_BE_NULL_OR_BLANK = "groupId cannot be null or blank.";
    public static final String MESSAGE_RULE_DESCRIPTION_CANNOT_BE_NULL_OR_BLANK = "ruleDescription cannot be null or blank.";
    public static final String MESSAGE_INSTANCE_ID_CANNOT_BE_NULL_OR_BLANK = "instanceId cannot be null or blank.";
    public static final String MESSAGE_TAG_NAME_CANNOT_BE_NULL_OR_BLANK = "tagName cannot be null or blank.";
    public static final String MESSAGE_PROTOCOL_CANNOT_BE_NULL_OR_BLANK = "protocol cannot be null or blank.";
    public static final String MESSAGE_IP_CANNOT_BE_NULL_OR_BLANK = "ip cannot be null or blank.";

    /**
     * Open an EC2 instance firewall (ip range, protocols) for our IP.
     *
     * @param tagName         the tag name for our EC2 instance resource.
     * @param ruleDescription the rule description.
     */
    public static void openEC2Ports(final String tagName, final String ruleDescription) {
        Validate.notBlank(tagName, MESSAGE_TAG_NAME_CANNOT_BE_NULL_OR_BLANK);
        Validate.notBlank(ruleDescription, MESSAGE_RULE_DESCRIPTION_CANNOT_BE_NULL_OR_BLANK);

        //TODO: We should check if your IP already has access to avoid opening the ports again.

        System.out.println("Opening server: " + tagName + " ports to our IP: " + AwsEc2Utils.getIpFromAws());
        try {
            updateSecurityGroupRuleWithMyIp(tagName, ruleDescription);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("Could not update AWS firewall");
        }
        System.out.println("Ip configuration done.");
    }

    /**
     * Gets list of EC2 instance names by tag name.
     *
     * @param tagName the tag name value.
     * @return the list of instance names by tag name.
     */
    public static List<String> getListOfInstanceNames(final String tagName) {
        Validate.notBlank(tagName, MESSAGE_TAG_NAME_CANNOT_BE_NULL_OR_BLANK);

        final DescribeInstancesResponse response;

        //Get all instances metadata.
        try (Ec2Client ec2Client = Ec2Client.builder().region(awsRegion).build()) {
            final DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
            response = ec2Client.describeInstances(request);
        }

        final List<String> instanceIdList = new ArrayList<>();

        //Search all instances for the "Name" tag, then compare that value with the name we are searching for.
        for (Reservation reservation : response.reservations()) {
            for (Instance instance : reservation.instances()) {
                for (Tag tag : instance.tags()) {
                    if (tag.key().equals("Name")) {
                        if (tag.value().contains(tagName)) {
                            instanceIdList.add(instance.instanceId());
                        }
                    }
                }
            }
        }
        return List.copyOf(instanceIdList);
    }

    /**
     * Get an EC2 instance by instanceId.
     *
     * @param instanceId the instance id.
     * @return An Optional with the instance.
     */
    public static Optional<Instance> getInstance(final String instanceId) {
        Validate.notBlank(instanceId, MESSAGE_INSTANCE_ID_CANNOT_BE_NULL_OR_BLANK);

        try {
            final Ec2Client ec2Client = Ec2Client.builder().region(awsRegion).build();

            final DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
            final DescribeInstancesResponse response = ec2Client.describeInstances(request);

            //Search for the instance id in the instance list.
            for (Reservation reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    if (instance.instanceId().equals(instanceId)) {
                        return Optional.of(instance);
                    }
                }
            }
            ec2Client.close();

        } catch (Ec2Exception e) {
            log.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return Optional.empty();
    }

    /**
     * Gets the security group list of an EC2 instance by id.
     *
     * @param instanceId the instance id.
     * @return the security group list.
     */
    public static List<String> getSecurityGroupList(final String instanceId) {
        Validate.notBlank(instanceId, MESSAGE_INSTANCE_ID_CANNOT_BE_NULL_OR_BLANK);

        final Optional<Instance> optionalInstance = getInstance(instanceId);
        final List<String> securityGroupId = new ArrayList<>();

        //Gets the security groups of the specific instanceId.
        for (GroupIdentifier groupIdentifier : optionalInstance.orElseThrow().securityGroups()) {
            securityGroupId.add(groupIdentifier.groupId());
        }
        return List.copyOf(securityGroupId);
    }

    /**
     * Add a new rule to an EC2 security group.
     *
     * @param groupId         the groupId.
     * @param ruleDescription the rule description.
     * @param protocol        the protocol.
     * @param ip              the ip.
     */
    public static void addEC2SecurityGroupRule(final String groupId, final String ruleDescription,
                                               final String protocol, final String ip) {
        Validate.notBlank(groupId, MESSAGE_GROUP_ID_CANNOT_BE_NULL_OR_BLANK);
        Validate.notBlank(ruleDescription, MESSAGE_RULE_DESCRIPTION_CANNOT_BE_NULL_OR_BLANK);
        Validate.notBlank(protocol, MESSAGE_PROTOCOL_CANNOT_BE_NULL_OR_BLANK);
        Validate.notBlank(ip, MESSAGE_IP_CANNOT_BE_NULL_OR_BLANK);


        final Ec2Client ec2Client = Ec2Client.builder().region(awsRegion).build();

        try {
            // AWS uses a netmask instead of a direct IP, a /32 netmask means a singe IP
            // check: https://superuser.com/questions/1473252/what-does-it-mean-to-have-a-subnet-mask-32
            final String authorizedIp = ip + "/32";

            //The IP range.
            final IpRange ipRange = IpRange.builder()
                    .description(ruleDescription)
                    .cidrIp(authorizedIp)
                    .build();

            //The IP permissions, includes protocols and IP range.
            final IpPermission ipPermission = IpPermission.builder()
                    .ipProtocol(protocol)
                    .ipRanges(ipRange)
                    .build();

            //Create the AWS firewall in request.
            final AuthorizeSecurityGroupIngressRequest request = AuthorizeSecurityGroupIngressRequest.builder()
                    .groupId(groupId)
                    .ipPermissions(ipPermission)
                    .build();

            //Does the actual modification.
            ec2Client.authorizeSecurityGroupIngress(request);

            ec2Client.close();
        } catch (Ec2Exception e) {
            log.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    /**
     * Remove a rule from an EC2 security group.
     *
     * @param groupId         the groupId.
     * @param ruleDescription the rule description.
     */
    public static void removeEC2SecurityGroupRule(final String groupId, final String ruleDescription)  {
        Validate.notBlank(groupId, MESSAGE_GROUP_ID_CANNOT_BE_NULL_OR_BLANK);
        Validate.notBlank(ruleDescription, MESSAGE_RULE_DESCRIPTION_CANNOT_BE_NULL_OR_BLANK);

        try {
            final Ec2Client ec2Client = Ec2Client.builder().region(awsRegion).build();
            final Optional<IpPermission> optionalIpPermission = getIpPermission(groupId, ruleDescription);
            final IpPermission ipPermission = optionalIpPermission.orElseThrow();

            //Create a revoke security group request. Includes the group id and the permissions.
            final RevokeSecurityGroupIngressRequest request = RevokeSecurityGroupIngressRequest.builder()
                    .groupId(groupId)
                    .ipPermissions(ipPermission)
                    .build();

            //Does the actual modification.
            ec2Client.revokeSecurityGroupIngress(request);

            ec2Client.close();
        } catch (Exception e) {
            log.error(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Gets the ip permissions of a rule in a security group by its description.
     *
     * @param groupId         the groupId.
     * @param ruleDescription the rule description.
     * @return the ip permission.
     * @throws Exception the exception if anything fails
     */
    public static Optional<IpPermission> getIpPermission(final String groupId,
                                                         final String ruleDescription) throws Exception {
        Validate.notBlank(groupId, MESSAGE_GROUP_ID_CANNOT_BE_NULL_OR_BLANK);
        Validate.notBlank(ruleDescription, MESSAGE_RULE_DESCRIPTION_CANNOT_BE_NULL_OR_BLANK);

        final Ec2Client ec2Client = Ec2Client.builder().region(awsRegion).build();
        IpPermission ipPermissionFound = null;

        try {
            //Create a describe(info) security group request
            final DescribeSecurityGroupsRequest request = DescribeSecurityGroupsRequest.builder()
                    .groupIds(groupId)
                    .build();

            //Get the response description(info) of a security group request.
            final DescribeSecurityGroupsResponse response = ec2Client.describeSecurityGroups(request);

            String cidrIp = null;
            String description = null;
            String protocol = null;

            ///AWS security groups have the following structure:
            // Groups have permissions, permissions consist of IP ranges.
            // Ranges have descriptions, protocol and an IP mask.
            for (SecurityGroup group : response.securityGroups()) {
                for (IpPermission ipPermission : group.ipPermissions()) {
                    for (IpRange ipRange : ipPermission.ipRanges()) {
                        if (ipRange.description().equals(ruleDescription)) {
                            log.info(ipRange.description());

                            cidrIp = ipRange.cidrIp();
                            description = ipRange.description();
                            protocol = ipPermission.ipProtocol();
                        }
                    }
                }
            }

            if (StringUtils.isEmpty(cidrIp)) {
                throw new Exception("There is no security rule with that description");
            }

            final IpRange iprange = IpRange.builder()
                    .cidrIp(cidrIp)
                    .description(description)
                    .build();

            ipPermissionFound = IpPermission.builder()
                    .ipRanges(iprange)
                    .ipProtocol(protocol)
                    .build();

            ec2Client.close();

        } catch (Ec2Exception e) {
            log.error(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return Optional.of(ipPermissionFound);
    }

    /**
     * Update a rule in an EC2 security group.
     *
     * @param groupId         the groupId.
     * @param ruleDescription the rule description.
     * @param ip              the ip.
     */
    public static void updateEC2SecurityGroupRule(final String groupId, final String ruleDescription, final String ip) {
        Validate.notBlank(groupId, MESSAGE_GROUP_ID_CANNOT_BE_NULL_OR_BLANK);
        Validate.notBlank(ruleDescription, MESSAGE_RULE_DESCRIPTION_CANNOT_BE_NULL_OR_BLANK);
        Validate.notBlank(ip, MESSAGE_IP_CANNOT_BE_NULL_OR_BLANK); //TODO: we should validate the IP format.

        try {
            final Optional<IpPermission> optionalIpPermission = getIpPermission(groupId, ruleDescription);
            final String protocol = optionalIpPermission.orElseThrow().ipProtocol();

            updateEC2SecurityGroupRule(groupId, ruleDescription, protocol, ip);
        } catch (Exception e) {
            log.error(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Update a rule in an EC2 security group.
     *
     * @param groupId         the groupId.
     * @param ruleDescription the rule description.
     * @param protocol        the protocol.
     * @param ip              the ip.
     */
    public static void updateEC2SecurityGroupRule(final String groupId, final String ruleDescription,
                                                  final String protocol, final String ip) {
        Validate.notBlank(groupId, MESSAGE_GROUP_ID_CANNOT_BE_NULL_OR_BLANK);
        Validate.notBlank(ruleDescription, MESSAGE_RULE_DESCRIPTION_CANNOT_BE_NULL_OR_BLANK);
        Validate.notBlank(protocol, MESSAGE_PROTOCOL_CANNOT_BE_NULL_OR_BLANK);
        Validate.notBlank(ip, MESSAGE_IP_CANNOT_BE_NULL_OR_BLANK); //TODO: we should validate the IP format.

        try {
            removeEC2SecurityGroupRule(groupId, ruleDescription);
            addEC2SecurityGroupRule(groupId, ruleDescription, protocol, ip);
        } catch (Exception e) {
            log.error(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Update a rule in a security group our ip.
     *
     * @param tagName         the tag name.
     * @param ruleDescription the rule description.
     * @throws Exception the exception if anything fails.
     */
    public static void updateSecurityGroupRuleWithMyIp(final String tagName, final String ruleDescription) throws Exception {
        Validate.notBlank(tagName, MESSAGE_TAG_NAME_CANNOT_BE_NULL_OR_BLANK);
        Validate.notBlank(ruleDescription, MESSAGE_RULE_DESCRIPTION_CANNOT_BE_NULL_OR_BLANK);

        final String instanceId;

        try {
            instanceId = getListOfInstanceNames(tagName).getFirst();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception("Could not find the AWS instance with that name/tag.");
        }

        //If the instance has more than one security group, we are screwed.
        final String securityGroupId = getSecurityGroupList(instanceId).getFirst();
        final String myIp = getIpFromAws();

        updateEC2SecurityGroupRule(securityGroupId, ruleDescription, myIp);
    }

    /**
     * Get the public IP for this computer (using the CheckIP AWS endpoint).
     *
     * @return the IP as a String.
     */
    private static String getIpFromAws(){
        return Unirest.get("https://checkip.amazonaws.com/").asString().getBody().trim();
    }
}
