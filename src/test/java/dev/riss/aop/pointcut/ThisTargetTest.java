package dev.riss.aop.pointcut;

import dev.riss.aop.member.MemberService;
import dev.riss.aop.pointcut.ThisTargetTest.ThisTargetAspect;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * this: 스프링 빈 객체(스프링 AOP 프록시)를 대상으로 하는 조인 포인트
 *  => 스프링 컨테이너에 등록되는 실제 스프링 빈
 * target: Target 객체(스프링 AOP 프록시가 가르키는 실제 대상)를 대상으로 하는 조인 포인트
 * 둘다, 적용 타입 하나를 정확하게 지정 해야 함
 * '*' 같은 패턴 사용 불가능
 * 부모 타입 허용
 * (대상 하나를 집어서 적용하는 것이기에, 잘 쓰이진 않음)
 *
 * [프록시 생성 방식에 따른 차이]
 *  - 스프링은 프록시를 생성할 때, JDK 동적 프록시와 CGLIB을 선택할 수 있음. 둘의 프록시 생성 방식이 다르기에 차이가 남
 *  - JDK 동적 프록시: 인터페이스 필수, 인터페이스를 구현한 프록시 객체 생성
 *  - CGLIB: 인터페이스가 있어도 구체 클래스를 상속 받아서 프록시 객체 생성
 *
 * [JDK 동적 프록시 적용 시 this, target] => JDK Proxy 객체는 MemberService 타입, target 객체는 MemberServiceImpl 타입
 * 1. MemberService 인터페이스 지정
 * - this(MemberService): proxy 객체를 보고 판단. 호출되는 프록시가 MemberService 타입 => AOP 적용
 *   (AOP =부모=> MemberService Ok)
 * - target(MemberService): target 객체를 보고 판단. target 은 부모 타입을 허용 => AOP 적용
 *   (target == MemberServiceImpl =부모=> MemberService Ok)
 *  => 두개 결과가 똑같음
 * 2. MemberServiceImpl 구체 클래스 지정
 * - this(MemberServiceImpl): proxy 객체를 보고 판단. 호출되는 JDK 동적 프록시로 만들어진 proxy 객체는 MemberService 인터페이스 기반으로 구현된 새로운 클래스.
 *   따라서 'MemberServiceImpl' 을 전혀 알지 못하므로 'AOP 적용 대상이 아님'
 *   (AOP =부모=> MemberService =X=> MemberServiceImpl Not Ok)
 * - target(MemberServiceImpl): target 객체를 보고 판단. target 이 MemberServiceImpl 타입 => AOP 적용
 *   (target == MemberServiceImpl Ok)
 * => 두개 결과가 다름
 *
 * [CGLIB 적용 시 this, target] => CGLIB Proxy 객체는 MemberServiceImpl 타입, target 객체는 MemberServiceImpl 타입
 * 1. MemberService 인터페이스 지정
 * - this(MemberService): proxy 객체를 보고 판단. this 는 부모 타입 허용 => AOP 적용
 *   (AOP =부모=> MemberServiceImpl =부모=> MemberService Ok)
 * - target(MemberService): target 객체를 보고 판단. target 은 부모 타입 허용 => AOP 적용
 *   (target == MemberServiceImpl =부모=> MemberService Ok)
 *
 * 2. MemberServiceImpl 구체 클래스 지정
 * - this(MemberServiceImpl): proxy 객체를 보고 판단. AOP 프록시 객체가 MemberServiceImpl 타입 => AOP 허용
 *   (AOP =부모=> MemberServiceImpl Ok)
 * - target(MemberServiceImpl): target 객체를 보고 판단. target 객체가 MemberServiceImpl 타입 => AOP 허용
 *   (target == MemberServiceImpl Ok)
 *
 * [결론]
 * - 프록시를 대상으로 하는 'this'의 경우, 구체 클래스 지정 시, 프록시 생성 전략에 따라서 결과가 다를 수 있음
 * - this, target 지시자는 단독으로 사용되기 보단 파라미터 바인딩에서 주로 사용
 */
@Slf4j
@Import({ ThisTargetAspect.class })
@SpringBootTest(properties = {"spring.aop.proxy-target-class=false"})
public class ThisTargetTest {

  @Autowired
  MemberService memberService;

  /**
   * log at spring.aop.proxy-target-class=true # AOP CGLIB 프록시 생성 (구체 클래스 기반)
   *  memberService proxy: class dev.riss.aop.member.MemberServiceImpl$$SpringCGLIB$$0
   *  [doTargetImpl] String dev.riss.aop.member.MemberServiceImpl.hello(String)
   *  [doTargetInterface] String dev.riss.aop.member.MemberServiceImpl.hello(String)
   *  [doThisImpl] String dev.riss.aop.member.MemberServiceImpl.hello(String)
   *  [doThisInterface] String dev.riss.aop.member.MemberServiceImpl.hello(String)
   *
   * log at spring.aop.proxy-target-class=false # AOP JDK 동적 프록시 우선 생성 (인터페이스 기반)
   *  memberService proxy: class jdk.proxy3.$Proxy69
   *  [doTargetImpl] String dev.riss.aop.member.MemberService.hello(String)
   *  [doTargetInterface] String dev.riss.aop.member.MemberService.hello(String)
   *  [doThisInterface] String dev.riss.aop.member.MemberService.hello(String)
   *
   *  => target 은 AOP 프록시 생성 전략에 관계 없이 로그가 남지만, this 는 JDK 동적 프록시 전략 시, doThisImpl advice 적용 안되어 로그가 남지 않음
   */
  @Test
  void success() {
    log.info("memberService proxy: {}", memberService.getClass());
    memberService.hello("helloA");
  }

  @Slf4j
  @Aspect
  static class ThisTargetAspect {

    // this 부모 타입 허용
    @Around("this(dev.riss.aop.member.MemberService)")
    public Object doThisInterface(ProceedingJoinPoint joinPoint) throws Throwable {
      log.info("[doThisInterface] {}", joinPoint.getSignature());
      return joinPoint.proceed();
    }

    // target 부모 타입 허용
    @Around("target(dev.riss.aop.member.MemberService)")
    public Object doTargetInterface(ProceedingJoinPoint joinPoint) throws Throwable {
      log.info("[doTargetInterface] {}", joinPoint.getSignature());
      return joinPoint.proceed();
    }

    // this 부모 타입 허용
    @Around("this(dev.riss.aop.member.MemberServiceImpl)")
    public Object doThisImpl(ProceedingJoinPoint joinPoint) throws Throwable {
      log.info("[doThisImpl] {}", joinPoint.getSignature());
      return joinPoint.proceed();
    }

    // target 부모 타입 허용
    @Around("target(dev.riss.aop.member.MemberServiceImpl)")
    public Object doTargetImpl(ProceedingJoinPoint joinPoint) throws Throwable {
      log.info("[doTargetImpl] {}", joinPoint.getSignature());
      return joinPoint.proceed();
    }
  }
}
