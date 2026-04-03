package org.example.recipebookserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.recipebookserver.DTO.RecipeCreateDTO;
import org.example.recipebookserver.DTO.RecipeDTO;
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

    public RecipeController(RecipeService recipeService, ObjectMapper objectMapper) {
        this.recipeService = recipeService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createRecipe(
            @RequestPart("recipe") String recipeJson, // Приймаємо JSON як текст
            @RequestPart(value = "images", required = false) List<MultipartFile> images // Приймаємо картинки
    ) {
        try {
            // Конвертуємо JSON-рядок в об'єкт RecipeCreateDTO
            RecipeCreateDTO dto = objectMapper.readValue(recipeJson, RecipeCreateDTO.class);

            // Викликаємо твій сервіс
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
    // Отримання одного рецепту за ID
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDTO> getRecipeById(@PathVariable Long id) {
        try {
            // Викликаємо метод сервісу, який ти вже оновив раніше
            RecipeDTO recipe = recipeService.getRecipeById(id);
            return ResponseEntity.ok(recipe);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build(); // Якщо рецепт не знайдено, віддаємо 404
        }
    }
}