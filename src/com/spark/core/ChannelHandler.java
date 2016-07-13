package com.spark.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface ChannelHandler {

//	/**
//	 * Gets called after the {@link ChannelHandler} was added to the actual
//	 * context and it's ready to handle events.
//	 */
//	void handlerAdded(ChannelHandlerContext ctx) throws Exception;
//
//	/**
//	 * Gets called after the {@link ChannelHandler} was removed from the actual
//	 * context and it doesn't handle events anymore.
//	 */
//	void handlerRemoved(ChannelHandlerContext ctx) throws Exception;
//
//	/**
//	 * Gets called if a {@link Throwable} was thrown.
//	 *
//	 * @deprecated is part of {@link ChannelInboundHandler}
//	 */
//	@Deprecated
//	void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;

	/**
	 * Indicates that the same instance of the annotated {@link ChannelHandler}
	 * can be added to one or more {@link ChannelPipeline}s multiple times
	 * without a race condition.
	 * <p>
	 * If this annotation is not specified, you have to create a new handler
	 * instance every time you add it to a pipeline because it has unshared
	 * state such as member variables.
	 * <p>
	 * This annotation is provided for documentation purpose, just like
	 * <a href="http://www.javaconcurrencyinpractice.com/annotations/doc/">the
	 * JCIP annotations</a>.
	 */
	@Inherited
	@Documented
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@interface Sharable {
		// no value
	}
}
