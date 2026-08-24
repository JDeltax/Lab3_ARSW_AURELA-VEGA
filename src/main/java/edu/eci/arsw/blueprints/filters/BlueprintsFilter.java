package edu.eci.arsw.blueprints.filters;

import java.util.Set;

import edu.eci.arsw.blueprints.model.Blueprint;

public interface BlueprintsFilter {
    Blueprint apply(Blueprint bp);
    Set<Blueprint> filter(Set<Blueprint> bps);
}
