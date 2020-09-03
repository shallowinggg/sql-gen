package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.env.PropertySource;
import io.github.shallowinggg.sqlgen.env.StandardEnvironment;
import io.github.shallowinggg.sqlgen.io.DefaultResourceLoader;
import io.github.shallowinggg.sqlgen.util.CollectionUtils;
import io.github.shallowinggg.sqlgen.util.ObjectUtils;
import io.github.shallowinggg.sqlgen.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ding shimin
 */
public class AbstractDbConfigFinderTest {

    @Test
    public void profileDbConfigFinder() {
        DbConfigFinder dbConfigFinder = new ProfileDbConfigFinder();
        DbConfig dbConfig = dbConfigFinder.find(new StandardEnvironment(), new DefaultResourceLoader());
        Assert.assertNotNull(dbConfig);
        Assert.assertEquals("jdbc:mysql://localhost:3306/test", dbConfig.getUrl());
        Assert.assertEquals("com.mysql.jdbc.Driver", dbConfig.getDriverName());
        Assert.assertEquals("root", dbConfig.getUsername());
        Assert.assertEquals("root", dbConfig.getPassword());
    }


    private static class ProfileDbConfigFinder extends AbstractDbConfigFinder {

        ProfileDbConfigFinder() {
            addDbPropertyConfig(new CommonDbPropertyConfig());
        }

        @Override
        protected Set<String> getSearchLocations() {
            return Collections.singleton("classpath:/finder/");
        }

        @Override
        protected Set<String> getSearchNames() {
            return Collections.singleton("test");
        }

        @Override
        protected List<Document> asDocuments(List<PropertySource<?>> loaded) {
            if (loaded == null) {
                return Collections.emptyList();
            }
            return loaded.stream().map((propertySource) -> {
                Set<String> activeProfiles = getProfiles("test.profile.active", propertySource);
                return new Document(propertySource, null, activeProfiles, null);
            }).collect(Collectors.toList());
        }

        private Set<String> getProfiles(String name, PropertySource<?> propertySource) {
            String[] profiles = StringUtils.trimArrayElements(StringUtils.commaDelimitedListToStringArray(
                    (String) propertySource.getProperty(name)));
            return ObjectUtils.isEmpty(profiles) ? Collections.emptySet() : asProfileSet(profiles);
        }

        private Set<String> asProfileSet(String[] profileNames) {
            return new LinkedHashSet<>(Arrays.asList(profileNames));
        }

        @Override
        protected void postProcessDocument(Document document) {
            if (CollectionUtils.isNotEmpty(document.getActiveProfiles())) {
                this.profiles.addAll(document.getActiveProfiles());
            }
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
}
