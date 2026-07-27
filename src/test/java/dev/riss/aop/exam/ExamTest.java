package dev.riss.aop.exam;

import dev.riss.aop.exam.aop.RetryAspect;
import dev.riss.aop.exam.aop.TraceAspect;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Slf4j
@Import({ TraceAspect.class, RetryAspect.class })
@SpringBootTest
public class ExamTest {

  @Autowired
  ExamService examService;

  @Test
  void test() {
    for (int i = 0; i < 5; i++) {
      log.info("client request i={}", i);
      examService.request("data" + i);
    }
  }

  /**
   * client request i=4 에서 (의도적 에러) 에러가 났으며,
   * 그때 재시도 해서(client request i = 4에서 실제 시도 2회) 성공했다는 로그
   *  client request i=0
   * [trace] void dev.riss.aop.exam.ExamService.request(String), args: [data0]
   * [trace] String dev.riss.aop.exam.ExamRepository.save(String), args: [data0]
   * [retry] save, retry=@dev.riss.aop.exam.annotation.Retry(4)
   * [retry] try count: 1/4
   *  client request i=1
   * [trace] void dev.riss.aop.exam.ExamService.request(String), args: [data1]
   * [trace] String dev.riss.aop.exam.ExamRepository.save(String), args: [data1]
   * [retry] save, retry=@dev.riss.aop.exam.annotation.Retry(4)
   * [retry] try count: 1/4
   *  client request i=2
   * [trace] void dev.riss.aop.exam.ExamService.request(String), args: [data2]
   * [trace] String dev.riss.aop.exam.ExamRepository.save(String), args: [data2]
   * [retry] save, retry=@dev.riss.aop.exam.annotation.Retry(4)
   * [retry] try count: 1/4
   *  client request i=3
   * [trace] void dev.riss.aop.exam.ExamService.request(String), args: [data3]
   * [trace] String dev.riss.aop.exam.ExamRepository.save(String), args: [data3]
   * [retry] save, retry=@dev.riss.aop.exam.annotation.Retry(4)
   * [retry] try count: 1/4
   *  client request i=4
   * [trace] void dev.riss.aop.exam.ExamService.request(String), args: [data4]
   * [trace] String dev.riss.aop.exam.ExamRepository.save(String), args: [data4]
   * [retry] save, retry=@dev.riss.aop.exam.annotation.Retry(4)
   * [retry] try count: 1/4
   * [retry] try count: 2/4
   */
}
