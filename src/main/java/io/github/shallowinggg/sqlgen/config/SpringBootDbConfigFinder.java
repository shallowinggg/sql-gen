package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.env.PropertySource;
import io.github.shallowinggg.sqlgen.util.Assert;
import io.github.shallowinggg.sqlgen.util.ClassUtils;
import io.github.shallowinggg.sqlgen.util.ObjectUtils;
import io.github.shallowinggg.sqlgen.util.ResourceUtils;
import io.github.shallowinggg.sqlgen.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default SpringBoot db config finder implementation for interface {@link DbConfigFinder}.
 * This implementation follow SpringBoot default convention:
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
 * If you want to customize your own db properties configuration, you can extend this class and
 * implement {@link #extendDbPropertyConfigs()} method to add more.
 *
 * @author ding shimin
 * @see BaseDbPropertyConfig
 * @see HikariDbPropertyConfig
 * @see Dbcp2DbPropertyConfig
 * @see TomcatDbPropertyConfig
 * @see DruidDbPropertyConfig
 * @see C3p0DbPropertyConfig
 * @since 1.0
 */
public class SpringBootDbConfigFinder extends AbstractDbConfigFinder {

    private static final String WILDCARD = "*";

    /**
     * The "active profiles" property name.
     */
    public static final String ACTIVE_PROFILES_PROPERTY = "spring.profiles.active";

    /**
     * The "includes profiles" property name.
     */
    public static final String INCLUDE_PROFILES_PROPERTY = "spring.profiles.include";

    /**
     * The "config name" property name.
     */
    public static final String CONFIG_NAME_PROPERTY = "spring.config.name";

    /**
     * The "config location" property name.
     */
    public static final String CONFIG_LOCATION_PROPERTY = "spring.config.location";

    /**
     * The "config additional location" property name.
     */
    public static final String CONFIG_ADDITIONAL_LOCATION_PROPERTY = "spring.config.additional-location";

    /**
     * Note the order is from least to most specific (last one wins)
     */
    private static final String DEFAULT_SEARCH_LOCATIONS = "classpath:/,classpath:/config/,file:./,file:./config/*/,file:./config/";

    private static final String DEFAULT_NAMES = "application";

    /**
     * Pseudo URL prefix for all matching resources from the class path: "classpath*:"
     * This differs from ResourceLoader's classpath URL prefix in that it
     * retrieves all matching resources for a given name (e.g. "/beans.xml"),
     * for example in the root of all deployed JAR files.
     */
    private static final String CLASSPATH_ALL_URL_PREFIX = "classpath*:";

    private String searchLocations;

    private String searchNames;

    private boolean activatedProfiles;

    private Set<String> defaultProfiles = Collections.emptySet();

    public SpringBootDbConfigFinder() {
        List<DbPropertyConfig> propertyConfigs = new ArrayList<>();
        propertyConfigs.add(new BaseDbPropertyConfig());
        propertyConfigs.add(new HikariDbPropertyConfig());
        propertyConfigs.add(new TomcatDbPropertyConfig());
        propertyConfigs.add(new Dbcp2DbPropertyConfig());
        propertyConfigs.add(new DruidDbPropertyConfig());
        propertyConfigs.add(new C3p0DbPropertyConfig());
        propertyConfigs.addAll(extendDbPropertyConfigs());
        propertyConfigs.forEach(this::addDbPropertyConfig);
    }

    @Override
    protected void initializeProfiles() {
        // The default profile for these purposes is represented as null. We add it
        // first so that it is processed first and has lowest priority.
        this.profiles.add(null);
        Set<String> activatedViaProperty = getProfiles(ACTIVE_PROFILES_PROPERTY);
        Set<String> includedViaProperty = getProfiles(INCLUDE_PROFILES_PROPERTY);
        List<String> otherActiveProfiles = getOtherActiveProfiles(activatedViaProperty, includedViaProperty);
        this.profiles.addAll(otherActiveProfiles);
        // Any pre-existing active profiles set via property sources (e.g.
        // System properties) take precedence over those added in config files.
        this.profiles.addAll(includedViaProperty);
        addActiveProfiles(activatedViaProperty);
        // only has null profile
        if (this.profiles.size() == 1) {
            defaultProfiles = asProfileSet(this.environment.getDefaultProfiles());
            this.profiles.addAll(defaultProfiles);
        }
    }

    private List<String> getOtherActiveProfiles(Set<String> activatedViaProperty,
                                                Set<String> includedViaProperty) {
        return Arrays.stream(this.environment.getActiveProfiles()).filter(
                (profile) -> !activatedViaProperty.contains(profile) && !includedViaProperty.contains(profile))
                .collect(Collectors.toList());
    }

    void addActiveProfiles(Set<String> profiles) {
        if (profiles.isEmpty()) {
            return;
        }
        if (this.activatedProfiles) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("Profiles already activated, '" + profiles + "' will not be applied");
            }
            return;
        }
        this.profiles.addAll(profiles);
        if (this.log.isDebugEnabled()) {
            this.log.debug("Activated activeProfiles " + StringUtils.collectionToCommaDelimitedString(profiles));
        }
        this.activatedProfiles = true;
        removeUnprocessedDefaultProfiles();
    }

    private void removeUnprocessedDefaultProfiles() {
        this.profiles.removeAll(defaultProfiles);
    }

    @Override
    protected Set<String> getSearchLocations() {
        Set<String> locations = getSearchLocations(CONFIG_ADDITIONAL_LOCATION_PROPERTY);
        if (this.environment.containsProperty(CONFIG_LOCATION_PROPERTY)) {
            locations.addAll(getSearchLocations(CONFIG_LOCATION_PROPERTY));
        } else {
            locations.addAll(asResolvedSet(this.searchLocations, DEFAULT_SEARCH_LOCATIONS));
        }
        return locations;
    }

    private Set<String> getSearchLocations(String propertyName) {
        Set<String> locations = new LinkedHashSet<>();
        if (this.environment.containsProperty(propertyName)) {
            for (String path : asResolvedSet(this.environment.getProperty(propertyName), null)) {
                if (!path.contains("$")) {
                    path = StringUtils.cleanPath(path);
                    Assert.state(!path.startsWith(CLASSPATH_ALL_URL_PREFIX),
                            "Classpath wildcard patterns cannot be used as a search location");
                    validateWildcardLocation(path);
                    if (!ResourceUtils.isUrl(path)) {
                        path = ResourceUtils.FILE_URL_PREFIX + path;
                    }
                }
                locations.add(path);
            }
        }
        return locations;
    }

    private void validateWildcardLocation(String path) {
        if (path.contains(WILDCARD)) {
            Assert.state(StringUtils.countOccurrencesOf(path, "*") == 1,
                    () -> "Search location '" + path + "' cannot contain multiple wildcards");
            String directoryPath = path.substring(0, path.lastIndexOf('/') + 1);
            Assert.state(directoryPath.endsWith("*/"), () -> "Search location '" + path + "' must end with '*/'");
        }
    }

    @Override
    protected Set<String> getSearchNames() {
        if (this.environment.containsProperty(CONFIG_NAME_PROPERTY)) {
            String property = this.environment.getProperty(CONFIG_NAME_PROPERTY);
            Set<String> names = asResolvedSet(property, null);
            names.forEach(this::assertValidConfigName);
            return names;
        }
        return asResolvedSet(this.searchNames, DEFAULT_NAMES);
    }

    private Set<String> asResolvedSet(String value, String fallback) {
        List<String> list = Arrays.asList(StringUtils.trimArrayElements(StringUtils.commaDelimitedListToStringArray(
                (value != null) ? this.environment.resolvePlaceholders(value) : fallback)));
        Collections.reverse(list);
        return new LinkedHashSet<>(list);
    }

    private void assertValidConfigName(String name) {
        Assert.state(!name.contains("*"), () -> "Config name '" + name + "' cannot contain wildcards");
    }

    @Override
    protected List<Document> asDocuments(List<PropertySource<?>> loaded) {
        if (loaded == null) {
            return Collections.emptyList();
        }
        return loaded.stream().map((propertySource) -> {
            String[] profiles = StringUtils.trimArrayElements(StringUtils.commaDelimitedListToStringArray(
                    this.environment.getProperty("spring.profiles")));
            profiles = ObjectUtils.isEmpty(profiles) ? null : profiles;
            Set<String> activeProfiles = getProfiles(ACTIVE_PROFILES_PROPERTY);
            Set<String> includeProfiles = getProfiles(INCLUDE_PROFILES_PROPERTY);
            return new Document(propertySource, profiles, activeProfiles, includeProfiles);
        }).collect(Collectors.toList());
    }

    private Set<String> getProfiles(String name) {
        String[] profiles = StringUtils.trimArrayElements(StringUtils.commaDelimitedListToStringArray(
                this.environment.getProperty(name)));
        return ObjectUtils.isEmpty(profiles) ? Collections.emptySet() : asProfileSet(profiles);
    }

    private Set<String> asProfileSet(String[] profileNames) {
        return new LinkedHashSet<>(Arrays.asList(profileNames));
    }

    @Override
    protected void postProcessDocument(Document document) {
        addActiveProfiles(document.getActiveProfiles());
        addIncludedProfiles(document.getIncludeProfiles());
    }

    private void addIncludedProfiles(Set<String> includeProfiles) {
        LinkedList<String> existingProfiles = new LinkedList<>(this.profiles);
        this.profiles.clear();
        this.profiles.addAll(includeProfiles);
        this.profiles.removeAll(this.processedProfiles);
        this.profiles.addAll(existingProfiles);
    }

    /**
     * Set the search locations that will be considered as a comma-separated list. Each
     * search location should be a directory path (ending in "/") and it will be prefixed
     * by the file names constructed from {@link #setSearchNames(String) search names} and
     * profiles (if any) plus file extensions supported by the properties loaders.
     * Locations are considered in the order specified, with later items taking precedence
     * (like a map merge).
     *
     * @param locations the search locations
     */
    public void setSearchLocations(String locations) {
        Assert.hasLength(locations, "Locations must not be empty");
        this.searchLocations = locations;
    }

    /**
     * Sets the names of the files that should be loaded (excluding file extension) as a
     * comma-separated list.
     *
     * @param names the names to load
     */
    public void setSearchNames(String names) {
        Assert.hasLength(names, "Names must not be empty");
        this.searchNames = names;
    }

    /**
     * Sub class can implement this method to add custom {@link DbPropertyConfig}s
     * and thus take advantage of this class.
     *
     * @return custom {@code DbPropertyConfig} list
     */
    protected List<DbPropertyConfig> extendDbPropertyConfigs() {
        return Collections.emptyList();
    }

    /**
     * Base spring boot datasource config
     */
    private static class BaseDbPropertyConfig implements DbPropertyConfig {

        @Override
        public String name() {
            return "springboot-default";
        }

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
    private static class HikariDbPropertyConfig implements DbPropertyConfig {

        @Override
        public String name() {
            return "springboot-hikari";
        }

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
    private static class Dbcp2DbPropertyConfig implements DbPropertyConfig {

        @Override
        public String name() {
            return "springboot-dbcp2";
        }

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
    private static class TomcatDbPropertyConfig implements DbPropertyConfig {

        @Override
        public String name() {
            return "springboot-tomcat";
        }

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
    private static class DruidDbPropertyConfig implements DbPropertyConfig {

        @Override
        public String name() {
            return "springboot-druid";
        }

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
    private static class C3p0DbPropertyConfig implements DbPropertyConfig {

        @Override
        public String name() {
            return "springboot-c3p0";
        }

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
