package com.wcorrea.library.exception.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiError {
  private final String origin;
  private final String method;
  private final LocalDateTime dataHora;

  private final Status status;
  private final Message message;

  public ApiError(String messageDescription, String details, HttpStatus httpStatus, String origin, String method) {
    this.dataHora = LocalDateTime.now();
    this.origin = origin;
    this.method = method;
    this.message = new Message(null, messageDescription, details);
    this.status = new Status(httpStatus.value(), httpStatus.getReasonPhrase());
  }

  public ApiError(String messageKey, String messageDescription, String details, HttpStatus httpStatus, String origin,
                  String method) {
    this.dataHora = LocalDateTime.now();
    this.origin = origin;
    this.method = method;
    this.message = new Message(messageKey, messageDescription, details);
    this.status = new Status(httpStatus.value(), httpStatus.getReasonPhrase());
  }

  @Getter
  public class Status {
    private final int code;
    private final String description;

    public Status(Integer code, String description) {
      this.code = code;
      this.description = description;
    }
  }

  @Getter
  public class Message {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final String key;
    private final String description;
    private final String detail;

    public Message(String key, String description, String detail) {
      this.key = key == null ? "" : key;
      this.description = description;
      this.detail = detail;
    }
  }
}
