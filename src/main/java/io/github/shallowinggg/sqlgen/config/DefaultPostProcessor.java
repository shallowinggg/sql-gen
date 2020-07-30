package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.env.*;
import io.github.shallowinggg.sqlgen.io.FileSystemResource;
import io.github.shallowinggg.sqlgen.io.Resource;
import io.github.shallowinggg.sqlgen.io.ResourceLoader;
import io.github.shallowinggg.sqlgen.io.support.FactoriesLoader;
import io.github.shallowinggg.sqlgen.util.ClassUtils;
import io.github.shallowinggg.sqlgen.util.CollectionUtils;
import io.github.shallowinggg.sqlgen.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ding shimin
 */
public class DefaultPostProcessor implements EnvironmentPostProcessor {

    private final Log logger = LogFactory.getLog(getClass());

    private ConfigurableEnvironment environment;

    private ResourceLoader resourceLoader;

    private final List<PropertySourceLoader> propertySourceLoaders = new ArrayList<>();

    private final List<PropertiesFinder> propertiesFinders = new ArrayList<>();

    private static final String NO_SEARCH_NAME = "";

    private static final Resource[] EMPTY_RESOURCES = {};

    private static final Comparator<File> FILE_COMPARATOR = Comparator.comparing(File::getAbsolutePath);

    private final Map<DocumentsCacheKey, List<PropertySource<?>>> loadDocumentsCache = new HashMap<>();

    public DefaultPostProcessor() {
        init();
    }

    private void init() {
        propertySourceLoaders.add(new PropertiesPropertySourceLoader());
        propertySourceLoaders.add(new YamlPropertySourceLoader());

        propertiesFinders.add(new DefaultPropertiesFinder());
        if (ClassUtils.isPresent("org.springframework.boot.SpringApplication", null)) {
            propertiesFinders.add(new SpringBootPropertiesFinder());
        }
        propertiesFinders.addAll(loadPropertiesFinders());
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
        List<PropertiesFinder> propertiesFinders = loadPropertiesFinders();
        for (PropertiesFinder finder : propertiesFinders) {
            if (finder instanceof ConfigurablePropertiesFinder) {
                ((EnvironmentAware) finder).setEnvironment(environment);
            }
        }

        List<String> profiles = initializeProfiles(environment);
        propertiesFinders.forEach(finder -> {
            profiles.forEach(profile -> load(profile, finder, environment.getPropertySources()::addLast));

            if(finder.getDbConfigProperties() != null) {
                finder.getDbConfigProperties().stream().filter(DbConfigProperties::isCandicate)
                        .forEach(properties -> {

                        });
            }
        });
    }

    private List<PropertiesFinder> loadPropertiesFinders() {
        return FactoriesLoader.loadFactories(PropertiesFinder.class, getClass().getClassLoader()).
                stream().filter(PropertiesFinder::isCandidate).collect(Collectors.toList());
    }

    private List<String> initializeProfiles(ConfigurableEnvironment environment) {
        List<String> profiles = new ArrayList<>();
        Collections.addAll(profiles, environment.getActiveProfiles());
        if (profiles.isEmpty()) {
            Collections.addAll(profiles, environment.getDefaultProfiles());
        }
        profiles.add(null);
        return profiles;
    }

    private void load(String profile, PropertiesFinder finder, PropertySourceConsumer consumer) {
        finder.getSearchLocations().forEach(location -> {
            boolean isFolder = location.endsWith("/");
            String name = isFolder ? finder.getSearchName() : NO_SEARCH_NAME;
            load(profile, location, name, consumer);
        });
    }

    private void load(String profile, String location, String name, PropertySourceConsumer consumer) {
        if (!StringUtils.hasText(name)) {
            for (PropertySourceLoader loader : this.propertySourceLoaders) {
                if (canLoadFileExtension(loader, location)) {
                    load(loader, location, consumer);
                    return;
                }
            }
            throw new IllegalStateException("File extension of config file location '" + location
                    + "' is not known to any PropertySourceLoader. If the location is meant to reference "
                    + "a directory, it must end in '/'");
        }
        Set<String> processed = new HashSet<>();
        for (PropertySourceLoader loader : this.propertySourceLoaders) {
            for (String fileExtension : loader.getFileExtensions()) {
                if (processed.add(fileExtension)) {
                    loadForFileExtension(loader, location + name, "." + fileExtension, profile,
                            consumer);
                }
            }
        }
    }

    private boolean canLoadFileExtension(PropertySourceLoader loader, String name) {
        return Arrays.stream(loader.getFileExtensions())
                .anyMatch((fileExtension) -> StringUtils.endsWithIgnoreCase(name, fileExtension));
    }


