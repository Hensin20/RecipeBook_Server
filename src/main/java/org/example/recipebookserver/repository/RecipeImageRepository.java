package org.example.recipebookserver.repository;

import org.example.recipebookserver.model.RecipeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeImageRepository extends JpaRepository<RecipeImage, Long> {
    // Метод, щоб швидко дістати всі фотографії конкретного рецепту
    List<RecipeImage> findByRecipeId(Long recipeId);
}