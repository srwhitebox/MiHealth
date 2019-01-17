package com.mihealth.db.service;

import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mihealth.db.model.BoardCategoryModel;
import com.mihealth.db.model.BoardContentModel;
import com.mihealth.db.model.BoardDetailedContentModel;
import com.mihealth.db.model.BoardModel;
import com.mihealth.db.model.PropertyModel;
import com.mihealth.db.model.StudentModel;
import com.mihealth.db.repositories.BoardCategoryRepository;
import com.mihealth.db.repositories.BoardContentRepository;
import com.mihealth.db.repositories.BoardRepository;
import com.mihealth.db.utils.EncodeUtils;
import com.ximpl.lib.constant.DbConst;
import com.ximpl.lib.db.EntityManagerHelper;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;
import com.ximpl.lib.util.XcUrlUtils;

@Service
public class BoardService {
	@Autowired
	private EntityManagerHelper entityManagerHelper;
	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private BoardCategoryRepository boardCategoryRepository;
	@Autowired
	private BoardContentRepository boardContentRepository;
	
	public void save(BoardModel board){
		final String queryText = "REPLACE INTO tb_board (uid, boardId, properties, comment, enabled, lastUpdated, registeredAt) "
				+ "values (UNHEX(:uid), :boardId, "
				+ XcJsonUtils.toJsonCreate(board.getProperties()) + ", :comment, :enabled, :lastUpdated, :registeredAt)" ;
		Query query = entityManagerHelper.query(queryText);
		query.setParameter(DbConst.FIELD_UID, board.getDecryptedUid());
		query.setParameter(DbConst.FIELD_BOARD_ID, board.getBoardId());
		query.setParameter(DbConst.FIELD_COMMENT, board.getComment());
		query.setParameter(DbConst.FIELD_ENABLED, board.getEnabled());
		query.setParameter(DbConst.FIELD_LAST_UPDATED, board.getLastUpdated());
		query.setParameter(DbConst.FIELD_REGISTERED_AT, board.getRegisteredAt());
		
		entityManagerHelper.execute(query);
	}
	
	public BoardModel getBoardByBoardId(String boardId){
		return boardRepository.findByBoardId(boardId);
	}

	public BoardModel getBoardByBoardUid(String boardUid){
		return boardRepository.findByBoardUid(boardUid);
	}

	public List<BoardModel> getBoards(Boolean enabled){
		if (enabled != null)
			return boardRepository.findByEnabled(enabled);
		else
			return boardRepository.getAll();
	}
	
	public List<BoardModel> getBoards(String orderBy, String filter, Boolean enabled, Integer page, Integer count) {
		final String selectStatement = "SELECT uid, boardId, CAST(COLUMN_JSON(properties) as CHAR) as properties, comment, enabled, lastUpdated, registeredAt FROM tb_board ";
		StringBuilder sb = new StringBuilder(selectStatement);
		
		StringBuilder sbWhere = new StringBuilder();
		
		JsonElement jElement = XcJsonUtils.toJsonElement(filter);
		if (jElement != null && jElement.isJsonObject()){
			JsonObject jFilter = jElement.getAsJsonObject();
			for(Entry<String, JsonElement> entry : jFilter.entrySet()){
				final String value = entry.getValue().getAsString();
				if (XcStringUtils.isNullOrEmpty(value))
					continue;
				if (sbWhere.length() == 0){
					sbWhere.append(" WHERE ");
				}else
					sbWhere.append(" AND ");
				if (entry.getKey().startsWith("properties")){
					String key = "COLUMN_GET(properties, '"+XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbWhere.append(key);
				}else{
					sbWhere.append(entry.getKey());
				}
				sbWhere.append(" like '%");
				sbWhere.append(value);
				sbWhere.append("%' ");
			}			
		}
		if (sbWhere.length() > 0)
			sb.append(sbWhere);
		
		jElement = XcJsonUtils.toJsonElement(orderBy);
		if (jElement != null && jElement.isJsonObject()){
			JsonObject jFilter = jElement.getAsJsonObject();
			StringBuilder sbOrderBy = new StringBuilder();
			for(Entry<String, JsonElement> entry : jFilter.entrySet()){
				if (sbOrderBy.length() == 0)
					sbOrderBy.append(" ORDER BY ");
				else
					sbOrderBy.append(", ");
				if (entry.getKey().startsWith("properties")){
					String key = "COLUMN_GET(properties, '"+ XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbOrderBy.append(key);
				}else if (entry.getKey().startsWith("registerProperties")){
					String key = "COLUMN_GET(registerProperties, '"+ XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbOrderBy.append(key);
				}else{
					sbOrderBy.append(entry.getKey());
				}
				sbOrderBy.append(" ");
				sbOrderBy.append(entry.getValue().getAsString());
			}
			if(sbOrderBy.length() > 0)
				sb.append(sbOrderBy);
		}
		
//		//Page & Count 
//		if (page != null && page>=1 && count != null && count >=0){
//			sb.append(" LIMIT ");
//			sb.append(page - 1);
//			sb.append(", ");
//			sb.append(count);
//			sb.append(" ");
//		}
		
		Query query = entityManagerHelper.query(sb.toString(), BoardModel.class);		
		return query.getResultList();
		
	}


