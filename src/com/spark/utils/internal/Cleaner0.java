package com.spark.utils.internal;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import com.spark.utils.internal.logging.InternalLogger;
import com.spark.utils.internal.logging.InternalLoggerFactory;
import sun.misc.Cleaner;

public class Cleaner0 {
	private static final long CLEANER_FIELD_OFFSET;
	private static final InternalLogger logger = InternalLoggerFactory.getInstance(Cleaner0.class);

	static {
		ByteBuffer direct = ByteBuffer.allocateDirect(1);
		Field cleanerField;
		long fieldOffset = -1;
		if (PlatformDependent0.hasUnsafe()) {
			try {
				cleanerField = direct.getClass().getDeclaredField("cleaner");
				cleanerField.setAccessible(true);
				Cleaner cleaner = (Cleaner) cleanerField.get(direct);
				cleaner.clean();
				fieldOffset = PlatformDependent0.objectFieldOffset(cleanerField);
			} catch (Throwable t) {
				// We don't have ByteBuffer.cleaner().
				fieldOffset = -1;
			}
		}
		logger.debug("java.nio.ByteBuffer.cleaner(): {}", fieldOffset != -1 ? "available" : "unavailable");
		CLEANER_FIELD_OFFSET = fieldOffset;

		// free buffer if possible
		freeDirectBuffer(direct);
	}

	static void freeDirectBuffer(ByteBuffer buffer) {
		if (CLEANER_FIELD_OFFSET == -1 || !buffer.isDirect()) {
			return;
		}
		try {
			Cleaner cleaner = (Cleaner) PlatformDependent0.getObject(buffer, CLEANER_FIELD_OFFSET);
			if (cleaner != null) {
				cleaner.clean();
			}
		} catch (Throwable t) {
			// Nothing we can do here.
		}
	}

	private Cleaner0() {
	}
}
