package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.Chat.*;
import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.repository.ChatParticipantRepository;
import com.bob_senior.bob_server.repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository, ChatParticipantRepository chatParticipantRepository){
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatRepository = chatRepository;
    }

    //채팅 페이지 가져오기
    public ChatPage loadChatPageData(Pageable pageable, int roomIdx) throws BaseException {
        Page<ChatMessage> pages =  chatRepository.findByChatRoomIdx(roomIdx,pageable);
        List<ChatDto> chats = new ArrayList<>();
        for (ChatMessage page : pages) {
            Integer sender = page.getSenderIdx();
            //해당 sender의 데이터를 여기서 쫙 가져오면 될듯 userRepository에서 - gravatar등등
            chats.add(ChatDto.builder()
                    .type("MESSAGE")
                    .senderIdx(sender)
                    .channelId(""+roomIdx)
                    .data(page.getMsgContent()).build());
        }
        ChatPage cp = ChatPage.builder()
                .curPage(pageable.getPageNumber())
                .length(pageable.getPageSize())
                .chatList(chats)
                .build();
        return cp;
    }

    //유저가 해당 방에 참여하는 여부 확인
    public boolean checkUserParticipantChatting(Integer chatIdx, Integer userIdx){
        boolean prev = chatParticipantRepository.existsChatParticipantById_ChatParticipantIdxAndId_ChatRoomIdx(chatIdx,userIdx);
        if(!prev){
            //아예 등록 기록이 없을시 return false
            return false;
        }
        //등록기록이 있더라도 status가 Q일시 return false
        ChatParticipant cp = chatParticipantRepository.getChatParticipantById_ChatParticipantIdxAndAndId_ChatRoomIdx(chatIdx,userIdx);
        if(cp.getStatus().equals("Q")) return false;
        return true;

    }

    //해당 유저의 lastRead기준으로 읽지 않은 개수 가져오기
    public Long getNumberOfUnreadChatByUserIdx(Integer userIdx,Integer roomIdx){
        Timestamp ts = chatParticipantRepository.getLastReadByUserIdx(userIdx);
        if(ts == null){
            //null일시 새로 데이터를 세팅해주고 0개 return
            return 0L;
        }
        LocalDateTime lastRead = ts.toLocalDateTime();
        return chatRepository.countBySentAtAfter(lastRead);
    }

    //해당 유저를 해당 채팅방에 참여시키기
    //필요한 데이터 : chatRoomIdx, chatParticipantIdx, status, lastRead
    public Timestamp userParticipant(Integer chatRoomIdx, Integer chatParticipantIdx){
        ChatNUser rau = new ChatNUser(chatRoomIdx,chatParticipantIdx);
        Long datetime = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(datetime);
        ChatParticipant cp = ChatParticipant.builder()
                .id(rau)
                .status("A")
                .lastRead(timestamp)
                .build();
        chatParticipantRepository.save(cp);
        return timestamp;
    }

    //새로운 message의 db저장
    public void  storeNewMessage(ChatDto msg,Timestamp ts,Integer roomIdx) {
        String chatId = UUID.randomUUID().toString();
        ChatMessage cmg = ChatMessage.builder()
                .chatRoomIdx(roomIdx)
                .senderIdx(msg.getSenderIdx())
                .sentAt(ts)
                .msgContent(msg.getData())
                .uuId(chatId)
                .build();
        chatRepository.save(cmg);
    }

    //user가 방 밖으로 나갈시 disable시키기
    public void deleteUserFromRoom(int roomId, Integer sender) {
        ChatNUser rau = new ChatNUser(roomId,sender);
        ChatParticipant cp = chatParticipantRepository.findChatParticipantById(rau);
        cp.setStatus("Q");
        chatParticipantRepository.save(cp);
    }
}
