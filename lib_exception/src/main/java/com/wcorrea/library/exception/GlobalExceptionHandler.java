package com.wcorrea.library.exception;

import com.wcorrea.library.exception.handler.BusinessException;
import com.wcorrea.library.exception.handler.NotFoundException;
import com.wcorrea.library.exception.model.ApiError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(value = "com.wcorrea.library")
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private final MessageSource messageSource;

  public GlobalExceptionHandler(@Lazy MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  /**
   * Create error list
   *
   * @param bindingResult {@link BindingResult}
   * @param httpStatus    {@link HttpStatus}
   * @param uri           {@link String}
   * @param method        {@link String}
   * @return List<ApiError>
   */
  public List<ApiError> createErrorList(BindingResult bindingResult, HttpStatus httpStatus, String uri, String method) {
    List<ApiError> errors = new ArrayList<>();
    for (FieldError fieldWithError : bindingResult.getFieldErrors()) {
      String message = messageSource.getMessage(fieldWithError, LocaleContextHolder.getLocale());
      String detail = fieldWithError.toString();
      errors.add(new ApiError(message, detail, httpStatus, uri, method));
    }
    return errors;
  }

  /**
   * Internal server error
   *
   * @param ex      the target exception
   * @param request the current request
   * @return
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleInternalServerError(Exception ex, WebRequest request) {
    String message = messageSource.getMessage("recurso.mensagem-erro-interno", null, LocaleContextHolder.getLocale());
    String detail = ex.toString();
    String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
    String method = ((ServletWebRequest) request).getRequest().getMethod();
    List<ApiError> errors =
        Collections.singletonList(new ApiError(message, detail, HttpStatus.INTERNAL_SERVER_ERROR, uri, method));

    log.error(ex.getMessage(), ex);
    return handleExceptionInternal(ex, errors, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  /**
   * Resource not found
   *
   * @param ex      the exception {@link NoHandlerFoundException}
   * @param headers the headers to be written to the response {@link HttpHeaders}
   * @param status  the selected response status {@link HttpStatus}
   * @param request the current request {@link WebRequest}
   * @return ResponseEntity<Object>
   */
  //TODO: Fixme
//  @Override
//  protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers,
//                                                                 HttpStatus status, WebRequest request) {
//    String message =
//        messageSource.getMessage("recurso.mensagem-recurso-nao-encontrado", null, LocaleContextHolder.getLocale());
//    String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
//    String detail = messageSource.getMessage("recurso.mensagem-recurso-nao-encontrado-detalhe", new Object[] {uri},
//        LocaleContextHolder.getLocale());
//    String method = ((ServletWebRequest) request).getRequest().getMethod();
//    List<ApiError> errors =
//        Collections.singletonList(new ApiError(message, detail, HttpStatus.BAD_REQUEST, uri, method));
//    return handleExceptionInternal(ex, errors, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
//  }

  /**
   * Database integrity violation - relationship between tables
   *
   * @param ex      {@link DataIntegrityViolationException}
   * @param request {@link WebRequest}
   * @return ResponseEntity<Object>
   */
  @ExceptionHandler({DataIntegrityViolationException.class})
  public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex,
                                                                      WebRequest request) {
    String message = messageSource.getMessage("resource.violation-of-integrity", null, LocaleContextHolder.getLocale());
    String detail = ExceptionUtils.getRootCauseMessage(ex);
    String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
    String method = ((ServletWebRequest) request).getRequest().getMethod();
    List<ApiError> errors = Collections.singletonList(new ApiError(message, detail, HttpStatus.CONFLICT, uri, method));
    return handleExceptionInternal(ex, errors, new HttpHeaders(), HttpStatus.CONFLICT, request);
  }

  /**
   * Handles unreadable error messages
   *
   * @param ex      the exception {@link HttpMessageNotReadableException}
   * @param headers the headers to be written to the response {@link HttpHeaders}
   * @param status  the selected response status {@link HttpStatus}
   * @param request the current request {@link WebRequest}
   * @return ResponseEntity<Object>
   */
  //TODO: Fixme
