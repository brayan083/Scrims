// Fichero: model/service/ScrimService.java
package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.Team;
import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.model.infrastructure.persistence.ScrimRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.TeamRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.UserRepository;
import com.uade.tpo.Scrims.model.patterns.state.BuscandoJugadoresState;
import com.uade.tpo.Scrims.model.patterns.state.CanceladoState;
import com.uade.tpo.Scrims.model.patterns.state.FinalizadoState;
import com.uade.tpo.Scrims.model.patterns.state.LobbyArmadoState;
import com.uade.tpo.Scrims.view.dto.request.CreateScrimRequest;
import com.uade.tpo.Scrims.view.dto.request.FinalizeScrimRequest;
import com.uade.tpo.Scrims.view.dto.request.PlayerStatisticDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.Scrims.view.dto.response.ScrimResponse;
import com.uade.tpo.Scrims.view.dto.response.StatisticResponseDTO;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.uade.tpo.Scrims.model.domain.Estadistica;
import com.uade.tpo.Scrims.model.domain.Postulation;
import com.uade.tpo.Scrims.model.infrastructure.persistence.EstadisticaRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.PostulationRepository;

@Service
public class ScrimService {
    @Autowired
    private ScrimRepository scrimRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostulationRepository postulationRepository;
    @Autowired
    private TeamRepository teamRepository;
    // Inyecta los beans de estado que creamos para que puedan acceder a los
    // repositorios
    @Autowired
    private BuscandoJugadoresState buscandoJugadoresState;
    @Autowired
    private LobbyArmadoState lobbyArmadoState;
    @Autowired
    private EstadisticaRepository estadisticaRepository;

    // Definimos una constante para el cambio de MMR
    private static final int MMR_CHANGE_ON_WIN = 5;
    private static final int MMR_CHANGE_ON_LOSS = -5;

    // 1. La firma del método ahora acepta un 'username'
    @Transactional // Es bueno usarlo aquí también
    public ScrimResponse createScrim(CreateScrimRequest request, String username) {
        User creador = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Creador no encontrado."));

        Scrim newScrim = new Scrim();
        newScrim.setJuego(request.getJuego());
        newScrim.setFormato(request.getFormato());
        newScrim.setRegion(request.getRegion());
        newScrim.setRangoMin(request.getRangoMin());
        newScrim.setRangoMax(request.getRangoMax());
        newScrim.setFechaHora(request.getFechaHora());
        newScrim.setState(new BuscandoJugadoresState());
        newScrim.setCreador(creador);

        // Guardamos el scrim primero para que tenga un ID
        Scrim savedScrim = scrimRepository.save(newScrim);

        // Creamos el Equipo A
        Team teamA = new Team();
        teamA.setScrim(savedScrim);
        teamA.setNombre("Equipo A");

        // --- ¡EL CAMBIO CLAVE ESTÁ AQUÍ! ---
        // Añadimos al creador como el primer miembro del Equipo A.
        teamA.getMiembros().add(creador);

        teamRepository.save(teamA); // Guardamos el Equipo A con su primer miembro

        // Creamos el Equipo B (vacío)
        Team teamB = new Team();
        teamB.setScrim(savedScrim);
        teamB.setNombre("Equipo B");
        teamRepository.save(teamB);

        return mapToScrimResponse(savedScrim);
    }

    public List<ScrimResponse> getAllScrims() {
        List<Scrim> scrims = scrimRepository.findAll();
        // Convertimos cada entidad Scrim a un ScrimResponse DTO
        return scrims.stream().map(this::mapToScrimResponse).collect(Collectors.toList());
    }

    // Método de ayuda privado para la conversión
    private ScrimResponse mapToScrimResponse(Scrim scrim) {
        ScrimResponse response = new ScrimResponse();
        response.setId(scrim.getId());
        response.setJuego(scrim.getJuego());
        response.setFormato(scrim.getFormato());
        response.setRegion(scrim.getRegion());
        response.setRangoMin(scrim.getRangoMin());
        response.setRangoMax(scrim.getRangoMax());
        response.setFechaHora(scrim.getFechaHora());
        response.setEstado(scrim.getEstado());
        // Aquí está el campo clave que la prueba espera
        response.setCreadorUsername(scrim.getCreador().getUsername());
        return response;
    }

