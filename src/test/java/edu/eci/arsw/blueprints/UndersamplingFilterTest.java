package edu.eci.arsw.blueprints;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import edu.eci.arsw.blueprints.filters.UndersamplingFilter;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;

class UndersamplingFilterTest {

    private final UndersamplingFilter filter = new UndersamplingFilter();

    @Test
    void shouldKeepOnlyEvenIndexPoints() {
        Blueprint bp = new Blueprint("john", "house",
                List.of(new Point(0,0), new Point(1,1), new Point(2,2), new Point(3,3)));
        Blueprint result = filter.apply(bp);
        assertEquals(2, result.getPoints().size());
        assertEquals(new Point(0,0), result.getPoints().get(0));
        assertEquals(new Point(2,2), result.getPoints().get(1));
    }

    @Test
    void shouldHandleEmptyPointList() {
        Blueprint bp = new Blueprint("john", "house", List.of());
        assertTrue(filter.apply(bp).getPoints().isEmpty());
    }
}