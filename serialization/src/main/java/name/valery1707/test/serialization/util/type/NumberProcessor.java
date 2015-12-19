package name.valery1707.test.serialization.util.type;

import name.valery1707.test.serialization.util.spi.TypeProcessor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberProcessor implements TypeProcessor<Number> {
	@Override
	public boolean canProcess(Class<?> type) {
		return Number.class.isAssignableFrom(type);
	}

	@Override
	public void write(Writer dst, Number value) throws IOException {
		BigDecimal bigDecimal = BigDecimal.valueOf(value.doubleValue());
		dst.append(bigDecimal.toPlainString());//todo Целые числа записываются с дробной частью
/*
		long integer = value.longValue();
		dst.append(String.valueOf(integer));
		if (value instanceof Double || value instanceof Float) {
			double fraction = value.doubleValue() - integer;
			if (fraction != 0) {
				dst.append('.').append(String)
			}
		}
*/
	}

	@Override
	public <R extends Number> R read(Reader src, Class<R> type) throws IOException {
		StringBuilder buf = new StringBuilder();
		int i;
		src.mark(1);
		while ((i = src.read()) >= 0) {
			char c = (char) i;
			if (c == '.' || Character.isDigit(c)) {
				buf.append(c);
			} else {
				src.reset();
				BigDecimal bigDecimal = new BigDecimal(buf.toString());
				return wrap(bigDecimal, type);
			}
			src.mark(1);
		}
		throw new IllegalStateException("Number is not started");
	}

	@SuppressWarnings("unchecked")
	private <R extends Number> R wrap(BigDecimal bigDecimal, Class<R> type) {
		if (Byte.class.equals(type)) {
			return (R) (Byte) bigDecimal.byteValue();
		} else if (Short.class.equals(type)) {
			return (R) (Short) bigDecimal.shortValue();
		} else if (Integer.class.equals(type)) {
			return (R) (Integer) bigDecimal.intValue();
		} else if (Long.class.equals(type)) {
			return (R) (Long) bigDecimal.longValue();
		} else if (Float.class.equals(type)) {
			return (R) (Float) bigDecimal.floatValue();
		} else if (Double.class.equals(type)) {
			return (R) (Double) bigDecimal.doubleValue();
		} else if (BigInteger.class.equals(type)) {
			return (R) bigDecimal.toBigInteger();
		} else if (BigDecimal.class.equals(type)) {
			return (R) bigDecimal;
		}
		//todo Точно нужны корвертеры
		throw new IllegalStateException("Unknown target type: " + type);
	}
}