    public Postulation applyToScrim(Long scrimId, String username) {
        // Buscamos el scrim y el usuario
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado"));
        User postulante = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // --- REGLAS DE NEGOCIO ---
        // 1. Un usuario no puede postularse a su propio scrim
        if (scrim.getCreador().getId().equals(postulante.getId())) {
            throw new IllegalStateException("No puedes postularte a tu propio scrim.");
        }

        // 2. El scrim debe estar buscando jugadores
        if (!"BUSCANDO_JUGADORES".equals(scrim.getEstado())) {
            throw new IllegalStateException("Este scrim no acepta postulaciones actualmente.");
        }

        // --- ¡NUEVA VALIDACIÓN DE DUPLICADOS! ---
        if (postulationRepository.existsByScrimIdAndPostulanteId(scrimId, postulante.getId())) {
            throw new IllegalStateException("Ya te has postulado a este scrim.");
        }

        // 3. --- ¡NUEVA VALIDACIÓN DE RANGO! ---
        if (postulante.getRango() == null) {
            throw new IllegalStateException("Tu perfil debe tener un rango para poder postularte.");
        }
        if (postulante.getRango() < scrim.getRangoMin() || postulante.getRango() > scrim.getRangoMax()) {
            throw new IllegalStateException(
                    "Tu rango (" + postulante.getRango() + ") no está dentro de los límites del scrim ["
                            + scrim.getRangoMin() + "-" + scrim.getRangoMax() + "].");
        }

        Postulation newPostulation = new Postulation();
        newPostulation.setScrim(scrim);
        newPostulation.setPostulante(postulante);
        newPostulation.setEstado("PENDIENTE");

        return postulationRepository.save(newPostulation);
    }

    @Transactional // Anotación para asegurar que todas las operaciones de BBDD se completen o
    public Postulation acceptPostulation(Long scrimId, Long postulationId, String creatorUsername) {

        // 1. --- BÚSQUEDA DE ENTIDADES ---
        // Obtenemos todas las entidades necesarias de la base de datos.
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado con ID: " + scrimId));

        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new RuntimeException("Usuario creador no encontrado: " + creatorUsername));

        Postulation postulation = postulationRepository.findById(postulationId)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada con ID: " + postulationId));

        // 2. --- VERIFICACIÓN DE PERMISOS ---
        // La regla de negocio más importante: solo el creador puede gestionar su scrim.
        if (!scrim.getCreador().getId().equals(creator.getId())) {
            // En una app real, aquí se lanzaría una excepción de seguridad más específica
            // (ej. AccessDeniedException)
            throw new RuntimeException("Acción no permitida: No eres el creador de este scrim.");
        }

        // 3. --- INTEGRACIÓN CON EL PATRÓN STATE ---
        // El objeto 'scrim' cargado de la BBDD tiene un estado 'puro' (creado con
        // 'new').
        // Ese estado 'puro' no tiene sus dependencias (@Autowired) inyectadas.
        // Aquí reemplazamos el estado 'puro' con el bean de estado gestionado por
        // Spring,
        // que sí tiene acceso a los repositorios.
        if (scrim.getCurrentState() instanceof BuscandoJugadoresState) {
            scrim.setCurrentState(buscandoJugadoresState);
        }
        // (Aquí irían otros 'else if' para otros estados que puedan manejar esta
        // acción)

        // 4. --- DELEGACIÓN DE LA LÓGICA ---
        // El servicio ya no sabe CÓMO se acepta un jugador. Simplemente le dice al
        // scrim que lo haga.
        // El objeto de estado actual (BuscandoJugadoresState en este caso) se encargará
        // de todo.
        scrim.aceptarPostulacion(postulation.getPostulante());

        // 5. --- PERSISTENCIA DE CAMBIOS ---
        // Actualizamos el estado de la postulación a "ACEPTADA".
        postulation.setEstado("ACEPTADA");
        postulationRepository.save(postulation);

        // MUY IMPORTANTE: Guardamos el scrim para persistir cualquier cambio de estado
        // que haya ocurrido dentro del objeto State (ej. cambiar de BUSCANDO a
        // LOBBY_ARMADO).
        scrimRepository.save(scrim);

