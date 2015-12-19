package name.valery1707.test.serialization.util;

import name.valery1707.test.serialization.util.spi.TypeProcessor;
import name.valery1707.test.serialization.util.utils.CombinedReader;
import one.util.streamex.StreamEx;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
		//todo Текущая мапа не хранит вычисленные null значение, и значит для не известных типов каждый раз идёт проход по коллекции
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
		return intReadSomething(new BufferedReader(src), clazz, null);
	}

	private <T> T intReadSomething(Reader src, Class<T> type, Type genericType) throws IOException, InstantiationException, IllegalAccessException {
		TypeProcessor<T> processor = getProcessor(type);
		if (processor != null) {
			return processor.read(src, type);
			//todo Process array
		} else if (Iterable.class.isAssignableFrom(type)) {
			return intReadIterable(src, type, genericType);
			//todo Process Map
		} else {
			return intReadObject(src, type);
		}
	}

	private <T> T intReadIterable(Reader src, Class<T> type, Type genericType) throws IOException, IllegalAccessException, InstantiationException {
		Collection collect;
		//todo Нужны подключаемые "конвертеры", которые будут разруливать эту ситуацию
		if (Set.class.isAssignableFrom(type)) {
			collect = new HashSet<>();
		} else if (List.class.isAssignableFrom(type)) {
			collect = new ArrayList<>();
		} else {
			throw new IllegalStateException("Unknown collection type: " + type);
		}
		T result = (T) collect;
		Class<?> itemType;
		if (genericType instanceof ParameterizedType) {
			itemType = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
		} else {
			itemType = Object.class;
		}

		int i;
		i = src.read();
		if (i < 0) {
			throw new IllegalStateException("Catch OEF in collection start");
		}
		if ((char) i != '[') {
			throw new IllegalStateException("Object definition started with illegal character: " + ((char) i));
		}
		while ((i = src.read()) >= 0) {
			char c = (char) i;
			switch (c) {
				case ']'://Object end
					return result;
				case ','://Next item
					break;
				default:
					CombinedReader reader = new CombinedReader(new CharArrayReader(new char[]{c}), src);
					collect.add(intReadSomething(reader, itemType, null));
			}
		}
		throw new IllegalStateException("Catch OEF in collection definition");
	}

	private <T> T intReadObject(Reader src, Class<T> type) throws IOException, IllegalAccessException, InstantiationException {
		Map<String, Field> fieldByName = StreamEx.of(getFields(type)).toMap(Field::getName, f -> f);
		int i;
		i = src.read();
		if (i < 0) {
			return null;
		}
		if ((char) i != '{') {
			throw new IllegalStateException("Object definition started with illegal character: " + ((char) i));
		}
		T dst = type.newInstance();
		String fieldName = null;
		while ((i = src.read()) >= 0) {
			char c = (char) i;
			switch (c) {
				case '}'://Object end
					return dst;
				case '"'://Field name
					fieldName = intReadString(src, true);
					break;
				case ':'://Field value
					Field field = fieldByName.get(fieldName);
					if (field != null) {
						Object value = intReadSomething(src, field.getType(), field.getGenericType());
						beanWriteFieldValue(dst, field, value);
					} else {
						intSkipValue(src);
					}
					break;
				case ','://Next field
					fieldName = null;
					break;
				//todo Пропускать "пустые" символы
				default:
					throw new IllegalStateException("Catch illegal character in object definition: " + c);
			}
		}
		throw new IllegalStateException("Catch OEF in object definition");
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
					if (deepCol == 0 && deepObj == 0) {
						return;
					}
					break;
				case '{'://Object start
					deepObj++;
					break;
				case '}'://Object end
					deepObj--;
					if (deepCol == 0 && deepObj == 0) {
						return;
					}
					break;
				case '"'://String start
					intReadString(src, true);
					break;
			}
		}
	}

	private void intWriteString(Writer dst, CharSequence value) throws IOException {
		getProcessor(CharSequence.class).write(dst, value);
	}

	private String intReadString(Reader src, boolean alreadyStarted) throws IOException {
		src = alreadyStarted ? new CombinedReader(new StringReader("\""), src) : src;
		return getProcessor(String.class).read(src, String.class);
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
				.mapToEntry(field -> beanReadFieldValue(src, field))
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

	private <T> void beanWriteFieldValue(T dst, Field field, Object value) throws IllegalAccessException {
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

	private Object beanReadFieldValue(Object src, Field field) {
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
