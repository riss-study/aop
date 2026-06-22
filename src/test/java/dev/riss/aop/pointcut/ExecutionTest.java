package dev.riss.aop.pointcut;

import static org.assertj.core.api.Assertions.assertThat;

import dev.riss.aop.member.MemberServiceImpl;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

@Slf4j
public class ExecutionTest {

  AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
  Method helloMethod;

  @BeforeEach
  public void init() throws NoSuchMethodException {
    helloMethod = MemberServiceImpl.class.getMethod("hello", String.class);
  }

  @Test
  void printMethod() {
    // execution(* ..package..Class.)
    // execution 으로 시작하는 포인트컷 표현식은 아래에 찍힌 이 메서드 정보를 매칭해서 포인트컷 대상을 찾음
    // public java.lang.String dev.riss.aop.member.MemberServiceImpl.hello(java.lang.String)
    log.info("helloMethod={}", helloMethod);
  }

  /**
   * 매칭 조건
   * 접근제어자?: public
   * 반환타입: String
   * 선언타입?: dev.riss.aop.member.MemberServiceImpl
   * 메서드이름: hello
   * 파라미터: (String)
   * 예외?: 생략
   *
   * `*`: 아무 값이나 들어와도 된다는 뜻
   * 파라미터에서의 `..`: 파라미터의 타입과 파라미터 수가 상관없다는 뜻 == `0..*`
   *
   * dev.riss.aop.member.*(1).*(2)
   * (1): 타입
   * (2): 메서드 이름
   * `.`: 정확한 해당 위치의 패키지
   * `..`: 해당 위치의 패키지와 그 하위 패키지도 포함
   */
  @Test
  void exactMatch() {
    // public java.lang.String dev.riss.aop.member.MemberServiceImpl.hello(java.lang.String)
    // String 같은 건 앞에 java.lang. 생략 가능
    pointcut.setExpression("execution(public String dev.riss.aop.member.MemberServiceImpl.hello(String))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  // 모든 조건 다 ok, 가장 많이 생략한 포인트컷
  @Test
  void allMatch() {
    pointcut.setExpression("execution(* *(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  /**
   * Name Match
   */

  @Test
  void nameMatch() {
    pointcut.setExpression("execution(* hello(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  // 메소드명 패턴 매칭
  @Test
  void nameMatchStart1() {
    pointcut.setExpression("execution(* hel*(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void nameMatchStart2() {
    pointcut.setExpression("execution(* *el*(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void nameMatchFalse() {
    pointcut.setExpression("execution(* riss(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();
  }

  /**
   * Package Match
   */

  @Test
  void packageExactMatch() {
    pointcut.setExpression("execution(* dev.riss.aop.member.MemberServiceImpl.hello(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void packageExactMatch2() {
    pointcut.setExpression("execution(* dev.riss.aop.member.*.*(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void packageExactMatchFalse() {
    // 이건 엄연히 dev -> riss -> aop -> * -> 아무 메서드
    // MemberServiceImpl 은 dev -> riss -> aop -> member -> * -> * 임
    // . 만 할거면 하이어라키가 맞아야 함 (안그러면 중간에 ..을 사용해야함)
    pointcut.setExpression("execution(* dev.riss.aop.*.*(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();
  }

  @Test
  void packageMatchSubPackage1() {
    // dev.riss.aop.member 와 그 하위 패키지 모두 매칭이란 뜻
    pointcut.setExpression("execution(* dev.riss.aop.member..*.*(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void packageMatchSubPackage2() {
    // dev.riss.aop 와 그 하위 패키지 모두 매칭이란 뜻
    pointcut.setExpression("execution(* dev.riss.aop..*.*(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  // 개인적으로 만들어본 괴랄한 매칭 포인트컷
  @Test
  void packageMatchSubPackage3() {
    pointcut.setExpression("execution(* dev..member..*ell*(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  /**
   *  Type Match
   */

  @Test
  void typeExactMatch() {
    pointcut.setExpression("execution(* dev.riss.aop.member.MemberServiceImpl.*(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void typeMatchSuperType() {
    // execution 은 다형성 적용됨(타입 매칭) -> 부모타입에서 선언한 메서드만 허용됨 (자식에만 있는 메서드는 매칭 안됨)
    // MemberServiceImpl 이 MemberService 의 구현체 (즉, 자식타입이라 부모타입도 매칭 성공)
    pointcut.setExpression("execution(* dev.riss.aop.member.MemberService.*(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void typeMatchInternal() throws NoSuchMethodException {
    // MemberServiceImpl 자신의 메서드라 매칭 통과
    pointcut.setExpression("execution(* dev.riss.aop.member.MemberServiceImpl.*(..))");
    Method internalMethod = MemberServiceImpl.class.getMethod("internal", String.class);
    assertThat(pointcut.matches(internalMethod, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void typeMatchNoSuperTypeMethodFalse() throws NoSuchMethodException {
    // execution 은 다형성 적용됨(타입 매칭) -> 부모타입에서 선언한 메서드만 허용됨 (자식에만 있는 메서드는 매칭 안됨)
    // MemberService 에는 internal 메서드 선언 안됐기 때문에 매칭 안됨
    pointcut.setExpression("execution(* dev.riss.aop.member.MemberService.*(..))");
    Method internalMethod = MemberServiceImpl.class.getMethod("internal", String.class);
    assertThat(pointcut.matches(internalMethod, MemberServiceImpl.class)).isFalse();
  }

  /**
   * Parameter Match
   */
  // String type 의 파라미터 허용
  // (String)
  @Test
  void argsMatch() {
    pointcut.setExpression("execution(* *(String))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  // 파라미터 없음 포인트컷 매칭 통과 안됨 (hello 메서드는 String 파라미터가 1개 있으므로)
  // ()
  @Test
  void argsMatchNoArgs() {
    pointcut.setExpression("execution(* *())");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();
  }

  // 정확히 하나의 파라미터 허용, 모든 타입 허용
  // (Xxx)
  @Test
  void argsMatchStar() {
    pointcut.setExpression("execution(* *(*))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  // 파라미터의 수와 무관하게 모든 파라미터, 모든 타입 허용
  // (), (Xxx), (Xxx, Xxx), ... => (..)
  @Test
  void argsMatchAll() {
    pointcut.setExpression("execution(* *(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  // 파라미터의 수와 무관하게 모든 파라미터, 모든 타입 허용
  // (String), (String, Xxx), (String, Xxx, Xxx), ... => (String, ..)
  @Test
  void argsMatchComplex() {
    pointcut.setExpression("execution(* *(String, ..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
  }

  /**
   * [파라미터 관련 execution 지시자]
   * 파라미터 없어여함 => ()
   * 둘다 스트링 => (String, String)
   * 파라미터 1개만, 모든 타입 허용 => (*)
   * 파라미터 갯수 2개인데, 첫번째 스트링, 두번째는 아무거나 => (String, *)
   * 첫번째 스트링, 두번째 없어도 되고, 무제한이어도 됨, 두번째부터는 타입 무관 => (String, ..)
   * 파라미터 갯수 무관, 모든 타입 허용 => (..)
   */
}
