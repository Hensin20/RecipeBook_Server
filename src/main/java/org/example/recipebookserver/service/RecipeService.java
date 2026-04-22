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
import java.util.List;
import java.util.UUID;

@Service
public class RecipeService {

    // Вказуємо папку для збереження фото з application.properties
    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    private final RecipeRepository recipeRepository;
    private final IngredientDictionaryRepository ingredientDictionaryRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // Нові репозиторії для фото та інструкцій
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

    public RecipeDTO mapToDTO(Recipe recipe) {
        RecipeDTO dto = new RecipeDTO();
        dto.setId(recipe.getId());
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());
        dto.setAverageRating(recipe.getAverageRating());
        dto.setVotesCount(recipe.getVotesCount() != null ? recipe.getVotesCount() : 0);
        dto.setCategoryName(recipe.getCategory() != null ? recipe.getCategory().getName() : null);
        dto.setAuthorName(recipe.getAuthor() != null ? recipe.getAuthor().getUsername() : null);

        List<String> urls = recipeImageRepository.findByRecipeId(recipe.getId())
                .stream()
                .map(RecipeImage::getImageUrl)
                .toList();
        dto.setImageUrls(urls);

        return dto;
    }

    // Додаємо @Transactional, щоб уникнути часткового збереження у разі збою
    @Transactional
    public Recipe createRecipe(RecipeCreateDTO dto, List<MultipartFile> images) throws IOException {
        Recipe recipe = new Recipe();
        recipe.setTitle(dto.getTitle());
        recipe.setDescription(dto.getDescription());
        recipe.setAverageRating(0.0); // дефолтне значення

        // 1. Категорія
        Category category = categoryRepository.findByName(dto.getCategoryName())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        recipe.setCategory(category);

        // 2. Автор
        User author = userRepository.findByUsername(dto.getAuthorName())
                .orElseThrow(() -> new RuntimeException("Author not found"));
        recipe.setAuthor(author);

        // Зберігаємо рецепт, щоб отримати його ID (він потрібен для інгредієнтів, фото та інструкцій)
        recipe = recipeRepository.save(recipe);

        // 3. Інструкція (Зберігаємо як 1-й крок)
        if (dto.getInstruction() != null && !dto.getInstruction().isEmpty()) {
            Instruction instruction = new Instruction();
            instruction.setRecipe(recipe);
            instruction.setStepNumber(1);
            instruction.setText(dto.getInstruction());
            instructionRepository.save(instruction);
        }

        // 4. Додаємо інгредієнти
        if (dto.getIngredients() != null) {
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
        }

        // 5. Зберігаємо фотографії на диск та в БД
        if (images != null && !images.isEmpty()) {
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            for (MultipartFile file : images) {
                if (file.isEmpty()) continue;

                // Генеруємо унікальне ім'я для файлу, щоб уникнути перезапису
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);

                // Зберігаємо фізично на сервер
                Files.write(filePath, file.getBytes());

                // Зберігаємо запис у базу даних
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

        // Захист від старих записів у базі, де ці поля можуть бути null
        double currentAvg = recipe.getAverageRating() != null ? recipe.getAverageRating() : 0.0;
        int count = recipe.getVotesCount() != null ? recipe.getVotesCount() : 0;

        // Формула середнього значення: (старе_середнє * кількість + нова_оцінка) / (кількість + 1)
        double updatedAvg = ((currentAvg * count) + newRating) / (count + 1);

        recipe.setAverageRating(updatedAvg);
        recipe.setVotesCount(count + 1);

        recipeRepository.save(recipe);

        return updatedAvg; // Повертаємо новий рейтинг, щоб показати його на телефоні
    }

    public List<RecipeDTO> getRecipesByAuthor(String username) {
        return recipeRepository.findByAuthorUsername(username).stream()
                .map(this::mapToDTO)
                .toList();
    }


}