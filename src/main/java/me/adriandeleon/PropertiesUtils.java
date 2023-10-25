package me.adriandeleon;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Validate;

/**
 * The type Properties utils.
 */
@Log4j2
@UtilityClass
public class PropertiesUtils {

    /**
     * The constant PROPERTY_KEY_AWS_REGION.
     */
    public final static String PROPERTY_KEY_AWS_REGION = "AWS_REGION";
    /**
     * The constant PROPERTY_KEY_VALID_INSTANCE_TAG_NAME.
     */
    public final static String PROPERTY_KEY_VALID_INSTANCE_TAG_NAME = "AWSUTILS_TEST_VALID_INSTANCE_TAG_NAME";
    /**
     * The constant PROPERTY_KEY_VALID_INSTANCE_ID.
     */
    public static final String PROPERTY_KEY_VALID_INSTANCE_ID = "AWSUTILS_TEST_VALID_INSTANCE_ID";

    /**
     * The constant MESSAGE_COULD_NOT_FOUND_ENV_VARIABLE.
     */
    public static final String MESSAGE_COULD_NOT_FOUND_ENV_VARIABLE = "Could not found environment variable: ";

    /**
     * Gets property value.
     *
     * @param key the key
     * @return the property value
     * @throws Exception the exception
     */
    public static String getPropertyValue(final String key) throws Exception {
        Validate.notBlank(key,"key cannot be null or blank.");
        String propertyValue;

        //Check the environment variables first. The env variable should be all caps and separated by `_`.
        final String envVariableProperty = System.getenv(key);

        //If we can't find the environment variable, get the value from the application.properties file.
        if(envVariableProperty == null || envVariableProperty.isEmpty()){
            throw new Exception(MESSAGE_COULD_NOT_FOUND_ENV_VARIABLE + key);
        }else{
            propertyValue = envVariableProperty;
        }
        return propertyValue;
    }
}
