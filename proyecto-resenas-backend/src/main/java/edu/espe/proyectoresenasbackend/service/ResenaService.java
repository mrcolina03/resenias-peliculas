package edu.espe.proyectoresenasbackend.service;

import edu.espe.proyectoresenasbackend.dto.ResenaRequest;
import edu.espe.proyectoresenasbackend.dto.ResenaResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ResenaService {
    Mono<ResenaResponse> create(ResenaRequest request);
    Mono<ResenaResponse> get(Long id);
    Flux<ResenaResponse> list();
    Mono<ResenaResponse> update(Long id, ResenaRequest request);
    Mono<Void> delete(Long id);
    Flux<ResenaResponse> listByPeliculaId(Long peliculaId);
    Flux<ResenaResponse> streamByPeliculaId(Long peliculaId);

    List<String> procesarCalificacionesPorLotes();
}
