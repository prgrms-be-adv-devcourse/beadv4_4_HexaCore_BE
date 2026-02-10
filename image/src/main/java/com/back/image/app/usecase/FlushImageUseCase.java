package com.back.image.app.usecase;

import com.back.common.annotation.Loggable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlushImageUseCase {

    @Loggable
    public void flushMultipleFile(List<File> tempFiles) {
        if (tempFiles == null || tempFiles.isEmpty()) {
            return;
        }

        List<Path> failedDeletions = tempFiles.stream()
                .filter(f -> !flushFile(f))
                .map(File::toPath)
                .toList();

        if (!failedDeletions.isEmpty()) {
            log.warn("[FileCleanupPartialFailure] 일부 임시 파일 삭제에 실패했습니다. 실패한 파일 경로: {}", failedDeletions);
        } else {
            log.info("[FileCleanupComplete] 모든 임시 파일이 성공적으로 삭제되었습니다.");
        }
    }

    @Loggable
    public boolean flushFile(File tempFile) {
        if (tempFile == null) return true;

        if (!tempFile.exists()) {
            log.warn("[FileCleanupSuccess] 삭제할 파일이 존재하지 않습니다: {}", tempFile.getAbsolutePath());
            return true;
        }

        try {
            if (Files.deleteIfExists(tempFile.toPath())) {
                return true;
            } else {
                log.warn("[FileCleanupFailed] 임시 파일 삭제 실패 (delete()가 false를 반환): {}", tempFile.getAbsolutePath());
                return false;
            }
        } catch (SecurityException e) {
            log.error("[FileCleanupFailed] 임시 파일 삭제 권한 없음: {}, 오류: {}", tempFile.getAbsolutePath(), e.getMessage());
            return false;
        } catch (IOException e) {
            log.error("[FileCleanupFailed] 임시 파일 삭제 실패: {}, 오류: {}", tempFile.getAbsolutePath(), e.getMessage());
            return false;
        }
    }
}
