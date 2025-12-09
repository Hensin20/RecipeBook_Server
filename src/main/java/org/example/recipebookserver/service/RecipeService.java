package org.example.recipebookserver.service;

import org.example.recipebookserver.DTO.RecipeDTO;
import org.example.recipebookserver.model.Recipe;
import org.example.recipebookserver.repository.RecipeIngredientRepository;
import org.example.recipebookserver.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public List<RecipeDTO> getAllRecipes() {
        return recipeRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<RecipeDTO> findByIngredient(String ingredientName) {
        return recipeRepository.findByIngredientName(ingredientName).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<RecipeDTO> findByTitle(String title){
        return recipeRepository.findByTitle(title).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<RecipeDTO> findByCategory(String category){
        return recipeRepository.findByCategory(category).stream()
                .map(this::mapToDTO)
                .toList();
    }

    private RecipeDTO mapToDTO(Recipe recipe) {
        RecipeDTO dto = new RecipeDTO();
        dto.setId(recipe.getId());
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());
        dto.setAverageRating(recipe.getAverageRating());
        dto.setCategoryName(recipe.getCategory() != null ? recipe.getCategory().getName() : null);
        dto.setAuthorName(recipe.getAuthor() != null ? recipe.getAuthor().getUsername() : null);
        return dto;
    }
}

