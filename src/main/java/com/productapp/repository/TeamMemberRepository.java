package com.productapp.repository;

import com.productapp.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByTeamId(Long teamId);

    @Query("SELECT tm FROM TeamMember tm WHERE tm.teamId IN :teamIds ORDER BY tm.memberName")
    List<TeamMember> findByTeamIdIn(@Param("teamIds") List<Long> teamIds);

    @Query("SELECT tm FROM TeamMember tm " +
           "JOIN Team t ON tm.teamId = t.id " +
           "JOIN CapacityPlan cp ON t.capacityPlanId = cp.id " +
           "WHERE cp.productId = :productId AND t.isActive = true " +
           "ORDER BY tm.memberName")
    List<TeamMember> findByProductId(@Param("productId") Long productId);

    @Query("SELECT tm FROM TeamMember tm " +
           "JOIN Team t ON tm.teamId = t.id " +
           "JOIN CapacityPlan cp ON t.capacityPlanId = cp.id " +
           "WHERE cp.productId = :productId AND t.isActive = true " +
           "AND tm.id NOT IN (" +
           "  SELECT ra.memberId FROM ResourceAssignment ra " +
           "  WHERE ra.startDate <= :endDate AND ra.endDate >= :startDate" +
           ") " +
           "ORDER BY tm.memberName")
    List<TeamMember> findAvailableMembers(@Param("productId") Long productId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);
}