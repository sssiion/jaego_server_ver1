package org.example.jaego.Service;

import org.example.jaego.Dto.*;


public interface SettingsService {

    // 앱 설정 조회
    AlertSettingsDto getAppSettings(Long userId);

    // 임박 기준일 설정
    OperationResult updateThreshold(Long userId, Integer days);

    // 알림 주기 설정
    OperationResult setAlertFrequency(Long userId, String frequency);

    UserSettingsDto getUserSettings(Long userId);

    OperationResult updateUserSettings(Long userId, UserSettingsDto dto);

}
