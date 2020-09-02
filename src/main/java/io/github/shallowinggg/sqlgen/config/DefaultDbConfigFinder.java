package io.github.shallowinggg.sqlgen.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Default db config finder implementation for {@link DbConfigFinder}.
 * It assumes that the config file name is db.
 * <p>
 * config file location:
 * <lo>
 * <li>classpath:/</li>
 * <li>classpath:/config/</li>
 * <li>file:./</li>
 * <li>file:./config/</li>
 * <lo/>
 * <p>
 * This implementation is fragile and useless in most cases and you
 * can consider it as a example.
 *
 * @author ding shimin
 */
public class DefaultDbConfigFinder extends AbstractDbConfigFinder {

    public DefaultDbConfigFinder() {
        addDbPropertyConfig(new CommonDbPropertyConfig());
    }

    @Override
    public Set<String> getSearchLocations() {
        return new LinkedHashSet<>(Arrays.asList("classpath:/", "classpath:/config/", "file:/", "file:/config/"));
    }

    @Override
    public Set<String> getSearchNames() {
        return Collections.singleton("db");
    }

    private static class CommonDbPropertyConfig implements DbPropertyConfig {

        @Override
        public String name() {
            return "default";
        }

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
