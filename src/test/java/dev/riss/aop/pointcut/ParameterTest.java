package dev.riss.aop.pointcut;

import dev.riss.aop.member.MemberService;
import dev.riss.aop.member.annotation.ClassAop;
import dev.riss.aop.member.annotation.MethodAop;
import dev.riss.aop.pointcut.ParameterTest.ParameterAspect;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Slf4j
@SpringBootTest
@Import(ParameterAspect.class)
public class ParameterTest {

  @Autowired
  MemberService memberService;

  @Test
  void success() {
    log.info("memberService Proxy={}", memberService.getClass());
    memberService.hello("helloA");
  }

  @Slf4j
  @Aspect
  static class ParameterAspect {

    @Pointcut("execution(* dev.riss.aop.member..*.*(..))")
    public void allMember() {}

    @Around("allMember()")
    public Object logArgs1(ProceedingJoinPoint joinPoint) throws Throwable {
      Object arg1 = joinPoint.getArgs()[0];
      log.info("[logArgs1] {}, args={}", joinPoint.getSignature(), arg1);
      return joinPoint.proceed();
    }

    // 파라미터 전달 args 로 깔끔하게
    @Around("allMember() && args(arg, ..)")
    public Object logArgs2(ProceedingJoinPoint joinPoint, Object arg) throws Throwable {
      log.info("[logArgs2] {}, arg={}", joinPoint.getSignature(), arg);
      return joinPoint.proceed();
    }

    // arg 가 String 인 것만 매칭 (파라미터 전달)
    @Before("allMember() && args(arg, ..)")
    public void logArgs3(String arg) {
      log.info("[logArgs3] arg={}", arg);
    }

    // this 에 MemberService 만 들어와야 함
    // this 는 스프링 컨테이너에 올라간 대상 -> 즉, 프록시 객체 MemberServiceImpl$$SpringCGLIB$$0
    @Before("allMember() && this(obj)")
    public void thisArgs(JoinPoint joinPoint, MemberService obj) {
      log.info("[thisArgs] {}, obj={}", joinPoint.getSignature(), obj.getClass());
    }

    // target 에 MemberService 만 들어와야 함
    // target 은 실제 대상 구현체 (MemberServiceImpl)
    @Before("allMember() && target(obj)")
    public void targetArgs(JoinPoint joinPoint, MemberService obj) {
      log.info("[targetArgs] {}, obj={}", joinPoint.getSignature(), obj.getClass());
    }

    // type 의 애노테이션을 전달 받음
    @Before("allMember() && @target(annotation)")
    public void atTargetArgs(JoinPoint joinPoint, ClassAop annotation) {
      log.info("[atTargetArgs] {}, obj={}", joinPoint.getSignature(), annotation);
    }

    // type 의 애노테이션을 전달 받음
    @Before("allMember() && @within(annotation)")
    public void atWithinArgs(JoinPoint joinPoint, ClassAop annotation) {
      log.info("[atWithinArgs] {}, obj={}", joinPoint.getSignature(), annotation);
    }

    // @annotation 은 메서드의 애노테이션을 전달 받음. annotation 안에 있는 value 를 꺼낼 수 있음
    @Before("allMember() && @annotation(annotation)")
    public void atAnnotationArgs(JoinPoint joinPoint, MethodAop annotation) {
      log.info("[atAnnotationArgs] {}, annotationValue={}", joinPoint.getSignature(), annotation.value());
    }
  }
}
