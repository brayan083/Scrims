// Fichero: src/test/java/.../model/patterns/state/BuscandoJugadoresStateTest.java
package com.uade.tpo.Scrims.model.patterns.state;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.Team;
import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.model.infrastructure.persistence.TeamRepository;
import com.uade.tpo.Scrims.model.patterns.observer.DomainEventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita la magia de Mockito
class BuscandoJugadoresStateTest {

    @Mock // Mockito creará un objeto "falso" de esta dependencia
    private TeamRepository teamRepository;

    @Mock // También creamos un mock del Event Bus para verificar que se llama
    private DomainEventBus eventBus;

    @InjectMocks // Crea una instancia REAL de BuscandoJugadoresState e inyecta los @Mock de
                 // arriba en ella
    private BuscandoJugadoresState buscandoJugadoresState;

    @Test
    void aceptarPostulacion_WhenLobbyIsFull_ShouldTransitionToLobbyArmadoState() {
        // --- 1. Arrange (Preparar el escenario) ---

        // Crear datos de prueba
        Scrim scrim = new Scrim();
        scrim.setId(1L);
        scrim.setFormato("1v1"); // Importante para que el lobby se llene con 2 jugadores

        User creador = new User();
        creador.setId(1L);
        User postulante = new User();
        postulante.setId(2L);

        Team teamA = new Team();
        teamA.getMiembros().add(creador); // El creador ya está en el equipo A
        Team teamB = new Team();

        // Configurar el comportamiento de los mocks
        // "Cuando se llame a teamRepository.findByScrimId(1L), devuelve esta lista de
        // equipos"
        when(teamRepository.findByScrimId(1L)).thenReturn(List.of(teamA, teamB));
        // "Cuando se llame a teamRepository.save con cualquier objeto Team, simplemente
        // devuelve ese mismo objeto"
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- 2. Act (Ejecutar la acción) ---

        // Llamamos al método que queremos probar
        buscandoJugadoresState.aceptarPostulacion(scrim, postulante);

        // --- 3. Assert (Verificar los resultados) ---

        // Verificamos que el estado del scrim ha cambiado correctamente
        assertEquals("LOBBY_ARMADO", scrim.getEstado(), "El estado del scrim debería haber cambiado a LOBBY_ARMADO.");

        // Verificamos que los métodos de nuestros mocks fueron llamados como
        // esperábamos
        // ¿Se guardó el equipo B (donde se añadió el nuevo jugador)?
        verify(teamRepository, times(1)).save(teamB);
        // ¿Se publicó un evento en el bus?
        verify(eventBus, times(1)).publish(any());
    }
}