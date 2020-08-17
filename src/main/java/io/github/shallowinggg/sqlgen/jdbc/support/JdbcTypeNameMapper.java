package io.github.shallowinggg.sqlgen.jdbc.support;

import io.github.shallowinggg.sqlgen.SqlGenException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * (Badly named) helper for dealing with standard JDBC types as defined by {@link java.sql.Types}
 * <p>
 * Hibernate, Relational Persistence for Idiomatic Java
 * <p>
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 *
 * @author Steve Ebersole
 */
public class JdbcTypeNameMapper {
    private static final Log logger = LogFactory.getLog(JdbcTypeNameMapper.class);

    private static final Map<Integer, String> JDBC_TYPE_MAP = buildJdbcTypeMap();

    private static Map<Integer, String> buildJdbcTypeMap() {
        HashMap<Integer, String> map = new HashMap<>(64);
        Field[] fields = java.sql.Types.class.getFields();
        for (Field field : fields) {
            try {
                final int code = field.getInt(null);
                String old = map.put(code, field.getName());
                if (old != null) {
                    logger.info(String.format("java.sql.Types mapped the same code [%s] multiple times; was [%s]; now [%s]",
                            code, old, field.getName()));
                }
            } catch (IllegalAccessException e) {
                throw new SqlGenException("Unable to access JDBC type mapping [" + field.getName() + "]", e);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Determine whether the given JDBC type code represents a standard JDBC type ("standard" being those defined on
     * {@link java.sql.Types}).
     * <p>
     * NOTE : {@link java.sql.Types#OTHER} is also "filtered out" as being non-standard.
     *
     * @param typeCode The JDBC type code to check
     * @return {@code true} to indicate the type code is a standard type code; {@code false} otherwise.
     */
    public static boolean isStandardTypeCode(int typeCode) {
        return isStandardTypeCode(Integer.valueOf(typeCode));
    }

    /**
     * Same as call to {@link #isStandardTypeCode(int)}
     *
     * @see #isStandardTypeCode(int)
     */
    public static boolean isStandardTypeCode(Integer typeCode) {
        return JDBC_TYPE_MAP.containsKey(typeCode);
    }

    /**
     * Get the type name as in the static field names defined on {@link java.sql.Types}.  If a type code is not
     * recognized, it is reported as {@code UNKNOWN(?)} where '?' is replace with the given type code.
     * <p>
     * Intended as useful for logging purposes...
     *
     * @param typeCode The type code to find the name for.
     * @return The type name.
     */
    public static String getTypeName(Integer typeCode) {
        String name = JDBC_TYPE_MAP.get(typeCode);
        if (name == null) {
            return "UNKNOWN(" + typeCode + ")";
        }
        return name;
    }

    private JdbcTypeNameMapper() {
    }

}
