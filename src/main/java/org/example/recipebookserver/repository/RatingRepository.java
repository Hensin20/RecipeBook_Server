package org.example.recipebookserver.repository;

import org.example.recipebookserver.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByRecipeIdAndUserId(Long recipeId, Long userId);

    @Query("SELECT COALESCE(AVG(r.score), 0.0) FROM Rating r WHERE r.recipeId = :recipeId")
    Double getAverageRatingByRecipeId(@Param("recipeId") Long recipeId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.recipeId = :recipeId")
    Integer countRatingsByRecipeId(@Param("recipeId") Long recipeId);
}