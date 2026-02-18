package com.back.chat.adapter.out.repository;

import com.back.chat.domain.entity.ChatReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatReportRepository extends JpaRepository<ChatReport,Long> {
}
