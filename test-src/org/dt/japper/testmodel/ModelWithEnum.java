package org.dt.japper.testmodel;

public class ModelWithEnum {
    private String id;

    /**
     * Enum value we want to make sure our property matcher ignores, instead of
     * blowing up on
     */
    private PossibleValues type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PossibleValues getType() {
        return type;
    }

    public void setType(PossibleValues type) {
        this.type = type;
    }
}
