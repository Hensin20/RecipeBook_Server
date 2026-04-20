package org.example.recipebookserver.DTO;

import java.util.List;

public class RecipeDTO {
    private Long id;
    private String title;
    private String description;
    private double averageRating;
    private int votesCount;
    private String categoryName;
    private String authorName;
    private List<IngredientDTO> ingredients;
    private String instruction;
    private List<String> imageUrls;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
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

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getVotesCount() {
        return votesCount;
    }

    public void setVotesCount(int votesCount) {
        this.votesCount = votesCount;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public List<IngredientDTO> getIngredients() { return ingredients; }
    public void setIngredients(List<IngredientDTO> ingredients) { this.ingredients = ingredients; }
}

