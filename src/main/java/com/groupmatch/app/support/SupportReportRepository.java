package com.groupmatch.app.support;

import com.groupmatch.app.domain.support.SupportReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportReportRepository extends JpaRepository<SupportReportEntity, Long> {
}
