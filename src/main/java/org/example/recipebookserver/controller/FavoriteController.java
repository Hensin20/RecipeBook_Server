package org.example.recipebookserver.controller;

import org.example.recipebookserver.DTO.FavoriteDTO;
import org.example.recipebookserver.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // Повертаємо FavoriteDTO замість RecipeDTO
    @GetMapping("/{username}")
    public ResponseEntity<List<FavoriteDTO>> getFavorites(@PathVariable String username) {
        return ResponseEntity.ok(favoriteService.getUserFavorites(username));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addFavorite(@RequestParam String username, @RequestParam Long recipeId) {
        favoriteService.add(username, recipeId);
        return ResponseEntity.ok("Рецепт додано до закладок");
    }

    // Додав метод для видалення
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFavorite(@RequestParam String username, @RequestParam Long recipeId) {
        favoriteService.remove(username, recipeId);
        return ResponseEntity.ok("Рецепт видалено із закладок");
    }
}