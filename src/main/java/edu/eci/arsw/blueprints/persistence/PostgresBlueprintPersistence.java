package edu.eci.arsw.blueprints.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;


@Primary
@Repository
public class PostgresBlueprintPersistence implements BlueprintPersistence {
    private final JdbcTemplate jdbcTemplate;

    public PostgresBlueprintPersistence(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM blueprints WHERE author = ? AND name = ?",
                Integer.class,
                bp.getAuthor(),
                bp.getName()
        );

        if (count != null && count > 0) {
            throw new BlueprintPersistenceException("Blueprint already exists: %s/%s".formatted(bp.getAuthor(), bp.getName()));
        }

        jdbcTemplate.update(
                "INSERT INTO blueprints (author, name) VALUES (?, ?)",
                bp.getAuthor(),
                bp.getName()
        );

        String insertPointSql = "INSERT INTO blueprint_points (author, name, x, y, point_order) VALUES (?, ?, ?, ?, ?)";
        for (int i = 0; i < bp.getPoints().size(); i++) {
            Point point = bp.getPoints().get(i);
            jdbcTemplate.update(insertPointSql, bp.getAuthor(), bp.getName(), point.x(), point.y(), i);
        }
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM blueprints WHERE author = ? AND name = ?",
                Integer.class,
                author,
                name
        );

        if (count == null || count == 0) {
            throw new BlueprintNotFoundException("Blueprint not found: %s/%s".formatted(author, name));
        }

        return new Blueprint(author, name, getPoints(author, name));
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        List<String> names = jdbcTemplate.queryForList(
                "SELECT name FROM blueprints WHERE author = ? ORDER BY name",
                String.class,
                author
        );

        if (names.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }

        Set<Blueprint> result = new HashSet<>();
        for (String name : names) {
            result.add(getBlueprint(author, name));
        }
        return result;
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        List<Blueprint> blueprints = jdbcTemplate.query(
                "SELECT author, name FROM blueprints ORDER BY author, name",
                (rs, rowNum) -> new Blueprint(
                        rs.getString("author"),
                        rs.getString("name"),
                        getPoints(rs.getString("author"), rs.getString("name"))
                )
        );
        return new HashSet<>(blueprints);
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM blueprints WHERE author = ? AND name = ?",
                Integer.class,
                author,
                name
        );

        if (count == null || count == 0) {
            throw new BlueprintNotFoundException("Blueprint not found: %s/%s".formatted(author, name));
        }

        Integer nextOrder = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(point_order), -1) + 1 FROM blueprint_points WHERE author = ? AND name = ?",
                Integer.class,
                author,
                name
        );

        jdbcTemplate.update(
                "INSERT INTO blueprint_points (author, name, x, y, point_order) VALUES (?, ?, ?, ?, ?)",
                author,
                name,
                x,
                y,
                nextOrder == null ? 0 : nextOrder
        );
    }

    private List<Point> getPoints(String author, String name) {
        return jdbcTemplate.query(
                "SELECT x, y FROM blueprint_points WHERE author = ? AND name = ? ORDER BY point_order",
                (rs, rowNum) -> new Point(rs.getInt("x"), rs.getInt("y")),
                author,
                name
        );
    }
}
