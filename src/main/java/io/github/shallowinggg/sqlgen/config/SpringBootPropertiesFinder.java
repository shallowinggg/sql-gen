package io.github.shallowinggg.sqlgen.config;

import java.util.List;
import java.util.Set;

/**
 * @author ding shimin
 */
public class SpringBootPropertiesFinder extends AbstractPropertiesFinder {

    @Override
    public String name() {
        return null;
    }

    @Override
    public Set<String> getSearchLocations() {
        return null;
    }

    @Override
    public String getSearchName() {
        return null;
    }

    @Override
    public List<DbConfigProperties> getDbConfigProperties() {
        return null;
    }

    @Override
    public boolean isCandidate() {
        return false;
    }

    protected SpringBootPropertiesFinder() {
        super(null, null);
    }
}
