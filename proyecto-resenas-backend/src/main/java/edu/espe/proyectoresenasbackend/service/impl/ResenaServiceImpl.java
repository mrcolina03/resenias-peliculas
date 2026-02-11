package edu.espe.proyectoresenasbackend.service.impl;

import edu.espe.proyectoresenasbackend.domain.Pelicula;
import edu.espe.proyectoresenasbackend.domain.Resena;
import edu.espe.proyectoresenasbackend.domain.Usuario;
import edu.espe.proyectoresenasbackend.dto.ResenaRequest;
import edu.espe.proyectoresenasbackend.dto.ResenaResponse;
import edu.espe.proyectoresenasbackend.repository.PeliculaRepository;
import edu.espe.proyectoresenasbackend.repository.ResenaRepository;
import edu.espe.proyectoresenasbackend.repository.UsuarioRepository;
import edu.espe.proyectoresenasbackend.service.ResenaService;
import edu.espe.proyectoresenasbackend.util.CustomSubscriber;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;

@Service
public class ResenaServiceImpl implements ResenaService {
    private final ResenaRepository repository;
    private final UsuarioRepository usuarioRepo;
    private final PeliculaRepository peliculaRepo;
    private final Sinks.Many<ResenaResponse> resenaSink;

    public ResenaServiceImpl(ResenaRepository repository, UsuarioRepository usuarioRepo, PeliculaRepository peliculaRepo) {
        this.repository = repository;
        this.usuarioRepo = usuarioRepo;
        this.peliculaRepo = peliculaRepo;
        this.resenaSink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @Override
    public Mono<ResenaResponse> create(ResenaRequest request) {
        return Mono.fromCallable(() -> {
            Usuario usuario = usuarioRepo.findById(request.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Pelicula pelicula = peliculaRepo.findById(request.getPeliculaId())
                    .orElseThrow(() -> new RuntimeException("Película no encontrada"));

            Resena r = new Resena();
            r.setComentario(request.getComentario());
            r.setCalificacion(request.getCalificacion());
            r.setUsuario(usuario);
            r.setPelicula(pelicula);

            return repository.save(r);
        }).map(this::toResponse)
                .doOnNext(resenaSink::tryEmitNext);
    }

    @Override
    public Mono<ResenaResponse> get(Long id) {
        return Mono.fromCallable(() -> repository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Reseña no encontrada")))
                .map(this::toResponse);
    }

    @Override
    public Flux<ResenaResponse> list() {
        return Flux.defer(() -> Flux.fromIterable(repository.findAll()))
                .map(this::toResponse);
    }

    @Override
    public Mono<ResenaResponse> update(Long id, ResenaRequest request) {
        return Mono.fromCallable(() -> {
            Resena r = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));

            Usuario usuario = usuarioRepo.findById(request.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Pelicula pelicula = peliculaRepo.findById(request.getPeliculaId())
                    .orElseThrow(() -> new RuntimeException("Película no encontrada"));

            r.setComentario(request.getComentario());
            r.setCalificacion(request.getCalificacion());
            r.setUsuario(usuario);
            r.setPelicula(pelicula);

            return repository.save(r);
        }).map(this::toResponse)
                .doOnNext(resenaSink::tryEmitNext);
    }

    @Override
    public Mono<Void> delete(Long id) {
        return Mono.fromRunnable(() -> repository.deleteById(id));
    }

    @Override
    public Flux<ResenaResponse> listByPeliculaId(Long peliculaId) {
        return Flux.defer(() -> Flux.fromIterable(repository.findByPeliculaId(peliculaId)))
                .map(this::toResponse);
    }

    @Override
    public Flux<ResenaResponse> streamByPeliculaId(Long peliculaId) {
        return resenaSink.asFlux()
                .filter(resena -> peliculaId.equals(resena.getPeliculaId()));
    }

    @Override
    public List<String> procesarCalificacionesPorLotes() {
        Flux<Integer> calificacionesFlux = Flux.fromIterable(repository.findAll())
                .map(Resena::getCalificacion);

        CustomSubscriber subscriber = new CustomSubscriber(3);
        calificacionesFlux.subscribe(subscriber);

        return subscriber.getLogs();
    }

    private ResenaResponse toResponse(Resena r) {
        ResenaResponse resp = new ResenaResponse();
        resp.setId(r.getId());
        resp.setComentario(r.getComentario());
        resp.setCalificacion(r.getCalificacion());
        resp.setFechaCreacion(r.getFechaCreacion());

        if (r.getUsuario() != null) {
            resp.setUsuarioId(r.getUsuario().getId());
            resp.setUsuarioNombre(r.getUsuario().getNombreCompleto());
        }

        if (r.getPelicula() != null) {
            resp.setPeliculaId(r.getPelicula().getId());
        }
        return resp;
    }
}
