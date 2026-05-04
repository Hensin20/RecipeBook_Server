package org.example.recipebookserver.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table (name = "recipes")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Double averageRating = 0.0;

    @Column(columnDefinition = "integer default 0")
    private Integer votesCount = 0;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    // --- ОНОВЛЕНО: Тепер рецепт може мати багато категорій ---
    @ManyToMany
    @JoinTable(
            name = "recipe_categories",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<RecipeImage> images;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Instruction> instructions;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public Integer getVotesCount() {
        return votesCount;
    }

    public void setVotesCount(Integer votesCount) {
        this.votesCount = votesCount;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    // --- ОНОВЛЕНІ GETTER та SETTER для категорій ---
    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }
}