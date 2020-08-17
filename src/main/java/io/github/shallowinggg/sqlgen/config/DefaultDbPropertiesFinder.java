package io.github.shallowinggg.sqlgen.config;

import java.util.List;

/**
 * @author ding shimin
 */
public class DefaultDbPropertiesFinder extends AbstractDbPropertiesFinder {

    public DefaultDbPropertiesFinder() {
        super(null, null);
    }

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
}
