package org.example.jaego.Controller;



import lombok.RequiredArgsConstructor;
import org.example.jaego.Dto.AlertSettingsDto;
import org.example.jaego.Dto.OperationResult;
import org.example.jaego.Service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    // 앱 설정 조회
    @GetMapping("/{userId}")
    public AlertSettingsDto getAppSettings(@PathVariable Long userId) {
        return settingsService.getAppSettings(userId);
    }

    // 임박 기준일 변경
    @PutMapping("/{userId}/threshold")
    public ResponseEntity<OperationResult> updateThreshold(@PathVariable Long userId,
                                                           @RequestParam Integer days) {
        return ResponseEntity.ok(settingsService.updateThreshold(userId, days));
    }

    // 알림 주기 변경
    @PutMapping("/{userId}/frequency")
    public ResponseEntity<OperationResult> setAlertFrequency(@PathVariable Long userId,
                                                             @RequestParam String frequency) {
        return ResponseEntity.ok(settingsService.setAlertFrequency(userId, frequency));
    }
}
