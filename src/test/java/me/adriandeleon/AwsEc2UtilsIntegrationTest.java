package me.adriandeleon;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

//These integration tests need a real AWS account to run,
// so these will probably not run for you, or will have to be modified.
@Tag("UnitTest")
class AwsEc2UtilsIntegrationTest {

    private static String VALID_INSTANCE_TAG_NAME;
    private static String VALID_INSTANCE_ID;

    @BeforeAll
    static void beforeAll() throws Exception {
        VALID_INSTANCE_TAG_NAME = PropertiesUtils.getPropertyValue(PropertiesUtils.PROPERTY_KEY_VALID_INSTANCE_TAG_NAME);
        VALID_INSTANCE_ID = PropertiesUtils.getPropertyValue(PropertiesUtils.PROPERTY_KEY_VALID_INSTANCE_ID);
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void openEC2Ports() throws URISyntaxException, IOException {
        AwsEc2Utils.openEC2Ports("Bitnami-GitLab-RepoServer", "Adrian De Leon");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getListOfInstanceNames_Test_NullAndEmptyInstanceTagName(final String tagName) {
        assertThatThrownBy(() -> AwsEc2Utils.getListOfInstanceNames(tagName))
                .hasMessage(AwsEc2Utils.MESSAGE_TAG_NAME_CANNOT_BE_NULL_OR_BLANK);
    }

    @Test
    void getListOfInstanceNames_Test_InvalidInstanceTagName() {
        final List<String> instanceList = AwsEc2Utils.getListOfInstanceNames("InstanceName-does-not-exist");
        assertThat(instanceList)
                .isNotNull()
                .isEmpty();
    }
    @Test
    void getListOfInstanceNames_Test_ValidInstanceTagName() {
        final List<String> instanceList = AwsEc2Utils.getListOfInstanceNames(VALID_INSTANCE_TAG_NAME);
        assertThat(instanceList)
                .isNotNull()
                .isNotEmpty()
                .contains(VALID_INSTANCE_ID);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getInstance_Test_NullAndEmptyInstanceId(final String instanceId) {
        assertThatThrownBy(() -> AwsEc2Utils.getInstance(instanceId))
                .hasMessage(AwsEc2Utils.MESSAGE_INSTANCE_ID_CANNOT_BE_NULL_OR_BLANK);
    }

    @Test
    void getInstance_Test_InvalidInstanceId() {
        final Optional<Instance> optionalInstance = AwsEc2Utils.getInstance("i-DoNotExist");
        assertThat(optionalInstance)
                .isNotNull()
                .isNotPresent();
    }

    @Test
    void getInstance_Test_ValidInstanceId() {
        final Optional<Instance> optionalInstance = AwsEc2Utils.getInstance(VALID_INSTANCE_ID);
        assertThat(optionalInstance)
                .isNotNull()
                .isPresent()
                .get()
                .extracting(Instance::instanceId)
                .asString()
                .isEqualTo(VALID_INSTANCE_ID);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getSecurityGroupList_Test_NullAndEmptyInstanceId(final String instanceId) {
        assertThatThrownBy(() -> AwsEc2Utils.getSecurityGroupList(instanceId))
                .hasMessage(AwsEc2Utils.MESSAGE_INSTANCE_ID_CANNOT_BE_NULL_OR_BLANK);

    }

    @Test
    void getSecurityGroupList_Test_ValidInstanceId() {
        final List<String> securityGroupList = AwsEc2Utils.getSecurityGroupList(VALID_INSTANCE_ID);
        assertThat(securityGroupList)
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    void addEC2SecurityGroupRule() {
    }

    @Test
    void removeEC2SecurityGroupRule() {
    }

    @Test
    void getIpPermission() {
    }

    @Test
    void updateEC2SecurityGroupRule() {
    }

    @Test
    void testUpdateEC2SecurityGroupRule() {
    }

    @Test
    void updateSecurityGroupRuleWithMyIp() {
    }
}