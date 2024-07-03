package com.fantion.backend.payment.tossgrpc;

import com.fantion.backend.payment.service.AccountService;
import com.fantion.backend.payment.service.HoldingService;
import com.fantion.backend.payment.service.TransactionService;
import com.fantion.backend.payment.dto.HoldingDTO;
import com.fantion.backend.payment.dto.TransactionDTO;
import com.fantion.backend.payment.dto.WithdrawalReqDTO;
import com.fantion.backend.payment.exception.*;
import io.tossgrpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TossPaymentServiceImpl extends TossPaymentServiceGrpc.PaymentServiceImplBase {

    private final AccountService accountService;
    private final HoldingService holdingService;
    private final TransactionService transactionService;

    @Override
    @Transactional
    public void doPayment(PaymentServiceOuterClass.TransactionRequest request, StreamObserver<PaymentServiceOuterClass.TransactionResponse> responseObserver) {
        try {
            TransactionDTO transactionDTO = transactionService.withdraw(WithdrawalReqDTO.builder().accountId(request.getTransaction().getAccountId())
                            .amount(request.getTransaction().getAmount())
                            .label(request.getTransaction().getLabel()).build(),
                    holdingService.getHoldingSummaryInfo(request.getTransaction().getAccountId()));

            responseObserver.onNext(PaymentServiceOuterClass.TransactionResponse.newBuilder()
                    .setResult(PaymentServiceOuterClass.Response.newBuilder()
                            .setResult(PaymentServiceOuterClass.Response.Result.OK).build())
                    .setTransaction(TossPaymentServiceOuterClass.Transaction.newBuilder()
                            .setId(transactionDTO.getId())
                            .setAccountId(transactionDTO.getAccountId())
                            .setType(PaymentServiceOuterClass.Transaction.Type.WITHDRAWAL)
                            .setAmount(transactionDTO.getAmount())
                            .setBalance(transactionDTO.getBalance())
                            .setLabel(transactionDTO.getLabel())
                            .setRecordedDate(transactionDTO.getRecordedDate().getTime()).build()).build());
        } catch (AccountNotFoundException e) {
            responseObserver.onNext(PaymentServiceOuterClass.TransactionResponse.newBuilder()
                    .setResult(PaymentServiceOuterClass.Response.newBuilder()
                            .setResult(PaymentServiceOuterClass.Response.Result.NOT_FOUND).build()).build());
        } catch (AccountIllegalStateException | AccountBalanceShortageException e) {
            responseObserver.onNext(PaymentServiceOuterClass.TransactionResponse.newBuilder()
                    .setResult(PaymentServiceOuterClass.Response.newBuilder()
                            .setResult(PaymentServiceOuterClass.Response.Result.CONFLICT).build()).build());
        }

        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void doHolding(PaymentServiceOuterClass.HoldingRequest request, StreamObserver<PaymentServiceOuterClass.HoldingResponse> responseObserver) {
        try {
            if (accountService.getAccountInfoList(request.getUserId()).stream().noneMatch(accountDTO -> accountDTO.getUserId() == request.getUserId())) {
                throw new InvalidRequestException();
            }

            HoldingDTO holdingDTO = holdingService.createHolding(HoldingDTO.builder().accountId(request.getHolding().getAccountId())
                    .amount(request.getHolding().getAmount())
                    .expiredDate(new Date(request.getHolding().getExpiredDate())).build());

            responseObserver.onNext(PaymentServiceOuterClass.HoldingResponse.newBuilder()
                    .setResult(PaymentServiceOuterClass.Response.newBuilder()
                            .setResult(PaymentServiceOuterClass.Response.Result.OK).build())
                    .setHolding(PaymentServiceOuterClass.Holding.newBuilder()
                            .setId(holdingDTO.getId())
                            .setAccountId(holdingDTO.getAccountId())
                            .setAmount(holdingDTO.getAmount())
                            .setBalance(holdingDTO.getBalance())
                            .setExpiredDate(holdingDTO.getExpiredDate().getTime())
                            .setRecordedDate(holdingDTO.getRecordedDate().getTime())
                            .setLastModifiedDate(holdingDTO.getLastModifiedDate().getTime())
                            .setStatus(PaymentServiceOuterClass.Holding.Status.valueOf(holdingDTO.getStatusType().name()))).build());
        } catch (AccountNotFoundException e) {
            responseObserver.onNext(PaymentServiceOuterClass.HoldingResponse.newBuilder()
                    .setResult(PaymentServiceOuterClass.Response.newBuilder()
                            .setResult(PaymentServiceOuterClass.Response.Result.NOT_FOUND).build()).build());
        } catch (AccountBalanceShortageException e) {
            responseObserver.onNext(PaymentServiceOuterClass.HoldingResponse.newBuilder()
                    .setResult(PaymentServiceOuterClass.Response.newBuilder()
                            .setResult(PaymentServiceOuterClass.Response.Result.CONFLICT).build()).build());
        }

        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void extendHolding(PaymentServiceOuterClass.HoldingRequest request, StreamObserver<PaymentServiceOuterClass.HoldingResponse> responseObserver) {
        try {
            HoldingDTO holdingDTO = holdingService.extendHolding(request.getHolding().getId(), new Date(request.getHolding().getExpiredDate()));

            responseObserver.onNext(PaymentServiceOuterClass.HoldingResponse.newBuilder()
                    .setResult(PaymentServiceOuterClass.Response.newBuilder()
                            .setResult(PaymentServiceOuterClass.Response.Result.OK).build())
                    .setHolding(PaymentServiceOuterClass.Holding.newBuilder()
                            .setId(holdingDTO.getId())
                            .setAccountId(holdingDTO.getAccountId())
                            .setAmount(holdingDTO.getAmount())
                            .setBalance(holdingDTO.getBalance())
                            .setExpiredDate(holdingDTO.getExpiredDate().getTime())
                            .setRecordedDate(holdingDTO.getRecordedDate().getTime())
                            .setLastModifiedDate(holdingDTO.getLastModifiedDate().getTime())
                            .setStatus(PaymentServiceOuterClass.Holding.Status.valueOf(holdingDTO.getStatusType().name()))).build());
        } catch (HoldingNotFoundException e) {
            responseObserver.onNext(PaymentServiceOuterClass.HoldingResponse.newBuilder()
                    .setResult(PaymentServiceOuterClass.Response.newBuilder()
                            .setResult(PaymentServiceOuterClass.Response.Result.NOT_FOUND).build()).build());
        } catch (InvalidRequestException e) {
            responseObserver.onNext(PaymentServiceOuterClass.HoldingResponse.newBuilder()
                    .setResult(PaymentServiceOuterClass.Response.newBuilder()
                            .setResult(PaymentServiceOuterClass.Response.Result.CONFLICT).build()).build());
        }

        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void clearHolding(PaymentServiceOuterClass.HoldingRequest request, StreamObserver<PaymentServiceOuterClass.Response> responseObserver) {
        try {
            holdingService.changeHoldingStatusClosed(request.getHolding().getId());

            responseObserver.onNext(PaymentServiceOuterClass.Response.newBuilder()
                    .setResult(PaymentServiceOuterClass.Response.Result.OK).build());
        } catch (HoldingNotFoundException e) {
            responseObserver.onNext(PaymentServiceOuterClass.Response.newBuilder()
                    .setResult(PaymentServiceOuterClass.Response.Result.NOT_FOUND).build());
        } catch (HoldingIllegalStateException e) {
            responseObserver.onNext(PaymentServiceOuterClass.Response.newBuilder()
                    .setResult(PaymentServiceOuterClass.Response.Result.CONFLICT).build());
        }

        responseObserver.onCompleted();
    }
}