package com.wcorrea.library.exception.handler;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

  private final String messageKey;
  private final String messageDescription;

  public BusinessException(String messageKey, String messageDescription) {
    super(messageDescription);
    this.messageKey = messageKey;
    this.messageDescription = messageDescription;
  }
}
