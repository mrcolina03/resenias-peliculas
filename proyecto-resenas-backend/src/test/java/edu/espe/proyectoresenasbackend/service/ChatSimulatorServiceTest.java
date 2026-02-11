package edu.espe.proyectoresenasbackend.service;

import edu.espe.proyectoresenasbackend.domain.Usuario;
import edu.espe.proyectoresenasbackend.repository.UsuarioRepository;
import edu.espe.proyectoresenasbackend.service.impl.ChatSimulatorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatSimulatorServiceTest {

    private UsuarioRepository usuarioRepository;
    private ChatService chatService;
    private PasswordEncoder passwordEncoder;
    private ChatSimulatorServiceImpl chatSimulatorService;

    @BeforeEach
    void setUp() {
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        chatService = Mockito.mock(ChatService.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        chatSimulatorService = new ChatSimulatorServiceImpl(usuarioRepository, chatService, passwordEncoder);

        when(passwordEncoder.encode(any(String.class))).thenReturn("hash-bot");
        when(usuarioRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(Math.abs(usuario.getEmail().hashCode()) + 0L);
            return usuario;
        });
        when(chatService.deleteSimulatedMessages()).thenReturn(Mono.empty());
    }

    @Test
    void startSimulation_whenSimulationIsNotRunning_shouldCreateBotsAndReturnSuccessPayload() {
        // Arrange
        // (mocks definidos en setUp)

        // Act
        Map<String, Object> result = chatSimulatorService.startSimulation();

        // Assert
        assertEquals(true, result.get("success"));
        assertEquals(10, result.get("activeUsers"));
        assertTrue((result.get("message")).toString().contains("iniciada"));
        verify(usuarioRepository, atLeast(10)).save(any(Usuario.class));

        Map<String, Object> stopResult = chatSimulatorService.stopSimulation();
        assertEquals(true, stopResult.get("success"));
    }

    @Test
    void stopSimulation_whenNoSimulationIsRunning_shouldReturnFailureMessageWithoutCallingCleanup() {
        // Arrange
        // simulación nunca iniciada

        // Act
        Map<String, Object> result = chatSimulatorService.stopSimulation();

        // Assert
        assertEquals(false, result.get("success"));
        assertTrue(result.get("message").toString().contains("No hay simulación"));
        verify(chatService, never()).deleteSimulatedMessages();
    }
}
