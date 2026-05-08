package com.groupmatch.app.support.service;

import com.groupmatch.app.auth.UserRepository;
import com.groupmatch.app.domain.support.SupportReportEntity;
import com.groupmatch.app.domain.user.UserEntity;
import com.groupmatch.app.support.SupportReportRepository;
import com.groupmatch.app.support.SupportReportRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@Service
public class SupportService {

    private final SupportReportRepository supportReportRepository;
    private final UserRepository userRepository;

    public SupportService(SupportReportRepository supportReportRepository,
                          UserRepository userRepository) {
        this.supportReportRepository = supportReportRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createReport(SupportReportRequest request, String userEmail) {
        UserEntity user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        supportReportRepository.save(new SupportReportEntity(user, request.getMessage()));
    }
}
