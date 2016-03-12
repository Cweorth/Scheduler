package cz.muni.fi.scheduler.data.builders;

import cz.muni.fi.scheduler.data.Field;

public class FieldBuilder {
    private long id;
    private String name;
    private String code;

    //<editor-fold desc="[  Getters  ]" defaultstate="collapsed">

    public long   getId()   { return id;   }
    public String getName() { return name; }
    public String getCode() { return code; }

    //</editor-fold>

    //<editor-fold desc="[  Setters  ]" defaultstate="collapsed">

    public FieldBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public FieldBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public FieldBuilder setCode(String code) {
        this.code = code;
        return this;
    }

    //</editor-fold>

    public Field value() {
        return new Field(id, name, code);
    }

}
