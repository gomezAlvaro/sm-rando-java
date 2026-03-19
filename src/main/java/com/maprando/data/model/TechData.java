package com.maprando.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * JSON data model for techs loaded from external files.
 */
public class TechData {
    @JsonProperty("techs")
    private List<TechDefinition> techs;

    public List<TechDefinition> getTechs() {
        return techs;
    }

    public void setTechs(List<TechDefinition> techs) {
        this.techs = techs;
    }

    /**
     * Represents a single tech definition from JSON.
     */
    public static class TechDefinition {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;

        @JsonProperty("index")
        private int index;

        @JsonProperty("requires")
        private List<String> requires;

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getIndex() { return index; }
        public List<String> getRequires() { return requires; }

        // Setters
        public void setId(String id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setDescription(String description) { this.description = description; }
        public void setIndex(int index) { this.index = index; }
        public void setRequires(List<String> requires) { this.requires = requires; }
    }
}
