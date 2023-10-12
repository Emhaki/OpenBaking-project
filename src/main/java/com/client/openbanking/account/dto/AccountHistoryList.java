package com.client.openbanking.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * 거래내역조회 api 응답메세지 - 조회된 거래내역
 * @author      : 김응경
 * @date        : 2023. 8. 28.
 */
public class AccountHistoryList {
	private String tran_date;
	private String tran_time;
	private String inout_type;
	private String tran_type;
	private String print_content;
	private String tran_amt;
	private String after_balance_amt;
	private String branch_name;
}