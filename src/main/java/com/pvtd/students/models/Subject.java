package com.pvtd.students.models;

public class Subject {
    private int id;
    private String profession;
    private String name;
    private String type;
    private int passMark;
    private int maxMark;
    private int displayOrder;
    private Integer parentSubjectId; // For 30/70 composite subjects
    private String subName;          // Display name for the sub-subject (e.g. "نظري" or "تحريري")

    public Subject(int id, String profession, String name, String type, int passMark, int maxMark, int displayOrder) {
        this(id, profession, name, type, passMark, maxMark, displayOrder, null, null);
    }

    public Subject(int id, String profession, String name, String type, int passMark, int maxMark, int displayOrder, Integer parentSubjectId, String subName) {
        this.id = id;
        this.profession = profession;
        this.name = name;
        this.type = type;
        this.passMark = passMark;
        this.maxMark = maxMark;
        this.displayOrder = displayOrder;
        this.parentSubjectId = parentSubjectId;
        this.subName = subName;
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

    public Integer getParentSubjectId() {
        return parentSubjectId;
    }

    public String getSubName() {
        return subName;
    }
}
