package com.productapp.repository;

import com.productapp.entity.UserStory;
import com.productapp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStoryRepository extends JpaRepository<UserStory, Long> {

    List<UserStory> findByEpicIdOrderByDisplayOrderAsc(String epicId);

    List<UserStory> findByProductAndEpicIdOrderByDisplayOrderAsc(Product product, String epicId);

    List<UserStory> findByProductOrderByEpicIdAscDisplayOrderAsc(Product product);

    @Query("SELECT us FROM UserStory us WHERE us.product.id = :productId AND us.epicId = :epicId ORDER BY us.displayOrder ASC")
    List<UserStory> findByProductIdAndEpicId(@Param("productId") Long productId, @Param("epicId") String epicId);

    @Query("SELECT COUNT(us) FROM UserStory us WHERE us.epicId = :epicId")
    Long countByEpicId(@Param("epicId") String epicId);

    @Query("SELECT us FROM UserStory us WHERE us.product.id = :productId AND us.status = :status ORDER BY us.epicId ASC, us.displayOrder ASC")
    List<UserStory> findByProductIdAndStatus(@Param("productId") Long productId, @Param("status") String status);

    void deleteByEpicId(String epicId);

    Optional<UserStory> findByIdAndProduct(Long id, Product product);
}