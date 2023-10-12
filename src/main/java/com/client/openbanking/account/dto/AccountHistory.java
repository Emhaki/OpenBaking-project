package com.client.openbanking.account.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * 거래내역조회 api 응답메세지
 * @author      : 김응경
 * @date        : 2023. 8. 28.
 */
public class AccountHistory {
	private String api_tran_id;
	private String api_tran_dtm;
	private String rsp_code;
	private String rsp_message;
	private String bank_tran_id;
	private String bank_tran_date;
	private String bank_code_tran;
	private String bank_rsp_code;
	private String bank_rsp_message;
	private String bank_name;
	private String savings_bank_name;
	private String fintech_use_num;
	private String balance_amt;
	private String page_record_cnt;
	private String next_page_yn;
	private String befor_inquiry_trace_info;
	private List<AccountHistoryList> res_list;
}