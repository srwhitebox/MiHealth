package com.mihealth.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mihealth.db.model.BoardModel;

public interface BoardRepository extends JpaRepository<BoardModel, String>{

	@Query(value = "SELECT uid, boardId, CAST(COLUMN_JSON(properties) as CHAR) as properties, comment, enabled, lastUpdated, registeredAt FROM tb_board WHERE boardUid = UNHEX(?1)", nativeQuery = true)
	BoardModel findByBoardUid(String boardUid);

	@Query(value = "SELECT uid, boardId, CAST(COLUMN_JSON(properties) as CHAR) as properties, comment, enabled, lastUpdated, registeredAt FROM tb_board WHERE boardId = ?1", nativeQuery = true)
	BoardModel findByBoardId(String boardId);

	@Query(value = "SELECT uid, boardId, CAST(COLUMN_JSON(properties) as CHAR) as properties, comment, enabled, lastUpdated, registeredAt FROM tb_board", nativeQuery = true)
	List<BoardModel> getAll();

	@Query(value = "SELECT uid, boardId, CAST(COLUMN_JSON(properties) as CHAR) as properties, comment, enabled, lastUpdated, registeredAt FROM tb_board WHERE enabled = ?1", nativeQuery = true)
	List<BoardModel> findByEnabled(boolean enabled);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE tb_board SET enabled = ?2 WHERE boardId = ?1", nativeQuery = true)
	void enableBoard(String boardId, boolean enabled);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tb_board WHERE boardId = ?1", nativeQuery = true)
	void deleteByBoardId(String boardId);


}
