package com.ganteater.ae.processor;

import java.io.File;
import java.util.function.Function;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import com.ganteater.ae.CommandException;
import com.ganteater.ae.util.xml.easyparser.Node;

public class Python extends BaseProcessor {

	public final class LogOut extends PyObject {
		private static final long serialVersionUID = -121688163044757100L;
		
		public boolean softspace = false; 

		private Function<String, Object> func;

		public LogOut(Function<String, Object> func) {
			this.func = func;
		}

		public void write(Object content) {
			String message = content.toString().trim();
			if (!message.isEmpty()) {
				this.func.apply(message);
			}
		}

		public void flush() {
		}
	}

	static {
		System.setProperty("python.import.site", "false");
	}

	@Override
	public void init(Processor aParent, Node action) throws CommandException {
		super.init(aParent, action);
		try (PythonInterpreter interpreter = new PythonInterpreter()) {
			interpreter.setOut(new LogOut((m) -> log.info(m)));
			interpreter.setErr(new LogOut((m) -> log.error(m)));
			String fileName = action.getAttribute("file");
			if (fileName != null) {
				File file = getFile(fileName);
				interpreter.execfile(file.getAbsolutePath());
			}
			String innerText = action.getInnerText();
			if (!innerText.isEmpty()) {
				interpreter.exec(innerText);
			}
		}
	}

}
