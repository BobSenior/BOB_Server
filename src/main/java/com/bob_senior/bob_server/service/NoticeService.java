package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.chat.entity.ChatParticipant;
import com.bob_senior.bob_server.domain.notice.ShownNotice;
import com.bob_senior.bob_server.domain.notice.entity.Notice;
import com.bob_senior.bob_server.repository.ChatParticipantRepository;
import com.bob_senior.bob_server.repository.ChatRepository;
import com.bob_senior.bob_server.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRepository chatRepository;
    
    
    @Autowired
    public NoticeService(NoticeRepository noticeRepository, ChatParticipantRepository chatParticipantRepository, ChatRepository chatRepository) {
        this.noticeRepository = noticeRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatRepository = chatRepository;
    }

    public long getTotalActivatingNoticeForUser(Long userIdx) throws BaseException {
        return noticeRepository.countByUserIdxAndAndFlag(userIdx,0);
    }

    public List<ShownNotice> getMyNoticeList(long userIdx, Pageable pageable) throws BaseException{

        List<Notice> notices = noticeRepository.findAllByUserIdxAndFlag(userIdx,0,pageable).getContent();

        long totalCount = 0;
        
        
        List<ChatParticipant> list = chatParticipantRepository.getAllByChatNUser_UserIdx(userIdx);
        for (ChatParticipant chatParticipant : list) {
            long roomIdx = chatParticipant.getChatNUser().getChatRoomIdx();
            totalCount+=getNumberOfUnreadChatByUserIdx(userIdx,roomIdx);
        }
        
        ShownNotice chatNotice = ShownNotice.builder()
                .postIdx(123123L)
                .type("UnreadChat")
                .unreadChatNum(totalCount)
                .build();
        
        List<ShownNotice> lists = new ArrayList<>();
        
        lists.add(chatNotice);
        
        for (Notice notice : notices) {
            lists.add(
            ShownNotice.builder()
                    .noticeIdx(notice.getNoticeIdx())
                    .postIdx(notice.getPostIdx())
                    .type(notice.getType())
                    .build()
            );
        }
        return lists;
    }

    private Long getNumberOfUnreadChatByUserIdx(Long userIdx,Long roomIdx){

        Timestamp ts = chatParticipantRepository.getLastReadByUserIdx(userIdx,roomIdx);
        if(ts == null){
            //null일시 새로 데이터를 세팅해주고 0개 return
            return chatRepository.countByChatRoomChatRoomIdx(roomIdx);
        }
        LocalDateTime lastRead = ts.toLocalDateTime();
        System.out.println("lastRead = " + lastRead);
        return chatRepository.countChatMessagesByChatRoom_ChatRoomIdxAndSentAtIsAfter(roomIdx,ts);
    }



}
