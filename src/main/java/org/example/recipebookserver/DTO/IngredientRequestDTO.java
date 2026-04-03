package org.example.recipebookserver.DTO;
public class IngredientRequestDTO {
    private String name;
    private String quantity;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
}