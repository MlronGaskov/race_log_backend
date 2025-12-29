package miron.gaskov.racelog.group;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByCoachId(Long coachId);
    List<Group> findByMembers_User_Id(Long userId);
    Optional<Group> findByInviteCode(String inviteCode);
}
