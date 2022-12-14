package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.chat.*;
import com.bob_senior.bob_server.domain.chat.entity.ChatMessage;
import com.bob_senior.bob_server.domain.chat.entity.ChatNUser;
import com.bob_senior.bob_server.domain.chat.entity.ChatParticipant;
import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.repository.*;
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
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository, ChatParticipantRepository chatParticipantRepository, ChatMessageRepository chatMessageRepository, ChatRoomRepository chatRoomRepository, PostRepository postRepository, UserRepository userRepository){
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatRepository = chatRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    //채팅 페이지 가져오기
    public List<ShownChat> loadChatPageData(Pageable pageable, Long postIdx) throws BaseException {
        long roomIdx = postRepository.findPostByPostIdx(postIdx).getChatRoomIdx();
        Page<ChatMessage> pages =  chatRepository.findByChatRoom_ChatRoomIdxOrderBySentAtDesc(roomIdx,pageable);
        List<ShownChat> chats = new ArrayList<>();
        for (ChatMessage page : pages) {
            Long sender = page.getSenderIdx();
            //해당 sender의 데이터를 여기서 쫙 가져오면 될듯 userRepository에서 - gravatar등등
            chats.add(ShownChat.builder()
                            .senderIdx(page.getSenderIdx())
                    .writtenAt(page.getSentAt().toString())
                    .nickname(userRepository.findUserByUserIdx(page.getSenderIdx()).getNickName())
                    .content(page.getMsgContent()).build());
        }
        return chats;
    }

    //유저가 해당 방에 참여하는 여부 확인
    public boolean checkUserParticipantChatting(Long chatIdx, Long userIdx){
        long chatRoomIdx = postRepository.findPostByPostIdx(chatIdx).getChatRoomIdx();
        System.out.println("chatRoomIdx = " + chatRoomIdx);
        System.out.println("userIdxxxxxx = " + userIdx);
        boolean prev = chatParticipantRepository.existsByChatNUser_UserIdxAndChatNUser_ChatRoomIdx(userIdx,chatRoomIdx);
        System.out.println("prevsssssss = " + prev);
        if(!prev){
            //아예 등록 기록이 없을시 return false
            return false;
        }
        //등록기록이 있더라도 status가 Q일시 return false
        ChatParticipant cp = chatParticipantRepository.getByChatNUser_ChatRoomIdxAndChatNUser_UserIdx(chatRoomIdx,userIdx);
        if(cp == null) return true;
        System.out.println("cppppppp     = " + cp.getStatus());
        if(cp.getStatus().equals("Q")) return false;
        return true;

    }

    //해당 유저의 lastRead기준으로 읽지 않은 개수 가져오기
    public Long getNumberOfUnreadChatByUserIdx(Long userIdx,Long roomIdx,boolean flag){

        if(flag){
            roomIdx = postRepository.findPostByPostIdx(roomIdx).getChatRoomIdx();
        }

        Timestamp ts = chatParticipantRepository.getLastReadByUserIdx(userIdx,roomIdx);
        System.out.println("ts = " + ts);
        if(ts == null){
            //null일시 새로 데이터를 세팅해주고 0개 return
            return chatRepository.countByChatRoomChatRoomIdx(roomIdx);
        }
        return chatRepository.countChatMessagesByChatRoom_ChatRoomIdxAndSentAtIsAfter(roomIdx,ts);
    }

    //모든 채팅의 읽지 않은 개수를 가져오기
    public Long getTotalNumberOfUnreadChatByUserIdx(Long userIdx){
        //1. chatParticipant에서 Q상태인 tuple의 timeStamp를 모두 가져오기
        List<ChatParticipant> participants = chatParticipantRepository.getTotalUnreadChatNumber(userIdx);
        //각각의 chatParticipant의 timeStamp와 chatIdx로 안읽은 채팅 개수 가져오기
        Long total = 0L;
        for (ChatParticipant participant : participants) {
            Long chatRoomIdx = participant.getChatNUser().getChatRoomIdx();
            total += chatMessageRepository.countChatMessagesByChatRoom_ChatRoomIdxAndSentAtAfter(chatRoomIdx,participant.getLastRead());
        }
        return total;
    }

    //해당 유저를 해당 채팅방에 참여시키기
    //필요한 데이터 : chatRoomIdx, chatParticipantIdx, status, lastRead
    public Timestamp userParticipant(Long chatRoomIdx, Long chatParticipantIdx){
        ChatNUser rau = new ChatNUser(chatRoomIdx,chatParticipantIdx);
        Long datetime = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(datetime);
        ChatParticipant cp = ChatParticipant.builder()
                .chatNUser(rau)
                .status("active")
                .lastRead(timestamp)
                .build();
        chatParticipantRepository.save(cp);
        return timestamp;
    }

    //새로운 message의 db저장
    public ShownChat  storeNewMessage(ChatDto msg,Timestamp ts,Long roomIdx) {
        String chatId = UUID.randomUUID().toString().substring(0,8);
        System.out.println(chatId);
        ChatMessage cmg = ChatMessage.builder()
                .chatRoom(chatRoomRepository.findChatRoomByChatRoomIdx(postRepository.findPostByPostIdx(roomIdx).getChatRoomIdx()))
                .senderIdx(msg.getSenderIdx())
                .sentAt(ts)
                .msgContent(msg.getData())
                .uuId(chatId)
                .build();
        chatRepository.save(cmg);

        return(
                ShownChat.builder().
                        nickname(userRepository.findUserByUserIdx(msg.getSenderIdx()).getNickName())
                        .writtenAt(ts.toString())
                        .senderIdx(msg.getSenderIdx())
                        .content(msg.getData())
                        .build()
                );

    }

    //user가 방 밖으로 나갈시 disable시키기
    public void deleteUserFromRoom(Long roomId, Long sender) {
        ChatNUser rau = new ChatNUser(roomId,sender);
        ChatParticipant cp = chatParticipantRepository.findChatParticipantByChatNUser(rau);
        chatParticipantRepository.save(cp);
    }

    public void activateChatParticipation(Long userIdx, Long roomId) {
        chatParticipantRepository.activateParticipation(userIdx,roomId);
    }

    public Long getAllUnreadChatNum(long userIdx) throws BaseException{

        Long totalCount = 0L;
        //1. 모든 chatParticipant가져오기
        List<ChatParticipant> list = chatParticipantRepository.getAllByChatNUser_UserIdx(userIdx);
        for (ChatParticipant chatParticipant : list) {
            long roomIdx = chatParticipant.getChatNUser().getChatRoomIdx();
            totalCount+=getNumberOfUnreadChatByUserIdx(userIdx,roomIdx,false);
        }
        return totalCount;
    }
}
