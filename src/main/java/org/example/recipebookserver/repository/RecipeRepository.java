package org.example.recipebookserver.repository;

import org.example.recipebookserver.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("SELECT r FROM Recipe r JOIN r.ingredients ri JOIN ri.ingredient i " +
            "WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :ingredientName, '%'))")
    List<Recipe> findByIngredientName(@Param("ingredientName") String ingredientName);

    @Query("SELECT r FROM Recipe r WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Recipe> findByTitle(@Param("title") String title);

    @Query("SELECT r FROM Recipe r JOIN r.category c WHERE LOWER(c.name) = LOWER(:categoryName)")
    List<Recipe> findByCategory(@Param("categoryName") String categoryName);
}