        // 6. --- DEVOLUCIÓN DEL RESULTADO ---
        return postulation;
    }

    @Transactional
    public void confirmParticipation(Long scrimId, String username) {
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado"));
        User jugador = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Inyectamos las dependencias al estado actual
        if (scrim.getCurrentState() instanceof LobbyArmadoState) {
            scrim.setCurrentState(lobbyArmadoState);
        }

        // Delegamos la acción al estado
        scrim.confirmarParticipacion(jugador);

        // Guardamos el scrim para persistir el cambio de estado si ocurre
        scrimRepository.save(scrim);
    }

    @Transactional
    public void finalizeScrim(Long scrimId, FinalizeScrimRequest request, String username) {

        // 1. --- BÚSQUEDA DE ENTIDADES Y VALIDACIÓN DE PERMISOS ---
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado con ID: " + scrimId));

        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario creador no encontrado."));

        if (!scrim.getCreador().getId().equals(creator.getId())) {
            throw new RuntimeException("Acción no permitida: No eres el creador de este scrim.");
        }

        // 2. --- VALIDACIÓN DE ESTADO ---
        if (!"EN_JUEGO".equals(scrim.getEstado())) {
            throw new IllegalStateException(
                    "El scrim no se puede finalizar porque no está en juego. Estado actual: " + scrim.getEstado());
        }

        // 3. --- CARGAR ESTADÍSTICAS ---
        System.out.println("Finalizando Scrim ID: " + scrimId + ". Resultado: " + request.getResultado());
        for (PlayerStatisticDTO statDTO : request.getPlayerStats()) {
            User player = userRepository.findById(statDTO.getUserId())
                    .orElseThrow(
                            () -> new RuntimeException("Jugador con ID " + statDTO.getUserId() + " no encontrado."));

            Estadistica estadistica = new Estadistica();
            estadistica.setScrim(scrim);
            estadistica.setUser(player);
            estadistica.setKda(statDTO.getKda());
            estadistica.setMvp(statDTO.isMvp());

            estadisticaRepository.save(estadistica);
            System.out.println("Estadística guardada para el jugador: " + player.getUsername());
        }

        // 4. --- RECALCULO DE MMR (RANGO) ---
        List<Team> teams = teamRepository.findByScrimId(scrimId);
        if (teams.size() < 2) {
            throw new IllegalStateException(
                    "El scrim no tiene los dos equipos configurados correctamente para calcular el MMR.");
        }

        Team teamA = teams.get(0);
        Team teamB = teams.get(1);

        List<User> ganadores;
        List<User> perdedores;

        if ("VICTORIA_EQUIPO_A".equalsIgnoreCase(request.getResultado())) {
            ganadores = teamA.getMiembros();
            perdedores = teamB.getMiembros();
        } else if ("VICTORIA_EQUIPO_B".equalsIgnoreCase(request.getResultado())) {
            ganadores = teamB.getMiembros();
            perdedores = teamA.getMiembros();
        } else {
            System.out.println("Resultado es EMPATE o no reconocido. No se ajustará el MMR.");
            // Si no hay un ganador claro, simplemente finalizamos el scrim sin cambiar
            // rangos.
            scrim.setState(new FinalizadoState());
            scrimRepository.save(scrim);
            return; // Salimos del método aquí.
        }

        System.out.println("--- Actualizando MMR ---");
        for (User ganador : ganadores) {
            int nuevoRango = ganador.getRango() + MMR_CHANGE_ON_WIN;
            System.out.println(
                    "Jugador " + ganador.getUsername() + " (ganador): " + ganador.getRango() + " -> " + nuevoRango);
            ganador.setRango(nuevoRango);
            userRepository.save(ganador);
        }

        for (User perdedor : perdedores) {
            int nuevoRango = perdedor.getRango() + MMR_CHANGE_ON_LOSS;
            if (nuevoRango < 0)
                nuevoRango = 0; // El MMR no puede ser negativo.
            System.out.println(
                    "Jugador " + perdedor.getUsername() + " (perdedor): " + perdedor.getRango() + " -> " + nuevoRango);
            perdedor.setRango(nuevoRango);
            userRepository.save(perdedor);
        }

        // 5. --- TRANSICIÓN DE ESTADO FINAL ---
        scrim.setState(new FinalizadoState());
        scrimRepository.save(scrim);

        System.out.println("Scrim ID: " + scrimId + " ha sido movido al estado FINALIZADO.");
        // TODO: Publicar un evento "ScrimFinalizadoEvent".
    }

    @Transactional
    public void cancelScrim(Long scrimId, String username) {
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado con ID: " + scrimId));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        // 1. Verificación de permisos
        if (!scrim.getCreador().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para cancelar este scrim.");
        }

        // 2. Verificación de estado
        List<String> cancellableStates = List.of("BUSCANDO_JUGADORES", "LOBBY_ARMADO", "CONFIRMADO");
        if (!cancellableStates.contains(scrim.getEstado())) {
            throw new IllegalStateException("No se puede cancelar un scrim que ya está en juego o ha finalizado.");
        }

        // 3. Transición de estado
        scrim.setState(new CanceladoState());
        scrimRepository.save(scrim);

        System.out.println("Scrim ID: " + scrimId + " ha sido CANCELADO.");
        // TODO: Publicar un evento "ScrimCanceladoEvent" para notificar a los
        // jugadores.
    }

    public List<StatisticResponseDTO> getScrimStatistics(Long scrimId) {
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado con ID: " + scrimId));
    
        // 1. Verificación de estado
        if (!"FINALIZADO".equals(scrim.getEstado())) {
            throw new IllegalStateException("Las estadísticas solo están disponibles para scrims finalizados.");
        }
    
        // 2. Búsqueda de estadísticas
        List<Estadistica> stats = estadisticaRepository.findByScrimId(scrimId);
    
        // 3. Mapeo a DTOs
        return stats.stream().map(stat -> {
            StatisticResponseDTO dto = new StatisticResponseDTO();
            dto.setUsername(stat.getUser().getUsername());
            dto.setKda(stat.getKda());
            dto.setMvp(stat.isMvp());
            return dto;
        }).collect(Collectors.toList());
    }
}