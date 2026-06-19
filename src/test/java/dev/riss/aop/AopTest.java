package dev.riss.aop;

import dev.riss.aop.order.OrderRepository;
import dev.riss.aop.order.OrderService;
import dev.riss.aop.order.aop.AspectV1;
import dev.riss.aop.order.aop.AspectV2;
import dev.riss.aop.order.aop.AspectV3;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Slf4j
@SpringBootTest
//@Import(AspectV1.class)
//@Import(AspectV2.class)
@Import(AspectV3.class)
public class AopTest {

  @Autowired
  OrderService orderService;

  @Autowired
  OrderRepository orderRepository;

  @Test
  void aopInfo() {
    log.info("isAopProxy of orderService: {}", AopUtils.isAopProxy(orderService));
    log.info("isAopProxy of orderRepository: {}", AopUtils.isAopProxy(orderRepository));
  }

  @Test
  void success() {
    orderService.orderItem("itemA");
  }

  @Test
  void exception() {
    Assertions.assertThatThrownBy(
        () -> orderService.orderItem("ex")
    ).isInstanceOf(IllegalStateException.class);
  }
}
