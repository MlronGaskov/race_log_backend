package miron.gaskov.group;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miron.gaskov.group.dto.GroupDtos;
import miron.gaskov.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService service;

    @PostMapping
    public GroupDtos.GroupDto create(
            @AuthenticationPrincipal User current,
            @Valid @RequestBody GroupDtos.CreateGroupRequest request
    ) {
        return service.createGroup(current, request);
    }

    @GetMapping
    public List<GroupDtos.GroupDto> myGroups(@AuthenticationPrincipal User current) {
        return service.getGroupsForUser(current);
    }

    @PostMapping("/join")
    public GroupDtos.GroupDto join(
            @AuthenticationPrincipal User current,
            @Valid @RequestBody GroupDtos.JoinGroupRequest request
    ) {
        return service.joinGroup(current, request.inviteCode());
    }

    @GetMapping("/{groupId}/members")
    public List<GroupDtos.GroupMemberDto> members(@PathVariable Long groupId) {
        return service.getMembers(groupId);
    }

    @GetMapping("/{groupId}/stats/discipline/{disciplineId}")
    public List<GroupDtos.GroupDisciplineStatDto> disciplineStats(
            @PathVariable Long groupId,
            @PathVariable Integer disciplineId
    ) {
        return service.getDisciplineStats(groupId, disciplineId);
    }
}
