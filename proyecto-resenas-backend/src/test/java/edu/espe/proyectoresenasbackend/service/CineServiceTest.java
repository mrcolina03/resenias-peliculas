package edu.espe.proyectoresenasbackend.service;

import edu.espe.proyectoresenasbackend.domain.Cine;
import edu.espe.proyectoresenasbackend.dto.CineRequest;
import edu.espe.proyectoresenasbackend.dto.CineResponse;
import edu.espe.proyectoresenasbackend.repository.CineRepository;
import edu.espe.proyectoresenasbackend.service.impl.CineServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CineServiceTest {

    private CineRepository cineRepository;
    private CineServiceImpl cineService;

    @BeforeEach
    void setUp() {
        cineRepository = Mockito.mock(CineRepository.class);
        cineService = new CineServiceImpl(cineRepository);
    }

    @Test
    void update_whenCineExists_shouldApplyAllIncomingFieldsAndReturnUpdatedDto() {
        // Arrange
        Cine cine = new Cine();
        cine.setId(4L);
        cine.setNombre("Cine Antiguo");

        CineRequest request = new CineRequest();
        request.setNombre("Cine Nuevo");
        request.setDireccion("Av. Siempre Viva");
        request.setCiudad("Quito");

        when(cineRepository.findById(4L)).thenReturn(Optional.of(cine));
        when(cineRepository.save(any(Cine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CineResponse response = cineService.update(4L, request);

        // Assert
        assertEquals("Cine Nuevo", response.getNombre());
        assertEquals("Av. Siempre Viva", response.getDireccion());
        assertEquals("Quito", response.getCiudad());
        verify(cineRepository).save(cine);
    }

    @Test
    void delete_whenIdIsProvided_shouldDelegateDeleteByIdToRepositoryExactlyOnce() {
        // Arrange
        Long cineId = 12L;

        // Act
        cineService.delete(cineId);

        // Assert
        verify(cineRepository, times(1)).deleteById(cineId);
    }
}
