package com.productapp.repository;

import com.productapp.entity.ResourceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ResourceAssignmentRepository extends JpaRepository<ResourceAssignment, Long> {

    List<ResourceAssignment> findByProductId(Long productId);

    List<ResourceAssignment> findByUserStoryId(Long userStoryId);

    @Query("SELECT ra FROM ResourceAssignment ra " +
           "JOIN UserStory us ON ra.userStoryId = us.id " +
           "WHERE us.epicId = :epicId " +
           "ORDER BY us.title, ra.startDate")
    List<ResourceAssignment> findByEpicId(@Param("epicId") String epicId);

    @Query("SELECT COUNT(ra) > 0 FROM ResourceAssignment ra " +
           "WHERE ra.memberId = :memberId " +
           "AND ra.startDate <= :endDate " +
           "AND ra.endDate >= :startDate")
    boolean hasConflictingAssignment(@Param("memberId") Long memberId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT ra FROM ResourceAssignment ra " +
           "WHERE ra.memberId = :memberId " +
           "AND ra.startDate <= :endDate " +
           "AND ra.endDate >= :startDate")
    List<ResourceAssignment> findConflictingAssignments(@Param("memberId") Long memberId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT ra FROM ResourceAssignment ra " +
           "JOIN TeamMember tm ON ra.memberId = tm.id " +
           "JOIN Team t ON tm.teamId = t.id " +
           "JOIN CapacityPlan cp ON t.capacityPlanId = cp.id " +
           "WHERE cp.productId = :productId " +
           "ORDER BY ra.startDate")
    List<ResourceAssignment> findByProductIdWithDetails(@Param("productId") Long productId);
}