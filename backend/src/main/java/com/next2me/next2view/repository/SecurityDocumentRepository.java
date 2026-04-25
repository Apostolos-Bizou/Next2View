package com.next2me.next2view.repository;

import com.next2me.next2view.model.SecurityDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SecurityDocumentRepository extends JpaRepository<SecurityDocument, UUID> {

    @Query("SELECT d FROM SecurityDocument d WHERE d.deletedAt IS NULL ORDER BY d.uploadedAt DESC")
    List<SecurityDocument> findAllActive();
}
