package name.valery1707.test.serialization.util;

import name.valery1707.test.serialization.util.spi.TypeProcessor;
import name.valery1707.test.serialization.util.utils.CombinedReader;
import one.util.streamex.StreamEx;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;

public class Serializer {

	private final List<? extends TypeProcessor<?>> processorList;
	private final Map<Class, TypeProcessor<?>> processorMap = new HashMap<>();
	//todo Нужно не напрямую читать значение из поля, а использовать getter если он есть
	private final Map<Class, List<Field>> classFields = new HashMap<>();

	public Serializer() {
		ServiceLoader<TypeProcessor> processorLoader = ServiceLoader.load(TypeProcessor.class);
		processorList = StreamEx.of(processorLoader.iterator()).map(p -> (TypeProcessor<?>) p).toList();
	}

	@SuppressWarnings("unchecked")
	private <T> TypeProcessor<T> getProcessor(Class<T> type) {
		//todo Current realization is not thread-safe
		TypeProcessor<?> processor = processorMap.computeIfAbsent(type, this::findProcessor);
		return (TypeProcessor<T>) processor;
	}

	private TypeProcessor<?> findProcessor(Class<?> type) {
		return StreamEx.of(processorList)
				.findAny(p -> p.canProcess(type))
				.orElse(null);
	}

	private List<Field> getFields(Class<?> type) {
		return classFields.computeIfAbsent(type, this::findFields);
	}

	private List<Field> findFields(Class type) {
		//todo Нужно перебирать все поля. То есть не только объявленные в текущем классе, но и в его родительских
		return StreamEx.of(type.getDeclaredFields())
				.remove(Field::isSynthetic)
				//todo Нужен механизм для удаления из сериализации определённых полей
				.toList();
	}

	public <T> T readValue(InputStream src, Charset charset, Class<T> clazz) throws Exception {
		return readValue(new InputStreamReader(src, charset), clazz);
	}

	public <T> T readValue(String src, Class<T> clazz) throws Exception {
		return readValue(new StringReader(src), clazz);
	}

	public <T> T readValue(Reader src, Class<T> clazz) throws Exception {
		Map<String, Field> fields = StreamEx.of(clazz.getDeclaredFields()).toMap(Field::getName, f -> f);
		T dst = null;
		String fieldName = null;
		int i;
		while ((i = src.read()) >= 0) {
			char c = (char) i;
			switch (c) {
				case '{'://Object start
					dst = clazz.newInstance();
					break;
				case '}'://Object end
					return dst;
				case '"'://Field name
					fieldName = intReadString(src, true);
					break;
				case ':'://Field value
					Field field = fields.get(fieldName);
					if (field != null) {
						Object value = intReadValue(src, field.getType());
						intWriteFieldValue(dst, field, value);
					} else {
						intSkipValue(src);
					}
					break;
				case ','://Next field
					fieldName = null;
			}
		}
		return dst;
	}

	private void intSkipValue(Reader src) throws IOException {
		int deepCol = 0;
		int deepObj = 0;
		int i;
		while ((i = src.read()) >= 0) {
			char c = (char) i;
			switch (c) {
				case '['://Collection start
					deepCol++;
					break;
				case ']'://Collection end
					deepCol--;
					if (deepCol == 0) {
						return;
					}
					break;
				case '{'://Object start
					deepObj++;
				case '}'://Object end
					deepObj--;
					if (deepObj == 0) {
						return;
					}
					break;
				case '"'://String start
					intReadString(src, true);
					break;
			}
		}
	}

	private Object intReadValue(Reader src, Class<?> type) throws IOException {
		TypeProcessor<?> processor = getProcessor(type);
		if (processor != null) {
			return processor.read(src);
		} else {
			intSkipValue(src);
			return null;
		}
	}

	private void intWriteString(Writer dst, CharSequence value) throws IOException {
		getProcessor(CharSequence.class).write(dst, value);
	}

	private String intReadString(Reader src, boolean alreadyStarted) throws IOException {
		src = alreadyStarted ? new CombinedReader(new StringReader("\""), src) : src;
		return getProcessor(String.class).read(src);
	}

	public String writeValueAsString(@Nullable Object src) throws IOException, IllegalAccessException {
		StringWriter dst = new StringWriter();
		writeValue(dst, src);
		return dst.toString();
	}

	public void writeValue(OutputStream dst, Charset charset, @Nullable Object src) throws IOException, IllegalAccessException {
		writeValue(new OutputStreamWriter(dst, charset), src);
	}

	public void writeValue(Writer dst, @Nullable Object src) throws IOException, IllegalAccessException {
		intWriteSomething(dst, src);
	}

	private void intWriteSomething(Writer dst, @Nullable Object src) throws IOException, IllegalAccessException {
		if (src == null) {
			return;
		}
		Class<?> type = src.getClass();
		TypeProcessor<Object> processor = (TypeProcessor<Object>) getProcessor(type);
		if (processor != null) {
			processor.write(dst, src);
			//todo Process array
		} else if (src instanceof Iterable<?>) {
			intWriteIterable(dst, (Iterable<?>) src);
			//todo Process Map
		} else {
			intWriteObject(dst, src);
		}
	}

	private void intWriteIterable(Writer dst, Iterable<?> src) throws IOException, IllegalAccessException {
		//todo Write empty collections?
		dst.append('[');
		for (Iterator<?> iterator = src.iterator(); iterator.hasNext(); ) {
			//todo Write null values?
			Object item = iterator.next();
			intWriteSomething(dst, item);
			if (iterator.hasNext()) {
				dst.append(',');
			}
		}
		dst.append(']');
	}

	private void intWriteObject(Writer dst, @Nonnull Object src) throws IOException, IllegalAccessException {
		//todo Необходимо учитывать возможность зацикленности графа объектов
		dst.append('{');
		Class<?> clazz = src.getClass();
		List<Map.Entry<Field, Object>> values = StreamEx.of(getFields(clazz))
				.mapToEntry(field -> intReadFieldValue(src, field))
				.nonNullValues()
				.toList();
		for (Iterator<Map.Entry<Field, Object>> iterator = values.iterator(); iterator.hasNext(); ) {
			Map.Entry<Field, Object> item = iterator.next();
			Field field = item.getKey();
			Object value = item.getValue();
			intWriteString(dst, field.getName());
			dst.append(':');
			intWriteSomething(dst, value);
			if (iterator.hasNext()) {
				dst.append(',');
			}
		}
		dst.append('}');
	}

	private <T> void intWriteFieldValue(T dst, Field field, Object value) throws IllegalAccessException {
		if (value == null) {
			return;
		}
		boolean accessible = field.isAccessible();
		try {
			if (!accessible) {
				field.setAccessible(true);
			}
			field.set(dst, value);
		} finally {
			if (!accessible) {
				field.setAccessible(false);
			}
		}
	}

	private Object intReadFieldValue(Object src, Field field) {
		boolean accessible = field.isAccessible();
		try {
			if (!accessible) {
				field.setAccessible(true);
			}
			return field.get(src);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} finally {
			if (!accessible) {
				field.setAccessible(false);
			}
		}
	}
}
