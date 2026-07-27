package dev.riss.aop.exam;

import dev.riss.aop.exam.annotation.Trace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExamService {

  private final ExamRepository examRepository;

  @Trace
  public void request(String itemId) {
    examRepository.save(itemId);
  }
}
