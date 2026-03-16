package com.pvtd.students.models;

public class Subject {
    private int id;
    private String profession;
    private String name;
    private String type;
    private int passMark;
    private int maxMark;
    private int displayOrder;

    public Subject(int id, String profession, String name, String type, int passMark, int maxMark, int displayOrder) {
        this.id = id;
        this.profession = profession;
        this.name = name;
        this.type = type;
        this.passMark = passMark;
        this.maxMark = maxMark;
        this.displayOrder = displayOrder;
    }

    public int getId() {
        return id;
    }

    public String getProfession() {
        return profession;
    }

    public String getName() {
        return name;
    }

    public int getPassMark() {
        return passMark;
    }

    public int getMaxMark() {
        return maxMark;
    }

    public String getType() {
        return type;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
