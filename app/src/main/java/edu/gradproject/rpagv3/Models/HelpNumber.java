package edu.gradproject.rpagv3.Models;

import edu.gradproject.rpagv3.R;

public class HelpNumber {
    private String countryCode;
    private String classCode;
    private String number;
    private String imageUrl;
    private String name;

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getClassIconId() {
        switch (classCode){
            case "police":
                return R.drawable.icon_type_blocked;
            case "firefighters":
                return R.drawable.icon_type_fire;
            case "paramedics":
                return R.drawable.icon_type_accident;
            default:
                return null;
        }
    }

    public Integer getClassNameId() {
        switch (classCode){
            case "police":
                return R.string.police;
            case "firefighters":
                return R.string.firefighters;
            case "paramedics":
                return R.string.paramedics;
            default:
                return R.string.unknown;
        }
    }
}
