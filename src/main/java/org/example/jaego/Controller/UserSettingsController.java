package org.example.jaego.Controller;



import lombok.RequiredArgsConstructor;
import org.example.jaego.Dto.OperationResult;
import org.example.jaego.Dto.UserSettingsDto;
import org.example.jaego.Service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-settings")
@RequiredArgsConstructor
public class UserSettingsController {

    private final SettingsService userSettingsService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserSettingsDto> getUserSettings(@PathVariable Long userId) {
        return ResponseEntity.ok(userSettingsService.getUserSettings(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<OperationResult> updateUserSettings(@PathVariable Long userId, @RequestBody UserSettingsDto dto) {
        return ResponseEntity.ok(userSettingsService.updateUserSettings(userId, dto));
    }
}
