package org.example.recipebookserver.controller;

import org.example.recipebookserver.DTO.IngredientDTO;
import org.example.recipebookserver.DTO.RecipeCreateDTO;
import org.example.recipebookserver.DTO.RecipeDTO;
import org.example.recipebookserver.model.Recipe;
import org.example.recipebookserver.repository.RecipeIngredientRepository;
import org.example.recipebookserver.service.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeService recipeService;
    private final RecipeIngredientRepository recipeIngredientRepository;

    public RecipeController(RecipeService recipeService, RecipeIngredientRepository recipeIngredientRepository) {
        this.recipeService = recipeService;
        this.recipeIngredientRepository = recipeIngredientRepository;
    }

    @PostMapping
    public ResponseEntity<RecipeDTO> createRecipe(@RequestBody RecipeCreateDTO dto) {
        Recipe recipe = recipeService.createRecipe(dto);

        RecipeDTO response = new RecipeDTO();
        response.setId(recipe.getId());
        response.setTitle(recipe.getTitle());
        response.setDescription(recipe.getDescription());
        response.setAverageRating(recipe.getAverageRating() != null ? recipe.getAverageRating() : 0.0);
        response.setCategoryName(recipe.getCategory().getName());
        response.setAuthorName(recipe.getAuthor().getUsername());

        List<IngredientDTO> ingredients = recipeIngredientRepository.findByRecipeId(recipe.getId())
                .stream()
                .map(ri -> {
                    IngredientDTO ingDto = new IngredientDTO();
                    ingDto.setName(ri.getIngredient().getName());
                    ingDto.setQuantity(ri.getQuantity());
                    return ingDto;
                })
                .toList();
        response.setIngredients(ingredients);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public List<RecipeDTO> getRecipes() {
        return recipeService.getAllRecipes();
    }

    @GetMapping("/search-by-ingredient")
    public List<RecipeDTO> searchByIngredient(@RequestParam String ingredient){
        return recipeService.findByIngredient(ingredient);
    }

    @GetMapping("/search-by-title")
    public List<RecipeDTO> searchByTitle(@RequestParam String title){
        return recipeService.findByTitle(title);
    }

    @GetMapping("/search-by-category")
    public List<RecipeDTO> searchByCategory(@RequestParam String category){
        return recipeService.findByCategory(category);
    }

}
