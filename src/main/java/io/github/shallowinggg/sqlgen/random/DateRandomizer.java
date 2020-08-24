package io.github.shallowinggg.sqlgen.random;

import io.github.shallowinggg.sqlgen.util.Assert;

import javax.annotation.concurrent.ThreadSafe;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static io.github.shallowinggg.sqlgen.util.Assert.notNull;

/**
 * Randomizer for type {@link java.sql.Date}.
 * <p>
 * This class is stateless and thread safe.
 *
 * @author ding shimin
 * @since 1.0
 */
@ThreadSafe
public class DateRandomizer extends AbstractTypedRandomizer<java.sql.Date> {

    private static final long DEFAULT_OFFSET = TimeUnit.DAYS.toMillis(30);

    private final long origin;
    private final long bound;

    public DateRandomizer() {
        long now = System.currentTimeMillis();
        this.origin = now - DEFAULT_OFFSET;
        this.bound = now;
    }

    public DateRandomizer(java.util.Date min, java.util.Date max) {
        this(notNull(min, "min must not be null").getTime(),
                notNull(max, "max must not be null").getTime());
    }

    public DateRandomizer(LocalDate min, LocalDate max) {
        this(min, max, ZoneId.systemDefault());
    }

    public DateRandomizer(LocalDate min, LocalDate max, ZoneId zoneId) {
        this(notNull(min, "min must not be null").atStartOfDay(
                notNull(zoneId, "zoneId must not be null")).toInstant().toEpochMilli(),
                notNull(max, "max must not be null").atStartOfDay(zoneId).toInstant().toEpochMilli());
    }

    public DateRandomizer(long min, long max) {
        Assert.isTrue(min < max, String.format("min must be less than max, actual: min [%d], max [%d]", min, max));

        this.origin = min;
        this.bound = max;
    }

    @Override
    public Date nextValue() {
        return new java.sql.Date(ThreadLocalRandom.current().nextLong(origin, bound));
    }
}
