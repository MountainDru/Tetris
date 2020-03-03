/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.bejinariu.models;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;

/**
 *
 * @author Dru
 */
public class Piece {
    private final List<Integer[]> combinations = new ArrayList<>(); 
    private Integer id; 
    private Color blockColor; 
    private double cost; 
    

    public Piece(Integer id, Color blockColor, double cost) {
        this.id = id;
        this.cost = cost;
        this.blockColor = blockColor;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
    
    
    public boolean addCombination(Integer[] combination){
        return this.combinations.add(combination); 
    }
    
    public void clearCombinations(){
        this.combinations.clear();
    }

    public List<Integer[]> getCombinations() {
        return new ArrayList<>(this.combinations); 
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Color getBlockColor() {
        return blockColor;
    }

    public void setBlockColor(Color blockColor) {
        this.blockColor = blockColor;
    }
    
    
}
