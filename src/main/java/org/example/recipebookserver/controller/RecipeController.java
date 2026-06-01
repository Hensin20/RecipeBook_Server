package org.example.recipebookserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.recipebookserver.DTO.RecipeCreateDTO;
import org.example.recipebookserver.DTO.RecipeDTO;
import org.example.recipebookserver.model.Recipe;
import org.example.recipebookserver.repository.RecipeRepository;
import org.example.recipebookserver.repository.UserRepository;
import org.example.recipebookserver.service.RecipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final ObjectMapper objectMapper;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    public RecipeController(RecipeService recipeService,
                            ObjectMapper objectMapper,
                            RecipeRepository recipeRepository,
                            UserRepository userRepository) {
        this.recipeService = recipeService;
        this.objectMapper = objectMapper;
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createRecipe(
            @RequestPart("recipe") String recipeJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        try {
            RecipeCreateDTO dto = objectMapper.readValue(recipeJson, RecipeCreateDTO.class);
            recipeService.createRecipe(dto, images);
            return ResponseEntity.status(HttpStatus.CREATED).body("Рецепт успішно створено!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Помилка при створенні рецепту: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<RecipeDTO>> getAllRecipes() {
        return ResponseEntity.ok(recipeService.getAllRecipes());
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<Double> rateRecipe(@PathVariable Long id, @RequestParam Long userId, @RequestParam int rating) {
        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().build();
        }
        try {
            double newAverage = recipeService.addRating(id, userId, rating);
            return ResponseEntity.ok(newAverage);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDTO> getRecipeById(@PathVariable Long id) {
        try {
            RecipeDTO recipe = recipeService.getRecipeById(id);
            return ResponseEntity.ok(recipe);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/author/{username}")
    public ResponseEntity<List<RecipeDTO>> getRecipesByAuthor(@PathVariable String username) {
        return ResponseEntity.ok(recipeService.getRecipesByAuthor(username));
    }

    @GetMapping("/search-by-category")
    public List<RecipeDTO> searchByCategory(@RequestParam String category) {
        return recipeService.findByCategory(category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id, @RequestParam Long userId) {
        Recipe recipe = recipeRepository.findById(id).orElse(null);

        if (recipe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Рецепт не знайдено");
        }

        org.example.recipebookserver.model.User requestUser = userRepository.findById(userId).orElse(null);
        if (requestUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Користувача не знайдено");
        }

        boolean isAuthor = (recipe.getAuthor() != null) && recipe.getAuthor().getId().equals(userId);
        boolean isAdmin = requestUser.isAdmin();

        if (!isAuthor && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ви можете видаляти лише свої рецепти");
        }

        recipeRepository.deleteById(id);
        return ResponseEntity.ok("Рецепт успішно видалено");
    }

    // --- ОНОВЛЕНИЙ МЕТОД PUT ДЛЯ РЕДАГУВАННЯ ФОТОГРАФІЙ ---
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateRecipe(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestPart("recipe") String recipeJson,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages) {
        try {
            RecipeDTO updatedRecipeDto = objectMapper.readValue(recipeJson, RecipeDTO.class);
            RecipeDTO savedRecipe = recipeService.updateRecipe(id, userId, updatedRecipeDto, newImages);
            return ResponseEntity.ok(savedRecipe);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Помилка: " + e.getMessage());
        }
    }

    @GetMapping("/search-by-ingredients")
    public ResponseEntity<List<RecipeDTO>> searchByIngredients(@RequestParam String ingredients) {
        if (ingredients == null || ingredients.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(recipeService.searchByIngredients(ingredients));
    }

    @GetMapping("/search-by-name")
    public ResponseEntity<List<RecipeDTO>> searchRecipesByName(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(recipeService.searchRecipes(query));
    }
}