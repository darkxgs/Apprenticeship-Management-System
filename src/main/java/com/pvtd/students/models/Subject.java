package com.pvtd.students.models;

public class Subject {
    private int id;
    private int specializationId;
    private String name;
    private String type;
    private int passMark;
    private int maxMark;

    public Subject(int id, int specializationId, String name, String type, int passMark, int maxMark) {
        this.id = id;
        this.specializationId = specializationId;
        this.name = name;
        this.type = type;
        this.passMark = passMark;
        this.maxMark = maxMark;
    }

    public int getId() {
        return id;
    }

    public int getSpecializationId() {
        return specializationId;
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
}
