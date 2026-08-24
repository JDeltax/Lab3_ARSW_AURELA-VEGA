package edu.eci.arsw.blueprints.controllers;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.eci.arsw.blueprints.dto.ApiResponse;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1/blueprints")
@Tag(name = "Blueprints Management", description = "Endpoints para consulta, creación y modificación de planos")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { 
        this.services = services; 
    }

    @Operation(summary = "Obtener todos los planos", description = "Retorna la lista de todos los planos registrados en el sistema.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Consulta exitosa",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        ApiResponse<Set<Blueprint>> response = ApiResponse.success(
                services.getAllBlueprints(),
                HttpStatus.OK.value(),
                "All blueprints retrieved successfully"
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener planos por autor", description = "Retorna todos los planos asociados a un autor específico.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Planos encontrados"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "No se encontraron planos para el autor especificado")
    })
    @GetMapping("/{author}")
    public ResponseEntity<?> byAuthor(
            @Parameter(description = "Nombre del autor", example = "john") 
            @PathVariable String author) {
        try {
            ApiResponse<Set<Blueprint>> response = ApiResponse.success(
                services.getBlueprintsByAuthor(author), 
                HttpStatus.OK.value(), 
                "Blueprints retrieved successfully"
            );
            return ResponseEntity.ok(response);
        } catch (BlueprintNotFoundException e) {
            ApiResponse<String> errorResponse = ApiResponse.error(
                e.getMessage(), 
                HttpStatus.NOT_FOUND.value(), 
                "Blueprints not found for author: " + author
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @Operation(summary = "Obtener plano por autor y nombre", description = "Consulta un único plano mediante el nombre del autor y el nombre del plano.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Plano encontrado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Plano no existente")
    })
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<?> byAuthorAndName(
            @Parameter(description = "Nombre del autor", example = "john") @PathVariable String author, 
            @Parameter(description = "Nombre del plano", example = "house") @PathVariable String bpname) {
        try {
            ApiResponse<Blueprint> response = ApiResponse.success(
                services.getBlueprint(author, bpname), 
                HttpStatus.OK.value(), 
                "Blueprint retrieved successfully"
            );
            return ResponseEntity.ok(response);
        } catch (BlueprintNotFoundException e) {
            ApiResponse<String> errorResponse = ApiResponse.error(
                e.getMessage(), 
                HttpStatus.NOT_FOUND.value(), 
                "Blueprint not found for author: " + author + " and name: " + bpname
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @Operation(summary = "Registrar un nuevo plano", description = "Crea un plano indicando autor, nombre y puntos.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "Plano creado exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "El plano ya existe en el sistema"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Datos de entrada inválidos")
    })
    @PostMapping
    public ResponseEntity<?> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            ApiResponse<String> response = ApiResponse.success(
                "Blueprint created successfully", 
                HttpStatus.CREATED.value(), 
                "Blueprint created successfully"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BlueprintPersistenceException e) {
            ApiResponse<String> errorResponse = ApiResponse.error(
                e.getMessage(), 
                HttpStatus.FORBIDDEN.value(), 
                "Error creating blueprint"
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
    }

    @Operation(summary = "Agregar un punto a un plano", description = "Añade un nuevo par de coordenadas (x, y) a un plano existente.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "202", 
            description = "Punto agregado exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Plano no encontrado")
    })
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<?> addPoint(
            @Parameter(description = "Nombre del autor", example = "john") @PathVariable String author, 
            @Parameter(description = "Nombre del plano", example = "house") @PathVariable String bpname,
            @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            ApiResponse<String> response = ApiResponse.success(
                "Point added successfully", 
                HttpStatus.ACCEPTED.value(), 
                "Point added successfully"
            );
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (BlueprintNotFoundException e) {
            ApiResponse<String> errorResponse = ApiResponse.error(
                e.getMessage(), 
                HttpStatus.NOT_FOUND.value(), 
                "Blueprint not found for author: " + author + " and name: " + bpname
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points
    ) { }
}