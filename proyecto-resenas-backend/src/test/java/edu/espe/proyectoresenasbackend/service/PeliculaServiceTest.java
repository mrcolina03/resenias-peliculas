package edu.espe.proyectoresenasbackend.service;

import edu.espe.proyectoresenasbackend.domain.Pelicula;
import edu.espe.proyectoresenasbackend.dto.PeliculaRequest;
import edu.espe.proyectoresenasbackend.dto.PeliculaResponse;
import edu.espe.proyectoresenasbackend.repository.PeliculaRepository;
import edu.espe.proyectoresenasbackend.service.impl.PeliculaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PeliculaServiceTest {

    private PeliculaRepository peliculaRepository;
    private PeliculaServiceImpl peliculaService;

    @BeforeEach
    void setUp() {
        peliculaRepository = Mockito.mock(PeliculaRepository.class);
        peliculaService = new PeliculaServiceImpl(peliculaRepository);
    }

    @Test
    void create_whenRequestIsValid_shouldMapEntitySaveAndReturnMappedResponse() {
        // Arrange
        PeliculaRequest request = new PeliculaRequest();
        request.setTitulo("Interstellar");
        request.setDirector("Christopher Nolan");
        request.setGenero("Ciencia ficción");
        request.setDuracionMinutos(169);
        request.setFechaEstreno(LocalDate.of(2014, 11, 7));

        when(peliculaRepository.save(any(Pelicula.class))).thenAnswer(invocation -> {
            Pelicula pelicula = invocation.getArgument(0);
            pelicula.setId(10L);
            return pelicula;
        });

        // Act
        PeliculaResponse response = peliculaService.create(request);

        // Assert
        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Interstellar", response.getTitulo());
        assertEquals(169, response.getDuracionMinutos());
        verify(peliculaRepository).save(any(Pelicula.class));
    }

    @Test
    void get_whenMovieDoesNotExist_shouldThrowRuntimeExceptionWithExpectedMessage() {
        // Arrange
        when(peliculaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> peliculaService.get(99L));
        assertEquals("Película no encontrada", exception.getMessage());
        verify(peliculaRepository).findById(99L);
    }
}
