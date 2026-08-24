package edu.eci.arsw.blueprints.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;

/**
 * Elimina puntos consecutivos duplicados (x,y) para reducir redundancia.
 * Perfil: "redundancy"
 */
@Component
@Profile("redundancy")
public class RedundancyFilter implements BlueprintsFilter {

    @Override
    public Blueprint apply(Blueprint bp) {
        List<Point> in = bp.getPoints();
        if (in == null || in.isEmpty()) return bp;

        List<Point> out = new ArrayList<>();
        Point prev = null;
        for (Point p : in) {
            if (prev == null || !(prev.x() == p.x() && prev.y() == p.y())) {
                out.add(p);
                prev = p;
            }
        }
        return new Blueprint(bp.getAuthor(), bp.getName(), out);
    }

    @Override
    public Set<Blueprint> filter(Set<Blueprint> bps) {
        if (bps == null) return Set.of();
        return bps.stream()
                .map(this::apply)
                .collect(Collectors.toSet());
    }
}