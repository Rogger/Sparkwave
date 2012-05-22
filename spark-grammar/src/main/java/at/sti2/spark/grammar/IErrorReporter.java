package at.sti2.spark.grammar;

import org.antlr.runtime.RecognitionException;

public interface IErrorReporter {
//	void reportError(String error);
	void reportError(String[] tokenNames, RecognitionException e, String hdr, String msg);
}
