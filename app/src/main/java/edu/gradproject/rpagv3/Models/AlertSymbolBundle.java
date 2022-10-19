package edu.gradproject.rpagv3.Models;

import com.mapbox.mapboxsdk.plugins.annotation.Symbol;

public class AlertSymbolBundle {
    private AlertData alertData;
    private Symbol symbol;

    public AlertSymbolBundle(AlertData alertData, Symbol symbol) {
        this.alertData = alertData;
        this.symbol = symbol;
    }

    public AlertData getAlertData() {
        return alertData;
    }

    public void setAlertData(AlertData alertData) {
        this.alertData = alertData;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }
}
