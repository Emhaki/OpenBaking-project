package com.client.openbanking.transfer.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 입금이체 API 응답 값
 * @author 이명학
 * Date : 2023. 8. 25.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepositResponse {
	private String api_tran_id;
	private String api_tran_dtm;
	private String rsp_code;
	private String rsp_message;
	private String wd_bank_code_std;
	private String wd_bank_code_sub;
	private String wd_bank_name;
	private String wd_account_num_masked;
	private String wd_print_content;
	private String wd_account_holder_name;
	private String res_cnt;
	private List<DepositResponseList> res_list;
	
}
