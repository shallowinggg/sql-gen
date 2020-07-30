package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.env.ConfigurableEnvironment;
import io.github.shallowinggg.sqlgen.env.PropertiesPropertySourceLoader;
import io.github.shallowinggg.sqlgen.env.PropertySource;
import io.github.shallowinggg.sqlgen.env.PropertySourceLoader;
import io.github.shallowinggg.sqlgen.env.YamlPropertySourceLoader;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    }

    private List<PropertiesFinder> loadPropertiesFinders() {
        return FactoriesLoader.loadFactories(PropertiesFinder.class, getClass().getClassLoader()).
                stream().filter(PropertiesFinder::isCandidate).collect(Collectors.toList());
    }

    private void load(PropertiesFinder finder) {
        finder.getSearchLocations().forEach(location ->
                load(location, propertySource -> environment.getPropertySources().addLast(propertySource)));
    }

    private void load(String prefix, PropertySourceConsumer consumer) {
        Set<String> processed = new HashSet<>();
        for (PropertySourceLoader loader : propertySourceLoaders) {
            for (String fileExtension : loader.getFileExtensions()) {
                if (processed.add(fileExtension)) {
                    loadForFileExtension(loader, prefix, "." + fileExtension, consumer);
                }
            }
        }
    }



    private void loadForFileExtension(PropertySourceLoader loader, String prefix, String extension, PropertySourceConsumer consumer) {
        String location = prefix + extension;
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
