package com.groupmatch.app.support;

import com.groupmatch.app.support.service.SupportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/support")
public class SupportController {

    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @PostMapping("/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public void createReport(
            @Valid @RequestBody SupportReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        supportService.createReport(request, userDetails.getUsername());
    }
}
