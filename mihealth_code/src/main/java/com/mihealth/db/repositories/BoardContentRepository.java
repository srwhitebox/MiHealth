package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mihealth.db.model.BoardContentModel;

public interface BoardContentRepository extends JpaRepository<BoardContentModel, String>{
	@Query(value = "SELECT uid, parentUid, boardUid, writerUid, categoryUid, title, content, CAST(COLUMN_JSON(properties) as CHAR) as properties, enabled, lastUpdated, registeredAt FROM tb_board_content WHERE uid = UNHEX(?1)", nativeQuery = true)
	BoardContentModel findByContetUid(String contentUid);

	@Query(value = "SELECT uid, parentUid, boardUid, writerUid, categoryUid, title, content, CAST(COLUMN_JSON(properties) as CHAR) as properties, enabled, lastUpdated, registeredAt FROM tb_board_content", nativeQuery = true)
	List<BoardContentModel> findAll();

	@Query(value = "SELECT uid, parentUid, boardUid, writerUid, categoryUid, title, content, CAST(COLUMN_JSON(properties) as CHAR) as properties, enabled, lastUpdated, registeredAt FROM tb_board_content WHERE boardUid = UNHEX(?1)", nativeQuery = true)
	List<BoardContentModel> findByBoardUid(String boardId);

	@Query(value = "SELECT uid, parentUid, boardUid, writerUid, categoryUid, title, content, CAST(COLUMN_JSON(properties) as CHAR) as properties, enabled, lastUpdated, registeredAt FROM tb_board_content WHERE boardUid = UNHEX(?1) AND categoryUid = UNHEX(?2)", nativeQuery = true)
	List<BoardContentModel> findByBoardUidAndCategoryUid(String boardUid, String categoryUid);

	@Query(value = "SELECT uid, parentUid, boardUid, writerUid, categoryUid, title, content, CAST(COLUMN_JSON(properties) as CHAR) as properties, enabled, lastUpdated, registeredAt FROM tb_board_content WHERE writerUid = UNHEX(?1)", nativeQuery = true)
	List<BoardContentModel> findByWriter(String writerUid);

	@Query(value = "SELECT uid, parentUid, boardUid, writerUid, categoryUid, title, content, CAST(COLUMN_JSON(properties) as CHAR) as properties, enabled, lastUpdated, registeredAt FROM tb_board_content WHERE writerUid = UNHEX(?1) AND boardId = ?2", nativeQuery = true)
	List<BoardContentModel> findByWriterAndBoardId(String writerUid, String boardId);

	@Query(value = "SELECT uid, parentUid, boardUid, writerUid, categoryUid, title, content, CAST(COLUMN_JSON(properties) as CHAR) as properties, enabled, lastUpdated, registeredAt FROM tb_board_content WHERE writerUid = UNHEX(?1) AND boardId = ?2 AND COLUMN_EXISTS(categories, ?3)", nativeQuery = true)
	List<BoardContentModel> findByWriterAndBoardIdAndCategory(String writerUid, String boardId, String categoryId);

	@Modifying
	@Transactional
	@Query(value = "UPDATE tb_board_content SET enabled = ?2 WHERE uid = = UNHEX(?1)", nativeQuery = true)
	void enable(String uid, boolean enabled);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tb_board_content WHERE uid = UNHEX(?1)", nativeQuery = true)
	void deleteByUid(String uid);

	@Modifying
	@Transactional
	@Query(value = "UPDATE tb_board_content SET categories = COLUMN_DELETE(categories, ?2) WHERE boardUid = UNHEX(?1)", nativeQuery = true)
	void removeCategoryFromContent(String decryptedBoardUid, String categoryId);
}
// uid, parentUid, boardId, userUid, categories, title, content, properties, enabled, lastUpdated, registeredAt