package dev.riss.aop.pointcut;


import dev.riss.aop.member.annotation.ClassAop;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * @target, @within 둘다 지시자 내에 명시한 애노테이션이 붙어있는 클래스의 메서드 적용
 * @target 은 인스턴스의 모든 메서드를 조인 포인트로 적용
 * @within 은 해당 타입 내에 있는 메서드만 조인 포인트로 적용
 * 쉽게 설명하면, @target 은 부모 클래스의 메서드까지 어드바이스 적용
 *            @within 은 자기 자신의 클래스에 정의된 메서드에만 어드바이스 적용
 * (execution vs within 과 비슷한 느낌)
 * (@target 은 ClassAop 가 자신에게 해당 에노테이션이 붙어있으면, 부모의 메서드도 통과)
 * (@within 은 classAop 가 자신에게 해당 애노테이션이 붙어있으면, 자신의 메서드만 통과, 부몬의 메서드는 통과 못함)
 *
 * 둘다 파라미터 바인딩에서 함께 사용
 * [주의]
 * args, @target, @within 은 단독으로 사용하면 안됨
 * @target 은 실제 객체 인스턴스가 생성되고 실행될 때 어드바이스 적용 여부를 확인할 수 있음
 * 실행 시점에 일어나는 포인트컷 적용 여부도 결국 프록시가 있어야 실행 시점에서 판단 가능.
 * 프록시가 없다면 판단 자체가 불가능함.
 * 스프링 컨테이너가 프록시를 생성하는 시점은 스프링 컨테이너가 만들어지는 애플리케이션 로딩 시점에 적용할 수 있음.
 * 따라서 args, @args, @target, @within 과 같은 포인트컷 지시자가 있으면, 스프링은 모든 스프링 빈에 AOP 를 적용하려고 시도함.
 * 앞서 설명한 것처럼 프록시가 없으면 실행 시점에 판단 자체가 불가능.
 * 문제는 이렇게 모든 스프링 빈에 AOP 프록시를 적용하려고 하면 스프링이 내부에서 사용하는 빈 중에서는 final 로 지정된 빈들도 있기 때문에 오류 발생 할 수 있음
 * 따라서 이러한 표현식은 최대한 프록시 적용 대상을 축소하는 표현식과 함께 사용해야 함(ex. execution)
 *
 * 실제로 밑의 Around 에서 execution 지시자 빼면 final class 에 적용하려고 한다고 에러남
 */
@Slf4j
@Import({ AtTargetAtWithinTest.Config.class })
@SpringBootTest
public class AtTargetAtWithinTest {

  @Autowired
  Child child;

  /**
   * 출력문
   * child Proxy: dev.riss.aop.pointcut.AtTargetAtWithinTest$Child@2cee5365
   * [@target] void dev.riss.aop.pointcut.AtTargetAtWithinTest$Child.childMethod()
   * [@within] void dev.riss.aop.pointcut.AtTargetAtWithinTest$Child.childMethod()
   * [@target] void dev.riss.aop.pointcut.AtTargetAtWithinTest$Parent.parentMethod()
   */
  @Test
  void success() {
    log.info("child Proxy: {}", child);
    child.childMethod(); // ClassAop annotation 이 붙어 있는 자식에서 선언된 메서드
    child.parentMethod(); // 부모 클래스에서 선언된 메서드
  }

  static class Config {

    @Bean
    public Parent parent() { return new Parent(); }

    @Bean
    public Child child() { return new Child(); }

    @Bean
    public AtTargetAtWithinAspect atTargetAtWithinAspect() {
      return new AtTargetAtWithinAspect();
    }
  }

  static class Parent {
    public void parentMethod() {} // 부모에만 있는 메서드
  }

  @ClassAop
  static class Child extends Parent {
    public void childMethod() {}
  }

  /**
   * 동적인 객체 인스턴스가 실제로 실행이 돼야 함
   * 객체 인스턴스로 판단하기 때문에, 실제 실행이 돼야 판단 가능
   * 포인트컷이 정적으로 판단할 수 있는 게 있고, 안되는 게 있는데 얘네는 안됨
   * 그래서 직접 Aspect 로 다 만들서, 스프링 컨테이너에 올려놓고 돌려야 확인 가능
   */
  @Slf4j
  @Aspect
  static class AtTargetAtWithinAspect {

    // @target: 인스턴스 기준으로 모든 메서드의 조인 포인트를 설정, 부모 타입의 메서드도 적용
    // 그러므로 parentMethod, childMethod 모두 통과
    @Around("execution(* dev.riss.aop..*(..)) && @target(dev.riss.aop.member.annotation.ClassAop)")
    public Object atTarget(ProceedingJoinPoint pjp) throws Throwable {
      log.info("[@target] {}", pjp.getSignature());
      return pjp.proceed();
    }

    // @within: 선택된 클래스 내부에 있는 메서드만 조인 포인트로 선정, 부모 타입의 메서드는 적용되지 않음
    // 그러므로 ClassAop 선언돼있는 Child 클래스 내부에 있는 childMethod 만 통과, parentMethod 는 통과 안됨
    @Around("execution(* dev.riss.aop..*(..)) && @within(dev.riss.aop.member.annotation.ClassAop)")
    public Object atWithin(ProceedingJoinPoint pjp) throws Throwable {
      log.info("[@within] {}", pjp.getSignature());
      return pjp.proceed();
    }
  }




}
