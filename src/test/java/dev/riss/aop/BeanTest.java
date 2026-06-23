package dev.riss.aop;

import dev.riss.aop.BeanTest.BeanAspect;
import dev.riss.aop.order.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * bean: 스프링 전용 포인트컷 지시자. 빈의 이름으로 지정(매칭).
 * (스프링에서만 사용 가능, not AspectJ)
 * ex. bean(orderService) || bean(*Repository)
 */
@Slf4j
@SpringBootTest
@Import(BeanAspect.class)
public class BeanTest {

  @Autowired
  OrderService orderService;

  @Test
  void success() {
    orderService.orderItem("itemA");
  }

  @Aspect
  static class BeanAspect {
    @Around("bean(orderService) || bean(*Repository)")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
      log.info("[bean] {}", joinPoint.getSignature());
      return joinPoint.proceed();
    }
  }
}
