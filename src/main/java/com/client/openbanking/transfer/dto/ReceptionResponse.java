package com.client.openbanking.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 수취조회 API 응답 값
 * @author 이명학
 * Date : 2023. 8. 30.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceptionResponse {
	
	private String api_tran_id;
	private String api_tran_dtm;
	private String rsp_code;
	private String rsp_message;
	private String bank_code_std;
	private String bank_code_sub;
	private String bank_name;
	private String savings_bank_name;
	private String account_num;
	private String account_seq;
	private String account_num_masked;
	private String print_content;
	private String account_holder_name;
	private String bank_tran_id;
	private String bank_tran_date;
	private String bank_code_tran;
	private String bank_rsp_code;
	private String bank_rsp_message;
	private String wd_bank_code_std;
	private String wd_bank_name;
	private String wd_account_num;
	private String tran_amt;
	private String cms_num;
	
}
