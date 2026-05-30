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

    @GetMapping("/{username}")
    public ResponseEntity<List<FavoriteDTO>> getFavorites(@PathVariable String username) {
        return ResponseEntity.ok(favoriteService.getUserFavorites(username));
    }

    // ОНОВЛЕНО: Приймаємо collectionName (необов'язковий параметр)
    @PostMapping("/add")
    public ResponseEntity<?> addFavorite(
            @RequestParam String username,
            @RequestParam Long recipeId,
            @RequestParam(required = false, defaultValue = "Улюблені") String collectionName) {

        favoriteService.add(username, recipeId, collectionName);
        return ResponseEntity.ok("Рецепт додано до закладок");
    }

    // ОНОВЛЕНО: Приймаємо collectionName для видалення
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFavorite(
            @RequestParam String username,
            @RequestParam Long recipeId,
            @RequestParam(required = false, defaultValue = "Улюблені") String collectionName) {

        favoriteService.remove(username, recipeId, collectionName);
        return ResponseEntity.ok("Рецепт видалено із закладок");
    }
}