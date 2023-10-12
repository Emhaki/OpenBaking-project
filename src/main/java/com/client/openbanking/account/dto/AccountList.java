package com.client.openbanking.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * 등록계좌조회 api 응답 메세지 - 사용자 등록계좌 목록
 * @author      : 김응경
 * @date        : 2023. 8. 21.
 */
public class AccountList {
	private String fintech_use_num;
	private String account_alias;
	private String bank_code_std;
	private String bank_code_sub;
	private String bank_name;
	private String savings_bank_name;
	private String account_num_masked;
	private String account_seq;
	private String account_holder_name;
	private String account_holder_type;
	private String account_type;
	private String inquiry_agree_yn;
	private String inquiry_agree_dtime;
	private String transfer_agree_yn;
	private String transfer_agree_dtime;
	private String account_state;
	
	// 잔액조회 API 응답 값 + 계좌 해지 API 응답값
	private String api_tran_id;
	private String api_tran_dtm;
	private String rsp_code;
	private String rsp_message;
	private String bank_tran_id;
	private String bank_tran_date;
	private String bank_code_tran;
	private String bank_rsp_code;
	private String bank_rsp_message;
	private String balance_amt;
	private String available_amt;
	private String product_name;
	private String account_issue_date;
	private String maturity_date;
	private String last_tran_date;
	
}
