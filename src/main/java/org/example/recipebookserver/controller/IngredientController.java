package org.example.recipebookserver.controller;

import org.example.recipebookserver.DTO.IngredientDTO;
import org.example.recipebookserver.model.IngredientDictionary;
import org.example.recipebookserver.repository.IngredientDictionaryRepository;
import org.example.recipebookserver.service.IngredientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {
    private final IngredientDictionaryRepository repository;

    // Конструктор для інжекції
    public IngredientController(IngredientDictionaryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<IngredientDTO> getAllIngredients() {
        return repository.findAll()
                .stream()
                .map(entity -> {
                    IngredientDTO dto = new IngredientDTO();
                    dto.setName(entity.getName());
                    return dto;
                })
                .toList();
    }

    @PostMapping
    public IngredientDTO addIngredient(@RequestBody IngredientDTO dto) {
        IngredientDictionary entity = new IngredientDictionary();
        entity.setName(dto.getName());
        repository.save(entity);
        return dto;
    }
}


