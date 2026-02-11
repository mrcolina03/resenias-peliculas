package edu.espe.proyectoresenasbackend.service;

import edu.espe.proyectoresenasbackend.domain.Pelicula;
import edu.espe.proyectoresenasbackend.domain.Resena;
import edu.espe.proyectoresenasbackend.domain.Usuario;
import edu.espe.proyectoresenasbackend.dto.ResenaRequest;
import edu.espe.proyectoresenasbackend.dto.ResenaResponse;
import edu.espe.proyectoresenasbackend.repository.PeliculaRepository;
import edu.espe.proyectoresenasbackend.repository.ResenaRepository;
import edu.espe.proyectoresenasbackend.repository.UsuarioRepository;
import edu.espe.proyectoresenasbackend.service.impl.ResenaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResenaServiceTest {

    private ResenaRepository resenaRepository;
    private UsuarioRepository usuarioRepository;
    private PeliculaRepository peliculaRepository;
    private ResenaServiceImpl resenaService;

    @BeforeEach
    void setUp() {
        resenaRepository = Mockito.mock(ResenaRepository.class);
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        peliculaRepository = Mockito.mock(PeliculaRepository.class);
        resenaService = new ResenaServiceImpl(resenaRepository, usuarioRepository, peliculaRepository);
    }

    @Test
    void create_whenRequestIsValid_shouldPersistReviewAndPublishItToMovieLiveStream() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombreCompleto("Ana");

        Pelicula pelicula = new Pelicula();
        pelicula.setId(9L);

        ResenaRequest request = new ResenaRequest();
        request.setUsuarioId(1L);
        request.setPeliculaId(9L);
        request.setComentario("Gran película");
        request.setCalificacion(5);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(peliculaRepository.findById(9L)).thenReturn(Optional.of(pelicula));
        when(resenaRepository.save(any(Resena.class))).thenAnswer(invocation -> {
            Resena saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        // Act
        ResenaResponse created = resenaService.create(request).block();
        ResenaResponse streamed = resenaService.streamByPeliculaId(9L).blockFirst();

        // Assert
        assertNotNull(created);
        assertEquals(100L, created.getId());
        assertNotNull(streamed);
        assertEquals(100L, streamed.getId());
        assertEquals(9L, streamed.getPeliculaId());
        verify(resenaRepository).save(any(Resena.class));
    }

    @Test
    void get_whenReviewDoesNotExist_shouldThrowRuntimeExceptionWithNotFoundMessage() {
        // Arrange
        when(resenaRepository.findById(404L)).thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> resenaService.get(404L).block());
        assertEquals("Reseña no encontrada", exception.getMessage());
        verify(resenaRepository).findById(404L);
    }
}
