package miron.gaskov.group;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMember.GroupMemberId> {
    List<GroupMember> findByGroup_Id(Long groupId);
    Optional<GroupMember> findByGroup_IdAndUser_Id(Long groupId, Long userId);
}
