package edu.espe.proyectoresenasbackend.service;

import edu.espe.proyectoresenasbackend.domain.Usuario;
import edu.espe.proyectoresenasbackend.dto.UsuarioRequestData;
import edu.espe.proyectoresenasbackend.dto.UsuarioResponse;
import edu.espe.proyectoresenasbackend.repository.UsuarioRepository;
import edu.espe.proyectoresenasbackend.service.impl.UsuarioServiceImpl;
import edu.espe.proyectoresenasbackend.web.advice.ConflictException;
import edu.espe.proyectoresenasbackend.web.advice.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        usuarioService = new UsuarioServiceImpl(usuarioRepository, passwordEncoder);
    }

    @Test
    void create_whenEmailAlreadyExists_shouldThrowConflictExceptionAndSkipSaveOperation() {
        // Arrange
        UsuarioRequestData request = new UsuarioRequestData();
        request.setEmail("repetido@correo.com");
        when(usuarioRepository.existsByEmail("repetido@correo.com")).thenReturn(true);

        // Act + Assert
        assertThrows(ConflictException.class, () -> usuarioService.create(request));
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void update_whenPasswordIsPresent_shouldEncodePasswordAndPersistUserWithNewValues() {
        // Arrange
        Usuario existente = new Usuario();
        existente.setId(20L);
        existente.setEmail("viejo@correo.com");
        existente.setContrasena("hash-antiguo");

        UsuarioRequestData request = new UsuarioRequestData();
        request.setNombreCompleto("Nuevo Nombre");
        request.setEmail("nuevo@correo.com");
        request.setContrasena("123456");
        request.setActivo(true);

        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByEmail("nuevo@correo.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hash-nuevo");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UsuarioResponse response = usuarioService.update(20L, request);

        // Assert
        assertEquals("Nuevo Nombre", response.getNombreCompleto());
        assertEquals("nuevo@correo.com", response.getEmail());
        verify(passwordEncoder).encode("123456");
        verify(usuarioRepository).save(existente);
    }

    @Test
    void delete_whenUserDoesNotExist_shouldThrowNotFoundExceptionWithoutDeletingAnything() {
        // Arrange
        when(usuarioRepository.existsById(77L)).thenReturn(false);

        // Act + Assert
        assertThrows(NotFoundException.class, () -> usuarioService.delete(77L));
        verify(usuarioRepository, never()).deleteById(anyLong());
    }
}
