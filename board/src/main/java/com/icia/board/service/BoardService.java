package com.icia.board.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.icia.board.config.util.PagingUtil;
import com.icia.board.dao.BoardDao;
import com.icia.board.dto.BoardDto;
import com.icia.board.dto.SearchDto;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BoardService {
	@Autowired
	private BoardDao bDao;
	
	private int lcnt = 10; // 한 화면(페이지)에 보여질 게시글 개수
	
	public String getBoardList(SearchDto sdto,
							   HttpSession session,
							   Model model) {
		log.info("getBoardList()");
		
		String view = "boardList";
		//DB에서 게시글 목록 가져오기
		int num = sdto.getPageNum();
		
		if(sdto.getListCnt() == 0) {
			sdto.setListCnt(lcnt); // 목록 개수 값 설정(초기 10개)
		}
		
		sdto.setPageNum((num - 1) * sdto.getListCnt());
		List<BoardDto> bList = bDao.selectBoardList(sdto);
		model.addAttribute("bList", bList);
		
		// 페이징 처리
		sdto.setPageNum(num);
		String pageHtml = getPaging(sdto);
		model.addAttribute("paging", pageHtml);
		
		// 페이지 관련 내용 세션에 저장
		if(sdto.getColname() != null) {
			session.setAttribute("sdto", sdto);
		} else {
			session.removeAttribute("sdto"); // 검색을 안한 목록을 위해 삭제.
		}
		// 별개로 페이지 번호도 저장
		session.setAttribute("pageNum", num);
		
		return view;
	}

	private String getPaging(SearchDto sdto) {
		log.info("getPaging()");
		
		String pageHtml = null;
		
		// 전체 게시글 개수
		int maxNum = bDao.selectBoardCnt(sdto);
		
		int pageCnt = 3; // 페이지에서 보여질 페이지 번호 개수
		
		String listName = "boardList?";
		if(sdto.getColname() != null) {
			// 검색 기능을 사용한 경우
			listName += "colname=" + sdto.getColname()
					 + "&keyword=" + sdto.getKeyword() +"&";
			// <a href='/boardList?colname=b_tiltle&keyword=3&pageNum=1'>
		}
		
		PagingUtil paging = new PagingUtil(maxNum, sdto.getPageNum(),
											sdto.getListCnt(),pageCnt,
											listName);
		
		pageHtml = paging.makePaging();
		
		return pageHtml;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
} //class end
