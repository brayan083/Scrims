package com.uade.tpo.Scrims.view.mapper;

import com.uade.tpo.Scrims.model.domain.Estadistica;
import com.uade.tpo.Scrims.model.domain.Postulation;
import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.Team;
import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.view.dto.response.PostulationResponseDTO;
import com.uade.tpo.Scrims.view.dto.response.ScrimDetailResponseDTO;
import com.uade.tpo.Scrims.view.dto.response.ScrimResponse;
import com.uade.tpo.Scrims.view.dto.response.StatisticResponseDTO;
import com.uade.tpo.Scrims.view.dto.response.TeamMemberResponseDTO;
import com.uade.tpo.Scrims.view.dto.response.TeamResponseDTO;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper component responsible for converting domain entities to DTOs.
 * Separates mapping logic from business logic in the service layer.
 */
@Component
public class ScrimMapper {

    /**
     * Converts a Scrim entity to a ScrimResponse DTO.
     *
     * @param scrim the Scrim entity
     * @return the ScrimResponse DTO
     */
    public ScrimResponse toScrimResponse(Scrim scrim) {
        if (scrim == null) {
            return null;
        }

        ScrimResponse response = new ScrimResponse();
        response.setId(scrim.getId());
        response.setJuego(scrim.getJuego());
        response.setFormato(scrim.getFormato());
        response.setRegion(scrim.getRegion());
        response.setRangoMin(scrim.getRangoMin());
        response.setRangoMax(scrim.getRangoMax());
        response.setFechaHora(scrim.getFechaHora());
        response.setEstado(scrim.getEstado());
        
        if (scrim.getCreador() != null) {
            response.setCreadorUsername(scrim.getCreador().getUsername());
        } else {
            response.setCreadorUsername(null);
        }
        
        return response;
    }

    /**
     * Converts a Scrim entity with teams and confirmations to a detailed response DTO.
     *
     * @param scrim the Scrim entity
     * @param teams the list of teams in the scrim
     * @param confirmedUserIds set of user IDs who have confirmed participation
     * @return the ScrimDetailResponseDTO
     */
    public ScrimDetailResponseDTO toScrimDetailResponse(Scrim scrim, List<Team> teams, Set<Long> confirmedUserIds) {
        if (scrim == null) {
            return null;
        }

        ScrimDetailResponseDTO responseDTO = new ScrimDetailResponseDTO();
        responseDTO.setId(scrim.getId());
        responseDTO.setJuego(scrim.getJuego());
        responseDTO.setFormato(scrim.getFormato());
        responseDTO.setRegion(scrim.getRegion());
        responseDTO.setRangoMin(scrim.getRangoMin());
        responseDTO.setRangoMax(scrim.getRangoMax());
        responseDTO.setFechaHora(scrim.getFechaHora());
        responseDTO.setEstado(scrim.getEstado());
        
        if (scrim.getCreador() != null) {
            responseDTO.setCreadorUsername(scrim.getCreador().getUsername());
        }

        List<TeamResponseDTO> teamDTOs = teams.stream()
                .map(team -> toTeamResponse(team, confirmedUserIds))
                .collect(Collectors.toList());

        responseDTO.setTeams(teamDTOs);

        return responseDTO;
    }

    /**
     * Converts a Team entity to a TeamResponseDTO.
     *
     * @param team the Team entity
     * @param confirmedUserIds set of user IDs who have confirmed participation
     * @return the TeamResponseDTO
     */
    private TeamResponseDTO toTeamResponse(Team team, Set<Long> confirmedUserIds) {
        TeamResponseDTO teamDTO = new TeamResponseDTO();
        teamDTO.setId(team.getId());
        teamDTO.setNombre(team.getNombre());

        List<TeamMemberResponseDTO> memberDTOs = team.getMiembros().stream()
                .map(member -> toTeamMemberResponse(member, confirmedUserIds))
                .collect(Collectors.toList());

        teamDTO.setMiembros(memberDTOs);
        return teamDTO;
    }

    /**
     * Converts a User to a TeamMemberResponseDTO.
     *
     * @param member the User entity
     * @param confirmedUserIds set of user IDs who have confirmed participation
     * @return the TeamMemberResponseDTO
     */
    private TeamMemberResponseDTO toTeamMemberResponse(User member, Set<Long> confirmedUserIds) {
        TeamMemberResponseDTO memberDTO = new TeamMemberResponseDTO();
        memberDTO.setUserId(member.getId());
        memberDTO.setUsername(member.getUsername());
        memberDTO.setUserRank(member.getRango());
        memberDTO.setHaConfirmado(confirmedUserIds.contains(member.getId()));

        return memberDTO;
    }

    /**
     * Converts a Postulation entity to a PostulationResponseDTO.
     *
     * @param postulation the Postulation entity
     * @return the PostulationResponseDTO
     */
    public PostulationResponseDTO toPostulationResponse(Postulation postulation) {
        if (postulation == null) {
            return null;
        }

        PostulationResponseDTO dto = new PostulationResponseDTO();
        User postulante = postulation.getPostulante();

        dto.setPostulationId(postulation.getId());
        dto.setUserId(postulante.getId());
        dto.setUsername(postulante.getUsername());
        dto.setUserRank(postulante.getRango());
        dto.setStatus(postulation.getEstado());

        return dto;
    }

    /**
     * Converts an Estadistica entity to a StatisticResponseDTO.
     *
     * @param estadistica the Estadistica entity
     * @return the StatisticResponseDTO
     */
    public StatisticResponseDTO toStatisticResponse(Estadistica estadistica) {
        if (estadistica == null) {
            return null;
        }

        StatisticResponseDTO dto = new StatisticResponseDTO();
        dto.setUsername(estadistica.getUser().getUsername());
        dto.setKda(estadistica.getKda());
        dto.setMvp(estadistica.isMvp());
        
        return dto;
    }
}
