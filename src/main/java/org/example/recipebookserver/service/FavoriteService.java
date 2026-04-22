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

@Service // Обов'язкова анотація, щоб Spring бачив цей клас
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeService recipeService; // Підключаємо для мапінгу

    public FavoriteService(FavoriteRepository favoriteRepository,
                           UserRepository userRepository,
                           RecipeRepository recipeRepository,
                           RecipeService recipeService) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
        this.recipeService = recipeService;
    }

    // Отримання списку закладок користувача
    public List<FavoriteDTO> getUserFavorites(String username) {
        return favoriteRepository.findByUserUsername(username).stream()
                .map(this::mapToFavoriteDTO)
                .collect(Collectors.toList());
    }

    // Додавання в закладки
    @Transactional
    public void add(String username, Long recipeId) {
        // Захист від дублікатів (щоб не додати двічі)
        if (favoriteRepository.existsByUserUsernameAndRecipeId(username, recipeId)) {
            return;
        }

        User user = userRepository.findFirstByUsername(username)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Рецепт не знайдено"));

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setRecipe(recipe);

        favoriteRepository.save(favorite);
    }

    // Видалення із закладок
    @Transactional
    public void remove(String username, Long recipeId) {
        favoriteRepository.deleteByUserUsernameAndRecipeId(username, recipeId);
    }

    // Мапінг у DTO
    private FavoriteDTO mapToFavoriteDTO(Favorite favorite) {
        FavoriteDTO dto = new FavoriteDTO();
        dto.setId(favorite.getId());
        dto.setAddedAt(favorite.getCreatedAt());

        // Викликаємо публічний метод мапінгу з RecipeService
        RecipeDTO recipeDTO = recipeService.mapToDTO(favorite.getRecipe());
        dto.setRecipe(recipeDTO);

        return dto;
    }
}