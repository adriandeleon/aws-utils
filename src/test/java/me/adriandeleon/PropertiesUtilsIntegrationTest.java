package me.adriandeleon;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("UnitTest")
class PropertiesUtilsIntegrationTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getPropertyValue_Test_GetValidInstanceId() throws Exception {
        final String validInstanceId = PropertiesUtils.getPropertyValue(PropertiesUtils.PROPERTY_KEY_VALID_INSTANCE_ID);
        assertThat(validInstanceId)
                .isNotNull()
                .isNotBlank();
    }
    @Test
    void getPropertyValue_Test_GetValidInstanceTagName() throws Exception {
        final String validInstanceId = PropertiesUtils.getPropertyValue(PropertiesUtils.PROPERTY_KEY_VALID_INSTANCE_TAG_NAME);
        assertThat(validInstanceId)
                .isNotNull()
                .isNotBlank();
    }

    @Test
    void getPropertyValue_Test_GetInvalidPropertyKey() throws Exception {
        final String envVariableKey = "I_DO_NOT_EXIST";
        assertThatThrownBy(() -> PropertiesUtils.getPropertyValue(envVariableKey))
                .hasMessage(PropertiesUtils.MESSAGE_COULD_NOT_FOUND_ENV_VARIABLE + envVariableKey);
    }
}