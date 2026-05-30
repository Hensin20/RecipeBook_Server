package org.example.recipebookserver.service;

import org.example.recipebookserver.DTO.FavoriteDTO;
import org.example.recipebookserver.DTO.RecipeDTO;
import org.example.recipebookserver.model.Favorite;
import org.example.recipebookserver.model.Recipe;
import org.example.recipebookserver.model.User;
import org.example.recipebookserver.repository.FavoriteRepository;
import org.example.recipebookserver.repository.RecipeRepository;
import org.example.recipebookserver.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeService recipeService;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           UserRepository userRepository,
                           RecipeRepository recipeRepository,
                           RecipeService recipeService) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
        this.recipeService = recipeService;
    }

    public List<FavoriteDTO> getUserFavorites(String username) {
        return favoriteRepository.findByUserUsername(username).stream()
                .map(this::mapToFavoriteDTO)
                .collect(Collectors.toList());
    }

    // ОНОВЛЕНО: Додано collectionName
    @Transactional
    public void add(String username, Long recipeId, String collectionName) {

        // Якщо папку не вказали, зберігаємо в дефолтну
        if (collectionName == null || collectionName.trim().isEmpty()) {
            collectionName = "Улюблені";
        }

        // Захист від дублікатів (щоб не додати двічі в одну і ту ж папку)
        if (favoriteRepository.existsByUserUsernameAndRecipeIdAndCollectionName(username, recipeId, collectionName)) {
            return;
        }

        User user = userRepository.findFirstByUsername(username)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Рецепт не знайдено"));

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setRecipe(recipe);
        favorite.setCollectionName(collectionName); // Встановлюємо папку

        favoriteRepository.save(favorite);
    }

    // ОНОВЛЕНО: Видалення з конкретної папки
    @Transactional
    public void remove(String username, Long recipeId, String collectionName) {
        if (collectionName == null || collectionName.trim().isEmpty()) {
            collectionName = "Улюблені";
        }
        favoriteRepository.deleteByUserUsernameAndRecipeIdAndCollectionName(username, recipeId, collectionName);
    }

    private FavoriteDTO mapToFavoriteDTO(Favorite favorite) {
        FavoriteDTO dto = new FavoriteDTO();
        dto.setId(favorite.getId());
        dto.setAddedAt(favorite.getCreatedAt());

        // Передаємо назву папки на Android
        dto.setCollectionName(favorite.getCollectionName());

        RecipeDTO recipeDTO = recipeService.mapToDTO(favorite.getRecipe());
        dto.setRecipe(recipeDTO);

        return dto;
    }
}