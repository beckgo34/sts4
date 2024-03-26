package com.icia.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardFileDto {
	private int bf_num; // boardfile 기본키(딱히 쓸데는 없음)
	private int bf_bnum;
	private String bf_oriname;
	private String bf_sysname;
}
