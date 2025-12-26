package com.hutech.demo.repository;

import com.hutech.demo.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversation_Id(Long conversationId);
    List<Message> findByConversation_IdOrderByCreatedAtDesc(Long conversationId);
    List<Message> findByConversation_IdAndIsReadFalse(Long conversationId);
}
