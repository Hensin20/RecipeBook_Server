package org.example.recipebookserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.recipebookserver.DTO.RecipeCreateDTO;
import org.example.recipebookserver.DTO.RecipeDTO;
import org.example.recipebookserver.model.Category;
import org.example.recipebookserver.model.Recipe;
import org.example.recipebookserver.repository.CategoryRepository;
import org.example.recipebookserver.repository.IngredientDictionaryRepository;
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
    private final CategoryRepository categoryRepository;
    private final IngredientDictionaryRepository ingredientDictionaryRepository;
    private final UserRepository userRepository;

    public RecipeController(RecipeService recipeService,
                            ObjectMapper objectMapper,
                            RecipeRepository recipeRepository,
                            CategoryRepository categoryRepository,
                            IngredientDictionaryRepository ingredientDictionaryRepository,
                            UserRepository userRepository) {
        this.recipeService = recipeService;
        this.objectMapper = objectMapper;
        this.recipeRepository = recipeRepository;
        this.categoryRepository= categoryRepository;
        this.ingredientDictionaryRepository = ingredientDictionaryRepository;
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
    public ResponseEntity<Double> rateRecipe(@PathVariable Long id, @RequestParam int rating) {
        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().build();
        }
        try {
            double newAverage = recipeService.addRating(id, rating);
            return ResponseEntity.ok(newAverage);
        } catch (Exception e) {
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecipe(@PathVariable Long id, @RequestBody RecipeDTO updatedRecipeDto, @RequestParam Long userId) {
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ви можете редагувати лише свої рецепти");
        }

        recipe.setTitle(updatedRecipeDto.getTitle());
        recipe.setDescription(updatedRecipeDto.getDescription());

        recipe.getInstructions().clear();
        org.example.recipebookserver.model.Instruction newInstruction = new org.example.recipebookserver.model.Instruction();
        newInstruction.setText(updatedRecipeDto.getInstruction());
        newInstruction.setRecipe(recipe);
        newInstruction.setStepNumber(1);
        recipe.getInstructions().add(newInstruction);

        recipe.getIngredients().clear();
        recipeRepository.saveAndFlush(recipe);
        if (updatedRecipeDto.getIngredients() != null) {
            for (org.example.recipebookserver.DTO.IngredientDTO ingDto : updatedRecipeDto.getIngredients()) {
                org.example.recipebookserver.model.IngredientDictionary dictItem =
                        ingredientDictionaryRepository.findByName(ingDto.getName()).orElse(null);

                if (dictItem == null) {
                    dictItem = new org.example.recipebookserver.model.IngredientDictionary();
                    dictItem.setName(ingDto.getName());
                    dictItem = ingredientDictionaryRepository.save(dictItem);
                }

                org.example.recipebookserver.model.RecipeIngredient newIngredient = new org.example.recipebookserver.model.RecipeIngredient();
                newIngredient.setIngredient(dictItem);
                newIngredient.setQuantity(ingDto.getQuantity());
                newIngredient.setRecipe(recipe);

                recipe.getIngredients().add(newIngredient);
            }
        }

        // ОНОВЛЕНО: Оновлюємо список категорій
        recipe.getCategories().clear();
        if (updatedRecipeDto.getCategoryNames() != null) {
            for (String catName : updatedRecipeDto.getCategoryNames()) {
                Category cat = categoryRepository.findByName(catName).orElse(null);
                if (cat != null) {
                    recipe.getCategories().add(cat);
                }
            }
        }

        recipeRepository.save(recipe);
        return ResponseEntity.ok(recipe);
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
