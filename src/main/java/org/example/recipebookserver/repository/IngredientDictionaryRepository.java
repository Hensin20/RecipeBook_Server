package org.example.recipebookserver.repository;

import org.example.recipebookserver.model.IngredientDictionary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngredientDictionaryRepository extends JpaRepository<IngredientDictionary, Long> {
    Optional<IngredientDictionary> findByName(String name);

}
