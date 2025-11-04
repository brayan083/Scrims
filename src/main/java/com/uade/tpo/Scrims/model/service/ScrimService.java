// Fichero: model/service/ScrimService.java
package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.Team;
import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.model.infrastructure.persistence.ScrimRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.TeamRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.UserRepository;
import com.uade.tpo.Scrims.view.dto.request.CreateScrimRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.Scrims.view.dto.response.ScrimResponse;
import java.util.List;
import java.util.stream.Collectors;
import com.uade.tpo.Scrims.model.domain.Postulation;
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

    // 1. La firma del método ahora acepta un 'username'
    public Scrim createScrim(CreateScrimRequest request, String username) {
        // 2. Buscamos al usuario por su username, que es seguro y viene del token
        User creador = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Creador no encontrado. El token puede ser inválido."));

        Scrim newScrim = new Scrim();
        newScrim.setJuego(request.getJuego());
        newScrim.setFormato(request.getFormato());
        newScrim.setRegion(request.getRegion());
        newScrim.setRangoMin(request.getRangoMin());
        newScrim.setRangoMax(request.getRangoMax());
        newScrim.setFechaHora(request.getFechaHora());
        newScrim.setEstado("BUSCANDO_JUGADORES");
        newScrim.setCreador(creador); // Asignamos el creador correcto

        // Guardamos el scrim primero para que tenga un ID
        Scrim savedScrim = scrimRepository.save(newScrim);

        // Creamos los dos equipos para este scrim
        Team teamA = new Team();
        teamA.setScrim(savedScrim);
        teamA.setNombre("Equipo A");
        teamRepository.save(teamA);

        Team teamB = new Team();
        teamB.setScrim(savedScrim);
        teamB.setNombre("Equipo B");
        teamRepository.save(teamB);

        return savedScrim;
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
            throw new RuntimeException("No puedes postularte a tu propio scrim.");
        }
        // 2. El scrim debe estar buscando jugadores
        if (!"BUSCANDO_JUGADORES".equals(scrim.getEstado())) {
            throw new RuntimeException("Este scrim no acepta postulaciones actualmente.");
        }
        // (Aquí irían más validaciones, como la de rango, etc.)

        Postulation newPostulation = new Postulation();
        newPostulation.setScrim(scrim);
        newPostulation.setPostulante(postulante);
        newPostulation.setEstado("PENDIENTE");

        return postulationRepository.save(newPostulation);
    }

    public Postulation acceptPostulation(Long scrimId, Long postulationId, String creatorUsername) {
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado"));
        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new RuntimeException("Usuario creador no encontrado"));

        // --- REGLA DE SEGURIDAD ---
        // Verificamos que quien hace la petición es realmente el creador del scrim
        if (!scrim.getCreador().getId().equals(creator.getId())) {
            throw new RuntimeException("No tienes permiso para gestionar este scrim.");
        }

        Postulation postulation = postulationRepository.findById(postulationId)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));

        // Cambiamos el estado de la postulación
        postulation.setEstado("ACEPTADA");

        // Añadimos al jugador al primer equipo que tenga espacio
        // (Esta es una lógica simple, se puede hacer más compleja después)
        List<Team> teams = teamRepository.findAll().stream()
                .filter(team -> team.getScrim().getId().equals(scrimId)).toList();

        Team teamToJoin = teams.get(0); // Lógica simple: unir al Equipo A
        teamToJoin.getMiembros().add(postulation.getPostulante());
        teamRepository.save(teamToJoin);

        // TODO: Aquí iría la lógica para comprobar si el lobby está lleno y cambiar el
        // estado del scrim

        return postulationRepository.save(postulation);
    }
}