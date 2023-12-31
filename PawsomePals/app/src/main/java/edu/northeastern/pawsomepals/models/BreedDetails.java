package edu.northeastern.pawsomepals.models;

public class BreedDetails {
    private BreedWeightDetails weight;
    private BreedHeightDetails height;
    private String id;
    private String name;
    private String bred_for;
    private String breed_group;
    private String life_span;
    private String temperament;
    private String origin;
    private String reference_image_id;
    private BreedImageDetails image;

    private Boolean isExpandable;

    public BreedDetails(BreedWeightDetails weight, BreedHeightDetails height, String id, String name, String bred_for, String breed_group, String life_span, String temperament, String origin, String reference_image_id, BreedImageDetails image) {
        this.weight = weight;
        this.height = height;
        this.id = id;
        this.name = name;
        this.bred_for = bred_for;
        this.breed_group = breed_group;
        this.life_span = life_span;
        this.temperament = temperament;
        this.origin = origin;
        this.reference_image_id = reference_image_id;
        this.image = image;
        this.isExpandable = false;
    }

    public Boolean getExpandable() {
        return isExpandable;
    }

    public void setExpandable(Boolean expandable) {
        isExpandable = expandable;
    }

    public BreedWeightDetails getWeight() {
        return weight;
    }

    public void setWeight(BreedWeightDetails weight) {
        this.weight = weight;
    }

    public BreedHeightDetails getHeight() {
        return height;
    }

    public void setHeight(BreedHeightDetails height) {
        this.height = height;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBred_for() {
        return bred_for;
    }

    public void setBred_for(String bred_for) {
        this.bred_for = bred_for;
    }

    public String getBreed_group() {
        return breed_group;
    }

    public void setBreed_group(String breed_group) {
        this.breed_group = breed_group;
    }

    public String getLife_span() {
        return life_span;
    }

    public void setLife_span(String life_span) {
        this.life_span = life_span;
    }

    public String getTemperament() {
        return temperament;
    }

    public void setTemperament(String temperament) {
        this.temperament = temperament;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getReference_image_id() {
        return reference_image_id;
    }

    public void setReference_image_id(String reference_image_id) {
        this.reference_image_id = reference_image_id;
    }

    public BreedImageDetails getImage() {
        return image;
    }

    public void setImage(BreedImageDetails image) {
        this.image = image;
    }
}
