package org.example.recipebookserver.controller;

import org.example.recipebookserver.DTO.RecipeDTO;
import org.example.recipebookserver.model.Recipe;
import org.example.recipebookserver.service.RecipeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
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
