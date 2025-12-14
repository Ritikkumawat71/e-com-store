package com.shopping.utils;

import org.apache.logging.log4j.Logger;


public class SHCCustomExceptionLogger {
	public static void customExceptionPrintTrace(Logger logger, Exception e) {
		StackTraceElement[] arr = e.getStackTrace();

		for (StackTraceElement s : arr) {
			if (s.getClassName().contains("com.shopping")) {
				logger.error("==> " + s.getClassName() + " : " + s.getMethodName() + " : " + s.getLineNumber() + " : "
						+ e.getMessage());
			}
		}

	}
	
	public static void customExceptionPrintTrace(Logger logger, Exception e, String msg) {
		StackTraceElement[] arr = e.getStackTrace();

		for (StackTraceElement s : arr) {
			if (s.getClassName().contains("com.shopping")) {
				logger.error("==> " + msg + " : " + s.getClassName() + " : " + s.getMethodName() + " : "
						+ s.getLineNumber() + " : " + e.getMessage());
			}
		}
	}
}
