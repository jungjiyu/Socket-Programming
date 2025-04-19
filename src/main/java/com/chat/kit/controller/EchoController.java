package com.chat.kit.controller;

import com.chat.kit.persistence.domain.ClientPacket;
import com.chat.kit.service.EchoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class EchoController {

    private final EchoService echoService;

    @MessageMapping("/echo")
    public void handleEcho(@Payload ClientPacket packet) {
        echoService.processAndRespond(packet);
    }
}