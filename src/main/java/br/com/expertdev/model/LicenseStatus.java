package br.com.expertdev.model;

public enum LicenseStatus {
    PREMIUM,
    TRIAL,
    EXPIRED,
    /** Credencial valida mas senha expirou (30 dias sem renovar). */
    PASSWORD_EXPIRED
}

