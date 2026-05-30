package org.example.recipebookserver.DTO;

import java.time.LocalDateTime;

public class FavoriteDTO {
    private Long id;
    private LocalDateTime addedAt;

    // ДОДАНО: Щоб Android знав, в якій папці лежить рецепт
    private String collectionName;

    private RecipeDTO recipe;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public RecipeDTO getRecipe() {
        return recipe;
    }

    public void setRecipe(RecipeDTO recipe) {
        this.recipe = recipe;
    }
}