//  @Override
//  protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
//                                                                HttpStatus status, WebRequest request) {
//
////        String mensagem = messageSource.getMessage("recurso.mensagem-invalida", null, LocaleContextHolder.getLocale());
////        String detalhes = ex.getCause() != null ? ex.getCause().toString() : ex.toString();
//
//    String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
//    String method = ((ServletWebRequest) request).getRequest().getMethod();
//
//    String detail;
//    String message;
//    Throwable rootCause = ExceptionUtils.getRootCause(ex);
//
//    if (rootCause instanceof InvalidFormatException) {
//      String path =
//          ((InvalidFormatException) rootCause).getPath().stream().map(JsonMappingException.Reference::getFieldName)
//              .collect(Collectors.joining("."));
//      message = messageSource.getMessage("recurso.mensagem-formato-invalido", null, LocaleContextHolder.getLocale());
//      detail = messageSource.getMessage("recurso.mensagem-formato-invalido-detalhe",
//          new Object[] {path, ((InvalidFormatException) rootCause).getValue(),
//              ((InvalidFormatException) rootCause).getTargetType().getSimpleName()}, LocaleContextHolder.getLocale());
//    } else if (rootCause instanceof UnrecognizedPropertyException) {
//      message =
//          messageSource.getMessage("recurso.mensagem-propriedade-inexistente", null, LocaleContextHolder.getLocale());
//      detail = messageSource.getMessage("recurso.mensagem-propriedade-inexistente-detalhe",
//          new Object[] {((UnrecognizedPropertyException) rootCause).getPropertyName()},
//          LocaleContextHolder.getLocale());
//    } else if (rootCause instanceof IgnoredPropertyException) {
//      message =
//          messageSource.getMessage("recurso.mensagem-propriedade-inexistente", null, LocaleContextHolder.getLocale());
//      detail = messageSource.getMessage("recurso.mensagem-propriedade-inexistente-detalhe",
//          new Object[] {((IgnoredPropertyException) rootCause).getPropertyName()}, LocaleContextHolder.getLocale());
//    } else if (rootCause instanceof JsonParseException) {
//      message = messageSource.getMessage("recurso.mensagem-formato-invalido", null, LocaleContextHolder.getLocale());
//      String campo = ((JsonParseException) rootCause).getProcessor().getParsingContext().getCurrentName();
//      if (StringUtils.isNotBlank(campo)) {
//        detail = messageSource.getMessage("recurso.mensagem-formato-invalido-detalhe2",
//            new Object[] {campo, rootCause.getMessage().substring(20).split("'")[0]}, LocaleContextHolder.getLocale());
//      } else {
//        detail = messageSource.getMessage("recurso.mensagem-invalida-detalhe", null, LocaleContextHolder.getLocale());
//      }
//    } else {
//      message = messageSource.getMessage("recurso.mensagem-invalida", null, LocaleContextHolder.getLocale());
//      detail = ex.getCause() != null ? ex.getCause().toString() : ex.toString();
//    }
//
//    List<ApiError> errors =
//        Collections.singletonList(new ApiError(message, detail, HttpStatus.BAD_REQUEST, uri, method));
//    return handleExceptionInternal(ex, errors, headers, HttpStatus.BAD_REQUEST, request);
//  }

  /**
   * Handles validation error messages for object attributes
   *
   * @param ex      the exception {@link MethodArgumentNotValidException}
   * @param headers the headers to be written to the response {@link HttpHeaders}
   * @param status  the selected response status {@link HttpStatus}
   * @param request the current request {@link WebRequest}
   * @return ResponseEntity<Object>
   */
  //TODO: Fixme
