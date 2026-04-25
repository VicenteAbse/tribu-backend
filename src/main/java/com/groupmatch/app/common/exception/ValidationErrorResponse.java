package com.groupmatch.app.common.exception;

import java.util.List;

public class ValidationErrorResponse {

    private List<FieldErrorResponse> errors;

    public ValidationErrorResponse(List<FieldErrorResponse> errors) {
        this.errors = errors;
    }

    public List<FieldErrorResponse> getErrors() {
        return errors;
    }
}

