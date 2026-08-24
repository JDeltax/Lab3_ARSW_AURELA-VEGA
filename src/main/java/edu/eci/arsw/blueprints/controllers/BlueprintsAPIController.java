package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import edu.eci.arsw.blueprints.dto.ApiResponse;
import java.util.Map;
import java.util.Set;

@RestController
//CAMBIÉ EL PATH COMO LO SOLICITAN MI PRI
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    // GET /blueprints
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        ApiResponse<Set<Blueprint>> response = ApiResponse.success(
                services.getAllBlueprints(),
                HttpStatus.OK.value(),
                "All blueprints retrieved successfully"
        );
        return ResponseEntity.ok(response);
    }

    // GET /blueprints/{author}
    @GetMapping("/{author}")
    public ResponseEntity<?> byAuthor(@PathVariable String author) {
        try {
            ApiResponse<Set<Blueprint>> response = ApiResponse.success(services.getBlueprintsByAuthor(author), HttpStatus.OK.value(), "Blueprints retrieved successfully");
            return ResponseEntity.ok(response);

        } catch (BlueprintNotFoundException e) {
            ApiResponse<String> errorResponse = ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value(), "Blueprints not found for author: " + author);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // GET /blueprints/{author}/{bpname}
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<?> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            ApiResponse<Blueprint> response = ApiResponse.success(services.getBlueprint(author, bpname), HttpStatus.OK.value(), "Blueprint retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (BlueprintNotFoundException e) {
            ApiResponse<String> errorResponse = ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value(), "Blueprint not found for author: " + author + " and name: " + bpname);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // POST /blueprints
    @PostMapping
    public ResponseEntity<?> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            ApiResponse<String> response = ApiResponse.success("Blueprint created successfully", HttpStatus.CREATED.value(), "Blueprint created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BlueprintPersistenceException e) {
            ApiResponse<String> errorResponse = ApiResponse.error(e.getMessage(), HttpStatus.FORBIDDEN.value(), "Error creating blueprint");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
    }

    // PUT /blueprints/{author}/{bpname}/points
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<?> addPoint(@PathVariable String author, @PathVariable String bpname,
                                      @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            ApiResponse<String> response = ApiResponse.success("Point added successfully", HttpStatus.ACCEPTED.value(), "Point added successfully");
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (BlueprintNotFoundException e) {
            ApiResponse <String> errorResponse = ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value(), "Blueprint not found for author: " + author + " and name: " + bpname);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points
    ) { }
}
