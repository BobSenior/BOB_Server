package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.notice.ShownNotice;
import com.bob_senior.bob_server.domain.notice.entity.Notice;
import com.bob_senior.bob_server.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Autowired
    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public long getTotalActivatingNoticeForUser(Long userIdx) throws BaseException {
        return noticeRepository.countByUserIdxAndAndFlag(userIdx,0);
    }

    public List<ShownNotice> getMyNoticeList(long userIdx) throws BaseException{

        List<Notice> notices = noticeRepository.findAllByUserIdxAndFlag(userIdx,0);
        List<ShownNotice> list = new ArrayList<>();
        for (Notice notice : notices) {
            list.add(
            ShownNotice.builder()
                    .postIdx(notice.getPostIdx())
                    .type(notice.getType())
                    .text(notice.getContent())
                    .build()
            );
        }
        return list;
    }


}
