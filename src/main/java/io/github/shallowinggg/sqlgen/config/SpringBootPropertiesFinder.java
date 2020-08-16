package io.github.shallowinggg.sqlgen.config;

import java.util.List;

/**
 * @author ding shimin
 */
public class SpringBootPropertiesFinder extends AbstractPropertiesFinder {

    @Override
    public List<String> getSearchLocations() {
        return null;
    }

    @Override
    public List<String> getSearchNames() {
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
