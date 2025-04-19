package com.chat.kit.service;

import com.chat.kit.persistence.domain.ClientPacket;
import com.chat.kit.persistence.domain.ErrorPacket;
import com.chat.kit.persistence.domain.ServerPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Echo 기능의 핵심 비즈니스 로직을 처리하는 서비스 클래스.
 * 클라이언트로부터 수신한 메시지와 옵션에 따라 메시지를 변환하고,
 * 결과를 다시 클라이언트에게 전송하거나 에러 응답을 반환한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EchoService {

    // 메시지 발송을 위한 Spring 제공 템플릿
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 클라이언트에서 전송한 패킷을 처리하고, 변환된 결과를 응답으로 전송한다.
     * 옵션이 잘못된 경우 오류 패킷을 반환한다.
     */
    public void processAndRespond(ClientPacket packet) {
        try {
            // 옵션에 따라 메시지를 처리
            String processedMessage = processMessage(packet.getOption(), packet.getMessage());

            // 처리 불가능한 옵션일 경우, 에러 패킷 생성 후 에러 채널로 전송
            if (processedMessage == null) {
                ErrorPacket error = new ErrorPacket(getErrorCode(packet.getOption()), getErrorMessage(packet.getOption()));
                messagingTemplate.convertAndSend("/sub/error/" + packet.getUsername(), error);
                return;
            }

            // 처리 전/후 메시지 로깅
            log.info("입력 옵션: {}, 입력 메시지: {}", packet.getOption(), packet.getMessage());
            log.info("Before: [{}]: {}", packet.getUsername(), packet.getMessage());
            log.info("After: [{}]: {}", packet.getUsername(), processedMessage);

            // 정상 응답 패킷 생성 및 클라이언트에게 전송
            ServerPacket response = new ServerPacket(200, packet.getUsername(), processedMessage);
            messagingTemplate.convertAndSend("/sub/echo/" + packet.getUsername(), response);

        } catch (Exception e) {
            // 예외 발생 시 서버 오류 응답 전송
            ErrorPacket error = new ErrorPacket(500, "server error: " + e.getMessage());
            messagingTemplate.convertAndSend("/sub/error/" + packet.getUsername(), error);
        }
    }

    /**
     * 옵션에 따라 메시지를 변환한다.
     * 1: 그대로 반환, 2: 대문자, 3: 소문자, 그 외: null 반환
     */
    private String processMessage(int option, String message) {
        switch (option) {
            case 1: return message;
            case 2: return message.toUpperCase();
            case 3: return message.toLowerCase();
            default: return null;
        }
    }

    /**
     * 옵션 값에 따라 오류 코드 반환
     */
    private int getErrorCode(int option) {
        if (option < 1) return 401;
        if (option > 3) return 403;
        return 402;
    }

    /**
     * 옵션 값에 따라 오류 메시지 반환
     */
    private String getErrorMessage(int option) {
        if (option < 1) return "option input error: integer less than 1";
        if (option > 3) return "option input error: integer greater than 3";
        return "option input error: non-integer or unsupported";
    }
}
