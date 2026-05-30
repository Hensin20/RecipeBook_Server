package org.example.recipebookserver.model;

import jakarta.persistence.*;

@Entity
@Table(name = "recipe_ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipe_id")
    private Long recipeId;

    @Column(name = "user_id")
    private Long userId;

    private int score;

    // Порожній конструктор
    public Rating() {}

    // Геттери та Сеттери
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}