package miron.gaskov.racelog.group;

import lombok.RequiredArgsConstructor;
import miron.gaskov.racelog.common.ForbiddenException;
import miron.gaskov.racelog.common.NotFoundException;
import miron.gaskov.racelog.group.dto.GroupDtos;
import miron.gaskov.racelog.result.Result;
import miron.gaskov.racelog.result.ResultRepository;
import miron.gaskov.racelog.user.User;
import miron.gaskov.racelog.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final ResultRepository resultRepository;

    private final SecureRandom random = new SecureRandom();

    @Transactional
    public GroupDtos.GroupDto createGroup(User coach, GroupDtos.CreateGroupRequest request) {
        if (coach.getRole() != UserRole.COACH) {
            throw new ForbiddenException("Только тренер может создавать группы");
        }

        String inviteCode = generateInviteCode();

        Group group = Group.builder()
                .name(request.name())
                .description(request.description())
                .inviteCode(inviteCode)
                .coach(coach)
                .build();

        Group saved = groupRepository.save(group);
        return new GroupDtos.GroupDto(saved.getId(), saved.getName(), saved.getDescription(), saved.getInviteCode());
    }

    @Transactional(readOnly = true)
    public List<GroupDtos.GroupDto> getGroupsForUser(User user) {
        if (user.getRole() == UserRole.COACH) {
            return groupRepository.findByCoachId(user.getId()).stream()
                    .map(g -> new GroupDtos.GroupDto(g.getId(), g.getName(), g.getDescription(), g.getInviteCode()))
                    .toList();
        } else {
            return groupRepository.findByMembers_User_Id(user.getId()).stream()
                    .map(g -> new GroupDtos.GroupDto(g.getId(), g.getName(), g.getDescription(), g.getInviteCode()))
                    .toList();
        }
    }

    @Transactional
    public GroupDtos.GroupDto joinGroup(User user, String inviteCode) {
        Group group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new NotFoundException("Группа с таким кодом не найдена"));

        memberRepository.findByGroup_IdAndUser_Id(group.getId(), user.getId())
                .ifPresent(m -> { throw new ForbiddenException("Вы уже состоите в этой группе"); });

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .build();
        memberRepository.save(member);

        return new GroupDtos.GroupDto(group.getId(), group.getName(), group.getDescription(), group.getInviteCode());
    }

    @Transactional(readOnly = true)
    public List<GroupDtos.GroupMemberDto> getMembers(Long groupId) {
        return memberRepository.findByGroup_Id(groupId).stream()
                .map(m -> new GroupDtos.GroupMemberDto(
                        m.getUser().getId(),
                        m.getUser().getLogin(),
                        m.getUser().getRole().name()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GroupDtos.GroupDisciplineStatDto> getDisciplineStats(Long groupId, Integer disciplineId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Группа не найдена"));

        List<GroupMember> members = memberRepository.findByGroup_Id(group.getId());
        if (members.isEmpty()) {
            return List.of();
        }

        List<Long> athleteIds = members.stream()
                .map(m -> m.getUser().getId())
                .toList();

        List<Result> results = resultRepository
                .findByAthleteIdInAndDisciplineIdOrderByDateDesc(athleteIds, disciplineId);

        if (results.isEmpty()) {
            return List.of();
        }

        Map<Long, List<Result>> byAthlete = results.stream()
                .collect(Collectors.groupingBy(r -> r.getAthlete().getId()));

        return byAthlete.entrySet().stream()
                .sorted(Comparator.comparing(entry ->
                        entry.getValue().stream()
                                .filter(r -> r.getResultNumeric() != null)
                                .map(Result::getResultNumeric)
                                .min(Double::compareTo)
                                .orElse(Double.MAX_VALUE)
                ))
                .map(entry -> {
                    Long athleteId = entry.getKey();
                    List<Result> resList = entry.getValue();
                    Result last = resList.getFirst();
                    Result best = resList.stream()
                            .filter(r -> r.getResultNumeric() != null)
                            .min(Comparator.comparing(Result::getResultNumeric))
                            .orElse(last);

                    String login = resList.getFirst().getAthlete().getLogin();

                    return new GroupDtos.GroupDisciplineStatDto(
                            athleteId,
                            login,
                            best.getResultValue(),
                            best.getDate().toString(),
                            last.getResultValue(),
                            last.getDate().toString()
                    );
                })
                .toList();
    }

    private String generateInviteCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}