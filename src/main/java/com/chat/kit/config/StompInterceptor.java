package com.chat.kit.config;

import com.chat.kit.persistence.domain.MemberChatRoom;
import com.chat.kit.persistence.repository.MemberChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class StompInterceptor implements ChannelInterceptor {
    private final JwtDecoder jwtDecoder;
    private final MemberChatRoomRepository memberChatRoomRepository;


    //TODO : 이거 실전용임
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor =
//                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//            String authHeader = accessor.getFirstNativeHeader("Authorization");
//
//            if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                String token = authHeader.substring(7);
//
//                Jwt jwt = jwtDecoder.decode(token);
//                Long memberId = (Long)jwt.getClaims().get("memberId");
//                Authentication user = new JwtAuthenticationToken(jwt, null, memberId.toString());
//                accessor.setUser(user);
//            }else{
//                throw new RuntimeException();
//            }
//        }
//
//        else if(StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())){
//            if(!accessor.getNativeHeader("dest").isEmpty() && accessor.getNativeHeader("dest").get(0).contains("/sub/chatroom/")){
//                String destination = accessor.getNativeHeader("dest").get(0);
//                Long chatRoomId = Long.parseLong(destination.substring(destination.lastIndexOf("/")+1));
//                Long memberId = Long.parseLong(accessor.getUser().getName());
//                MemberChatRoom memberChatRoom = memberChatRoomRepository.findByChatRoomIdAndMemberId(chatRoomId, memberId)
//                        .orElseThrow();
//
//                memberChatRoom.setLastLeavedTime(LocalDateTime.now());
//            }
//        }
//        return message;
//    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            handleConnect(accessor);
        } else if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
            handleUnsubscribe(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Jwt jwt = jwtDecoder.decode(token);
                Long memberId = ((Number) jwt.getClaims().get("memberId")).longValue();
                Authentication user = new JwtAuthenticationToken(jwt, null, memberId.toString());
                accessor.setUser(user);
            } else {
                // 테스트용: Authorization 없이 memberId만으로 인증 처리
                String memberIdStr = accessor.getFirstNativeHeader("memberId");
                if (memberIdStr != null) {
                    Authentication user = new UsernamePasswordAuthenticationToken(memberIdStr, null, null);
                    accessor.setUser(user);
                } else {
                    throw new RuntimeException("Authorization header or memberId header missing.");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("WebSocket authentication failed", e);
        }
    }

    private void handleUnsubscribe(StompHeaderAccessor accessor) {
        List<String> destHeaders = accessor.getNativeHeader("dest");

        if (destHeaders != null && !destHeaders.isEmpty() &&
                destHeaders.get(0).contains("/sub/chatroom/")) {

            String destination = destHeaders.get(0);
            Long chatRoomId = Long.parseLong(destination.substring(destination.lastIndexOf("/") + 1));
            Long memberId = Long.parseLong(accessor.getUser().getName());

            memberChatRoomRepository.findByChatRoomIdAndMemberId(chatRoomId, memberId)
                    .ifPresent(mcr -> mcr.setLastLeavedTime(LocalDateTime.now()));

            log.debug("UNSUBSCRIBE 처리 완료: memberId={}, chatRoomId={}", memberId, chatRoomId);
        }
    }}
