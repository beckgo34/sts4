package com.icia.board.service;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.icia.board.config.util.PagingUtil;
import com.icia.board.dao.BoardDao;
import com.icia.board.dao.MemberDao;
import com.icia.board.dto.BoardDto;
import com.icia.board.dto.BoardFileDto;
import com.icia.board.dto.ReplyDto;
import com.icia.board.dto.SearchDto;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BoardService {
	@Autowired
	private BoardDao bDao;
	@Autowired
	private MemberDao mDao;  // 회원 point 정보 변경에 사용
	
	// transaction 관련
	@Autowired
	private PlatformTransactionManager manager;
	@Autowired
	private TransactionDefinition definition;
	
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
	
	// 게시글, 파일 저장 및 회원 정보(point) 변경
	public String boardWrite(List<MultipartFile> files,
							 BoardDto board,
							 HttpSession session,
							 RedirectAttributes rttr) {
		log.info("boardWrite()");
		
		// 트랜젝션 상태 처리 객체
		TransactionStatus status = manager.getTransaction(definition);
		
		String view = null;
		String msg = null;
		
		try {
			// 게시글 저장
			bDao.insertBoard(board);
			log.info("b_num: {}", board.getB_num());
			
			// 파일 저장
			if(!files.get(0).isEmpty()) {// 업로드 파일이 있다면
				fileUpload(files, session, board.getB_num());
			}
			
			// 작성자의 point 수정
			
			// commit 수행
			manager.commit(status);
			view = "redirect:boardList?pageNum=1";
			msg = "작성 성공";
		}catch (Exception e) {
			e.printStackTrace();
			// rollback 수행
			manager.rollback(status);
			view = "redirect:writeForm";
			msg = "작성 실패";
		}
		
		rttr.addFlashAttribute("msg", msg);
		
		return view;
	}

	private void fileUpload(List<MultipartFile> files, 
							HttpSession session,
							int b_num) throws Exception {
		// 파일 저장 실패 시 데이터베이스 롤백작업이 이루어지도록 예외를 throws 할 것.
		log.info("fileUpload()");
		
		// 파일 저장 위치 처리(session에서 저장 경로를 구함)
		String realPath = session.getServletContext().getRealPath("/");
		log.info("realPath : {}", realPath);
		
		realPath += "upload/"; // 파일 업로드 폴더
		
		File folder = new File(realPath);
		if(folder.isDirectory() == false) {
			folder.mkdir(); // 폴더 생성 메소드
		}
		
		for(MultipartFile mf : files) {
			// 파일명 추출
			String oriname = mf.getOriginalFilename();
			
			BoardFileDto bfd = new BoardFileDto();
			bfd.setBf_oriname(oriname);
			bfd.setBf_bnum(b_num);
			String sysname = System.currentTimeMillis()
					+ oriname.substring(oriname.lastIndexOf("."));
			// 확장자 : 파일을 구분하기 위한 식별 체계. (예. image.jpg)
			bfd.setBf_sysname(sysname);
			
			// 파일 저장
			File file = new File(realPath + sysname);
			mf.transferTo(file);
			
			// 파일 정보 저장
			bDao.insertFile(bfd);
		}
	}

	public String getBoard(int b_num, 
						Model model) {
		log.info("getBoard()");
		
		// 게시글 번호(b_num)로 게시물 가져오기
		BoardDto board = bDao.selectBoard(b_num);
		model.addAttribute("board", board);
		
		// 파일 목록 가져오기
		List<BoardFileDto> bfList = bDao.selectFileList(b_num);
		model.addAttribute("bfList", bfList);
		// 댓글 목록 가져오기
		List<ReplyDto> rList = bDao.selectReplyDto(b_num);
		model.addAttribute("rlist", rList);
		return "boardDetail";
	}
	
	
	
	
	
	
	
	
	
	
	
	
} //class end
