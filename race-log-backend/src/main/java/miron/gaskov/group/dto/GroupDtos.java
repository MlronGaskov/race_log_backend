package miron.gaskov.group.dto;

import jakarta.validation.constraints.NotBlank;

public class GroupDtos {

    public record CreateGroupRequest(
            @NotBlank String name,
            String description
    ) {}

    public record JoinGroupRequest(
            @NotBlank String inviteCode
    ) {}

    public record GroupDto(
            Long id,
            String name,
            String description,
            String inviteCode
    ) {}

    public record GroupMemberDto(
            Long id,
            String login,
            String role
    ) {}

    public record GroupDisciplineStatDto(
            Long athleteId,
            String login,
            String bestResultValue,
            String bestResultDate,
            String lastResultValue,
            String lastResultDate
    ) {}
}
