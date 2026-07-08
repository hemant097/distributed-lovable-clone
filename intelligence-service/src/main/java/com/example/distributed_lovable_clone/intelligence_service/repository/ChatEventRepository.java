package com.example.distributed_lovable_clone.intelligence_service.repository;

import com.example.distributed_lovable_clone.intelligence_service.entity.ChatEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatEventRepository extends JpaRepository<ChatEvent,Long> {
}
