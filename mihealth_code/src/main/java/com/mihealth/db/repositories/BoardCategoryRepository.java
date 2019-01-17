package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mihealth.db.model.BoardCategoryModel;

public interface BoardCategoryRepository extends JpaRepository<BoardCategoryModel, String>{
	@Query(value = "SELECT uid, parentUid, boardUid, categoryId, CAST(COLUMN_JSON(properties) as CHAR) as properties, displayOrder, enabled, lastUpdated, registeredAt FROM tb_board_category", nativeQuery = true)
	List<BoardCategoryModel> getAll();

	@Query(value = "SELECT uid, parentUid, boardUid, categoryId, CAST(COLUMN_JSON(properties) as CHAR) as properties, displayOrder, enabled, lastUpdated, registeredAt FROM tb_board_category WHERE boardUid = UNHEX(?1) AND categoryId = ?2", nativeQuery = true)
	BoardCategoryModel findByCategoryId(String boardUid, String categoryId);

	@Query(value = "SELECT uid, parentUid, boardUid, categoryId, CAST(COLUMN_JSON(properties) as CHAR) as properties, displayOrder, enabled, lastUpdated, registeredAt FROM tb_board_category WHERE categoryUid = UNHEX(?1)", nativeQuery = true)
	BoardCategoryModel findByCategoryUid(String categoryUid);

	@Query(value = "SELECT uid, parentUid, boardUid, categoryId, CAST(COLUMN_JSON(properties) as CHAR) as properties, displayOrder, enabled, lastUpdated, registeredAt FROM tb_board_category WHERE boardUid = UNHEX(?1)", nativeQuery = true)
	List<BoardCategoryModel> findByBoardUid(String boardUid);

	@Query(value = "SELECT uid, parentUid, boardUid, categoryId, CAST(COLUMN_JSON(properties) as CHAR) as properties, displayOrder, enabled, lastUpdated, registeredAt FROM tv_board_category WHERE boardId = ?1", nativeQuery = true)
	List<BoardCategoryModel> findByBoardId(String boardId);

	@Query(value = "SELECT uid, parentUid, boardUid, categoryId, CAST(COLUMN_JSON(properties) as CHAR) as properties, displayOrder, enabled, lastUpdated, registeredAt FROM tb_board_category WHERE boardUid = UNHEX(?1) AND enabled = ?2", nativeQuery = true)
	List<BoardCategoryModel> findByBoardUidAndEnabled(String boardUid, Boolean enabled);

	@Query(value = "SELECT uid, parentUid, boardUid, categoryId, CAST(COLUMN_JSON(properties) as CHAR) as properties, displayOrder, enabled, lastUpdated, registeredAt FROM tb_board_category WHERE boardId = ?1 AND enabled = ?2", nativeQuery = true)
	List<BoardCategoryModel> findByBoardIdAndEnabled(String boardId, Boolean enabled);

	@Modifying
	@Transactional
	@Query(value = "UPDATE tb_board_category SET enabled = ?2 WHERE categoryUid = UNHEX(?1)", nativeQuery = true)
	void enable(String categoryUid, boolean enabled);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tb_board_category WHERE categoryUid = UNHEX(?1)", nativeQuery = true)
	void deleteByUid(String categoryUid);

}
