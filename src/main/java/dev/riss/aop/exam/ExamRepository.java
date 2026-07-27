package dev.riss.aop.exam;

import dev.riss.aop.exam.annotation.Retry;
import dev.riss.aop.exam.annotation.Trace;
import org.springframework.stereotype.Repository;

@Repository
public class ExamRepository {

  private static int seq = 0;

  /**
   * 5번에 1번 실패하는 요청 (의도적)
   *
   * 보통 서버 to 서버 조회 같은 경우, 간헐적으로 튐 -> 이건 그냥 재조회하면 됨 -> 이럴 때 Retry 씀
   * => 실무에서 조심할 것: 횟수 제한이 있어야 함. 무제한이면 무제한 리퀘스트 가능성 => 셀프 디도스 가능
   */
  @Trace
  @Retry(value = 4) // default value 3 이지만 해당 메서드 한해서 커스텀 가능
  public String save(String itemId) {
    seq++;
    if ((seq % 5) == 0) {
      throw new IllegalStateException("예외 발생");
    }
    return "ok";
  }
}
