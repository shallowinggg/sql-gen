package io.github.shallowinggg.sqlgen.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Default db properties finder implementation for {@link DbPropertiesFinder}.
 *
 * @author ding shimin
 */
public class DefaultDbPropertiesFinder implements DbPropertiesFinder {

    @Override
    public boolean isCandidate() {
        return true;
    }

    @Override
    public List<String> getSearchLocations() {
        return Arrays.asList("classpath:/", "classpath:/config/", "file:/", "file:/config/");
    }

    @Override
    public List<String> getSearchNames() {
        return Collections.singletonList("db.properties");
    }

    @Override
    public List<DbConfigProperties> getDbConfigProperties() {
        return Collections.singletonList(new CommonConfigProperties());
    }

    private static class CommonConfigProperties implements DbConfigProperties {
        @Override
        public boolean isCandidate() {
            return true;
        }

        @Override
        public String getUrlPropertyName() {
            return "url";
        }

        @Override
        public String getDriverNamePropertyName() {
            return "driverClassName";
        }

        @Override
        public String getUsernamePropertyName() {
            return "username";
        }

        @Override
        public String getPasswordPropertyName() {
            return "password";
        }
    }
}
