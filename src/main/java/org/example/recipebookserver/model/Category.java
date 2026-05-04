package org.example.recipebookserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // --- ОНОВЛЕНО: Тепер це ManyToMany і вказує на нове поле "categories" у Recipe ---
    @ManyToMany(mappedBy = "categories")
    @JsonIgnore // Це важливо, щоб сервер не завис при перетворенні в JSON (нескінченний цикл)
    private List<Recipe> recipes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }
}