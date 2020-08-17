package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.env.Environment;
import io.github.shallowinggg.sqlgen.util.Assert;
import io.github.shallowinggg.sqlgen.util.ClassUtils;
import io.github.shallowinggg.sqlgen.util.CollectionUtils;
import io.github.shallowinggg.sqlgen.util.ResourceUtils;
import io.github.shallowinggg.sqlgen.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default SpringBoot db config finder implementation for interface {@link DbPropertiesFinder}.
 * This implementation use SpringBoot default convention:
 * <p>
 * config file name: application.
 * <p>
 * config file location:
 * <lo>
 * <li>classpath:/</li>
 * <li>classpath:/config/</li>
 * <li>file:./</li>
 * <li>file:./config/</li>
 * <lo/>
 * <p>
 * Also, you can use system property like "spring.config.location" and "spring.config.name" to
 * override this default configuration.
 * <p>
 * Otherwise, you can specify profiles like what you do in plain springboot project. And this
 * profiles will only be used to find config files.
 * <p>
 * This implementation provide six different common datasource config default:
 * <lo>
 * <li>spring base</li>
 * <li>hikari</li>
 * <li>dbcp2</li>
 * <li>tomcat</li>
 * <li>druid</li>
 * <li>c3p0</li>
 * </lo>
 * <p>
 * If you want to custom your own db properties configuration, you can extend this class and
 * implement {@link #extendDbConfigProperties()} method to add.
 *
 * @author ding shimin
 * @see BaseConfigProperties
 * @see HikariConfigProperties
 * @see Dbcp2ConfigProperties
 * @see TomcatConfigProperties
 * @see DruidConfigProperties
 * @see C3p0ConfigProperties
 * @since 1.0
 */
public class SpringBootDbPropertiesFinder implements DbPropertiesFinder, EnvironmentAware {

    private final Log log = LogFactory.getLog(getClass());

    public static final String ACTIVE_PROFILES_PROPERTY = "spring.profiles.active";
    public static final String INCLUDE_PROFILES_PROPERTY = "spring.profiles.include";
    public static final String CONFIG_NAME_PROPERTY = "spring.config.name";
    public static final String CONFIG_LOCATION_PROPERTY = "spring.config.location";
    public static final String CONFIG_ADDITIONAL_LOCATION_PROPERTY = "spring.config.additional-location";
    private static final String DEFAULT_SEARCH_LOCATIONS = "classpath:/,classpath:/config/,file:./,file:./config/";
    private static final String DEFAULT_NAMES = "application";

    private final Deque<String> profiles = new LinkedList<>();

    private List<String> searchLocations;

    private List<String> searchNames;

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        Assert.notNull(environment, "environment must not be null");
        this.environment = environment;
    }

    @Override
    public boolean isCandidate() {
        return ClassUtils.isPresent("org.springframework.boot.SpringApplication", null);
    }

    @Override
    public List<String> getSearchLocations() {
        List<String> searchLocations = this.searchLocations;
        if (searchLocations == null) {
            if (this.environment.containsProperty(CONFIG_LOCATION_PROPERTY)) {
                searchLocations = this.getSearchLocations(CONFIG_LOCATION_PROPERTY);
            } else {
                searchLocations = this.getSearchLocations(CONFIG_ADDITIONAL_LOCATION_PROPERTY);
                searchLocations.addAll(this.asResolvedSet(null, DEFAULT_SEARCH_LOCATIONS));
            }

            searchLocations = Collections.unmodifiableList(searchLocations);
            this.searchLocations = searchLocations;
        }
        return searchLocations;
    }

    @Override
    public List<String> getSearchNames() {
        List<String> searchNames = this.searchNames;
        if (searchNames == null) {
            initializeProfiles();

            Set<String> names;
            if (this.environment.containsProperty(CONFIG_NAME_PROPERTY)) {
                String property = this.environment.getProperty(CONFIG_NAME_PROPERTY);
                names = asResolvedSet(property, null);
            } else {
                names = asResolvedSet(null, DEFAULT_NAMES);
            }

            searchNames = new ArrayList<>();
            for (String name : names) {
                searchNames.add(name);
                for (String profile : profiles) {
                    searchNames.add(name + "-" + profile);
                }
            }

            searchNames = Collections.unmodifiableList(searchNames);
            this.searchNames = searchNames;
        }
        return searchNames;
    }

    /**
     * Initialize profile information from both the {@link Environment} active
     * profiles and any {@code spring.profiles.active}/{@code spring.profiles.include}
     * properties that are already set.
     */
    private void initializeProfiles() {
        Set<String> activatedViaProperty = getProfilesActivatedViaProperty();
        this.profiles.addAll(getOtherActiveProfiles(activatedViaProperty));
        // Any pre-existing active profiles set via property sources (e.g.
        // System properties) take precedence over those added in config files.
        addActiveProfiles(activatedViaProperty);
        this.profiles.remove("");
        if (this.profiles.isEmpty()) { // only has null profile
            this.profiles.addAll(Arrays.asList(this.environment.getDefaultProfiles()));
        }
    }

    private Set<String> getProfilesActivatedViaProperty() {
        if (!this.environment.containsProperty(ACTIVE_PROFILES_PROPERTY)
                && !this.environment.containsProperty(INCLUDE_PROFILES_PROPERTY)) {
            return Collections.emptySet();
        }

        Set<String> activeProfiles = new LinkedHashSet<>();
        activeProfiles.addAll(Arrays.asList(StringUtils.trimArrayElements(
                StringUtils.commaDelimitedListToStringArray(
                        this.environment.getProperty(INCLUDE_PROFILES_PROPERTY)))));
        activeProfiles.addAll(Arrays.asList(StringUtils.trimArrayElements(
                StringUtils.commaDelimitedListToStringArray(
                        this.environment.getProperty(ACTIVE_PROFILES_PROPERTY)))));
        return activeProfiles;
    }

    private List<String> getOtherActiveProfiles(Set<String> activatedViaProperty) {
        return Arrays.stream(this.environment.getActiveProfiles())
                .filter((profile) -> !activatedViaProperty.contains(profile))
                .collect(Collectors.toList());
    }

    void addActiveProfiles(Set<String> profiles) {
        if (profiles.isEmpty()) {
            return;
        }
        this.profiles.addAll(profiles);
        if (this.log.isDebugEnabled()) {
            this.log.debug("Activated activeProfiles "
                    + StringUtils.collectionToCommaDelimitedString(profiles));
        }
    }

    private List<String> getSearchLocations(String propertyName) {
        Set<String> locations = new LinkedHashSet<>();
        if (this.environment.containsProperty(propertyName)) {
            for (String path : asResolvedSet(
                    this.environment.getProperty(propertyName), null)) {
                if (!path.contains("$")) {
                    path = StringUtils.cleanPath(path);
                    if (!ResourceUtils.isUrl(path)) {
                        path = ResourceUtils.FILE_URL_PREFIX + path;
                    }
                }
                locations.add(path);
            }
        }
        return new ArrayList<>(locations);
    }

    private Set<String> asResolvedSet(String value, String fallback) {
        List<String> list = Arrays.asList(StringUtils.trimArrayElements(
                StringUtils.commaDelimitedListToStringArray((value != null)
                        ? this.environment.resolvePlaceholders(value) : fallback)));
        Collections.reverse(list);
        return new LinkedHashSet<>(list);
    }

    @Override
    public List<DbConfigProperties> getDbConfigProperties() {
        List<DbConfigProperties> dbConfigPropertiesList = new ArrayList<>(Arrays.asList(new BaseConfigProperties(),
                new HikariConfigProperties(), new TomcatConfigProperties(), new DruidConfigProperties(),
                new Dbcp2ConfigProperties(), new C3p0ConfigProperties()));

        List<DbConfigProperties> extendDbConfigPropertiesList = extendDbConfigProperties();
        if (CollectionUtils.isNotEmpty(extendDbConfigPropertiesList)) {
            dbConfigPropertiesList.addAll(extendDbConfigPropertiesList);
        }
        return Collections.unmodifiableList(dbConfigPropertiesList);
    }

    /**
     * Sub class can implement this method to add custom {@link DbConfigProperties}
     * and thus take advantage of this class.
     *
     * @return custom {@code DbConfigProperties} list
     */
    protected List<DbConfigProperties> extendDbConfigProperties() {
        return Collections.emptyList();
    }

    /**
     * Base spring boot datasource config
     */
    private static class BaseConfigProperties implements DbConfigProperties {

        @Override
        public boolean isCandidate() {
            return true;
        }

        @Override
        public String getUrlPropertyName() {
            return "spring.datasource.url";
        }

        @Override
        public String getDriverNamePropertyName() {
            return "spring.datasource.driver-class-name";
        }

        @Override
        public String getUsernamePropertyName() {
            return "spring.datasource.username";
        }

        @Override
        public String getPasswordPropertyName() {
            return "spring.datasource.password";
        }
    }

    /**
     * hikari datasource config
     */
    private static class HikariConfigProperties implements DbConfigProperties {
        @Override
        public boolean isCandidate() {
            return ClassUtils.isPresent("com.zaxxer.hikari.HikariDataSource", null);
        }

        @Override
        public String getUrlPropertyName() {
            return "spring.datasource.hikari.jdbc-url";
        }

        @Override
        public String getDriverNamePropertyName() {
            return "spring.datasource.hikari.driver-class-name";
        }

        @Override
        public String getUsernamePropertyName() {
            return "spring.datasource.hikari.username";
        }

        @Override
        public String getPasswordPropertyName() {
            return "spring.datasource.hikari.password";
        }
    }

    /**
     * dbcp2 datasource config
     */
    private static class Dbcp2ConfigProperties implements DbConfigProperties {
        @Override
        public boolean isCandidate() {
            return ClassUtils.isPresent("org.apache.commons.dbcp2.BasicDataSource", null);
        }

        @Override
        public String getUrlPropertyName() {
            return "spring.datasource.dbcp2.url";
        }

        @Override
        public String getDriverNamePropertyName() {
            return "spring.datasource.dbcp2.driver-class-name";
        }

        @Override
        public String getUsernamePropertyName() {
            return "spring.datasource.dbcp2.username";
        }

        @Override
        public String getPasswordPropertyName() {
            return "spring.datasource.dbcp2.password";
        }
    }

    /**
     * tomcat datasource config
     */
    private static class TomcatConfigProperties implements DbConfigProperties {
        @Override
        public boolean isCandidate() {
            return ClassUtils.isPresent("org.apache.tomcat.jdbc.pool.DataSource", null);
        }

        @Override
        public String getUrlPropertyName() {
            return "spring.datasource.tomcat.url";
        }

        @Override
        public String getDriverNamePropertyName() {
            return "spring.datasource.tomcat.driver-class-name";
        }

        @Override
        public String getUsernamePropertyName() {
            return "spring.datasource.tomcat.username";
        }

        @Override
        public String getPasswordPropertyName() {
            return "spring.datasource.tomcat.password";
        }
    }

    /**
     * druid datasource config
     */
    private static class DruidConfigProperties implements DbConfigProperties {
        @Override
        public boolean isCandidate() {
            return ClassUtils.isPresent("com.alibaba.druid.pool.DruidDataSource", null);
        }

        @Override
        public String getUrlPropertyName() {
            return "spring.datasource.druid.url";
        }

        @Override
        public String getDriverNamePropertyName() {
            return "spring.datasource.druid.driverClassName";
        }

        @Override
        public String getUsernamePropertyName() {
            return "spring.datasource.druid.username";
        }

        @Override
        public String getPasswordPropertyName() {
            return "spring.datasource.druid.password";
        }
    }

    /**
     * c3p0 datasource config
     */
    private static class C3p0ConfigProperties implements DbConfigProperties {
        @Override
        public boolean isCandidate() {
            return ClassUtils.isPresent("com.mchange.v2.c3p0.ComboPooledDataSource", null);
        }

        @Override
        public String getUrlPropertyName() {
            return "c3p0.url";
        }

        @Override
        public String getDriverNamePropertyName() {
            return "c3p0.driverClass";
        }

        @Override
        public String getUsernamePropertyName() {
            return "c3p0.user";
        }

        @Override
        public String getPasswordPropertyName() {
            return "c3p0.password";
        }
    }
}
