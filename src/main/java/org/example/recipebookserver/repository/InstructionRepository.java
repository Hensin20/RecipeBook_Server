package org.example.recipebookserver.repository;

import org.example.recipebookserver.model.Instruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstructionRepository extends JpaRepository<Instruction, Long> {
    // Метод, щоб дістати всі кроки рецепту, одразу відсортовані по порядку (1, 2, 3...)
    List<Instruction> findByRecipeIdOrderByStepNumberAsc(Long recipeId);
}