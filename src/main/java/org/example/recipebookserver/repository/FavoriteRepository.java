package org.example.recipebookserver.repository;

import org.example.recipebookserver.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserUsername(String username);

    // ОНОВЛЕНО: Тепер перевіряємо дублікати не просто за рецептом, а в конкретній папці
    boolean existsByUserUsernameAndRecipeIdAndCollectionName(String username, Long recipeId, String collectionName);

    // ОНОВЛЕНО: Видаляємо рецепт із конкретної папки
    void deleteByUserUsernameAndRecipeIdAndCollectionName(String username, Long recipeId, String collectionName);
}