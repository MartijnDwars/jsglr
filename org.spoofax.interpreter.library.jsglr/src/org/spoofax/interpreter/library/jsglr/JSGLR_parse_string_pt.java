package org.spoofax.interpreter.library.jsglr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.spoofax.interpreter.IContext;
import org.spoofax.interpreter.InterpreterException;
import org.spoofax.interpreter.Tools;
import org.spoofax.interpreter.adapter.aterm.WrappedATermFactory;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLR;
import org.spoofax.jsglr.SGLRException;

public class JSGLR_parse_string_pt extends JSGLRPrimitive {

	private WrappedATermFactory factory;

	protected JSGLR_parse_string_pt(WrappedATermFactory termFactory) {
		super("JSGLR_parse_string_pt", 1, 4);
		this.factory = termFactory;
	}

	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars)
			throws InterpreterException {
		
		if(!Tools.isTermString(tvars[0]))
			return false;
		if(!Tools.isTermInt(tvars[1]))
			return false;
		
		JSGLRLibrary lib = getLibrary(env);
		ParseTable pt = lib.getParseTable(Tools.asJavaInt(tvars[1]));
		
		SGLR parser = new SGLR(factory.getFactory(), pt);
		
		InputStream is = new ByteArrayInputStream(Tools.asJavaString(tvars[0]).getBytes());
		try {
			env.setCurrent(factory.wrapTerm(parser.parse(is)));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SGLRException e) {
			e.printStackTrace();
		}
		return false;
	}

}