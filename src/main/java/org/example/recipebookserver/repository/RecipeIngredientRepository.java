package org.example.recipebookserver.repository;

import org.example.recipebookserver.model.Recipe;
import org.example.recipebookserver.model.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {
    @Query("SELECT DISTINCT r FROM RecipeIngredient ri JOIN ri.recipe r JOIN ri.ingredient i " +
            "WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Recipe> findRecipesByIngredient(@Param("name") String name);
}