	public void enableBoard(BoardModel board, boolean enabled){
		boardRepository.enableBoard(board.getBoardId(), enabled);
	}

	public void delete(BoardModel board){
		boardRepository.deleteByBoardId(board.getBoardId());
	}

	public void save(BoardCategoryModel category){
		final String queryText = "REPLACE INTO tb_board_category (uid, parentUid, boardUid, categoryId, properties, displayOrder, enabled, lastUpdated, registeredAt) "
				+ "values (UNHEX(:uid), UNHEX(:parentUid), UNHEX(:boardUid), :categoryId, "
				+ XcJsonUtils.toJsonCreate(category.getProperties()) + ", :displayOrder, :enabled, :lastUpdated, :registeredAt)" ;
		Query query = entityManagerHelper.query(queryText);
		query.setParameter(DbConst.FIELD_UID, category.getDecryptedUid());
		query.setParameter("parentUid", category.getDecryptedParentUid());
		query.setParameter("boardUid", category.getDecryptedBoardUid());
		query.setParameter("categoryId", category.getCategoryId());
		query.setParameter("displayOrder", category.getDisplayOrder());
		query.setParameter(DbConst.FIELD_ENABLED, category.getEnabled());
		query.setParameter(DbConst.FIELD_LAST_UPDATED, category.getLastUpdated());
		query.setParameter(DbConst.FIELD_REGISTERED_AT, category.getRegisteredAt());
		
		entityManagerHelper.execute(query);
	}
	
	public BoardCategoryModel getCategoryByCategoryId(String boardUid, String categoryId){
		return boardCategoryRepository.findByCategoryId(EncodeUtils.decrypt(boardUid), categoryId);
	}
	
	public BoardCategoryModel getCategoryByCategoryUid(String categoryUid){
		return boardCategoryRepository.findByCategoryUid(EncodeUtils.decrypt(categoryUid));
	}

	public List<BoardCategoryModel> getCategories(){
		return boardCategoryRepository.getAll();
	}
	
	public List<BoardCategoryModel> getCategoriesByBoardUid(String boardUid){
		return boardCategoryRepository.findByBoardUid(EncodeUtils.decrypt(boardUid));
	}

	public List<BoardCategoryModel> getCategoriesByBoardId(String boardId){
		return boardCategoryRepository.findByBoardId(boardId);
	}

	public List<BoardCategoryModel> getCategoriesByBoardUid(String boardUid, Boolean enabled){
		if (enabled != null)
			return boardCategoryRepository.findByBoardUidAndEnabled(EncodeUtils.decrypt(boardUid), enabled);
		else
			return getCategoriesByBoardUid(boardUid);
	}

