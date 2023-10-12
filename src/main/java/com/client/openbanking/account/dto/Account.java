package com.client.openbanking.account.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * 등록계좌 api 응답 메세지 
 * @author      : 김응경
 * @date        : 2023. 8. 21.
 */
public class Account {
	private String api_tran_id;
	private String api_tran_dtm;
	private String rsp_code;
	private String rsp_message;
	private String user_name;
	private String res_cnt;
	private List<AccountList> res_list;
	
}
