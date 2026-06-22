package dev.riss.aop.pointcut;

import static org.assertj.core.api.Assertions.assertThat;

import dev.riss.aop.member.MemberServiceImpl;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

public class ArgsTest {

  Method helloMethod;

  @BeforeEach
  public void init() throws NoSuchMethodException {
    helloMethod = MemberServiceImpl.class.getMethod("hello", String.class);
  }

  private AspectJExpressionPointcut pointcut(String expression) {
    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
    pointcut.setExpression(expression);
    return pointcut;
  }

  /**
   * args 인자가 주어진 타입의 인스턴스인 조인 포인트로 매칭
   * 기본 문법은 execution 의 args 부분과 같음
   * 단독 사용보다는 파라미터 바인딩에 주로 사용
   *
   * execution vs args
   *  - execution 은 파라미터 타입이 정확하게 매칭돼야 함. execution 은 클래스에 선언된 정보를 기반으로 판단
   *  - args 는 부모 타입을 허용. args 는 실제 넘어온 파라미터 객체 인스턴스를 보고 판단.
   */

  @Test
  void args() {
    // hello(String) 과 매칭
    assertThat(pointcut("args(String)")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    assertThat(pointcut("args(Object)")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    assertThat(pointcut("args()")
        .matches(helloMethod, MemberServiceImpl.class)).isFalse();
    assertThat(pointcut("args(..)")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    assertThat(pointcut("args(*)")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    assertThat(pointcut("args(String, ..)")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    assertThat(pointcut("args(String, *)")
        .matches(helloMethod, MemberServiceImpl.class)).isFalse();
    // 내가 추가해봄
    assertThat(pointcut("execution(* *..*(Object))")
        .matches(helloMethod, MemberServiceImpl.class)).isFalse();
  }

  /**
   * execution(* *(java.io.Serializable)): 메서드의 시그니처(ex. String 이면 그것만 봄)로 판단 (정적)
   * => 정적으로 클래스에 선언된 정보만 보고 판단
   * args(java.io.Serializable): 런타임에 전달된 인수(객체 인스턴스를 보고 판단)로 판단 (동적)
   * => 동적으로 실제 파라미터로 넘어온 객체 인스턴스로 판단
   */
  @Test
  void argsVsExecution() {
    // Args
    assertThat(pointcut("args(String)")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    assertThat(pointcut("args(java.io.Serializable)")   // Java 의 String 은 Serializable 인터페이스의 구현체
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    assertThat(pointcut("args(Object)")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();

    // Execution
    // args 부모 타입 x, Object 도 x
    assertThat(pointcut("execution(* *(String))")
        .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    assertThat(pointcut("execution(* *(java.io.Serializable))")
        .matches(helloMethod, MemberServiceImpl.class)).isFalse();
    assertThat(pointcut("execution(* *(Object))")
        .matches(helloMethod, MemberServiceImpl.class)).isFalse();
  }

}
