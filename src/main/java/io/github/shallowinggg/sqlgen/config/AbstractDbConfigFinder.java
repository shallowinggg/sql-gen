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
import io.github.shallowinggg.sqlgen.io.Resource;
import io.github.shallowinggg.sqlgen.io.ResourceLoader;
import io.github.shallowinggg.sqlgen.util.CollectionUtils;
import io.github.shallowinggg.sqlgen.util.ObjectUtils;
import io.github.shallowinggg.sqlgen.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author ding shimin
 */
public abstract class AbstractDbConfigFinder implements DbConfigFinder {

    protected final Log log = LogFactory.getLog(getClass());

    private static final String WILDCARD = "*";

    private static final Set<String> NO_SEARCH_NAMES = Collections.singleton("");

    private static final Resource[] EMPTY_RESOURCES = {};

    private static final Comparator<File> FILE_COMPARATOR = Comparator.comparing(File::getAbsolutePath);

    private final List<PropertySourceLoader> propertySourceLoaders;

    private final Map<DocumentsCacheKey, List<Document>> loadDocumentsCache = new HashMap<>();

    protected Deque<String> profiles;

    private List<String> processedProfiles;

    private Map<String, MutablePropertySources> loaded;

    protected ConfigurableEnvironment environment;

    protected ResourceLoader resourceLoader;

    protected AbstractDbConfigFinder() {
        this.propertySourceLoaders = Arrays.asList(new PropertiesPropertySourceLoader(),
                new YamlPropertySourceLoader());
    }

    @Override
    public DbConfig find(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
        this.environment = environment != null ? environment : new StandardEnvironment();
        this.resourceLoader = resourceLoader != null ? resourceLoader : new DefaultResourceLoader();
        load();
        return null;
    }

    private void load() {
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
        try {
            Resource resource = this.resourceLoader.getResource(location);
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
            throw new IllegalStateException("Failed to load property " + "source from location '" + location + "'",
                    ex);
        }
    }

    /**
     * Post process the given loaded {@code Document}.
     * Sub class can customize this method.
     *
     * @param document the loaded {@code Document}
     */
    protected void postProcessDocument(Document document) {
    }

    /**
     * Initialize the profiles.
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
        List<Document> documents = this.loadDocumentsCache.get(cacheKey);
        if (documents == null) {
            List<PropertySource<?>> loaded = loader.load(name, resource);
            documents = asDocuments(loaded);
            this.loadDocumentsCache.put(cacheKey, documents);
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
    private static class Document {

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

        boolean match(Document document);

    }

    /**
     * Consumer used to handle a loaded {@link Document}.
     */
    @FunctionalInterface
    private interface DocumentConsumer {

        void accept(String profile, Document document);

    }

}