	public List<BoardCategoryModel> getCategoriesByBoardId(String boardId, Boolean enabled){
		if (enabled != null)
			return boardCategoryRepository.findByBoardUidAndEnabled(boardId, enabled);
		else
			return getCategoriesByBoardId(boardId);
	}

	public void enable(BoardCategoryModel category, boolean enabled){
		boardCategoryRepository.enable(category.getDecryptedUid(), enabled);
	}

	public void delete(BoardCategoryModel category){
		boardCategoryRepository.deleteByUid(category.getDecryptedUid());
		// Need to remove category from the content
		boardContentRepository.removeCategoryFromContent(category.getDecryptedBoardUid(), category.getCategoryId());
	}
	
	public void save(BoardContentModel content){
		final String queryText = "REPLACE INTO tb_board_content (uid, parentUid, boardUid, writerUid, categoryUid, title, content, properties, enabled, lastUpdated, registeredAt) "
				+ "values (UNHEX(:uid), UNHEX(:parentUid), UNHEX(:boardUid), UNHEX(:writerUid), UNHEX(:categoryUid), :title, :content, " 
				+ XcJsonUtils.toJsonCreate(content.getProperties()) + ", :enabled, :lastUpdated, :registeredAt)" ;
		Query query = entityManagerHelper.query(queryText);
		query.setParameter(DbConst.FIELD_UID, content.getDecryptedUid());
		query.setParameter(DbConst.FIELD_PARENT_UID, content.getDecryptedParentUid());
		query.setParameter(DbConst.FIELD_BOARD_UID, content.getDecryptedBoardUid());
		query.setParameter(DbConst.FIELD_CATEGORY_UID, content.getDecryptedCategoryUid());
		query.setParameter(DbConst.FIELD_WRITER_UID, content.getDecryptedWriterUid());
		query.setParameter(DbConst.FIELD_TITLE, content.getTitle());
		query.setParameter(DbConst.FIELD_CONTENT, content.getContent());
		query.setParameter(DbConst.FIELD_ENABLED, content.getEnabled());
		query.setParameter(DbConst.FIELD_LAST_UPDATED, content.getLastUpdated());
		query.setParameter(DbConst.FIELD_REGISTERED_AT, content.getRegisteredAt());
		
		entityManagerHelper.execute(query);
	}
	
	public BoardContentModel getContent(String contentUid){
		return boardContentRepository.findByContetUid(EncodeUtils.decrypt(contentUid));
	}
	
