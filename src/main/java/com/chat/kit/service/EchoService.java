package com.chat.kit.service;


import com.chat.kit.persistence.domain.ClientPacket;
import com.chat.kit.persistence.domain.ErrorPacket;
import com.chat.kit.persistence.domain.ServerPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EchoService {

    private final SimpMessagingTemplate messagingTemplate;

    public void processAndRespond(ClientPacket packet) {
        try {
            String processedMessage = processMessage(packet.getOption(), packet.getMessage());

            if (processedMessage == null) {
                ErrorPacket error = new ErrorPacket(getErrorCode(packet.getOption()), getErrorMessage(packet.getOption()));
                messagingTemplate.convertAndSend("/sub/error/" + packet.getUsername(), error);
                return;
            }

            log.info("입력 옵션: {}, 입력 메시지: {}", packet.getOption(), packet.getMessage());
            log.info("Before: [{}]: {}", packet.getUsername(), packet.getMessage());
            log.info("After: [{}]: {}", packet.getUsername(), processedMessage);

            ServerPacket response = new ServerPacket(200, packet.getUsername(), processedMessage);
            messagingTemplate.convertAndSend("/sub/echo/" + packet.getUsername(), response);

        } catch (Exception e) {
            ErrorPacket error = new ErrorPacket(500, "server error: " + e.getMessage());
            messagingTemplate.convertAndSend("/sub/error/" + packet.getUsername(), error);
        }
    }

    private String processMessage(int option, String message) {
        switch (option) {
            case 1: return message;
            case 2: return message.toUpperCase();
            case 3: return message.toLowerCase();
            default: return null;
        }
    }

    private int getErrorCode(int option) {
        if (option < 1) return 401;
        if (option > 3) return 403;
        return 402;
    }

    private String getErrorMessage(int option) {
        if (option < 1) return "option input error: integer less than 1";
        if (option > 3) return "option input error: integer greater than 3";
        return "option input error: non-integer or unsupported";
    }
}
