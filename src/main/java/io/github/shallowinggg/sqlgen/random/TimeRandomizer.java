package io.github.shallowinggg.sqlgen.random;

import io.github.shallowinggg.sqlgen.util.Assert;

import javax.annotation.concurrent.ThreadSafe;
import java.sql.Time;
import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;

import static io.github.shallowinggg.sqlgen.util.Assert.notNull;

/**
 * Randomizer for type {@link Time}.
 * <p>
 * This class is stateless and thread safe.
 *
 * @author ding shimin
 * @since 1.0
 */
@ThreadSafe
public class TimeRandomizer extends AbstractTypedRandomizer<Time> {

    private final long origin;
    private final long bound;

    public TimeRandomizer() {
        this(0, 24 * 3600 * 1000);
    }

    public TimeRandomizer(java.util.Date min, java.util.Date max) {
        this(notNull(min, "min must not be null").getTime(),
                notNull(max, "max must not be null").getTime());
    }

    public TimeRandomizer(LocalTime min, LocalTime max) {
        this(notNull(min, "min must not be null").toSecondOfDay() * 1000,
                notNull(max, "max must not be null").toSecondOfDay() * 1000);
    }

    public TimeRandomizer(long min, long max) {
        Assert.isTrue(min < max, String.format("min must be less than max, actual: min [%d], max [%d]", min, max));

        this.origin = min;
        this.bound = max;
    }

    @Override
    public Time nextValue() {
        return new Time(ThreadLocalRandom.current().nextLong(origin, bound));
    }
}
