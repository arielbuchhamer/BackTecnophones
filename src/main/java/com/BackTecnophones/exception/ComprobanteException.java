package com.BackTecnophones.exception;

public class ComprobanteException extends RuntimeException {
	public ComprobanteException(String message) {
		super(message);
	}

	public ComprobanteException(String message, Throwable cause) {
		super(message, cause);
	}
}
