package io.github.shallowinggg.sqlgen.config;

import io.github.shallowinggg.sqlgen.env.ConfigurableEnvironment;
import io.github.shallowinggg.sqlgen.env.MutablePropertySources;
import io.github.shallowinggg.sqlgen.env.Profiles;
import io.github.shallowinggg.sqlgen.env.PropertiesPropertySourceLoader;
import io.github.shallowinggg.sqlgen.env.PropertySource;
import io.github.shallowinggg.sqlgen.env.PropertySourceLoader;
import io.github.shallowinggg.sqlgen.env.StandardEnvironment;
import io.github.shallowinggg.sqlgen.env.YamlPropertySourceLoader;
import io.github.shallowinggg.sqlgen.io.DefaultResourceLoader;
import io.github.shallowinggg.sqlgen.io.FileSystemResource;
import io.github.shallowinggg.sqlgen.io.Resource;
import io.github.shallowinggg.sqlgen.io.ResourceLoader;
import io.github.shallowinggg.sqlgen.util.Assert;
import io.github.shallowinggg.sqlgen.util.CollectionUtils;
import io.github.shallowinggg.sqlgen.util.ObjectUtils;
import io.github.shallowinggg.sqlgen.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract skeleton implementation for interface {@link DbConfigFinder}.
 * It is coupled with SpringBoot profile implementation and thus sub class
 * will be restricted, but it is available enough now. This class will be
 * refactor in the future to solve this problem.
 * <p>
 * Sub class can only implement methods {@link #getSearchLocations()},
 * {@link #getSearchNames()} to provide necessary information which used
 * by this class. You can also extend methods {@link #initializeProfiles()},
 * {@link #asDocuments(List)}, {@link #postProcessDocument(Document)} to
 * get more customization.
 *
 * @author ding shimin
 */
public abstract class AbstractDbConfigFinder implements DbConfigFinder {

    protected final Log log = LogFactory.getLog(getClass());

    private static final Set<String> NO_SEARCH_NAMES = Collections.singleton("");

    private static final Resource[] EMPTY_RESOURCES = {};

    private static final Comparator<File> FILE_COMPARATOR = Comparator.comparing(File::getAbsolutePath);

    private final List<PropertySourceLoader> propertySourceLoaders;

    private final Map<String, DbPropertyConfig> dbPropertyConfigMap = new LinkedHashMap<>();

    private final Map<DocumentsCacheKey, List<Document>> loadDocumentsCache = new HashMap<>();

    private Map<String, MutablePropertySources> loaded;

    protected Deque<String> profiles;

    protected List<String> processedProfiles;

    protected ConfigurableEnvironment environment;

    protected ResourceLoader resourceLoader;

    protected AbstractDbConfigFinder() {
        this.propertySourceLoaders = Arrays.asList(
                new PropertiesPropertySourceLoader(),
                new YamlPropertySourceLoader());
    }

    @Override
    public void addDbPropertyConfig(DbPropertyConfig property) {
        Assert.notNull(property, "property must not be null");
        String name = property.name();
        Assert.hasText(name, "property name must has text");
        dbPropertyConfigMap.put(name, property);
    }

    @Nullable
    @Override
    public DbPropertyConfig getDbPropertyConfig(String name) {
        Assert.hasText(name, "name must has text");
        return dbPropertyConfigMap.get(name);
    }

    @Override
    public List<DbPropertyConfig> getAllDbPropertyConfigs() {
        Collection<DbPropertyConfig> propertyConfigs = dbPropertyConfigMap.values();
        if (CollectionUtils.isNotEmpty(propertyConfigs)) {
            return Collections.unmodifiableList(new ArrayList<>(propertyConfigs));
        }
        return Collections.emptyList();
    }

    @Override
    @Nullable
    public DbConfig find(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
        this.environment = environment != null ? environment : new StandardEnvironment();
        this.resourceLoader = resourceLoader != null ? resourceLoader : new DefaultResourceLoader();

        loadConfigs();
        List<DbPropertyConfig> candidates = getAllDbPropertyConfigs()
                .stream()
                .filter(DbPropertyConfig::isCandidate)
                .collect(Collectors.toList());
        for (DbPropertyConfig properties : candidates) {
            DbConfig dbConfig = resolve(properties);
            if (dbConfig != null) {
                return dbConfig;
            }
        }
        return null;
    }

    private DbConfig resolve(DbPropertyConfig config) {
        DbConfig dbConfig = new DbConfig(environment.getProperty(config.getUrlPropertyName()),
                environment.getProperty(config.getDriverNamePropertyName()),
                environment.getProperty(config.getUsernamePropertyName()),
                environment.getProperty(config.getPasswordPropertyName()));

        if (dbConfig.isValid()) {
            return dbConfig;
        }
        return null;
    }

    private void loadConfigs() {
        this.profiles = new LinkedList<>();
        this.processedProfiles = new LinkedList<>();
        this.loaded = new LinkedHashMap<>();
        initializeProfiles();
        while (!profiles.isEmpty()) {
            String profile = profiles.poll();
            load(profile, this::getPositiveProfileFilter, addToLoaded(MutablePropertySources::addLast, false));
            this.processedProfiles.add(profile);
        }
        load(null, this::getNegativeProfileFilter, addToLoaded(MutablePropertySources::addLast, true));
    }

    private void load(String profile, DocumentFilterFactory filterFactory, DocumentConsumer consumer) {
        getSearchLocations().forEach(location -> {
            boolean isFolder = location.endsWith("/");
            Set<String> names = isFolder ? getSearchNames() : NO_SEARCH_NAMES;
            names.forEach((name) -> load(location, name, profile, filterFactory, consumer));
        });
    }

    private void load(String location, String name, String profile, DocumentFilterFactory filterFactory,
                      DocumentConsumer consumer) {
        if (!StringUtils.hasText(name)) {
            for (PropertySourceLoader loader : this.propertySourceLoaders) {
                if (canLoadFileExtension(loader, location)) {
                    load(loader, location, profile, filterFactory.getDocumentFilter(profile), consumer);
                    return;
                }
            }
        }
        Set<String> processed = new HashSet<>();
        for (PropertySourceLoader loader : this.propertySourceLoaders) {
            for (String fileExtension : loader.getFileExtensions()) {
                if (processed.add(fileExtension)) {
                    loadForFileExtension(loader, location + name, "." + fileExtension, profile,
                            filterFactory, consumer);
                }
            }
        }
    }

    private boolean canLoadFileExtension(PropertySourceLoader loader, String name) {
        return Arrays.stream(loader.getFileExtensions())
                .anyMatch((fileExtension) -> StringUtils.endsWithIgnoreCase(name, fileExtension));
    }

    private void loadForFileExtension(PropertySourceLoader loader, String prefix, String fileExtension,
                                      String profile, DocumentFilterFactory filterFactory, DocumentConsumer consumer) {
        DocumentFilter defaultFilter = filterFactory.getDocumentFilter(null);
        DocumentFilter profileFilter = filterFactory.getDocumentFilter(profile);
        if (profile != null) {
            // Try profile-specific file & profile section in profile file (gh-340)
            String profileSpecificFile = prefix + "-" + profile + fileExtension;
            load(loader, profileSpecificFile, profile, defaultFilter, consumer);
            load(loader, profileSpecificFile, profile, profileFilter, consumer);
            // Try profile specific sections in files we've already processed
            for (String processedProfile : this.processedProfiles) {
                if (processedProfile != null) {
                    String previouslyLoaded = prefix + "-" + processedProfile + fileExtension;
                    load(loader, previouslyLoaded, profile, profileFilter, consumer);
                }
            }
        }
        // Also try the profile-specific section (if any) of the normal file
        load(loader, prefix + fileExtension, profile, profileFilter, consumer);
    }

    private void load(PropertySourceLoader loader, String location, String profile, DocumentFilter filter,
                      DocumentConsumer consumer) {

        Resource[] resources = getResources(location);
        for (Resource resource : resources) {
            try {
                if (resource == null || !resource.exists()) {
                    if (this.log.isTraceEnabled()) {
                        StringBuilder description = getDescription("Skipped missing config ", location, resource,
                                profile);
                        this.log.trace(description);
                    }
                    return;
                }
                if (!StringUtils.hasText(StringUtils.getFilenameExtension(resource.getFilename()))) {
                    if (this.log.isTraceEnabled()) {
                        StringBuilder description = getDescription("Skipped empty config extension ", location,
                                resource, profile);
                        this.log.trace(description);
                    }
                    return;
                }
                String name = "applicationConfig: [" + location + "]";
                List<Document> documents = loadDocuments(loader, name, resource);
                if (CollectionUtils.isEmpty(documents)) {
                    if (this.log.isTraceEnabled()) {
                        StringBuilder description = getDescription("Skipped unloaded config ", location, resource,
                                profile);
                        this.log.trace(description);
                    }
                    return;
                }
                List<Document> loaded = new ArrayList<>();
                for (Document document : documents) {
                    if (filter.match(document)) {
                        postProcessDocument(document);
                        loaded.add(document);
                    }
                }
                Collections.reverse(loaded);
                if (!loaded.isEmpty()) {
                    loaded.forEach((document) -> consumer.accept(profile, document));
                    if (this.log.isDebugEnabled()) {
                        StringBuilder description = getDescription("Loaded config file ", location, resource, profile);
                        this.log.debug(description);
                    }
                }
            } catch (Exception ex) {
                StringBuilder description = getDescription("Failed to load property source from ", location,
                        resource, profile);
                throw new IllegalStateException(description.toString(), ex);
            }
        }
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

    private Resource[] getResourcesFromPatternLocation(String location) throws IOException {
        String directoryPath = location.substring(0, location.indexOf("*/"));
        Resource resource = this.resourceLoader.getResource(directoryPath);
        File[] files = resource.getFile().listFiles(File::isDirectory);
        if (files != null) {
            String fileName = location.substring(location.lastIndexOf('/') + 1);
            Arrays.sort(files, FILE_COMPARATOR);
            return Arrays.stream(files).map((file) -> file.listFiles((dir, name) -> name.equals(fileName)))
                    .filter(Objects::nonNull).flatMap((Function<File[], Stream<File>>) Arrays::stream)
                    .map(FileSystemResource::new).toArray(Resource[]::new);
        }
        return EMPTY_RESOURCES;
    }

    /**
     * Post process the given loaded {@code Document}.
     * Sub class can apply additional processing as required.
     *
     * @param document the loaded {@code Document}
     */
    protected void postProcessDocument(Document document) {
    }

    /**
     * Initialize the profiles. Sub class can add more profiles
     * by extending this method.
     */
    protected void initializeProfiles() {
        // The default profile for these purposes is represented as null. We add it
        // first so that it is processed first and has lowest priority.
        this.profiles.add(null);
    }

    private DocumentFilter getPositiveProfileFilter(String profile) {
        return (Document document) -> {
            if (profile == null) {
                return ObjectUtils.isEmpty(document.getProfiles());
            }
            return ObjectUtils.containsElement(document.getProfiles(), profile)
                    && this.environment.acceptsProfiles(Profiles.of(document.getProfiles()));
        };
    }

    private DocumentFilter getNegativeProfileFilter(String profile) {
        return (Document document) -> (profile == null && !ObjectUtils.isEmpty(document.getProfiles())
                && this.environment.acceptsProfiles(Profiles.of(document.getProfiles())));
    }

    /**
     * Return search locations for the config files. Each search
     * location should be a directory path (ending in "/") and it
     * will be the prefixed by the file names constructed from
     * {@link #getSearchNames() search names}.
     *
     * @return the search locations
     */
    protected abstract Set<String> getSearchLocations();

    /**
     * Return the names of the files that should be loaded (excluding
     * file extension).
     *
     * @return the file names
     */
    protected abstract Set<String> getSearchNames();

    private DocumentConsumer addToLoaded(BiConsumer<MutablePropertySources, PropertySource<?>> addMethod,
                                         boolean checkForExisting) {
        return (profile, document) -> {
            if (checkForExisting) {
                for (MutablePropertySources merged : this.loaded.values()) {
                    if (merged.contains(document.getPropertySource().getName())) {
                        return;
                    }
                }
            }
            MutablePropertySources merged = this.loaded.computeIfAbsent(profile,
                    (k) -> new MutablePropertySources());
            addMethod.accept(merged, document.getPropertySource());
        };
    }

    private List<Document> loadDocuments(PropertySourceLoader loader, String name, Resource resource)
            throws IOException {
        DocumentsCacheKey cacheKey = new DocumentsCacheKey(loader, resource);
        List<Document> documents = loadDocumentsCache.get(cacheKey);
        if (documents == null) {
            List<PropertySource<?>> loaded = loader.load(name, resource);
            documents = asDocuments(loaded);
            loadDocumentsCache.put(cacheKey, documents);
        }
        return documents;
    }

    /**
     * Convert {@code PropertySource} to {@code Document}. Sub class
     * can extend this implementation and do more things.
     *
     * @param loaded The loaded PropertySources
     * @return {@code Document} converted from {@code PropertySource}
     */
    protected List<Document> asDocuments(List<PropertySource<?>> loaded) {
        if (loaded == null) {
            return Collections.emptyList();
        }
        return loaded.stream().map((propertySource) ->
                new Document(propertySource, null, null, null))
                .collect(Collectors.toList());
    }

    private StringBuilder getDescription(String prefix, String location, Resource resource, String profile) {
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
        if (profile != null) {
            result.append(" for profile ");
            result.append(profile);
        }
        return result;
    }

    /**
     * A single document loaded by a {@link PropertySourceLoader}.
     */
    protected static class Document {

        private final PropertySource<?> propertySource;

        private final String[] profiles;

        private final Set<String> activeProfiles;

        private final Set<String> includeProfiles;

        Document(PropertySource<?> propertySource, String[] profiles, Set<String> activeProfiles,
                 Set<String> includeProfiles) {
            this.propertySource = propertySource;
            this.profiles = profiles;
            this.activeProfiles = activeProfiles;
            this.includeProfiles = includeProfiles;
        }

        public PropertySource<?> getPropertySource() {
            return this.propertySource;
        }

        public String[] getProfiles() {
            return this.profiles;
        }

        public Set<String> getActiveProfiles() {
            return this.activeProfiles;
        }

        public Set<String> getIncludeProfiles() {
            return this.includeProfiles;
        }

        @Override
        public String toString() {
            return this.propertySource.toString();
        }

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

    /**
     * Factory used to create a {@link DocumentFilter}.
     */
    @FunctionalInterface
    private interface DocumentFilterFactory {

        /**
         * Create a filter for the given profile.
         *
         * @param profile the profile or {@code null}
         * @return the filter
         */
        DocumentFilter getDocumentFilter(String profile);

    }

    /**
     * Filter used to restrict when a {@link Document} is loaded.
     */
    @FunctionalInterface
    private interface DocumentFilter {

        /**
         * Determine if the given document meet the conditions supplied
         * by this method. If meet, add it to the loaded collections;
         * otherwise, this document will be ignored.
         *
         * @param document the document to determine
         * @return {@code true} if the given document meet conditions
         */
        boolean match(Document document);

    }

    /**
     * Consumer used to handle a loaded {@link Document}.
     */
    @FunctionalInterface
    private interface DocumentConsumer {

        /**
         * Handle the given loaded document and the profile
         * which document belongs to.
         *
         * @param profile  profile which document belongs to
         * @param document the loaded document
         */
        void accept(String profile, Document document);

    }

}