    private void loadForFileExtension(PropertySourceLoader loader, String prefix, String extension, String profile,
                                      PropertySourceConsumer consumer) {
        String location;
        if (StringUtils.hasText(profile)) {
            location = prefix + "-" + profile + extension;
        } else {
            location = prefix + extension;
        }
        load(loader, location, consumer);
    }

    private void load(PropertySourceLoader loader, String location, PropertySourceConsumer consumer) {
        Resource[] resources = getResources(location);
        for (Resource resource : resources) {
            try {
                if (resource == null || !resource.exists()) {
                    if (this.logger.isTraceEnabled()) {
                        StringBuilder description = getDescription("Skipped missing config ", location, resource);
                        this.logger.trace(description);
                    }
                    continue;
                }
                if (!StringUtils.hasText(StringUtils.getFilenameExtension(resource.getFilename()))) {
                    if (this.logger.isTraceEnabled()) {
                        StringBuilder description = getDescription("Skipped empty config extension ", location, resource);
                        this.logger.trace(description);
                    }
                    continue;
                }
                String name = "applicationConfig: [" + getLocationName(location, resource) + "]";
                List<PropertySource<?>> documents = loadDocuments(loader, name, resource);
                if (CollectionUtils.isEmpty(documents)) {
                    if (this.logger.isTraceEnabled()) {
                        StringBuilder description = getDescription("Skipped unloaded config ", location, resource);
                        this.logger.trace(description);
                    }
                    continue;
                }

                documents.forEach(consumer);
                if (this.logger.isDebugEnabled()) {
                    StringBuilder description = getDescription("Loaded config file ", location, resource);
                    this.logger.debug(description);
                }
            } catch (Exception ex) {
                StringBuilder description = getDescription("Failed to load property source from ", location,
                        resource);
                throw new IllegalStateException(description.toString(), ex);
            }
        }
    }

    private String getLocationName(String location, Resource resource) {
        if (!location.contains("*")) {
            return location;
        }
        if (resource instanceof FileSystemResource) {
            return ((FileSystemResource) resource).getPath();
        }
        return resource.getDescription();
    }

    private Resource[] getResources(String location) {
        try {
            if (location.contains("*")) {
                return getResourcesFromPatternLocation(location);
            }
            return new Resource[]{this.resourceLoader.getResource(location)};
        } catch (Exception ex) {
            return EMPTY_RESOURCES;
        }
    }

    private StringBuilder getDescription(String prefix, String location, Resource resource) {
        StringBuilder result = new StringBuilder(prefix);
        try {
            if (resource != null) {
                String uri = resource.getURI().toASCIIString();
                result.append("'");
                result.append(uri);
                result.append("' (");
                result.append(location);
                result.append(")");
            }
        } catch (IOException ex) {
            result.append(location);
        }
        return result;
    }

    private Resource[] getResourcesFromPatternLocation(String location) throws IOException {
        String directoryPath = location.substring(0, location.indexOf("*/"));
        Resource resource = this.resourceLoader.getResource(directoryPath);
        File[] files = resource.getFile().listFiles(File::isDirectory);
        if (files != null) {
            String fileName = location.substring(location.lastIndexOf("/") + 1);
            Arrays.sort(files, FILE_COMPARATOR);
            return Arrays.stream(files).map((file) -> file.listFiles((dir, name) -> name.equals(fileName)))
                    .filter(Objects::nonNull).flatMap((Function<File[], Stream<File>>) Arrays::stream)
                    .map(FileSystemResource::new).toArray(Resource[]::new);
        }
        return EMPTY_RESOURCES;
    }

    private List<PropertySource<?>> loadDocuments(PropertySourceLoader loader, String name, Resource resource)
            throws IOException {
        DocumentsCacheKey cacheKey = new DocumentsCacheKey(loader, resource);
        List<PropertySource<?>> documents = this.loadDocumentsCache.get(cacheKey);
        if (documents == null) {
            documents = loader.load(name, resource);
            this.loadDocumentsCache.put(cacheKey, documents);
        }
        return documents;
    }

    /**
     * Cache key used to save loading the same document multiple times.
     */
    private static class DocumentsCacheKey {

        private final PropertySourceLoader loader;

        private final Resource resource;

        DocumentsCacheKey(PropertySourceLoader loader, Resource resource) {
            this.loader = loader;
            this.resource = resource;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            DocumentsCacheKey other = (DocumentsCacheKey) obj;
            return this.loader.equals(other.loader) && this.resource.equals(other.resource);
        }

        @Override
        public int hashCode() {
            return this.loader.hashCode() * 31 + this.resource.hashCode();
        }

    }

    private interface PropertySourceConsumer extends Consumer<PropertySource<?>> {
        @Override
        void accept(PropertySource<?> propertySource);
    }
}