	public List<BoardDetailedContentModel> getContents(String orderBy, String filter, Boolean enabled, Integer page, Integer count){
		String selectStatement = "SELECT boardUid, boardId, "
				+ "CAST(COLUMN_JSON(boardProperties) AS CHAR) AS boardProperties, "
				+ "writerUid, writerName, "
				+ "CAST(COLUMN_JSON(writerProperties) AS CHAR) AS writerProperties, "
				+ "contentUid, parentUid, title, content, "
				+ "CAST(COLUMN_JSON(contentProperties) AS CHAR) AS contentProperties, enabled, lastUpdated, registeredAt FROM tv_board_content ";
		
		StringBuilder sb = new StringBuilder(selectStatement);
		
		StringBuilder sbWhere = new StringBuilder();
		
		JsonElement jElement = XcJsonUtils.toJsonElement(filter);
		if (jElement != null && jElement.isJsonObject()){
			JsonObject jFilter = jElement.getAsJsonObject();
			for(Entry<String, JsonElement> entry : jFilter.entrySet()){
				final String value = entry.getValue().getAsString();
				if (XcStringUtils.isNullOrEmpty(value))
					continue;
				if (sbWhere.length() == 0){
					sbWhere.append(" WHERE ");
				}else
					sbWhere.append(" AND ");
				String key = entry.getKey();
				if (key.endsWith("Uid")){
					sbWhere.append(key);
					sbWhere.append(" = UNHEX('");
					sbWhere.append(EncodeUtils.decrypt(value));
					sbWhere.append("') ");
					continue;
				}
				if (entry.getKey().startsWith("properties")){
					key = "COLUMN_GET(properties, '"+XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbWhere.append(key);
				}else{
					sbWhere.append(key);
				}
				sbWhere.append(" like '%");
				sbWhere.append(value);
				sbWhere.append("%' ");
			}			
		}
		if (sbWhere.length() > 0)
			sb.append(sbWhere);
		
		jElement = XcJsonUtils.toJsonElement(orderBy);
		if (jElement != null && jElement.isJsonObject()){
			JsonObject jFilter = jElement.getAsJsonObject();
			StringBuilder sbOrderBy = new StringBuilder();
			for(Entry<String, JsonElement> entry : jFilter.entrySet()){
				if (sbOrderBy.length() == 0)
					sbOrderBy.append(" ORDER BY ");
				else
					sbOrderBy.append(", ");
				if (entry.getKey().startsWith("boardProperties")){
					String key = "COLUMN_GET(boardProperties, '"+ XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbOrderBy.append(key);
				}else if (entry.getKey().startsWith("writerProperties")){
					String key = "COLUMN_GET(writerProperties, '"+ XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbOrderBy.append(key);
				}else if (entry.getKey().startsWith("contentProperties")){
					String key = "COLUMN_GET(contentProperties, '"+ XcUrlUtils.getPropertyKey(entry.getKey())+"' AS CHAR)";
					sbOrderBy.append(key);
				}else{
					sbOrderBy.append(entry.getKey());
				}
				sbOrderBy.append(" ");
				sbOrderBy.append(entry.getValue().getAsString());
			}
			if(sbOrderBy.length() > 0)
				sb.append(sbOrderBy);
		}
		
//		//Page & Count 
//		if (page != null && page>=1 && count != null && count >=0){
//			sb.append(" LIMIT ");
//			sb.append(page - 1);
//			sb.append(", ");
//			sb.append(count);
//			sb.append(" ");
//		}
		
		Query query = entityManagerHelper.query(sb.toString(), BoardDetailedContentModel.class);		
		return query.getResultList();
	}
	
	
	public List<BoardContentModel> getAllContents(){
		return boardContentRepository.findAll();
	}
	
	/**
	 * 
	 * @param boardId
	 * @param categoryId
	 * @return
	 */
	public List<BoardContentModel> getContentsById(String boardId, String categoryId){
		if (XcStringUtils.isNullOrEmpty(boardId))
			return null;
		
		BoardModel board = this.getBoardByBoardId(boardId);
		if (board == null)
			return null;
		
		if (XcStringUtils.isValid(categoryId)){
			BoardCategoryModel category = this.getCategoryByCategoryId(board.getUid(), categoryId);
			if (category == null)
				return null;
			return getContentsByUid(board.getUid(), category.getUid());
		}else{
			return getContentsByUid(board.getDecryptedUid());
		}
	}

	public List<BoardContentModel> getContentsByUid(String boardUid){
		return boardContentRepository.findByBoardUid(EncodeUtils.decrypt(boardUid));
	}

	/**
	 * 
	 * @param boardUid
	 * @param categoryUid
	 * @return
	 */
	public List<BoardContentModel> getContentsByUid(String boardUid, String categoryUid){
		return boardContentRepository.findByBoardUidAndCategoryUid(EncodeUtils.decrypt(boardUid), EncodeUtils.decrypt(categoryUid));
	}

	/**
	 * Enable/disable to visible the content
	 * @param content
	 * @param enabled
	 */
	public void enable(BoardContentModel content, boolean enabled){
		boardContentRepository.enable(content.getDecryptedUid(), enabled);
	}

	/**
	 * Delete content
	 * @param content
	 */
	public void delete(BoardContentModel content){
		boardContentRepository.deleteByUid(content.getDecryptedUid());
	}
}
