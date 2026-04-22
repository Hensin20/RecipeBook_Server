package org.example.recipebookserver.DTO;

import org.example.recipebookserver.model.Recipe;
import org.example.recipebookserver.model.User;

import java.time.LocalDateTime;

public class FavoriteDTO {
    private Long id; // ID запису в таблиці favorites
    private RecipeDTO recipe; // Дані самого рецепту (назва, фото, рейтинг)
    private LocalDateTime addedAt; // Дата, коли рецепт було додано в закладки

    // --- Гетери та Сетери ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RecipeDTO getRecipe() { return recipe; }
    public void setRecipe(RecipeDTO recipe) { this.recipe = recipe; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
