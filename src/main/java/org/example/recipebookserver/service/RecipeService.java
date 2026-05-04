package org.example.recipebookserver.service;

import org.example.recipebookserver.DTO.IngredientDTO;
import org.example.recipebookserver.DTO.RecipeCreateDTO;
import org.example.recipebookserver.DTO.RecipeDTO;
import org.example.recipebookserver.model.*;
import org.example.recipebookserver.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    private final RecipeRepository recipeRepository;
    private final IngredientDictionaryRepository ingredientDictionaryRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RecipeImageRepository recipeImageRepository;
    private final InstructionRepository instructionRepository;

    public RecipeService(RecipeRepository recipeRepository,
                         IngredientDictionaryRepository ingredientDictionaryRepository,
                         RecipeIngredientRepository recipeIngredientRepository,
                         CategoryRepository categoryRepository,
                         UserRepository userRepository,
                         RecipeImageRepository recipeImageRepository,
                         InstructionRepository instructionRepository) {

        this.recipeRepository = recipeRepository;
        this.ingredientDictionaryRepository = ingredientDictionaryRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.recipeImageRepository = recipeImageRepository;
        this.instructionRepository = instructionRepository;
    }

    public List<RecipeDTO> getAllRecipes() {
        return recipeRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<RecipeDTO> searchByIngredients(String ingredientsString) {
        List<String> names = Arrays.stream(ingredientsString.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        List<Recipe> recipes = recipeRepository.findByIngredientsMatch(names, (long) names.size());

        return recipes.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<RecipeDTO> findByTitle(String title) {
        return recipeRepository.findByTitle(title).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<RecipeDTO> findByCategory(String category) {
        return recipeRepository.findByCategoriesName(category).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public RecipeDTO mapToDTO(Recipe recipe) {
        RecipeDTO dto = new RecipeDTO();
        dto.setId(recipe.getId());
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());
        dto.setAverageRating(recipe.getAverageRating());
        dto.setVotesCount(recipe.getVotesCount() != null ? recipe.getVotesCount() : 0);

        // ОНОВЛЕНО: Беремо список категорій і перетворюємо їх на список назв
        List<String> catNames = recipe.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toList());
        dto.setCategoryNames(catNames);

        dto.setAuthorName(recipe.getAuthor() != null ? recipe.getAuthor().getUsername() : null);

        List<String> urls = recipeImageRepository.findByRecipeId(recipe.getId())
                .stream()
                .map(RecipeImage::getImageUrl)
                .toList();
        dto.setImageUrls(urls);

        return dto;
    }

    @Transactional
    public Recipe createRecipe(RecipeCreateDTO dto, List<MultipartFile> images) throws IOException {
        Recipe recipe = new Recipe();
        recipe.setTitle(dto.getTitle());
        recipe.setDescription(dto.getDescription());
        recipe.setAverageRating(0.0);

        // ОНОВЛЕНО: Додаємо кілька категорій
        if (dto.getCategoryNames() != null) {
            for (String catName : dto.getCategoryNames()) {
                Category cat = categoryRepository.findByName(catName).orElse(null);
                if (cat != null) {
                    recipe.getCategories().add(cat);
                }
            }
        }

        User author = userRepository.findByUsername(dto.getAuthorName())
                .orElseThrow(() -> new RuntimeException("Author not found"));
        recipe.setAuthor(author);

        recipe = recipeRepository.save(recipe);

        if (dto.getInstruction() != null && !dto.getInstruction().isEmpty()) {
            Instruction instruction = new Instruction();
            instruction.setRecipe(recipe);
            instruction.setStepNumber(1);
            instruction.setText(dto.getInstruction());
            instructionRepository.save(instruction);
        }

        if (dto.getIngredients() != null) {
            for (IngredientDTO ing : dto.getIngredients()) {
                IngredientDictionary dict = ingredientDictionaryRepository.findByName(ing.getName())
                        .orElseGet(() -> {
                            IngredientDictionary newIng = new IngredientDictionary();
                            newIng.setName(ing.getName());
                            return ingredientDictionaryRepository.save(newIng);
                        });

                RecipeIngredient ri = new RecipeIngredient();
                ri.setRecipe(recipe);
                ri.setIngredient(dict);
                ri.setQuantity(ing.getQuantity());

                recipeIngredientRepository.save(ri);
            }
        }

        if (images != null && !images.isEmpty()) {
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            for (MultipartFile file : images) {
                if (file.isEmpty()) continue;
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);
                Files.write(filePath, file.getBytes());

                RecipeImage recipeImage = new RecipeImage();
                recipeImage.setRecipe(recipe);
                recipeImage.setImageUrl(fileName);
                recipeImageRepository.save(recipeImage);
            }
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
        dto.setVotesCount(recipe.getVotesCount() != null ? recipe.getVotesCount() : 0);

        // ОНОВЛЕНО: Беремо список категорій
        List<String> catNames = recipe.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toList());
        dto.setCategoryNames(catNames);

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
        dto.setIngredients(ingredients);

        List<Instruction> instructionsList = instructionRepository.findByRecipeIdOrderByStepNumberAsc(recipe.getId());
        if (!instructionsList.isEmpty()) {
            dto.setInstruction(instructionsList.get(0).getText());
        }

        List<String> urls = recipeImageRepository.findByRecipeId(recipe.getId())
                .stream()
                .map(RecipeImage::getImageUrl)
                .toList();
        dto.setImageUrls(urls);

        return dto;
    }

    @Transactional
    public double addRating(Long recipeId, int newRating) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        double currentAvg = recipe.getAverageRating() != null ? recipe.getAverageRating() : 0.0;
        int count = recipe.getVotesCount() != null ? recipe.getVotesCount() : 0;
        double updatedAvg = ((currentAvg * count) + newRating) / (count + 1);

        recipe.setAverageRating(updatedAvg);
        recipe.setVotesCount(count + 1);
        recipeRepository.save(recipe);

        return updatedAvg;
    }

    public List<RecipeDTO> getRecipesByAuthor(String username) {
        return recipeRepository.findByAuthorUsername(username).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<RecipeDTO> searchRecipes(String query) {
        List<Recipe> recipes = recipeRepository.findByTitleContainingIgnoreCase(query);
        return recipes.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}