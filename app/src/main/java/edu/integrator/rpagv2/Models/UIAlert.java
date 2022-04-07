package edu.integrator.rpagv2.Models;

import com.mapbox.mapboxsdk.plugins.annotation.Symbol;

import java.util.Date;

public class UIAlert {
    private AlertData alertData;
    private AlertClass alertClass;
    private Symbol symbol;

    public UIAlert(AlertData alertData, AlertClass alertClass, Symbol symbol) {
        this.alertData = alertData;
        this.alertClass = alertClass;
        this.symbol = symbol;
    }

    public UIAlert(AlertData alertData, AlertClass alertClass) {
        this.alertData = alertData;
        this.alertClass = alertClass;
    }

    public AlertData getAlertData() {
        return alertData;
    }

    public void setAlertData(AlertData alertData) {
        this.alertData = alertData;
    }

    public AlertClass getAlertClass() {
        return alertClass;
    }

    public void setAlertClass(AlertClass alertClass) {
        this.alertClass = alertClass;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }
}
