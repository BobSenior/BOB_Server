package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.notice.ShownNotice;
import com.bob_senior.bob_server.domain.notice.entity.Notice;
import com.bob_senior.bob_server.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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

    public List<ShownNotice> getMyNoticeList(long userIdx, Pageable pageable) throws BaseException{

        List<Notice> notices = noticeRepository.findAllByUserIdxAndFlag(userIdx,0,pageable).getContent();
        List<ShownNotice> list = new ArrayList<>();
        for (Notice notice : notices) {
            list.add(
            ShownNotice.builder()
                    .noticeIdx(notice.getNoticeIdx())
                    .postIdx(notice.getPostIdx())
                    .type(notice.getType())
                    .text(notice.getContent())
                    .build()
            );
        }
        return list;
    }


}
