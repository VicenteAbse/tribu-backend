package com.groupmatch.app.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SupportReportRequest {

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(min = 10, max = 2000, message = "El mensaje debe tener entre 10 y 2000 caracteres")
    private String message;

    public SupportReportRequest() {}

    public String getMessage() { return message; }
}
