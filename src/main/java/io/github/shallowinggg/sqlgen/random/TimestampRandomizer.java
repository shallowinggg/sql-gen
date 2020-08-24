package io.github.shallowinggg.sqlgen.random;

import io.github.shallowinggg.sqlgen.util.Assert;

import javax.annotation.concurrent.ThreadSafe;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static io.github.shallowinggg.sqlgen.util.Assert.notNull;

/**
 * Randomizer for type {@link Timestamp}.
 * <p>
 * This class is stateless and thread safe.
 *
 * @author ding shimin
 * @since 1.0
 */
@ThreadSafe
public class TimestampRandomizer extends AbstractTypedRandomizer<Timestamp> {

    private static final long DEFAULT_OFFSET = TimeUnit.DAYS.toMillis(30);

    private final long origin;
    private final long bound;

    public TimestampRandomizer() {
        long now = System.currentTimeMillis();
        this.origin = now - DEFAULT_OFFSET;
        this.bound = now;
    }

    public TimestampRandomizer(java.util.Date min, java.util.Date max) {
        this(notNull(min, "min must not be null").getTime(),
                notNull(max, "max must not be null").getTime());
    }

    public TimestampRandomizer(LocalDateTime min, LocalDateTime max) {
        this(min, max, ZoneId.systemDefault());
    }

    public TimestampRandomizer(LocalDateTime min, LocalDateTime max, ZoneId zoneId) {
        this(notNull(min, "min must not be null").atZone(
                notNull(zoneId, "zoneId must not be null")).toInstant().toEpochMilli(),
                notNull(max, "max must not be null").atZone(zoneId).toInstant().toEpochMilli());
    }

    public TimestampRandomizer(long min, long max) {
        Assert.isTrue(min < max, String.format("min must be less than max, actual: min [%d], max [%d]", min, max));

        this.origin = min;
        this.bound = max;
    }

    @Override
    public Timestamp nextValue() {
        return new Timestamp(ThreadLocalRandom.current().nextLong(origin, bound));
    }
}
