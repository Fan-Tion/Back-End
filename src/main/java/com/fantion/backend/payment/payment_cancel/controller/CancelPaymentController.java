package com.fantion.backend.payment.payment_cancel.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import com.fantion.backend.payment.payment_cancel.base.dto.CommonResult;
import com.fantion.backend.payment.payment_cancel.base.dto.ListResult;
import com.fantion.backend.payment.payment_cancel.base.exception.BussinessException;
import com.fantion.backend.payment.payment_cancel.base.service.ResponseService;
import com.fantion.backend.payment.payment_cancel.dto.CancelPaymentRes;
import com.fantion.backend.payment.payment_cancel.dto.CancelPaymentReq;
import com.fantion.backend.payment.payment_cancel.service.CancelPaymentService;

@Api(tags = "13. 결제 취소")
@RequestMapping("/v1/api/cancelPayment")
@RestController
@RequiredArgsConstructor
public class CancelPaymentController {

	private final ResponseService responseService;
	private final CancelPaymentService cancelPaymentService;
	private final int FAIL = -1;

	@PostMapping
	@ApiOperation(
			value = "결제취소",
			notes = "결제완료 건에 대해서 결제취소합니다.<br>"
	)
	public CommonResult requestPaymentCancel(
			@ApiParam(value = "이메일", required = true) @RequestParam String email,
			@ApiParam(value = "예약 번호", required = true) @RequestParam Long reservationSeq,
			@ApiParam(value = "가상계좌 취소시 작성") @ModelAttribute CancelPaymentReq cancelPaymentReq
			) {
		try {
			cancelPaymentService.requestPaymentCancel(email, reservationSeq, cancelPaymentReq);
			return responseService.getSuccessResult();
		} catch (Exception e) {
			return responseService.getFailResult(
					-1,
					e.getMessage()
			);
		}
	}

	@GetMapping
	@ApiOperation(value = "고객 예약 취소 목록 전체 조회", notes = "고객이 취소한 모든 예약 목록을 조회합니다.")
	public ListResult<CancelPaymentRes> getAllCancelPayments(
			@ApiParam(value = "고객 이메일", required = true) @RequestParam String memberEmail,
			@ApiParam(value = "PAGE 번호 (0부터)", required = true) @RequestParam(defaultValue = "0") int page,
			@ApiParam(value = "PAGE 크기", required = true) @RequestParam(defaultValue = "20") int size
	) {
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by("cancelDate").descending());
		try {
			return responseService.getListResult(
					cancelPaymentService.getAllCancelPayments(memberEmail, pageRequest)
			);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BussinessException(e.getMessage());
		}
	}
}
