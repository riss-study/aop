package dev.riss.aop.exam.aop;

import dev.riss.aop.exam.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Slf4j
@Aspect
public class RetryAspect {

  // Retry 애노테이션을 파라미터로 받으면 그 변수로 타입 정보 대체 가능
  @Around("@annotation(retry)")
  public Object retry(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {

    log.info("[retry] {}, retry={}", joinPoint.getSignature().getName(), retry);

    // 재시도 횟수 가져옴
    int maxRetry = retry.value();
    Exception exceptionHolder = null;

    // 1. 재시도 횟수 만큼 시도
    for (int retryCount = 1; retryCount <= maxRetry; retryCount++) {
      try {
        log.info("[retry] try count: {}/{}", retryCount, maxRetry);
        // 성공하면 리턴
        return joinPoint.proceed();
      } catch (Exception e) { // Exception 까지만 잡음. Throwable 이 터졌으면 자바 메모리가 없거나 컨트롤이 불가능한 예외 이므로 밖으로 던짐
        // 실패하면 exception 저장
        exceptionHolder = e;
      }
    }
    // for 문 빠져나옴 -> 재시도 횟수 만큼 시도하였으나 실패 -> 에러 던짐
    throw exceptionHolder;
  }
}
