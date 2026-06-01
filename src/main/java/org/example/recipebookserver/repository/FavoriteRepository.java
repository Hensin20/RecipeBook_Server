package org.example.recipebookserver.repository;

import org.example.recipebookserver.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserUsername(String username);

    boolean existsByUserUsernameAndRecipeIdAndCollectionName(String username, Long recipeId, String collectionName);

    void deleteByUserUsernameAndRecipeIdAndCollectionName(String username, Long recipeId, String collectionName);

    // НОВІ МЕТОДИ ДЛЯ ПАПОК:
    List<Favorite> findByUserUsernameAndCollectionName(String username, String collectionName);

    void deleteByUserUsernameAndCollectionName(String username, String collectionName);
}