package edu.espe.proyectoresenasbackend.web.controller;

import edu.espe.proyectoresenasbackend.dto.ResenaRequest;
import edu.espe.proyectoresenasbackend.dto.ResenaResponse;
import edu.espe.proyectoresenasbackend.service.ResenaService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/resenas/resena")
public class ResenaController {
    private final ResenaService service;

    public ResenaController(ResenaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ResenaResponse> create(@RequestBody ResenaRequest request) {
        ResenaResponse response = service.create(request).block();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResenaResponse> get(@PathVariable Long id) {
        ResenaResponse response = service.get(id).block();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ResenaResponse>> list() {
        List<ResenaResponse> response = service.list().collectList().block();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResenaResponse> update(@PathVariable Long id, @RequestBody ResenaRequest request) {
        ResenaResponse response = service.update(id, request).block();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id).block();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pelicula/{peliculaId}")
    public ResponseEntity<List<ResenaResponse>> listByPelicula(@PathVariable Long peliculaId) {
        List<ResenaResponse> response = service.listByPeliculaId(peliculaId).collectList().block();
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/stream/pelicula/{peliculaId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ResenaResponse>> streamByPelicula(@PathVariable Long peliculaId) {
        return service.streamByPeliculaId(peliculaId)
                .map(resena -> ServerSentEvent.<ResenaResponse>builder()
                        .event("resena-nueva")
                        .data(resena)
                        .build());
    }

    @PostMapping("/demo-backpressure")
    public ResponseEntity<List<String>> demoBackpressure() {
        List<String> logs = service.procesarCalificacionesPorLotes();
        return ResponseEntity.ok(logs);
    }
}
