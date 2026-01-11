package org.example.recipebookserver.service;

import org.example.recipebookserver.DTO.IngredientDTO;
import org.example.recipebookserver.model.IngredientDictionary;
import org.example.recipebookserver.repository.IngredientDictionaryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IngredientService {
    private final IngredientDictionaryRepository repository;

    // Конструктор для інжекції
    public IngredientService(IngredientDictionaryRepository repository) {
        this.repository = repository;
    }

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

    public IngredientDTO addIngredient(IngredientDTO dto) {
        IngredientDictionary entity = new IngredientDictionary();
        entity.setName(dto.getName());
        repository.save(entity);
        return dto;
    }
}

