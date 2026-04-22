package br.com.expertdev.model;

public class AuthSession {

    private final String username;
    private final String email;
    private final LicenseStatus licenseStatus;
    private final int trialDaysRemaining;

    public AuthSession(String username, String email, LicenseStatus licenseStatus, int trialDaysRemaining) {
        this.username = username;
        this.email = email;
        this.licenseStatus = licenseStatus;
        this.trialDaysRemaining = Math.max(0, trialDaysRemaining);
    }

    public static AuthSession premium(String username, String email) {
        return new AuthSession(username, email, LicenseStatus.PREMIUM, 0);
    }

    public static AuthSession trial(int trialDaysRemaining) {
        return new AuthSession("Visitante", "", LicenseStatus.TRIAL, trialDaysRemaining);
    }

    public static AuthSession expired() {
        return new AuthSession("Visitante", "", LicenseStatus.EXPIRED, 0);
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public LicenseStatus getLicenseStatus() {
        return licenseStatus;
    }

    public int getTrialDaysRemaining() {
        return trialDaysRemaining;
    }

    public boolean isPremium() {
        return licenseStatus == LicenseStatus.PREMIUM;
    }

    public boolean isTrial() {
        return licenseStatus == LicenseStatus.TRIAL;
    }

    public String getDisplayName() {
        if (username != null && !username.trim().isEmpty()) {
            return username;
        }
        return "Visitante";
    }
}

