package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mihealth.db.model.BoardDetailedContentModel;
import com.mihealth.db.model.LocaleModel;

public interface BoardDetailedContentRepository extends JpaRepository<BoardDetailedContentModel, String>{
}
