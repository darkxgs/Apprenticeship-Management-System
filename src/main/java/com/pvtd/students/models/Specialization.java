package com.pvtd.students.models;

public class Specialization {
    private int id;
    private int departmentId;
    private String name;
    private String description;

    public Specialization(int id, int departmentId, String name, String description) {
        this.id = id;
        this.departmentId = departmentId;
        this.name = name;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
