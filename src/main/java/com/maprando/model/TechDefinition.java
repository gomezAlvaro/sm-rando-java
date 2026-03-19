package com.maprando.model;

import java.util.List;

/**
 * Definition of a tech ability loaded from data.
 * Techs represent capabilities that items can enable or require.
 */
public class TechDefinition {
    private final String id;
    private final String name;
    private final String description;
    private final int index; // Index for boolean array tracking
    private final List<String> requires; // List of tech IDs required for this tech

    /**
     * Creates a new tech definition without requirements.
     */
    public TechDefinition(String id, String name, String description, int index) {
        this(id, name, description, index, null);
    }

    /**
     * Creates a new tech definition with requirements.
     */
    public TechDefinition(String id, String name, String description, int index, List<String> requires) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.index = index;
        this.requires = requires;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getIndex() { return index; }
    public List<String> getRequires() { return requires; }

    @Override
    public String toString() {
        return "TechDefinition{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", index=" + index +
                ", requires=" + requires +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TechDefinition that = (TechDefinition) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
