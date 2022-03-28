package com.cartuploader;

import java.util.List;

public class FunctionOutput {
    private final List<Account> accounts;
    private final String errorMessage;

    public FunctionOutput(List<Account> accounts, String errorMessage) {
        this.accounts = accounts;
        this.errorMessage = errorMessage;
    }

    public FunctionOutput(List<Account> accounts) {
        this(accounts, null);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
