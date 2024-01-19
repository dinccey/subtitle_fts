package org.vaslim.subtitle_fts.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.vaslim.subtitle_fts.exception.SubtitleFtsException;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler({SubtitleFtsException.class})
    public ResponseEntity<Void> handleSubtitleFtsException(SubtitleFtsException ex, WebRequest request){

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