//  @Override
//  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
//                                                                HttpStatus status, WebRequest request) {
//    String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
//    String method = ((ServletWebRequest) request).getRequest().getMethod();
//    List<ApiError> errors = createErrorList(ex.getBindingResult(), HttpStatus.BAD_REQUEST, uri, method);
//    return handleExceptionInternal(ex, errors, headers, HttpStatus.BAD_REQUEST, request);
//  }

  /**
   * Handles error messages when resource is not found
   *
   * @param ex      {@link EmptyResultDataAccessException}
   * @param request {@link WebRequest}
   * @return ResponseEntity<Object>
   */
  @ExceptionHandler({EmptyResultDataAccessException.class})
  public ResponseEntity<Object> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex,
                                                                     WebRequest request) {
    String message = messageSource.getMessage("resource.not-found", null, LocaleContextHolder.getLocale());
    String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
    String method = ((ServletWebRequest) request).getRequest().getMethod();
    List<ApiError> errors =
        Collections.singletonList(new ApiError(message, ex.toString(), HttpStatus.NOT_FOUND, uri, method));
    return handleExceptionInternal(ex, errors, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
  }

  /**
   * It handles parameters entered incorrectly
   *
   * @param ex      the exception {@link BindException}
   * @param headers the headers to be written to the response {@link HttpHeaders}
   * @param status  the selected response status {@link HttpStatus}
   * @param request the current request {@link WebRequest}
   * @return ResponseEntity<Object>
   */
  //TODO: Fixme
//  @Override
//  protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status,
//                                                       WebRequest request) {
//    String message = messageSource.getMessage("resource.method-not-supported", null, LocaleContextHolder.getLocale());
//    String detail = ex.toString();
//    String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
//    String method = ((ServletWebRequest) request).getRequest().getMethod();
//    List<ApiError> errors = Arrays.asList(new ApiError(message, detail, HttpStatus.METHOD_NOT_ALLOWED, uri, method));
//    return handleExceptionInternal(ex, errors, new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED, request);
//  }

  /**
   * Handles type conversion error messages
   *
   * @param ex      {@link MethodArgumentTypeMismatchException}
   * @param request {@link WebRequest}
   * @return ResponseEntity<Object>
   */
  @ExceptionHandler({MethodArgumentTypeMismatchException.class})
  public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex,
                                                                          WebRequest request) {
    String message =
        messageSource.getMessage("resource.type-incorrect-attribute", null, LocaleContextHolder.getLocale());
    String detail = messageSource.getMessage("recurso.tipo-atributo-incorreto-detalhe",
        new Object[] {ex.getName(), ex.getValue(), Objects.requireNonNull(ex.getRequiredType()).getSimpleName()},
        LocaleContextHolder.getLocale());
    String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
    String method = ((ServletWebRequest) request).getRequest().getMethod();
    List<ApiError> errors =
        Collections.singletonList(new ApiError(message, detail, HttpStatus.BAD_REQUEST, uri, method));
    return handleExceptionInternal(ex, errors, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  /**
   * Error handling object
   *
   * @param ex      {@link InvalidDataAccessApiUsageException}
   * @param request {@link WebRequest}
   * @return ResponseEntity<Object>
   */
  @ExceptionHandler({InvalidDataAccessApiUsageException.class})
  public ResponseEntity<Object> handleInvalidDataAccessApiUsageException(InvalidDataAccessApiUsageException ex,
                                                                         WebRequest request) {
    String message = messageSource.getMessage("resource.construction-of-the-object-is-incorrect", null,
        LocaleContextHolder.getLocale());
    String detail = ExceptionUtils.getRootCauseMessage(ex);
    String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
    String method = ((ServletWebRequest) request).getRequest().getMethod();
    List<ApiError> errors =
        Collections.singletonList(new ApiError(message, detail, HttpStatus.BAD_REQUEST, uri, method));
    return handleExceptionInternal(ex, errors, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  /**
   * Unsupported method
   *
   * @param ex      the exception {@link HttpRequestMethodNotSupportedException}
   * @param headers the headers to be written to the response {@link HttpHeaders}
   * @param status  the selected response status {@link HttpStatus}
   * @param request the current request {@link WebRequest}
   * @return ResponseEntity<Object>
   */
  //TODO: Fixme
//  @Override
//  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
//                                                                       HttpHeaders headers, HttpStatus status,
//                                                                       WebRequest request) {
//    String message = messageSource.getMessage("resource.method-not-supported", null, LocaleContextHolder.getLocale());
//    String detail = ex.toString();
//    String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
//    String method = ((ServletWebRequest) request).getRequest().getMethod();
//    List<ApiError> errors =
//        Collections.singletonList(new ApiError(message, detail, HttpStatus.METHOD_NOT_ALLOWED, uri, method));
//    return handleExceptionInternal(ex, errors, new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED, request);
//  }

  /**
   * Application business rule
   *
   * @param ex      {@link BusinessException}
   * @param request {@link WebRequest}
   * @return ResponseEntity<Object>
   */
  @ExceptionHandler({BusinessException.class})
  public ResponseEntity<Object> handlerBusinessException(BusinessException ex, WebRequest request) {

    String messagekey = ex.getMessageKey();
    String messageDescription = ex.getMessageDescription();
    String detail = ex.toString();

    String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
    String method = ((ServletWebRequest) request).getRequest().getMethod();
    List<ApiError> errors = Collections.singletonList(
        new ApiError(messagekey, messageDescription, detail, HttpStatus.BAD_REQUEST, uri, method));
    return handleExceptionInternal(ex, errors, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  /**
   * Not Found
   *
   * @param ex      {@link NotFoundException}
   * @param request {@link WebRequest}
   * @return ResponseEntity<Object>
   */
  @ExceptionHandler({NotFoundException.class})
  public ResponseEntity<Object> handlerBusinessNotFoundException(NotFoundException ex, WebRequest request) {

    String messagekey = ex.getMessageKey();
    String messageDescription = ex.getMessageDescription();
    String detail = ex.toString();

    String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
    String method = ((ServletWebRequest) request).getRequest().getMethod();
    List<ApiError> errors = Collections.singletonList(
        new ApiError(messagekey, messageDescription, detail, HttpStatus.NOT_FOUND, uri, method));
    return handleExceptionInternal(ex, errors, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
  }
}
