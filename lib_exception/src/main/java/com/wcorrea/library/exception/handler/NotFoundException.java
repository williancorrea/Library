package com.wcorrea.library.exception.handler;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {

  private final String messageKey;
  private final String messageDescription;

  public NotFoundException(String messageKey, String messageDescription) {
    super(messageDescription);
    this.messageKey = messageKey;
    this.messageDescription = messageDescription;
  }
}
