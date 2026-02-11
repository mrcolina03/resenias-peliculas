package edu.espe.proyectoresenasbackend.service;

import edu.espe.proyectoresenasbackend.domain.ChatMessage;
import edu.espe.proyectoresenasbackend.domain.Usuario;
import edu.espe.proyectoresenasbackend.dto.ChatMessageDTO;
import edu.espe.proyectoresenasbackend.dto.ChatMessageRequest;
import edu.espe.proyectoresenasbackend.repository.ChatMessageRepository;
import edu.espe.proyectoresenasbackend.repository.UsuarioRepository;
import edu.espe.proyectoresenasbackend.service.impl.ChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    private ChatMessageRepository chatMessageRepository;
    private UsuarioRepository usuarioRepository;
    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        chatMessageRepository = Mockito.mock(ChatMessageRepository.class);
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        chatService = new ChatServiceImpl(chatMessageRepository, usuarioRepository);
    }

    @Test
    void sendMessage_whenUserExists_shouldSaveMessageAndReturnDtoWithExpectedData() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombreCompleto("Carlos");

        ChatMessageRequest request = new ChatMessageRequest();
        request.setUsuarioId(1L);
        request.setContenido("Hola chat");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage msg = invocation.getArgument(0);
            msg.setId(50L);
            return msg;
        });

        // Act
        ChatMessageDTO response = chatService.sendMessage(request).block();

        // Assert
        assertNotNull(response);
        assertEquals(50L, response.getId());
        assertEquals("Carlos", response.getUsuarioNombre());
        assertFalse(response.getEsSimulado());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    void getRecentMessages_whenRepositoryReturnsEntities_shouldMapEveryEntityToDtoList() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setNombreCompleto("Marta");

        ChatMessage message = new ChatMessage();
        message.setId(77L);
        message.setUsuario(usuario);
        message.setContenido("Mensaje reciente");
        message.setEsSimulado(true);

        when(chatMessageRepository.findTop50ByOrderByFechaEnvioDesc()).thenReturn(List.of(message));

        // Act
        List<ChatMessageDTO> messages = chatService.getRecentMessages().block();

        // Assert
        assertNotNull(messages);
        assertEquals(1, messages.size());
        assertEquals("Mensaje reciente", messages.getFirst().getContenido());
        assertTrue(messages.getFirst().getEsSimulado());
    }
}
