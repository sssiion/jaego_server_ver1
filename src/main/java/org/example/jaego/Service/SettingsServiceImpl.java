package org.example.jaego.Service;


import lombok.RequiredArgsConstructor;
import org.example.jaego.Entity.UserSettings;
import org.example.jaego.Dto.*;

import org.example.jaego.Exception.SettingsValidationException;
import org.example.jaego.Repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SettingsServiceImpl implements SettingsService {

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Override
    public AlertSettingsDto getAppSettings(Long userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new SettingsValidationException("설정을 찾을 수 없습니다."));

        return AlertSettingsDto.builder()
                .userId(settings.getUserId())
                .alertThreshold(settings.getAlertThreshold())
                .alertFrequency(settings.getAlertFrequency())
                .enableExpiryAlerts(settings.getEnableExpiryAlerts())
                .enableLowStockAlerts(settings.getEnableLowStockAlerts())
                .build();
    }

    @Override
    public OperationResult updateThreshold(Long userId, Integer days) {
        if (days <= 0) throw new SettingsValidationException("임박 기준일은 1일 이상이어야 합니다.");
        userSettingsRepository.updateAlertThreshold(userId, days);
        return OperationResult.success("임박 기준일이 수정되었습니다.");
    }

    @Override
    public OperationResult setAlertFrequency(Long userId, String frequency) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new SettingsValidationException("설정을 찾을 수 없습니다."));
        settings.setAlertFrequency(frequency);
        userSettingsRepository.save(settings);
        return OperationResult.success("알림 주기가 변경되었습니다.");
    }

    @Override
    @Transactional(readOnly = true)
    public UserSettingsDto getUserSettings(Long userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        return mapToDto(settings);
    }

    @Override
    public OperationResult updateUserSettings(Long userId, UserSettingsDto dto) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        settings.setAlertThreshold(dto.getAlertThreshold());
        settings.setEnableExpiryAlerts(dto.getEnableExpiryAlerts());
        // ... DTO의 다른 값들로 엔티티 업데이트 ...
        userSettingsRepository.save(settings);
        return OperationResult.success("사용자 설정이 업데이트되었습니다.");
    }
    private UserSettings createDefaultSettings(Long userId) {
        return UserSettings.builder().userId(userId).build();

    }
    private UserSettingsDto mapToDto(UserSettings settings) {
        return UserSettingsDto.builder()
                .userId(settings.getUserId())
                .alertThreshold(settings.getAlertThreshold())
                .enableExpiryAlerts(settings.isExpiryAlertEnabled())
                // ... 다른 필드 매핑 ...
                .build();
    }


}
