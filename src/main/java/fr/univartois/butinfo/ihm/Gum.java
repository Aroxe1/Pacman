package fr.univartois.butinfo.ihm;

public enum Gum {

    PACGUM("pacgum"),
    MEGAGUM("megagum");

    private final String name;

    Gum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
