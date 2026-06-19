package dev.riss.aop.order.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Slf4j
@Aspect
public class AspectV6Advice {

  /**
   * Around: 가장 강력한 어드바이스
     * 메서드 실행의 주변에서 실행, 메서드 실행 전후에 작업 수행 -> 조인포인트 실행 여부 및 위치 선택 가능 (proceed 호출 위치)
     * 전달 값(인수) 변환 가능 -> joinPoint.proceed(args[]); => 실제 타겟에 다른 아규먼트를 넘겨줄 수도 있음
     * 반환 값 변환 가능 -> result 를 다른 걸로 리턴하면 됨
     * 예외 변환 가능 -> catch 해서 다른 예외 던지면 됨
     * 트랜잭션 처럼 try-catch-finally 구문 처리 가능 (before, after 등 다른 어드바이스는 안됨)
   * 어드바이스의 첫 번째 파라미터는 무조건 ProceedingJoinPoint 사용해야 함
   * proceed() 를 통해 대상 수행 -> proceed() 여러번 호출하여 여러번 실행할 수도 있음(재시도)
   * proceed() 를 무조건 호출해야 함 -> 호출 안하면, 다음 체인 호출 x (어드바이스, 타겟 등) -> 의도가 아니라면, 실제 타겟 호출 절대 안돼서 심각한 장애 야기
   */
  @Around("dev.riss.aop.order.aop.PointCuts.orderAndService()")
  public Object doTransaction(ProceedingJoinPoint joinPoint) throws Throwable {

    try {
      // @Before
      log.info("[transaction start] {}", joinPoint.getSignature());
      Object result = joinPoint.proceed();
      // @AfterReturning
      log.info("[transaction commit] {}", joinPoint.getSignature());
      return result;
    } catch (Exception e) {
      // @AfterThrowing
      log.info("[transaction rollback] {}", joinPoint.getSignature());
      throw e;
    } finally {
      // @After (개념적으로는 메서드 실행 종료 시)
      log.info("[resource release] {}", joinPoint.getSignature());
    }
  }

  // Around 제외한 종류의 Aspect
  // 1. JoinPoint 사용 (not ProceedingJoinPoint)
  // 2. joinPoint.proceed() 메서드 호출 x (각 영역만 작성)
  // 3. joinPoint 필요 없으면 파라미터에 안 적어도 됨

  // joinPoint.proceed 하기 전
  @Before("dev.riss.aop.order.aop.PointCuts.orderAndService()")
  public void deBefore(JoinPoint joinPoint) {
    log.info("[Before] {}", joinPoint.getSignature());
  }

  // joinPoint.proceed 한 후 (proceed 성공 시)
  // Around 에서는 return 값 조작해서 return 가능, AfterReturning 은 불가능 (다른 객체로 조작)
  @AfterReturning(value = "dev.riss.aop.order.aop.PointCuts.orderAndService()",
  returning = "result")
  public void doAfterReturning(JoinPoint joinPoint, Object result) {
    log.info("[AfterReturning] {}, return={}", joinPoint.getSignature(), result);
  }

  // returning 속성에 사용된 이름은 어드바이스 메서드의 매개변수 이름과 일치해야 함
  // returning 절에 지정된 타입의 값을 반환하는 메서드만 대상으로 실행함 (부모 타입으로 지정하면 모든 자식 타입을 return 하는 메서드 타겟 가능)
  // => returning 에서 명시한 매개변수와 실제 aop 타켓의 리턴 타입을 맞춰줘야함(상속 가능, 그래서 일반적으로 Object 로 함)
  // => result returnType 이 String
  // => orderItem 은 void return 이므로 여기에 걸리지 않음
  // => void 는 Object 의 하위이므로 doAfterReturning 은 로그 찍힘
  // => Integer result 로 바꿨을 땐 아무 타겟도 호출 안됨
  // * AfterThrowing 에서의 예외 타입도 마찬가지 (부모 타입의 예외를 지정해주면 됨, 다른 타입의 예외면 해당 메서드는 호출 타겟 x)
  @AfterReturning(value = "dev.riss.aop.order.aop.PointCuts.allOrder()",
      returning = "result")
  public void doAfterReturning2(JoinPoint joinPoint, String result) {
    log.info("[AfterReturning2] {}, return={}", joinPoint.getSignature(), result);
  }

  // joinPoint.proceed 에러난 경우 (try-catch-finally 에서 catch)
  @AfterThrowing(value = "dev.riss.aop.order.aop.PointCuts.orderAndService()",
  throwing = "ex")
  public void doAfterThrowing(JoinPoint joinPoint, Exception ex) {
    log.info("[AfterThrowing] {}", joinPoint.getSignature(), ex);
  }

  // joinPoint.proceed try-catch-finally 에서 finally 부분
  // 엄연히 개념적으로는 메서드 호출 종료 시의 영역
  // 일반적으로 리소스 해제하는 데 사용
  @After(value = "dev.riss.aop.order.aop.PointCuts.orderAndService()")
  public void doAfter(JoinPoint joinPoint) {
    log.info("[After] {}", joinPoint.getSignature());
  }

  /**
   * 어드바이스 호출 순서 (같은 @Aspect 안에서의 우선 순위)
   * @Around -> @Before -> @After -> @AfterReturning -> @AfterThrowing -> target
   *         <-         <-        <-                 <-                <-
   * 이 호출 순서로 인해 실제로 After 이후에 transaction commit 혹은 transaction rollback 로그가 찍히는 걸 볼 수 있음
   * (위에서 얘기한 @After 가 try-catch-finally 에서의 finally 위치라는 건 개념적인 설명)
   * ** 동일한 종류의 어드바이스가 하나의 Aspect 안에 있으면 이 순서는 보장 안됨 (해결책으로 AspectV5Order.java 참조)
   *
   * @Around 만 있으면 되는데, 다른 어드바이스 종류가 존재하는 이유
    * 제약이 핵심. ex. Around 어드바이스에서 proceed() 호출하지 않는다면, 장애
    * @before 로 돼있으면, 실제로 타겟 실행 전에 하는 거라는 의도 -> 제약으로 역할 명확해짐
   */
}
