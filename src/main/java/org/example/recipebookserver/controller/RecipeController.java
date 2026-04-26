package org.example.recipebookserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.recipebookserver.DTO.RecipeCreateDTO;
import org.example.recipebookserver.DTO.RecipeDTO;
import org.example.recipebookserver.model.Category;
import org.example.recipebookserver.model.Recipe;
import org.example.recipebookserver.repository.CategoryRepository;
import org.example.recipebookserver.repository.IngredientDictionaryRepository;
import org.example.recipebookserver.repository.RecipeRepository;
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
    private final CategoryRepository categoryRepository;
    private final IngredientDictionaryRepository ingredientDictionaryRepository;

    public RecipeController(RecipeService recipeService, ObjectMapper objectMapper, RecipeRepository recipeRepository, CategoryRepository categoryRepository, IngredientDictionaryRepository ingredientDictionaryRepository) {
        this.recipeService = recipeService;
        this.objectMapper = objectMapper;
        this.recipeRepository = recipeRepository;
        this.categoryRepository= categoryRepository;
        this.ingredientDictionaryRepository = ingredientDictionaryRepository;
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

    // Додай цей метод під getRecipeById
    @PostMapping("/{id}/rate")
    public ResponseEntity<Double> rateRecipe(@PathVariable Long id, @RequestParam int rating) {
        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().build(); // Захист від неправильних оцінок
        }
        try {
            double newAverage = recipeService.addRating(id, rating);
            return ResponseEntity.ok(newAverage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

    @GetMapping("/author/{username}")
    public ResponseEntity<List<RecipeDTO>> getRecipesByAuthor(@PathVariable String username) {
        return ResponseEntity.ok(recipeService.getRecipesByAuthor(username));
    }

    @GetMapping("/search-by-category")
    public List<RecipeDTO> searchByCategory(@RequestParam String category) {
        return recipeService.findByCategory(category);
    }
    // ВИДАЛЕННЯ РЕЦЕПТУ
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id, @RequestParam Long userId) {
        Recipe recipe = recipeRepository.findById(id).orElse(null);

        if (recipe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Рецепт не знайдено");
        }

        // Перевірка, чи користувач є автором (якщо у тебе є зв'язок з автором)
        if (!recipe.getAuthor().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ви можете видаляти лише свої рецепти");
        }

        recipeRepository.deleteById(id);
        return ResponseEntity.ok("Рецепт успішно видалено");
    }

    // РЕДАГУВАННЯ РЕЦЕПТУ
    // РЕДАГУВАННЯ РЕЦЕПТУ
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecipe(@PathVariable Long id, @RequestBody RecipeDTO updatedRecipeDto, @RequestParam Long userId) {
        Recipe recipe = recipeRepository.findById(id).orElse(null);

        if (recipe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Рецепт не знайдено");
        }

        if (!recipe.getAuthor().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ви можете редагувати лише свої рецепти");
        }

        // Оновлюємо текстові поля
        recipe.setTitle(updatedRecipeDto.getTitle());
        recipe.setDescription(updatedRecipeDto.getDescription());

        // --- ОНОВЛЕННЯ ІНСТРУКЦІЇ (ВИПРАВЛЕНО) ---
        recipe.getInstructions().clear();
        org.example.recipebookserver.model.Instruction newInstruction = new org.example.recipebookserver.model.Instruction();
        newInstruction.setText(updatedRecipeDto.getInstruction());
        newInstruction.setRecipe(recipe);

        // ДОДАНО: Вказуємо номер кроку, щоб база не сварилася!
        newInstruction.setStepNumber(1);

        recipe.getInstructions().add(newInstruction);
        // -----------------------------------------
// --- ОНОВЛЕННЯ ІНГРЕДІЄНТІВ ---
        recipe.getIngredients().clear();
        recipeRepository.saveAndFlush(recipe);
        if (updatedRecipeDto.getIngredients() != null) {
            for (org.example.recipebookserver.DTO.IngredientDTO ingDto : updatedRecipeDto.getIngredients()) {

                // 1. Шукаємо інгредієнт у довіднику за назвою
                org.example.recipebookserver.model.IngredientDictionary dictItem =
                        ingredientDictionaryRepository.findByName(ingDto.getName()).orElse(null);

                // 2. Якщо такого інгредієнта ще ніколи не було — створюємо його в довіднику
                if (dictItem == null) {
                    dictItem = new org.example.recipebookserver.model.IngredientDictionary();
                    dictItem.setName(ingDto.getName());
                    dictItem = ingredientDictionaryRepository.save(dictItem);
                }

                // 3. Створюємо зв'язок для конкретного рецепту
                org.example.recipebookserver.model.RecipeIngredient newIngredient = new org.example.recipebookserver.model.RecipeIngredient();

                // ЗАМІСТЬ setName() ВИКОРИСТОВУЄМО setIngredient()
                newIngredient.setIngredient(dictItem);
                newIngredient.setQuantity(ingDto.getQuantity());
                newIngredient.setRecipe(recipe);

                recipe.getIngredients().add(newIngredient);
            }
        }
        // ------------------------------

        // Оновлення категорії
        Category category = categoryRepository.findByName(updatedRecipeDto.getCategoryName()).orElse(null);
        if (category != null) {
            recipe.setCategory(category);
        }

        recipeRepository.save(recipe);
        return ResponseEntity.ok(recipe);
    }

}