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
import java.util.Optional;
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
    private final RatingRepository ratingRepository;

    public RecipeService(RecipeRepository recipeRepository,
                         IngredientDictionaryRepository ingredientDictionaryRepository,
                         RecipeIngredientRepository recipeIngredientRepository,
                         CategoryRepository categoryRepository,
                         UserRepository userRepository,
                         RecipeImageRepository recipeImageRepository,
                         InstructionRepository instructionRepository,
                         RatingRepository ratingRepository) {

        this.recipeRepository = recipeRepository;
        this.ingredientDictionaryRepository = ingredientDictionaryRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.recipeImageRepository = recipeImageRepository;
        this.instructionRepository = instructionRepository;
        this.ratingRepository = ratingRepository;
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

    // --- НОВИЙ МЕТОД ОНОВЛЕННЯ РЕЦЕПТА З ФОТОГРАФІЯМИ ---
    @Transactional
    public RecipeDTO updateRecipe(Long recipeId, Long userId, RecipeDTO updatedRecipeDto, List<MultipartFile> newImages) throws IOException {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Рецепт не знайдено"));

        User requestUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

        boolean isAuthor = (recipe.getAuthor() != null) && recipe.getAuthor().getId().equals(userId);
        boolean isAdmin = requestUser.isAdmin();

        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("Ви можете редагувати лише свої рецепти");
        }

        // Оновлюємо базові поля
        recipe.setTitle(updatedRecipeDto.getTitle());
        recipe.setDescription(updatedRecipeDto.getDescription());

        // Оновлюємо інструкцію
        recipe.getInstructions().clear();
        Instruction newInstruction = new Instruction();
        newInstruction.setText(updatedRecipeDto.getInstruction());
        newInstruction.setRecipe(recipe);
        newInstruction.setStepNumber(1);
        recipe.getInstructions().add(newInstruction);

        // Оновлюємо інгредієнти
        recipe.getIngredients().clear();
        recipeRepository.saveAndFlush(recipe);
        if (updatedRecipeDto.getIngredients() != null) {
            for (IngredientDTO ingDto : updatedRecipeDto.getIngredients()) {
                IngredientDictionary dictItem = ingredientDictionaryRepository.findByName(ingDto.getName()).orElse(null);
                if (dictItem == null) {
                    dictItem = new IngredientDictionary();
                    dictItem.setName(ingDto.getName());
                    dictItem = ingredientDictionaryRepository.save(dictItem);
                }

                RecipeIngredient newIngredient = new RecipeIngredient();
                newIngredient.setIngredient(dictItem);
                newIngredient.setQuantity(ingDto.getQuantity());
                newIngredient.setRecipe(recipe);
                recipe.getIngredients().add(newIngredient);
            }
        }

        // Оновлюємо категорії
        recipe.getCategories().clear();
        if (updatedRecipeDto.getCategoryNames() != null) {
            for (String catName : updatedRecipeDto.getCategoryNames()) {
                Category cat = categoryRepository.findByName(catName).orElse(null);
                if (cat != null) {
                    recipe.getCategories().add(cat);
                }
            }
        }

        // --- ЛОГІКА ДЛЯ ФОТОГРАФІЙ ---
        // 1. Видаляємо ті фото, яких більше немає у списку від клієнта
        if (updatedRecipeDto.getImageUrls() != null) {
            List<String> remainingUrls = updatedRecipeDto.getImageUrls();
            recipe.getImages().removeIf(img -> !remainingUrls.contains(img.getImageUrl()));
        } else {
            recipe.getImages().clear();
        }

        // 2. Зберігаємо нові фото
        if (newImages != null && !newImages.isEmpty()) {
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            for (MultipartFile file : newImages) {
                if (file.isEmpty()) continue;
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);
                Files.write(filePath, file.getBytes());

                RecipeImage recipeImage = new RecipeImage();
                recipeImage.setRecipe(recipe);
                recipeImage.setImageUrl(fileName);
                recipe.getImages().add(recipeImage);
            }
        }

        recipe = recipeRepository.save(recipe);
        return mapToDTO(recipe);
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
    public double addRating(Long recipeId, Long userId, int newRating) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        Optional<Rating> existingRating = ratingRepository.findByRecipeIdAndUserId(recipeId, userId);

        if (existingRating.isPresent()) {
            Rating rating = existingRating.get();
            rating.setScore(newRating);
            ratingRepository.save(rating);
        } else {
            Rating rating = new Rating();
            rating.setRecipeId(recipeId);
            rating.setUserId(userId);
            rating.setScore(newRating);
            ratingRepository.save(rating);
        }

        Double avg = ratingRepository.getAverageRatingByRecipeId(recipeId);
        Integer count = ratingRepository.countRatingsByRecipeId(recipeId);

        double roundedAvg = Math.round(avg * 10.0) / 10.0;

        recipe.setAverageRating(roundedAvg);
        recipe.setVotesCount(count);
        recipeRepository.save(recipe);

        return roundedAvg;
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