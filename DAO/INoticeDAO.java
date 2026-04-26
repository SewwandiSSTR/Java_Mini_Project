package ums.dao;

import ums.model.Notice;
import java.util.List;

public sealed interface INoticeDAO permits NoticeDAOImpl {
    boolean createNotice(Notice n);
    List<Notice> getAllActive();
    boolean deleteNotice(int noticeId);
    boolean updateNotice(Notice n);
}
