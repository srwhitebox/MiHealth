package com.mihealth.webservice;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mihealth.db.model.BoardCategoryModel;
import com.mihealth.db.model.BoardContentModel;
import com.mihealth.db.model.BoardDetailedContentModel;
import com.mihealth.db.model.BoardModel;
import com.mihealth.db.model.TokenModel;
import com.mihealth.db.model.UserModel;
import com.mihealth.db.service.BoardService;
import com.mihealth.db.service.TokenService;
import com.ximpl.lib.constant.DbConst;
import com.ximpl.lib.util.XcJsonUtils;
import com.ximpl.lib.util.XcStringUtils;

@Controller
@RequestMapping(value = "/api/board")
public class BoardApi {
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private BoardService boardService;

	/**
	 * Save board information.
	 * If the data doesn't include board UID, it'll create new board.
	 * @param request
	 * @param principal
	 * @return
	 */
	@RequestMapping(value = "save", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String addBoard(HttpServletRequest request, Authentication authentication){
		BoardModel board = (BoardModel) XcJsonUtils.toObject(request, BoardModel.class);
		if (board !=null && board.getUid() == null){
			board.initUid();
			board.initRegisteredAt();
		}
		boardService.save(board);
		return ApiResponse.getSucceedResponse(board);
	}

	
	@RequestMapping(value = "category/save", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String addCategory(HttpServletRequest request, Authentication authentication) {
		BoardCategoryModel category = (BoardCategoryModel) XcJsonUtils.toObject(request, BoardCategoryModel.class);
		
		if (category == null){
			return ApiResponse.getFailedResponse();
		}
		
		if (XcStringUtils.isNullOrEmpty(category.getUid())){
			category.initUid();
		}
		
		if (category.getRegisteredAt() == null){
			category.initRegisteredAt();
		}else
			category.setLastUpdated(new DateTime(DateTimeZone.UTC));
		
		boardService.save(category);
		
		return ApiResponse.getSucceedResponse(category);
	}

	/**
	 * Save message content.
	 * If the content doesn't have content UID, it'll create new content.
	 * @param request
	 * @param authentication
	 * @return
	 */
	@RequestMapping(value = "content/save", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String saveContent(HttpServletRequest request, Authentication authentication){
		BoardDetailedContentModel detailedContent = (BoardDetailedContentModel) XcJsonUtils.toObject(request, BoardDetailedContentModel.class);
		BoardContentModel content = new BoardContentModel();
		if (detailedContent !=null && detailedContent.getContentUid() == null){
			content.initUid();
			content.initRegisteredAt();
			
			UserModel user = (UserModel)authentication.getPrincipal();
			content.setWriterUid(user.getUid());
			
		}else{
			content.setUid(detailedContent.getContentUid());
			content.setLastUpdated(new Date());
			content.setRegisteredAt(detailedContent.getRegisteredAt());
			content.setWriterUid(detailedContent.getWriterUid());
		}

		content.setBoardUid(detailedContent.getBoardUid());
		content.setTitle(detailedContent.getTitle());
		content.setContent(detailedContent.getContent());

		boardService.save(content);
		
		return ApiResponse.getSucceedResponse(content);
	}
	
	/**
	 * Return board list.
	 * @param enabled
	 * @param principal
	 * @return
	 */
	@RequestMapping(value = "list", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String boardList(
			@RequestParam(required=false) String orderBy, 
			@RequestParam(required=false) String filter,
			@RequestParam(required=false) Boolean enabled,
			@RequestParam(required=false) Integer count, @RequestParam(required=false) Integer page,
			Principal principal){
		
		List<BoardModel> list = boardService.getBoards(orderBy, filter, enabled, page, count);
		
		if (list == null || list.isEmpty())
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_RECORD, "No board found.");
		
		return ApiResponse.getSucceedResponse(list);
	}

	@RequestMapping(value = "category/list", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String categoryList(
			@RequestParam(value = "boardUid", required = false) String boardUid, 
			@RequestParam(value = "boardId", required = false) String boardId,
			@RequestParam(value = DbConst.FIELD_ENABLED, required = false) Boolean enabled,
			Principal principal){
		
		List<BoardCategoryModel> list = null;
		if (XcStringUtils.isValid(boardUid))
			list = boardService.getCategoriesByBoardUid(boardUid, enabled);
		else if (XcStringUtils.isValid(boardId))
			list = boardService.getCategoriesByBoardId(boardId, enabled);
		else
			list = boardService.getCategories();
		
		if (list == null || list.isEmpty())
			return ApiResponse.getMessage(ApiResponse.CODE_FAILED_NO_RECORD, "No board found.");
		return ApiResponse.getSucceedResponse(list);
	}

	
	@RequestMapping(value = "content/list", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String list(
			@RequestParam(required=false) String orderBy, 
			@RequestParam(required=false) String filter,
			@RequestParam(required=false) Boolean enabled,
			@RequestParam(required=false) Integer count, @RequestParam(required=false) Integer page,
			Principal principal){
		
		List<BoardDetailedContentModel> list = boardService.getContents(orderBy, filter, enabled, page, count);
		
		return ApiResponse.getSucceedResponse(list);	
	}	

	
	@RequestMapping(value = "delete/{boardUid}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String deleteBoard(HttpServletRequest request, @PathVariable("boardUid") String boardUid, Principal principal) {
		BoardModel board = boardService.getBoardByBoardId(boardUid);
		if (board == null)
			return ApiResponse.getMessage(ApiResponse.CODE_UNKOWN, "Unknown board.");

		boardService.delete(board);
		
		return ApiResponse.getSucceedResponse("Board has been removed.");
	}

	@RequestMapping(value = "delete/category/{categoryUid}", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String deleteBoardCategry(HttpServletRequest request, @PathVariable("categoryUid") String categoryUid, Principal principal) {
		BoardCategoryModel category = boardService.getCategoryByCategoryUid(categoryUid);
		if (category == null)
			return ApiResponse.getMessage(ApiResponse.CODE_UNKOWN, "Unknown board.");

		boardService.delete(category);
		
		return ApiResponse.getSucceedResponse("Board has been removed.");
	}

	@RequestMapping(value = "content/delete/{contentUid}", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody public String deleteBoardContent(HttpServletRequest request, @PathVariable("contentUid") String contentUid, Principal principal) {
		BoardContentModel content = boardService.getContent(contentUid);
		if (content == null)
			return ApiResponse.getMessage(ApiResponse.CODE_UNKOWN, "Unknown board.");

		boardService.delete(content);
		
		return ApiResponse.getSucceedResponse("Board has been removed.");
	}

}
