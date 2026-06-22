package dev.riss.aop.pointcut;

import static org.assertj.core.api.Assertions.assertThat;

import dev.riss.aop.member.MemberServiceImpl;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

public class WithinTest {

  AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
  Method helloMethod;

  @BeforeEach
  public void init() throws NoSuchMethodException {
    helloMethod = MemberServiceImpl.class.getMethod("hello", String.class);
  }

  /**
   * 쉽게 설명하면,
   * type matching 하는 것
   * 타입(ex. MemberServiceImpl)이 매칭되면, 해당 타입의 메서드(ex. hello, internal)는 모두 통과
   *
   * 주의: 표현식에 부모 타입 지정 X, 정확하게 타입이 맞아야 함(다형성 적용 X)
   *
   * 실질적으로 execution 으로도 표현이 가능하고, execution 으로 대부분 기능이 되므로 execution 주로 사용
   * (실무에서 within 사용 잘 안함)
   */

  @Test
  void withinExact() {
    pointcut.setExpression("within(dev.riss.aop.member.MemberServiceImpl)");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void withinStar() {
    pointcut.setExpression("within(dev.riss.aop.member.*Service*)");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void withinSubPackage() {
    pointcut.setExpression("within(dev.riss.aop..*)");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  @Test
  @DisplayName("타켓의 타입에만 직접 적용(실제 클래스/조직 기반), 인터페이스 선정 안됨. 실제 타입이 맞아야 함.")
  void withinSuperTypeFalse() {
    pointcut.setExpression("within(dev.riss.aop.member.MemberService)");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();
  }

  @Test
  @DisplayName("execution 은 타입 기반이므로, 인터페이스 선정이 가능")
  void executionSuperTypeTrue() {
    pointcut.setExpression("execution(* dev.riss.aop.member.MemberService.*(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }
}
