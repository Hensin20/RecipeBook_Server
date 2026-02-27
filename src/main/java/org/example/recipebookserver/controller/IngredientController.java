package org.example.recipebookserver.controller;

import org.example.recipebookserver.model.IngredientDictionary;
import org.example.recipebookserver.repository.IngredientDictionaryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients") // Окремий шлях!
public class IngredientController {

    private final IngredientDictionaryRepository ingredientDictionaryRepository;

    public IngredientController(IngredientDictionaryRepository ingredientDictionaryRepository) {
        this.ingredientDictionaryRepository = ingredientDictionaryRepository;
    }

    @GetMapping
    public List<IngredientDictionary> getAllIngredients() {
        return ingredientDictionaryRepository.findAll();
    }
}