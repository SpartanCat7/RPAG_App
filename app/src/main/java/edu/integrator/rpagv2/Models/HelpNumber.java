package edu.integrator.rpagv2.Models;

import com.integrator.rpagv2.R;

import edu.integrator.rpagv2.MainActivity;

public class HelpNumber {
    private String countryCode;
    private String classCode;
    private String number;

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

    public Integer getClassIconId() {
        switch (classCode){
            case "police":
                return R.drawable.blocked;
            case "firefighters":
                return R.drawable.fire;
            case "paramedics":
                return R.drawable.accident;
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
