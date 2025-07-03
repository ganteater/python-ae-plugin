package com.ganteater.ae.processor;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import com.ganteater.ae.CommandException;
import com.ganteater.ae.processor.annotation.CommandExamples;
import com.ganteater.ae.util.AEUtils;
import com.ganteater.ae.util.xml.easyparser.Node;

public class Python extends BaseProcessor {

	static {
		System.setProperty("python.import.site", "false");
	}

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

	private PythonInterpreter interpreter;

	@Override
	public void init(Processor aParent, Node action) throws CommandException {
		super.init(aParent, action);
		interpreter = new PythonInterpreter();
		interpreter.setOut(new LogOut((m) -> log.info(m)));
		interpreter.setErr(new LogOut((m) -> log.error(m)));
		String innerText = action.getInnerText();
		IOException fileError = null;
		String fileName = action.getAttribute("file");
		if (StringUtils.isEmpty(innerText)) {
			if (fileName != null) {
				InputStream inputStream;
				try {
					inputStream = AEUtils.getInputStream(fileName, getBaseDir());
					innerText = IOUtils.toString(inputStream);
				} catch (IOException e) {
					fileError = e;
				}
			}
		}
		innerText = replaceProperties(innerText);
		if (!innerText.isEmpty()) {
			interpreter.exec(innerText);
		} else if (fileName != null && fileError != null) {
			throw new CommandException(fileError, this, action);
		}
	}

	@CommandExamples({ "<Run name='type:property'> ... </Run>" })
	public void runCommandRun(Node command) {
		String innerText = command.getInnerText();
		innerText = replaceProperties(innerText);
		interpreter.exec(innerText);
		String name = command.getAttribute("name");
		if (name != null) {
			setVariableValue(name, interpreter.get(name));
		}
	}

	@Override
	public void stop() {
		super.stop();
		interpreter.close();
	}
}
