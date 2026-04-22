package org.example.recipebookserver.repository;

import org.example.recipebookserver.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserUsername(String username);
    boolean existsByUserUsernameAndRecipeId(String username, Long recipeId);
    void deleteByUserUsernameAndRecipeId(String username, Long recipeId);
}
