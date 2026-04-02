package com.pvtd.students.models;

public class Student {
    private int id;
    private String serial; // مسلسل
    private String name; // الاسم
    private String registrationNo; // رقم التسجيل
    private String nationalId; // الرقم القومى
    private String region; // المنطقة
    private String profession; // المهنة
    private String examSystem; // النظام
    private String seatNo; // رقم الجلوس
    private String secretNo; // الرقم السرى
    private String professionalGroup; // المجموعة المهنية
    private String coordinationNo; // رقم التنسيق

    // DOB Parts
    private String dobDay;
    private String dobMonth;
    private String dobYear;

    private String gender; // النوع
    private String neighborhood; // حي / قرية
    private String governorate; // محافظة
    private String religion; // ديانة
    private String nationality; // جنسية
    private String address; // عنوان
    private String otherNotes; // اخري
    private String imagePath; // مسار الصورة (الصورة الشخصية pic)

    // V3 Added Fields
    private String centerName; // اسم المركز
    private String idFrontPath; // وجه البطاقة
    private String idBackPath; // ظهر البطاقة

    // Grades (subject_id -> obtained_mark)
    private java.util.Map<Integer, Integer> grades;

    // Status
    private String status; // حالة الطالب (ناجح/راسب/دور ثان/غائب...)

    // V4 Added Fields
    private String phoneNumber; // رقم التليفون

    // Constructor
    public Student(int id, String serial, String name, String registrationNo, String nationalId,
            String region, String centerName, String profession, String examSystem,
            String seatNo, String secretNo, String professionalGroup, String coordinationNo,
            String dobDay, String dobMonth, String dobYear, String gender, String neighborhood,
            String governorate, String religion, String nationality, String address,
            String otherNotes, String imagePath, String idFrontPath, String idBackPath,
            java.util.Map<Integer, Integer> grades, String status) {
        this.id = id;
        this.serial = serial;
        this.name = name;
        this.registrationNo = registrationNo;
        this.nationalId = nationalId;
        this.region = region;
        this.centerName = centerName;
        this.profession = profession;
        this.examSystem = examSystem;
        this.seatNo = seatNo;
        this.secretNo = secretNo;
        this.professionalGroup = professionalGroup;
        this.coordinationNo = coordinationNo;
        this.dobDay = dobDay;
        this.dobMonth = dobMonth;
        this.dobYear = dobYear;
        this.gender = gender;
        this.neighborhood = neighborhood;
        this.governorate = governorate;
        this.religion = religion;
        this.nationality = nationality;
        this.address = address;
        this.otherNotes = otherNotes;
        this.imagePath = imagePath;
        this.idFrontPath = idFrontPath;
        this.idBackPath = idBackPath;
        this.grades = grades;
        this.status = status;
    }

    // Default Constructor (kept for flexibility, can be removed if not needed)
    public Student() {
    }

    // Getters and Setters ...
    // (To keep file brief initially, we omit boilerplate getters/setters but will
    // generate them via IDE or add them directly if needed.
    // Given the agent setup, we'll write them explicitly to ensure the code
    // compiles without Lombok)

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getExamSystem() {
        return examSystem;
    }

    public void setExamSystem(String examSystem) {
        this.examSystem = examSystem;
    }

    public String getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(String seatNo) {
        this.seatNo = seatNo;
    }

    public String getSecretNo() {
        return secretNo;
    }

    public void setSecretNo(String secretNo) {
        this.secretNo = secretNo;
    }

    public String getProfessionalGroup() {
        return professionalGroup;
    }

    public void setProfessionalGroup(String professionalGroup) {
        this.professionalGroup = professionalGroup;
    }

    public String getCoordinationNo() {
        return coordinationNo;
    }

    public void setCoordinationNo(String coordinationNo) {
        this.coordinationNo = coordinationNo;
    }

    public String getDobDay() {
        return dobDay;
    }

    public void setDobDay(String dobDay) {
        this.dobDay = dobDay;
    }

    public String getDobMonth() {
        return dobMonth;
    }

    public void setDobMonth(String dobMonth) {
        this.dobMonth = dobMonth;
    }

    public String getDobYear() {
        return dobYear;
    }

    public void setDobYear(String dobYear) {
        this.dobYear = dobYear;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getGovernorate() {
        return governorate;
    }

    public void setGovernorate(String governorate) {
        this.governorate = governorate;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOtherNotes() {
        return otherNotes;
    }

    public void setOtherNotes(String otherNotes) {
        this.otherNotes = otherNotes;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCenterName() {
        return centerName;
    }

    public void setCenterName(String centerName) {
        this.centerName = centerName;
    }

    public String getIdFrontPath() {
        return idFrontPath;
    }

    public void setIdFrontPath(String idFrontPath) {
        this.idFrontPath = idFrontPath;
    }

    public String getIdBackPath() {
        return idBackPath;
    }

    public void setIdBackPath(String idBackPath) {
        this.idBackPath = idBackPath;
    }

    public java.util.Map<Integer, Integer> getGrades() {
        return grades;
    }

    public void setGrades(java.util.Map<Integer, Integer> grades) {
        this.grades = grades;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
