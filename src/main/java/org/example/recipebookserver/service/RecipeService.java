package org.example.recipebookserver.service;

import org.example.recipebookserver.DTO.IngredientDTO;
import org.example.recipebookserver.DTO.RecipeCreateDTO;
import org.example.recipebookserver.DTO.RecipeDTO;

import org.example.recipebookserver.model.*;
import org.example.recipebookserver.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final IngredientDictionaryRepository ingredientDictionaryRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public RecipeService(RecipeRepository recipeRepository, IngredientDictionaryRepository ingredientDictionaryRepository,
                         RecipeIngredientRepository recipeIngredientRepository, CategoryRepository categoryRepository, UserRepository userRepository) {

        this.recipeRepository = recipeRepository;
        this.ingredientDictionaryRepository = ingredientDictionaryRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
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

    public List<RecipeDTO> findByTitle(String title) {
        return recipeRepository.findByTitle(title).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<RecipeDTO> findByCategory(String category) {
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

    public Recipe createRecipe(RecipeCreateDTO dto) {
        Recipe recipe = new Recipe();
        recipe.setTitle(dto.getTitle());
        recipe.setDescription(dto.getDescription());
        recipe.setAverageRating(0.0); // дефолтне значення

        // Категорія
        Category category = categoryRepository.findByName(dto.getCategoryName())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        recipe.setCategory(category);

        // Автор
        User author = userRepository.findByUsername(dto.getAuthorName())
                .orElseThrow(() -> new RuntimeException("Author not found"));
        recipe.setAuthor(author);

        recipeRepository.save(recipe);

        // ✅ Додаємо інгредієнти
        for (IngredientDTO ing : dto.getIngredients()) {
            // шукаємо інгредієнт у словнику
            IngredientDictionary dict = ingredientDictionaryRepository.findByName(ing.getName())
                    .orElseGet(() -> {
                        // якщо немає — створюємо новий
                        IngredientDictionary newIng = new IngredientDictionary();
                        newIng.setName(ing.getName());
                        return ingredientDictionaryRepository.save(newIng);
                    });

            // створюємо зв’язок рецепт ↔ інгредієнт
            RecipeIngredient ri = new RecipeIngredient();
            ri.setRecipe(recipe);
            ri.setIngredient(dict);
            ri.setQuantity(ing.getQuantity());

            recipeIngredientRepository.save(ri);
        }

        return recipe;
    }


    public RecipeDTO getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        RecipeDTO dto = new RecipeDTO();
        dto.setId(recipe.getId());
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());
        dto.setAverageRating(recipe.getAverageRating());
        dto.setCategoryName(recipe.getCategory().getName());
        dto.setAuthorName(recipe.getAuthor().getUsername());

        List<IngredientDTO> ingredients = recipeIngredientRepository.findByRecipeId(recipe.getId())
                .stream()
                .map(ri -> {
                    IngredientDTO ingDto = new IngredientDTO();
                    ingDto.setName(ri.getIngredient().getName());
                    ingDto.setQuantity(ri.getQuantity());
                    return ingDto;
                })
                .toList();

        // можна додати поле ingredients у RecipeDTO, якщо хочеш бачити їх у відповіді
        dto.setIngredients(ingredients);

        return dto;
    }


}

